package dagger.internal.codegen.validation;

import com.google.auto.common.MoreTypes;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleTypeVisitor8;

import dagger.Component;
import dagger.internal.codegen.base.ClearableCache;
import dagger.internal.codegen.base.ComponentAnnotation;
import dagger.internal.codegen.binding.ComponentKind;
import dagger.internal.codegen.binding.DependencyRequestFactory;
import dagger.internal.codegen.binding.ErrorMessages;
import dagger.internal.codegen.binding.MethodSignatureFormatter;
import dagger.internal.codegen.binding.ModuleKind;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.internal.codegen.validation.ComponentCreatorValidator;
import dagger.internal.codegen.validation.DependencyRequestValidator;
import dagger.internal.codegen.validation.ModuleValidator;
import dagger.internal.codegen.validation.ValidationReport;
import dagger.producers.ProductionComponent;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.Key;

import static com.google.auto.common.MoreElements.asType;
import static com.google.auto.common.MoreTypes.asDeclared;
import static com.google.auto.common.MoreTypes.asExecutable;
import static com.google.auto.common.MoreTypes.asTypeElement;
import static com.google.common.base.Verify.verify;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Multimaps.asMap;
import static com.google.common.collect.Sets.intersection;
import static dagger.internal.codegen.base.ComponentAnnotation.anyComponentAnnotation;
import static dagger.internal.codegen.base.ModuleAnnotation.moduleAnnotation;
import static dagger.internal.codegen.base.Util.reentrantComputeIfAbsent;
import static dagger.internal.codegen.binding.ComponentCreatorAnnotation.creatorAnnotationsFor;
import static dagger.internal.codegen.binding.ComponentCreatorAnnotation.productionCreatorAnnotations;
import static dagger.internal.codegen.binding.ComponentCreatorAnnotation.subcomponentCreatorAnnotations;
import static dagger.internal.codegen.binding.ComponentKind.annotationsFor;
import static dagger.internal.codegen.binding.ConfigurationAnnotations.enclosedAnnotatedTypes;
import static dagger.internal.codegen.binding.ConfigurationAnnotations.getTransitiveModules;
import static dagger.internal.codegen.binding.ErrorMessages.ComponentCreatorMessages.builderMethodRequiresNoArgs;
import static dagger.internal.codegen.binding.ErrorMessages.ComponentCreatorMessages.moreThanOneRefToSubcomponent;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.internal.codegen.langmodel.DaggerElements.getAnnotationMirror;
import static dagger.internal.codegen.langmodel.DaggerElements.getAnyAnnotation;
import static dagger.internal.codegen.langmodel.DaggerElements.isAnnotationPresent;
import static java.util.Comparator.comparing;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.type.TypeKind.VOID;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * Performs superficial validation of the contract of the {@link Component} and {@link
 * ProductionComponent} annotations.
 */
@Singleton
public final class ComponentValidator implements ClearableCache {

    private final DaggerElements elements;
    private final DaggerTypes types;
    private final ModuleValidator moduleValidator;
    private final ComponentCreatorValidator creatorValidator;
    private final DependencyRequestValidator dependencyRequestValidator;
    private final MembersInjectionValidator membersInjectionValidator;
    private final MethodSignatureFormatter methodSignatureFormatter;
    private final DependencyRequestFactory dependencyRequestFactory;
    private final Map<TypeElement, ValidationReport> reports = new HashMap<>();
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    ComponentValidator(
            DaggerElements elements,
            DaggerTypes types,
            ModuleValidator moduleValidator,
            ComponentCreatorValidator creatorValidator,
            DependencyRequestValidator dependencyRequestValidator,
            MembersInjectionValidator membersInjectionValidator,
            MethodSignatureFormatter methodSignatureFormatter,
            DependencyRequestFactory dependencyRequestFactory,
            KotlinMetadataUtil metadataUtil) {
        this.elements = elements;
        this.types = types;
        this.moduleValidator = moduleValidator;
        this.creatorValidator = creatorValidator;
        this.dependencyRequestValidator = dependencyRequestValidator;
        this.membersInjectionValidator = membersInjectionValidator;
        this.methodSignatureFormatter = methodSignatureFormatter;
        this.dependencyRequestFactory = dependencyRequestFactory;
        this.metadataUtil = metadataUtil;
    }

