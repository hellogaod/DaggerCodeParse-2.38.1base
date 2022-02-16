package dagger.internal.codegen.validation;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.base.Equivalence;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import dagger.internal.codegen.binding.ComponentCreatorDescriptor;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.ComponentRequirement;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.ErrorMessages;
import dagger.internal.codegen.binding.MethodSignatureFormatter;
import dagger.internal.codegen.binding.ModuleDescriptor;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.compileroption.ValidationType;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.Scope;

import static com.google.auto.common.MoreTypes.asDeclared;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Collections2.transform;
import static dagger.internal.codegen.base.ComponentAnnotation.rootComponentAnnotation;
import static dagger.internal.codegen.base.DiagnosticFormatting.stripCommonTypePrefixes;
import static dagger.internal.codegen.base.Formatter.INDENT;
import static dagger.internal.codegen.base.Scopes.getReadableSource;
import static dagger.internal.codegen.base.Scopes.scopesOf;
import static dagger.internal.codegen.base.Scopes.singletonScope;
import static dagger.internal.codegen.base.Util.reentrantComputeIfAbsent;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSetMultimap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.tools.Diagnostic.Kind.ERROR;

public final class ComponentDescriptorValidator {

    private final DaggerElements elements;
    private final DaggerTypes types;
    private final CompilerOptions compilerOptions;
    private final MethodSignatureFormatter methodSignatureFormatter;
    private final ComponentHierarchyValidator componentHierarchyValidator;
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    ComponentDescriptorValidator(
            DaggerElements elements,
            DaggerTypes types,
            CompilerOptions compilerOptions,
            MethodSignatureFormatter methodSignatureFormatter,
            ComponentHierarchyValidator componentHierarchyValidator,
            KotlinMetadataUtil metadataUtil) {
        this.elements = elements;
        this.types = types;
        this.compilerOptions = compilerOptions;
        this.methodSignatureFormatter = methodSignatureFormatter;
        this.componentHierarchyValidator = componentHierarchyValidator;
        this.metadataUtil = metadataUtil;
    }

    //校验componentDescriptor
    public ValidationReport validate(ComponentDescriptor component) {
        ComponentValidation validation = new ComponentValidation(component);
        validation.visitComponent(component);

        validation.report(component).addSubreport(componentHierarchyValidator.validate(component));
        return validation.buildReport();
    }

    private final class ComponentValidation {
        final ComponentDescriptor rootComponent;
        final Map<ComponentDescriptor, ValidationReport.Builder> reports = new LinkedHashMap<>();

        ComponentValidation(ComponentDescriptor rootComponent) {
            this.rootComponent = checkNotNull(rootComponent);
        }

        /**
         * Returns a report that contains all validation messages found during traversal.
         */
        ValidationReport buildReport() {
            ValidationReport.Builder report = ValidationReport.about(rootComponent.typeElement());
            reports.values().forEach(subreport -> report.addSubreport(subreport.build()));
            return report.build();
        }

        /**
         * Returns the report builder for a (sub)component.
         */
        private ValidationReport.Builder report(ComponentDescriptor component) {
            return reentrantComputeIfAbsent(
                    reports, component, descriptor -> ValidationReport.about(descriptor.typeElement()));
        }

        private void reportComponentItem(
                Diagnostic.Kind kind, ComponentDescriptor component, String message) {
            report(component)
                    .addItem(message, kind, component.typeElement(), component.annotation().annotation());
        }

        private void reportComponentError(ComponentDescriptor component, String error) {
            reportComponentItem(ERROR, component, error);
        }

        void visitComponent(ComponentDescriptor component) {

            //1. component节点和componentAnnotation#dependencies中的dependency节点关于使用Scope注解类型判断：
            //
            // - （1）component节点如果没有使用Scope类型的注解，那么componentAnnotation#dependencies中的dependency节点不允许使用Scope类型注解；
            //
            // - （2）如果component节点使用了@Singleton注解，那么componentAnnotation#dependencies中的dependency节点最好不要使用Scope类型注解；
            //
            // - （3）如果component注解使用了非@Singleton类型的Scope注解，那么component节点使用的Scope注解不允许再次出现在componentAnnotation#dependencies中的dependency（如果dependency是component节点，那么同样的道理）节点上；
            validateDependencyScopes(component);

            //2. 当前component节点使用的注解是Component或ProductionComponent，最好不要再次出现在该节点使用的注解componentAnnotation#dependencies中；
            validateComponentDependencyHierarchy(component);

            //3. 当前componentAnnotation#modules中的module节点如果是abstact抽象类或接口 && module节点使用了@Produces或@Provides修饰的bindingMethod绑定方法，那么该module节点不可以被实例化；
            // 所以如果module节点是abstract修饰的抽象类，那么@Produces或@Provides修饰的bindingMethod方法必须使用static修饰；
            validateModules(component);

            //4. component节点中存在内部类creator节点，那么对creator校验：
            //
            // - （1）creator节点中没有使用@BindsInstance修饰的方法或方法参数，该参数来源于component关联的所有非abstact修饰的module节点 或 componentAnnotation#dependencies中的dependency节点；
            //
            // - （2）component节点中收集mustBePassed节点：① componentAnnotation#dependencies中的dependency节点；② component关联的所有module节点，筛选出abstact抽象module节点但是里面的bindingMethod方法都是非abstact和非static修饰的具体实现方法；mustBePassed节点必须通过creator中非@BindsInstanc修饰的方法（或方法参数）的参数传递实例化对象；
            //
            // - （3）非@BindsInstance修饰的方法或方法参数，该方法参数类型在当前creator中只能被设置一次，表示的外面实例化传递到当前component节点；
            validateCreators(component);

            //对component类中的子component也进行类似的校验
            component.childComponents().forEach(this::visitComponent);
        }

        /**
         * Validates that component dependencies do not form a cycle.
         */
        private void validateComponentDependencyHierarchy(ComponentDescriptor component) {
            validateComponentDependencyHierarchy(component, component.typeElement(), new ArrayDeque<>());
        }

        /**
         * Recursive method to validate that component dependencies do not form a cycle.
         */
        private void validateComponentDependencyHierarchy(
                ComponentDescriptor component, TypeElement dependency, Deque<TypeElement> dependencyStack) {

            if (dependencyStack.contains(dependency)) {
                // Current component has already appeared in the component chain.
                StringBuilder message = new StringBuilder();
                message.append(component.typeElement().getQualifiedName());
                message.append(" contains a cycle in its component dependencies:\n");
                dependencyStack.push(dependency);
                appendIndentedComponentsList(message, dependencyStack);
                dependencyStack.pop();
                reportComponentItem(
                        compilerOptions.scopeCycleValidationType().diagnosticKind().get(),
                        component,
                        message.toString());
            } else if (compilerOptions.validateTransitiveComponentDependencies()
                    // Always validate direct component dependencies referenced by this component regardless
                    // of the flag value
                    || dependencyStack.isEmpty()) {

                rootComponentAnnotation(dependency)
                        .ifPresent(
                                componentAnnotation -> {
                                    dependencyStack.push(dependency);

                                    for (TypeElement nextDependency : componentAnnotation.dependencies()) {
                                        validateComponentDependencyHierarchy(
                                                component, nextDependency, dependencyStack);
                                    }

                                    dependencyStack.pop();
                                });
            }
        }

        /**
         * Validates that among the dependencies there are no cycles within the scoping chain, and that
         * singleton components have no scoped dependencies.
         */
        private void validateDependencyScopes(ComponentDescriptor component) {
            ImmutableSet<Scope> scopes = component.scopes();//component类上使用Scope注解修饰的注解

            //componentAnnotation#dependencies中的所有类，筛选出使用Scope修饰的注解修饰的类
            ImmutableSet<TypeElement> scopedDependencies =
                    scopedTypesIn(
                            component
                                    //componentAnnotation#dependencies里面的类生成DEPENDENCY类型的ComponentRequirement对象集合
                                    .dependencies()
                                    .stream()
                                    .map(ComponentRequirement::typeElement)
                                    .collect(toImmutableSet()));


            if (!scopes.isEmpty()) {//表示component节点上使用了Scope注解修饰的注解
                //Singleton注解转换成Scope对象
                Scope singletonScope = singletonScope(elements);
                // Dagger 1.x scope compatibility requires this be suppress-able.
                if (compilerOptions.scopeCycleValidationType().diagnosticKind().isPresent()
                        && scopes.contains(singletonScope)) {
                    // Singleton is a special-case representing the longest lifetime, and therefore
                    // @Singleton components may not depend on scoped components
                    //如果component使用了@Singleton注解修饰，componentAnnotation#dependency里面的dependency节点最好不要使用Scope修饰的注解修饰
                    if (!scopedDependencies.isEmpty()) {
                        StringBuilder message =
                                new StringBuilder(
                                        "This @Singleton component cannot depend on scoped components:\n");
                        appendIndentedComponentsList(message, scopedDependencies);
                        reportComponentItem(
                                compilerOptions.scopeCycleValidationType().diagnosticKind().get(),
                                component,
                                message.toString());
                    }
                } else {
                    // Dagger 1.x scope compatibility requires this be suppress-able.
                    //如果component节点没有使用Singleton注解而是其他的Scope类型注解：component节点使用的Scope注解类型在componentAnnotation#dependencies中的dependency
                    // （或如果dependency是component，那么这里的componentAnnotation#dependency）中不允许再次出现。
                    if (!compilerOptions.scopeCycleValidationType().equals(ValidationType.NONE)) {
                        validateDependencyScopeHierarchy(
                                component, component.typeElement(), new ArrayDeque<>(), new ArrayDeque<>());
                    }
                }
            } else {
                // Scopeless components may not depend on scoped components.
                //如果component节点没有使用Scope注解修饰的注解修饰，那么componentAnnotation#dependencies里面的dependency节点不允许使用Scope注解修饰的注解修饰；
                if (!scopedDependencies.isEmpty()) {
                    StringBuilder message =
                            new StringBuilder(component.typeElement().getQualifiedName())
                                    .append(" (unscoped) cannot depend on scoped components:\n");
                    appendIndentedComponentsList(message, scopedDependencies);
                    reportComponentError(component, message.toString());
                }
            }
        }