    @Override
    public void clearCache() {
        reports.clear();
    }

    /**
     * Validates the given component.
     * <p>
     * 校验入口，校验使用(Production)(Sub)Component注解的节点
     */
    public ValidationReport validate(TypeElement component) {
        return reentrantComputeIfAbsent(reports, component, this::validateUncached);
    }

    private ValidationReport validateUncached(TypeElement component) {
        return new ElementValidator(component).validateElement();
    }

    private class ElementValidator {
        private final TypeElement component;
        private final ValidationReport.Builder report;
        private final ImmutableSet<ComponentKind> componentKinds;

        // Populated by ComponentMethodValidators
        //key:方法返回类型，value：方法节点
        private final SetMultimap<Element, ExecutableElement> referencedSubcomponents =
                LinkedHashMultimap.create();

        ElementValidator(TypeElement component) {
            this.component = component;
            this.report = ValidationReport.about(component);
            this.componentKinds = ComponentKind.getComponentKinds(component);
        }

        private ComponentKind componentKind() {
            return getOnlyElement(componentKinds);
        }

        //component类上使用的(Producer)Component 或 (Producer)Subcomponent注解，对该注解生成ComponentAnnotation对象
        private ComponentAnnotation componentAnnotation() {
            return anyComponentAnnotation(component).get();
        }

        private DeclaredType componentType() {
            return asDeclared(component.asType());
        }

        //节点校验入口
        ValidationReport validateElement() {
            //1.(Producer)Module,(Production)(Sub)Component注解最多只能使用其中的一个，否则报错
            if (componentKinds.size() > 1) {
                return moreThanOneComponentAnnotation();
            }

            //2.校验@CancellationPolicy注解的用法：
            // 如果当前component类使用了@CancellationPolicy注解，那么只能被Production(Sub)Component修饰
            validateUseOfCancellationPolicy();

            //3.component类只能是这两种情况：要么是接口，要么是抽象类
            validateIsAbstractType();

            //4.校验component类中的creator
            validateCreators();

            //5.@Reusable不允许被使用在component类上
            validateNoReusableAnnotation();

            //6.校验component类的方法
            validateComponentMethods();

            //7.校验没有冲突的入口点：入口方法最多只能有一个
            validateNoConflictingEntryPoints();

            //8.referencedSubcomponents集合超过1个item:即方法返回类型 使用(Production)SubComponent注解 和 方法返回类型 使用component类的Builder或Factory注解 只能出现一个
            validateSubcomponentReferences();

            //9. component#dependencies校验：里面的类不能使用(Producer)Module注解，否则报错
            validateComponentDependencies();

            //10.Module#includes校验
            validateReferencedModules();

            //11.subcomponent的校验： 使用(Production)SubComponent注解 或 使用component类的Builder或Factory注解的方法返回类型校验（该方法最多只会存在一个）
            validateSubcomponents();

            return report.build();
        }

        private ValidationReport moreThanOneComponentAnnotation() {
            String error =
                    "Components may not be annotated with more than one component annotation: found "
                            + annotationsFor(componentKinds);
            report.addError(error, component);
            return report.build();
        }

        private void validateUseOfCancellationPolicy() {
            if (isAnnotationPresent(component, TypeNames.CANCELLATION_POLICY)
                    && !componentKind().isProducer()) {
                report.addError(
                        "@CancellationPolicy may only be applied to production components and subcomponents",
                        component);
            }
        }

        private void validateIsAbstractType() {
            if (!component.getKind().equals(INTERFACE)
                    && !(component.getKind().equals(CLASS) && component.getModifiers().contains(ABSTRACT))) {
                report.addError(
                        String.format(
                                "@%s may only be applied to an interface or abstract class",
                                componentKind().annotation().simpleName()),
                        component);
            }
        }