        private void validateModules(ComponentDescriptor component) {
            for (ModuleDescriptor module : component.modules()) {
                //module使用了abstract修饰
                if (module.moduleElement().getModifiers().contains(Modifier.ABSTRACT)) {
                    //查找使用Produces或Provides修饰的绑定方法
                    for (ContributionBinding binding : module.bindings()) {
                        //如果当前绑定没有使用abstract和static修饰，这说明需要实例化module类
                        if (binding.requiresModuleInstance()) {
                            report(component).addError(abstractModuleHasInstanceBindingMethodsError(module));
                            break;
                        }
                    }
                }
            }
        }

        private String abstractModuleHasInstanceBindingMethodsError(ModuleDescriptor module) {
            String methodAnnotations;
            switch (module.kind()) {
                case MODULE:
                    methodAnnotations = "@Provides";
                    break;
                case PRODUCER_MODULE:
                    methodAnnotations = "@Provides or @Produces";
                    break;
                default:
                    throw new AssertionError(module.kind());
            }
            return String.format(
                    "%s is abstract and has instance %s methods. Consider making the methods static or "
                            + "including a non-abstract subclass of the module instead.",
                    module.moduleElement(), methodAnnotations);
        }

        private void validateCreators(ComponentDescriptor component) {
            //creator节点不存在，不需要校验了
            if (!component.creatorDescriptor().isPresent()) {
                // If no builder, nothing to validate.
                return;
            }

            ComponentCreatorDescriptor creator = component.creatorDescriptor().get();
            ErrorMessages.ComponentCreatorMessages messages = ErrorMessages.creatorMessagesFor(creator.annotation());

            // Requirements for modules and dependencies that the creator can set
            //creator中的方法（或方法参数）没有使用BindsInstance注解修饰
            Set<ComponentRequirement> creatorModuleAndDependencyRequirements =
                    creator.moduleAndDependencyRequirements();

            // Modules and dependencies the component requires
            //当前component类关联的所有非abstract修饰的module类和componentAnnotation#dependencies生成的ComponentRequirement对象
            Set<ComponentRequirement> componentModuleAndDependencyRequirements =
                    component.dependenciesAndConcreteModules();

            // Requirements that the creator can set that don't match any requirements that the component
            // actually has.
            //（1）component类关联的所有非abstract修饰的module类和componentAnnotation#dependencies生成的ComponentRequirement对象 和
            // （2）creator中的方法（或方法参数）没有使用BindsInstance注解修饰
            //（1）中筛选出（2）中不存在的item
            Set<ComponentRequirement> inapplicableRequirementsOnCreator =
                    Sets.difference(
                            creatorModuleAndDependencyRequirements, componentModuleAndDependencyRequirements);

            DeclaredType container = asDeclared(creator.typeElement().asType());

            //creator中的除了BindsInstance（表示外部注入）外的ComponentRequirement对象一定是来源于所在component类关联的所有非abstact修饰的module节点和componentAnnotation#dependencies
            if (!inapplicableRequirementsOnCreator.isEmpty()) {
                Collection<Element> excessElements =
                        Multimaps.filterKeys(
                                creator.unvalidatedRequirementElements(), in(inapplicableRequirementsOnCreator))
                                .values();
                String formatted =
                        excessElements.stream()
                                .map(element -> formatElement(element, container))
                                .collect(joining(", ", "[", "]"));
                report(component)
                        .addError(String.format(messages.extraSetters(), formatted), creator.typeElement());
            }

            // Component requirements that the creator must be able to set
            //component类关联的可实例化的module类和componentAnnotation#dependencies筛选：
            // （1）所有的dependency
            // （2）component类关联的所有非abstract修饰的module类，并且该类不能直接实例化 && 该类的bingdingMethod方法使用非abstract和非static修饰
            Set<ComponentRequirement> mustBePassed =
                    Sets.filter(
                            componentModuleAndDependencyRequirements,
                            input -> input.nullPolicy(elements, metadataUtil).equals(ComponentRequirement.NullPolicy.THROW));

            // Component requirements that the creator must be able to set, but can't
            //mustBePassed中筛选出 creatorModuleAndDependencyRequirements不存在的
            Set<ComponentRequirement> missingRequirements =
                    Sets.difference(mustBePassed, creatorModuleAndDependencyRequirements);

            //意思就是：当前creator中必须通过方法设置component关联的module不能被实例化的module和componentAnnotation#dependencies
            if (!missingRequirements.isEmpty()) {
                report(component)
                        .addError(
                                String.format(
                                        messages.missingSetters(),
                                        missingRequirements.stream().map(ComponentRequirement::type).collect(toList())),
                                creator.typeElement());
            }

            // Validate that declared creator requirements (modules, dependencies) have unique types.
            //非@BindsInstance修饰的方法或方法参数，该方法参数类型在当前creator中只能被设置一次
            ImmutableSetMultimap<Equivalence.Wrapper<TypeMirror>, Element> declaredRequirementsByType =
                    Multimaps.filterKeys(
                            creator.unvalidatedRequirementElements(),
                            creatorModuleAndDependencyRequirements::contains)
                            .entries().stream()
                            .collect(
                                    toImmutableSetMultimap(entry -> entry.getKey().wrappedType(), Map.Entry::getValue));

            declaredRequirementsByType
                    .asMap()
                    .forEach(
                            (typeWrapper, elementsForType) -> {
                                if (elementsForType.size() > 1) {
                                    TypeMirror type = typeWrapper.get();
                                    // TODO(cgdecker): Attach this error message to the factory method rather than
                                    // the component type if the elements are factory method parameters AND the
                                    // factory method is defined by the factory type itself and not by a supertype.
                                    report(component)
                                            .addError(
                                                    String.format(
                                                            messages.multipleSettersForModuleOrDependencyType(),
                                                            type,
                                                            transform(
                                                                    elementsForType, element -> formatElement(element, container))),
                                                    creator.typeElement());
                                }
                            });

            // TODO(cgdecker): Duplicate binding validation should handle the case of multiple elements
            // that set the same bound-instance Key, but validating that here would make it fail faster
            // for subcomponents.
        }

        private String formatElement(Element element, DeclaredType container) {
            // TODO(cgdecker): Extract some or all of this to another class?
            // But note that it does different formatting for parameters than
            // DaggerElements.elementToString(Element).
            switch (element.getKind()) {
                case METHOD:
                    return methodSignatureFormatter.format(
                            MoreElements.asExecutable(element), Optional.of(container));
                case PARAMETER:
                    return formatParameter(MoreElements.asVariable(element), container);
                default:
                    // This method shouldn't be called with any other type of element.
                    throw new AssertionError();
            }
        }

        private String formatParameter(VariableElement parameter, DeclaredType container) {
            // TODO(cgdecker): Possibly leave the type (and annotations?) off of the parameters here and
            // just use their names, since the type will be redundant in the context of the error message.
            StringJoiner joiner = new StringJoiner(" ");
            parameter.getAnnotationMirrors().stream().map(Object::toString).forEach(joiner::add);
            TypeMirror parameterType = resolveParameterType(parameter, container);
            return joiner
                    .add(stripCommonTypePrefixes(parameterType.toString()))
                    .add(parameter.getSimpleName())
                    .toString();
        }

        private TypeMirror resolveParameterType(VariableElement parameter, DeclaredType container) {
            ExecutableElement method =
                    MoreElements.asExecutable(parameter.getEnclosingElement());
            int parameterIndex = method.getParameters().indexOf(parameter);

            ExecutableType methodType = MoreTypes.asExecutable(types.asMemberOf(container, method));
            return methodType.getParameterTypes().get(parameterIndex);
        }