        private void validateCreators() {
            //1.对creator校验
            ImmutableList<DeclaredType> creators =
                    creatorAnnotationsFor(componentAnnotation()).stream()
                            .flatMap(annotation -> enclosedAnnotatedTypes(component, annotation).stream())
                            .collect(toImmutableList());
            creators.forEach(
                    creator -> report.addSubreport(creatorValidator.validate(asTypeElement(creator))));

            //2.校验component类中的内部类最多只能有一个使用(Production)(Sub)Component.Builder或(Production)(Sub)ComponentFactory的注解
            if (creators.size() > 1) {
                report.addError(
                        String.format(
                                ErrorMessages.componentMessagesFor(componentKind()).moreThanOne(), creators),
                        component);
            }
        }

        private void validateNoReusableAnnotation() {
            Optional<AnnotationMirror> reusableAnnotation =
                    getAnnotationMirror(component, TypeNames.REUSABLE);

            if (reusableAnnotation.isPresent()) {
                report.addError(
                        "@Reusable cannot be applied to components or subcomponents",
                        component,
                        reusableAnnotation.get());
            }
        }

        //校验component类的方法
        private void validateComponentMethods() {

            //1.如果component是kotlin文件，那么不允许使用java关键字命名
            validateClassMethodName();

            //2.对component类的非private、非static、abstract修饰的方法校验
            elements.getUnimplementedMethods(component).stream()
                    .map(ComponentMethodValidator::new)
                    .forEachOrdered(ComponentMethodValidator::validateMethod);
        }

        private void validateClassMethodName() {
            if (metadataUtil.hasMetadata(component)) {
                metadataUtil
                        .getAllMethodNamesBySignature(component)
                        .forEach(
                                (signature, name) -> {
                                    if (SourceVersion.isKeyword(name)) {
                                        report.addError("Can not use a Java keyword as method name: " + signature);
                                    }
                                });
            }
        }

        private class ComponentMethodValidator {
            private final ExecutableElement method;
            private final ExecutableType resolvedMethod;
            private final List<? extends TypeMirror> parameterTypes;
            private final List<? extends VariableElement> parameters;
            private final TypeMirror returnType;

            ComponentMethodValidator(ExecutableElement method) {
                this.method = method;
                this.resolvedMethod = asExecutable(types.asMemberOf(componentType(), method));
                //getParameterTypes():参数类型
                this.parameterTypes = resolvedMethod.getParameterTypes();
                //getParameters():参数节点
                this.parameters = method.getParameters();
                this.returnType = resolvedMethod.getReturnType();
            }

            //校验component类的方法入口
            void validateMethod() {

                //1.方法不允许使用泛型类型
                validateNoTypeVariables();

                // abstract methods are ones we have to implement, so they each need to be validated
                // first, check the return type. if it's a subcomponent, validate that method as
                // such.
                //returnType类型表示的类或接口使用了那种注解(是(Production)SubComponent注解)
                Optional<AnnotationMirror> subcomponentAnnotation = subcomponentAnnotation();

                //2.如果方法返回的类型使用了(Production)SubComponent注解,校验该注解
                if (subcomponentAnnotation.isPresent()) {

                    validateSubcomponentFactoryMethod(subcomponentAnnotation.get());

                }

                //3.如果方法返回类型使用了component类的Builder或Factory注解，校验
                else if (subcomponentCreatorAnnotation().isPresent()) {

                    validateSubcomponentCreatorMethod();

                }
                //4.方法返回类型不是使用(Production)SubComponent注解 && 方法返回类型不是使用component类的Builder或Factory注解
                //（1）无参，对返回类型进行依赖校验
                //（2） ①作为成员注解校验，自行去看逻辑；②方法返回类型 是void 或者方法返回类型和参数类型相同。
                //（3）参数超过1个，报错
                else {

                    // if it's not a subcomponent...
                    switch (parameters.size()) {
                        case 0:
                            validateProvisionMethod();
                            break;
                        case 1:
                            validateMembersInjectionMethod();
                            break;
                        default:
                            reportInvalidMethod();
                            break;
                    }
                }
            }

            private void validateNoTypeVariables() {
                if (!resolvedMethod.getTypeVariables().isEmpty()) {
                    report.addError("Component methods cannot have type variables", method);
                }
            }