        /**
         * Validates that scopes do not participate in a scoping cycle - that is to say, scoped
         * components are in a hierarchical relationship terminating with Singleton.
         *
         * <p>As a side-effect, this means scoped components cannot have a dependency cycle between
         * themselves, since a component's presence within its own dependency path implies a cyclical
         * relationship between scopes. However, cycles in component dependencies are explicitly checked
         * in {@link #validateComponentDependencyHierarchy(ComponentDescriptor)}.
         * <p>
         * 当前component和componentAnnotation#dependencies（如果这里的dependency是一个component，
         * 那么也包括这个dependency的componentAnnotation#dependencies）存在使用了同一个Scope注解修饰的注解，根据bazel命令配置决定是报错还是警告（无则不作任何处理）
         * <p>
         * （1）收集dependency使用Scope注解修饰的注解，如果和scopeStack集合中有交集，那么根据compilerOptions.scopeCycleValidationType().diagnosticKind()确定是报错还是警告；
         * （2）如果dependency使用Scope注解修饰的注解和scopeStack集合没有交集，那么对dependency中如果使用了Component注解修饰，获取Component#dependencies里面的dependency重复（1）操作。
         */
        private void validateDependencyScopeHierarchy(
                ComponentDescriptor component,
                TypeElement dependency,
                //（1）收集componentAnnotation#dependencies中dependency使用了Scope注解修饰的注解；（2）如果dependency如果是component类，那么还有继续对该dependency进行递归（1）
                Deque<ImmutableSet<Scope>> scopeStack,
                Deque<TypeElement> scopedDependencyStack//push存储在处理的dependency类，处理过后即可pop移除
        ) {

            ImmutableSet<Scope> scopes = scopesOf(dependency);

            //如果两个集合存在交集：dependency节点上使用的scope注解修饰的注解 和 scopeStack存在交集，
            if (stackOverlaps(scopeStack, scopes)) {

                scopedDependencyStack.push(dependency);

                // Current scope has already appeared in the component chain.
                StringBuilder message = new StringBuilder();
                message.append(component.typeElement().getQualifiedName());
                message.append(" depends on scoped components in a non-hierarchical scope ordering:\n");
                appendIndentedComponentsList(message, scopedDependencyStack);
                if (compilerOptions.scopeCycleValidationType().diagnosticKind().isPresent()) {
                    reportComponentItem(
                            compilerOptions.scopeCycleValidationType().diagnosticKind().get(),
                            component,
                            message.toString());
                }
                scopedDependencyStack.pop();

            } else if (compilerOptions.validateTransitiveComponentDependencies()
                    // Always validate direct component dependencies referenced by this component regardless
                    // of the flag value
                    || scopedDependencyStack.isEmpty()) {

                // TODO(beder): transitively check scopes of production components too.
                rootComponentAnnotation(dependency)
                        //dependency节点上的componentAnnotation
                        .filter(componentAnnotation -> !componentAnnotation.isProduction())

                        .ifPresent(
                                componentAnnotation -> {
                                    //componentAnnotation#depencencies中的类，筛选使用Scope注解修饰的注解修饰的类
                                    ImmutableSet<TypeElement> scopedDependencies =
                                            scopedTypesIn(componentAnnotation.dependencies());

                                    if (!scopedDependencies.isEmpty()) {
                                        // empty can be ignored (base-case)
                                        scopeStack.push(scopes);
                                        scopedDependencyStack.push(dependency);
                                        for (TypeElement scopedDependency : scopedDependencies) {
                                            validateDependencyScopeHierarchy(
                                                    component,
                                                    scopedDependency,
                                                    scopeStack,
                                                    scopedDependencyStack);
                                        }
                                        scopedDependencyStack.pop();
                                        scopeStack.pop();
                                    }
                                }); // else: we skip component dependencies which are not components
            }
        }

        private <T> boolean stackOverlaps(Deque<ImmutableSet<T>> stack, ImmutableSet<T> set) {
            for (ImmutableSet<T> entry : stack) {
                //Sets.intersection():返回两个集合的交集的不可更改的视图
                if (!Sets.intersection(entry, set).isEmpty()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Appends and formats a list of indented component types (with their scope annotations).
         */
        private void appendIndentedComponentsList(StringBuilder message, Iterable<TypeElement> types) {
            for (TypeElement scopedComponent : types) {
                message.append(INDENT);
                for (Scope scope : scopesOf(scopedComponent)) {
                    message.append(getReadableSource(scope)).append(' ');
                }
                message
                        .append(stripCommonTypePrefixes(scopedComponent.getQualifiedName().toString()))
                        .append('\n');
            }
        }

        /**
         * Returns a set of type elements containing only those found in the input set that have a
         * scoping annotation.
         * <p>
         * 筛选types集合中使用了Scope注解修饰的注解
         */
        private ImmutableSet<TypeElement> scopedTypesIn(Collection<TypeElement> types) {
            return types.stream().filter(type -> !scopesOf(type).isEmpty()).collect(toImmutableSet());
        }
    }
}