            //returnType类型表示的类或接口使用了那种注解(是(Production)Component注解)
            private Optional<AnnotationMirror> subcomponentAnnotation() {
                return checkForAnnotations(
                        returnType,
                        componentKind().legalSubcomponentKinds().stream()
                                .map(ComponentKind::annotation)
                                .collect(toImmutableSet()));
            }

            //返回类型subcomponentCreator注解，Builder或Factory注解
            private Optional<AnnotationMirror> subcomponentCreatorAnnotation() {
                return checkForAnnotations(
                        returnType,
                        componentAnnotation().isProduction()
                                ? intersection(subcomponentCreatorAnnotations(), productionCreatorAnnotations())
                                : subcomponentCreatorAnnotations());
            }

            //方法返回类型使用了(Production)Subcomponent注解的校验:
            //1.如果方法参数是module类型，那么该类型的参数的注解在当前方法中只允许出现1次
            //2.如果方法参数是module类型，那么这个module类一定存在于transitiveModules集合中
            //3.subcomponent类的方法只允许接受module类型参数
            private void validateSubcomponentFactoryMethod(AnnotationMirror subcomponentAnnotation) {
                //key：方法的返回类型，value：方法节点
                referencedSubcomponents.put(MoreTypes.asElement(returnType), method);

                //返回类型使用的ComponentKind类型
                ComponentKind subcomponentKind =
                        ComponentKind.forAnnotatedElement(MoreTypes.asTypeElement(returnType)).get();

                //获取Subcomponent#modules 里面的类
                ImmutableSet<TypeElement> moduleTypes =
                        ComponentAnnotation.componentAnnotation(subcomponentAnnotation).modules();

                // TODO(gak): This logic maybe/probably shouldn't live here as it requires us to traverse
                // subcomponents and their modules separately from how it is done in ComponentDescriptor and
                // ModuleDescriptor
                //moduleTypes集合收集每一个module类及其父类中的(Producer)Module#includes里面的module类
                @SuppressWarnings("deprecation")
                ImmutableSet<TypeElement> transitiveModules =
                        getTransitiveModules(types, elements, moduleTypes);

                Set<TypeElement> variableTypes = Sets.newHashSet();

                for (int i = 0; i < parameterTypes.size(); i++) {
                    VariableElement parameter = parameters.get(i);
                    TypeMirror parameterType = parameterTypes.get(i);

                    //判断方法参数是否是module类
                    Optional<TypeElement> moduleType =
                            parameterType.accept(
                                    new SimpleTypeVisitor8<Optional<TypeElement>, Void>() {
                                        @Override
                                        protected Optional<TypeElement> defaultAction(TypeMirror e, Void p) {
                                            return Optional.empty();
                                        }

                                        @Override
                                        public Optional<TypeElement> visitDeclared(DeclaredType t, Void p) {
                                            for (ModuleKind moduleKind : subcomponentKind.legalModuleKinds()) {
                                                if (isAnnotationPresent(t.asElement(), moduleKind.annotation())) {
                                                    return Optional.of(MoreTypes.asTypeElement(t));
                                                }
                                            }
                                            return Optional.empty();
                                        }
                                    },
                                    null);

                    //如果subcomponent类的方法参数是module类
                    if (moduleType.isPresent()) {
                        //1.如果方法参数是module类型，那么该类型的参数在当前方法中只允许出现1次
                        if (variableTypes.contains(moduleType.get())) {
                            report.addError(
                                    String.format(
                                            "A module may only occur once an an argument in a Subcomponent factory "
                                                    + "method, but %s was already passed.",
                                            moduleType.get().getQualifiedName()),
                                    parameter);
                        }
                        //2.如果方法参数是module类型，那么这个module类一定存在于transitiveModules集合中
                        if (!transitiveModules.contains(moduleType.get())) {
                            report.addError(
                                    String.format(
                                            "%s is present as an argument to the %s factory method, but is not one of the"
                                                    + " modules used to implement the subcomponent.",
                                            moduleType.get().getQualifiedName(),
                                            MoreTypes.asTypeElement(returnType).getQualifiedName()),
                                    method);
                        }
                        variableTypes.add(moduleType.get());
                    } else {
                        //3.subcomponent类的方法只允许接受module类型参数
                        report.addError(
                                String.format(
                                        "Subcomponent factory methods may only accept modules, but %s is not.",
                                        parameterType),
                                parameter);
                    }
                }
            }

            //如果方法返回类型使用了component类的Builder或Factory注解:
            //1.方法参数必须为空
            //2.校验该返回类型，作为component类的Builder或Factory注解校验
            private void validateSubcomponentCreatorMethod() {
                referencedSubcomponents.put(MoreTypes.asElement(returnType).getEnclosingElement(), method);

                //参数必须为空，否则报错
                if (!parameters.isEmpty()) {
                    report.addError(builderMethodRequiresNoArgs(), method);
                }

                TypeElement creatorElement = MoreTypes.asTypeElement(returnType);
                // TODO(sameb): The creator validator right now assumes the element is being compiled
                // in this pass, which isn't true here.  We should change error messages to spit out
                // this method as the subject and add the original subject to the message output.
                report.addSubreport(creatorValidator.validate(creatorElement));
            }

            //依赖校验
            private void validateProvisionMethod() {
                dependencyRequestValidator.validateDependencyRequest(report, method, returnType);
            }

            //component正常方法（该方法返回类型不是使用Builder或Factory或(Production)Subcomponent注解修饰），有且仅有一个参数的情况下的校验
            //1.作为成员注入方法注解校验，自行去看逻辑
            //2.方法返回类型 是void 或者方法返回类型和参数类型相同。
            private void validateMembersInjectionMethod() {
                TypeMirror parameterType = getOnlyElement(parameterTypes);
                report.addSubreport(
                        membersInjectionValidator.validateMembersInjectionMethod(method, parameterType));
                if (!(returnType.getKind().equals(VOID) || types.isSameType(returnType, parameterType))) {
                    report.addError(
                            "Members injection methods may only return the injected type or void.", method);
                }
            }

            //参数不得超过1个，否则报错
            private void reportInvalidMethod() {
                report.addError(
                        "This method isn't a valid provision method, members injection method or "
                                + "subcomponent factory method. Dagger cannot implement this method",
                        method);
            }
        }

        private void validateNoConflictingEntryPoints() {
            // Collect entry point methods that are not overridden by others. If the "same" method is
            // inherited from more than one supertype, each will be in the multimap.
            SetMultimap<String, ExecutableElement> entryPointMethods = HashMultimap.create();

            methodsIn(elements.getAllMembers(component)).stream()
                    .filter(//1.是入口方法：如果方法是abstract修饰 && 方法参数为空 && 方法返回类型不是void
                            method ->
                                    isEntryPoint(method, asExecutable(types.asMemberOf(componentType(), method))))
                    .forEach(//2.方法没有被覆盖
                            method ->
                                    addMethodUnlessOverridden(
                                            method, entryPointMethods.get(method.getSimpleName().toString())));


            for (Set<ExecutableElement> methods : asMap(entryPointMethods).values()) {
                //如果方法存在不止一个，报错
                if (distinctKeys(methods).size() > 1) {
                    reportConflictingEntryPoints(methods);
                }
            }
        }

        private void validateSubcomponentReferences() {
            Maps.filterValues(referencedSubcomponents.asMap(), methods -> methods.size() > 1)
                    .forEach(
                            (subcomponent, methods) ->
                                    report.addError(
                                            String.format(moreThanOneRefToSubcomponent(), subcomponent, methods),
                                            component));
        }

        private void validateComponentDependencies() {//component#dependencies校验：里面的类不能使用(Producer)Module注解，否则报错
            for (TypeMirror type : componentAnnotation().dependencyTypes()) {
                type.accept(CHECK_DEPENDENCY_TYPES, report);
            }
        }

        private void reportConflictingEntryPoints(Collection<ExecutableElement> methods) {
            verify(
                    methods.stream().map(ExecutableElement::getEnclosingElement).distinct().count()
                            == methods.size(),
                    "expected each method to be declared on a different type: %s",
                    methods);
            StringBuilder message = new StringBuilder("conflicting entry point declarations:");
            methodSignatureFormatter
                    .typedFormatter(componentType())
                    .formatIndentedList(
                            message,
                            ImmutableList.sortedCopyOf(
                                    comparing(
                                            method -> asType(method.getEnclosingElement()).getQualifiedName().toString()),
                                    methods),
                            1);
            report.addError(message.toString());
        }

        private void validateReferencedModules() {//Module#includes校验
            report.addSubreport(
                    moduleValidator.validateReferencedModules(
                            component,
                            componentAnnotation().annotation(),
                            componentKind().legalModuleKinds(),
                            new HashSet<>()));
        }

        private void validateSubcomponents() {
            // Make sure we validate any subcomponents we're referencing.
            for (Element subcomponent : referencedSubcomponents.keySet()) {
                ValidationReport subreport = validate(asType(subcomponent));
                report.addSubreport(subreport);
            }
        }

        //这里的key是方法的返回类型生成的。
        private ImmutableSet<Key> distinctKeys(Set<ExecutableElement> methods) {
            return methods.stream()
                    .map(this::dependencyRequest)
                    .map(DependencyRequest::key)
                    .collect(toImmutableSet());
        }

        //方法生成依赖对象
        private DependencyRequest dependencyRequest(ExecutableElement method) {
            ExecutableType methodType = asExecutable(types.asMemberOf(componentType(), method));
            return ComponentKind.forAnnotatedElement(component).get().isProducer()
                    ? dependencyRequestFactory.forComponentProductionMethod(method, methodType)
                    : dependencyRequestFactory.forComponentProvisionMethod(method, methodType);
        }
    }

    //入口判断：如果方法是abstract修饰 && 方法参数为空 && 方法返回类型不是void
    private static boolean isEntryPoint(ExecutableElement method, ExecutableType methodType) {
        return method.getModifiers().contains(ABSTRACT)
                && method.getParameters().isEmpty()
                && !methodType.getReturnType().getKind().equals(VOID)
                && methodType.getTypeVariables().isEmpty();
    }

    //除非被覆盖，否则添加方法
    private void addMethodUnlessOverridden(ExecutableElement method, Set<ExecutableElement> methods) {
        if (methods.stream().noneMatch(existingMethod -> overridesAsDeclared(existingMethod, method))) {
            methods.removeIf(existingMethod -> overridesAsDeclared(method, existingMethod));
            methods.add(method);
        }
    }

    /**
     * Returns {@code true} if {@code overrider} overrides {@code overridden} considered from within
     * the type that declares {@code overrider}.
     * <p>
     * overrider是否继承了overridden
     */
    // TODO(dpb): Does this break for ECJ?
    private boolean overridesAsDeclared(ExecutableElement overrider, ExecutableElement overridden) {
        return elements.overrides(overrider, overridden, asType(overrider.getEnclosingElement()));
    }

    private static final TypeVisitor<Void, ValidationReport.Builder> CHECK_DEPENDENCY_TYPES =
            new SimpleTypeVisitor8<Void, ValidationReport.Builder>() {
                @Override
                protected Void defaultAction(TypeMirror type, ValidationReport.Builder report) {
                    report.addError(type + " is not a valid component dependency type");
                    return null;
                }

                @Override
                public Void visitDeclared(DeclaredType type, ValidationReport.Builder report) {
                    if (moduleAnnotation(MoreTypes.asTypeElement(type)).isPresent()) {
                        report.addError(type + " is a module, which cannot be a component dependency");
                    }
                    return null;
                }
            };

    //type类型是否使用了annotations注解，如果用了，随便返回其中的一个
    private static Optional<AnnotationMirror> checkForAnnotations(
            TypeMirror type, final Set<ClassName> annotations) {
        return type.accept(
                new SimpleTypeVisitor8<Optional<AnnotationMirror>, Void>(Optional.empty()) {
                    @Override
                    public Optional<AnnotationMirror> visitDeclared(DeclaredType t, Void p) {
                        return getAnyAnnotation(t.asElement(), annotations);
                    }
                },
                null);
    }
}
