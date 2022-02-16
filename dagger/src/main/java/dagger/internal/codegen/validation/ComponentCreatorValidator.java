package dagger.internal.codegen.validation;


import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ObjectArrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import dagger.internal.codegen.base.ClearableCache;
import dagger.internal.codegen.binding.ComponentCreatorAnnotation;
import dagger.internal.codegen.binding.ErrorMessages;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.base.Util.reentrantComputeIfAbsent;
import static dagger.internal.codegen.binding.ComponentCreatorAnnotation.getCreatorAnnotations;
import static dagger.internal.codegen.langmodel.DaggerElements.isAnnotationPresent;
import static javax.lang.model.SourceVersion.isKeyword;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * Validates types annotated with component creator annotations.
 */
@Singleton
public final class ComponentCreatorValidator implements ClearableCache {

    private final DaggerElements elements;
    private final DaggerTypes types;
    private final Map<TypeElement, ValidationReport> reports = new HashMap<>();
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    ComponentCreatorValidator(
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil metadataUtil
    ) {
        this.elements = elements;
        this.types = types;
        this.metadataUtil = metadataUtil;
    }

    @Override
    public void clearCache() {
        reports.clear();
    }


    /**
     * Validates that the given {@code type} is potentially a valid component creator type.
     * <p>
     * 校验入口，校验使用Builder或Factory注解的节点
     */
    public ValidationReport validate(TypeElement type) {
        return reentrantComputeIfAbsent(reports, type, this::validateUncached);
    }

    private ValidationReport validateUncached(TypeElement type) {
        ValidationReport.Builder report = ValidationReport.about(type);

        ImmutableSet<ComponentCreatorAnnotation> creatorAnnotations = getCreatorAnnotations(type);

        //1. creator节点上的creatorAnnotation注解最多只能使用一个；
        if (!validateOnlyOneCreatorAnnotation(creatorAnnotations, report)) {
            return report.build();
        }

        // Note: there's more validation in ComponentDescriptorValidator:
        // - to make sure the setter methods/factory parameters mirror the deps
        // - to make sure each type or key is set by only one method or parameter
        ElementValidator validator =
                new ElementValidator(type, report, getOnlyElement(creatorAnnotations));
        return validator.validate();
    }

    private boolean validateOnlyOneCreatorAnnotation(
            ImmutableSet<ComponentCreatorAnnotation> creatorAnnotations,
            ValidationReport.Builder report) {
        // creatorAnnotations should never be empty because this should only ever be called for
        // types that have been found to have some creator annotation
        if (creatorAnnotations.size() > 1) {
            String error =
                    "May not have more than one component Factory or Builder annotation on a type"
                            + ": found "
                            + creatorAnnotations;
            report.addError(error);
            return false;
        }

        return true;
    }


    /**
     * Validator for a single {@link TypeElement} that is annotated with a {@code Builder} or {@code
     * Factory} annotation.
     */
    private final class ElementValidator {
        private final TypeElement type;
        private final Element component;
        private final ValidationReport.Builder report;
        private final ComponentCreatorAnnotation annotation;
        private final ErrorMessages.ComponentCreatorMessages messages;

        private ElementValidator(
                TypeElement type, ValidationReport.Builder report, ComponentCreatorAnnotation annotation) {
            this.type = type;
            this.component = type.getEnclosingElement();
            this.report = report;
            this.annotation = annotation;
            this.messages = ErrorMessages.creatorMessagesFor(annotation);
        }

        /**
         * Validates the creator type.
         * <p>
         * 校验入口：
         */
        final ValidationReport validate() {

            //2. creator节点所在的父级节点一定是componentAll节点;
            if (!isAnnotationPresent(component, annotation.componentAnnotation())) {
                report.addError(messages.mustBeInComponent());
            }

            // If the type isn't a class or interface, don't validate anything else since the rest of the
            // messages will be bogus.
            //3. creator节点只能是类或接口，并且如果是类的话，该类只能存在一个无参非private修饰的构造函数；
            if (!validateIsClassOrInterface()) {
                return report.build();
            }

            //4. creator节点不能使用泛型，并且creatorMethod不能使用private修饰，必须使用static和abstract修饰：
            // - 存在疑点，creator如果是一个接口，creatorMethod没有使用static和abstract修饰也是没有问题的
            validateTypeRequirements();

            //针对Factory和Builder做分别校验
            switch (annotation.creatorKind()) {
                case FACTORY:
                    //5. 如果creator节点是factory类型：
                    // - （1）非private、非static、abstract修饰的（包括继承的）的factoryMethod方法有且仅有一个；
                    // - （2）factoryMethod方法不允许使用泛型；
                    // - （3）factoryMethod方法返回类型必须是componentAll节点（factoryMethod父级creator节点的父级componentAll节点）或componentAll的继承类；
                    // - 注：factoryMethod方法返回类型是componentAll节点的继承类，并且componentAll节点中的如果没有componentMethod方法，会报警告；
                    // - （4）factoryMethod方法不能使用@BindsInstance修饰;
                    // - （5） factoryMethod方法参数要么是@BindsInstance修饰 || 要么不使用原始类型；
                    validateFactory();
                    break;
                case BUILDER:
                    //6. 如果creator节点是builder类型：
                    //
                    // - （1）如果当前builder是kotlin文件，那么builderMethod不要使用了java的关键字；
                    //
                    // - （2）builderMethod方法总共能有两种：有且仅有一个参数的setterMethod方法和无参的buildMethod方法：
                    //
                    //  - ① buildMethod方法有且仅有一个，并且不允许使用泛型类型；
                    //
                    //  - ② buildMethod方法返回类型必须是componenteAll节点（buildMethod方法父级creator节点的父级componentAll节点）或componentAll继承类；
                    //
                    //  - 注：buildMethod方法返回类型是componentAll节点的继承类，并且componentAll节点中的如果没有componentMethod方法，会报警告;
                    //
                    //  - ③ buildMethod方法不允许使用@BindsInstance修饰;
                    //
                    //  - ④ setterMethod方法不能使用泛型，并且方法返回类型是void || 是builder节点及其子类；
                    //
                    //  - ⑤ setterMethod方法和方法参数不允许同时使用@BindsInstance修饰，如果setterMethod方法和方法参数都没有使用@BindsInstance修饰，那么setterMethod方法参数不能使用原始类型；
                    validateBuilder();
            }

            return report.build();
        }

        /**
         * Validates that the type is a class or interface type and returns true if it is.
         */
        private boolean validateIsClassOrInterface() {
            switch (type.getKind()) {
                case CLASS:
                    validateConstructor();
                    return true;
                case INTERFACE:
                    return true;
                default:
                    report.addError(messages.mustBeClassOrInterface());
            }
            return false;
        }

        //该类只能存在一个无参非private修饰的构造函数
        private void validateConstructor() {
            List<? extends Element> allElements = type.getEnclosedElements();
            List<ExecutableElement> constructors = ElementFilter.constructorsIn(allElements);

            boolean valid = true;
            if (constructors.size() != 1) {
                valid = false;
            } else {
                ExecutableElement constructor = getOnlyElement(constructors);
                valid =
                        constructor.getParameters().isEmpty() && !constructor.getModifiers().contains(PRIVATE);
            }

            if (!valid) {
                report.addError(messages.invalidConstructor());
            }
        }

        /**
         * Validates basic requirements about the type that are common to both creator kinds.
         */
        private void validateTypeRequirements() {

            //creator节点不能使用泛型类型
            if (!type.getTypeParameters().isEmpty()) {
                report.addError(messages.generics());
            }

            //creatorMethod不能使用private修饰，必须使用static修饰，必须使用abstract修饰
            Set<Modifier> modifiers = type.getModifiers();
            if (modifiers.contains(PRIVATE)) {
                report.addError(messages.isPrivate());
            }
            if (!modifiers.contains(STATIC)) {
                report.addError(messages.mustBeStatic());
            }
            // Note: Must be abstract, so no need to check for final.
            if (!modifiers.contains(ABSTRACT)) {
                report.addError(messages.mustBeAbstract());
            }
        }

        //校验使用Builder注解的节点
        private void validateBuilder() {
            //校验如果是kotlin文件不允许使用java关键字作为方法名
            validateClassMethodName();

            ExecutableElement buildMethod = null;
            //非private、非static、abstract修饰的（包括继承的）方法
            //1）.该方法参数最多只允许1个，否则报错
            //2）.方法无参校验方法返回类型：方法返回类型必须是component类型或其子类
            for (ExecutableElement method : elements.getUnimplementedMethods(type)) {

                switch (method.getParameters().size()) {
                    case 0: // If this is potentially a build() method, validate it returns the correct type.
                        if (validateFactoryMethodReturnType(method)) {
                            if (buildMethod != null) {
                                // If we found more than one build-like method, fail.
                                error(
                                        method,
                                        messages.twoFactoryMethods(),
                                        messages.inheritedTwoFactoryMethods(),
                                        buildMethod);
                            }
                        }
                        // We set the buildMethod regardless of the return type to reduce error spam.
                        buildMethod = method;
                        break;

                    case 1: // If this correctly had one parameter, make sure the return types are valid.
                        validateSetterMethod(method);
                        break;

                    default: // more than one parameter
                        error(
                                method,
                                messages.setterMethodsMustTakeOneArg(),
                                messages.inheritedSetterMethodsMustTakeOneArg());
                        break;
                }
            }

            //3.buildMethod方法有且仅有一个，并且该方法不允许使用泛型类型，否则报错
            if (buildMethod == null) {
                report.addError(messages.missingFactoryMethod());
            } else {
                validateNotGeneric(buildMethod);
            }
        }

        //校验如果是kotlin文件不允许使用java关键字作为方法名
        private void validateClassMethodName() {
            // Only Kotlin class can have method name the same as a Java reserved keyword, so only check
            // the method name if this class is a Kotlin class.
            if (metadataUtil.hasMetadata(type)) {
                metadataUtil
                        .getAllMethodNamesBySignature(type)
                        .forEach(
                                (signature, name) -> {
                                    if (isKeyword(name)) {
                                        report.addError("Can not use a Java keyword as method name: " + signature);
                                    }
                                });
            }
        }

        //对使用Builder注解的类里面的有且仅有一个参数的方法校验：
        private void validateSetterMethod(ExecutableElement method) {

            // 返回类型不是void && 方法返回类型必须不是builder类型或其子类，否则报错
            TypeMirror returnType = types.resolveExecutableType(method, type.asType()).getReturnType();
            if (returnType.getKind() != TypeKind.VOID && !types.isSubtype(type.asType(), returnType)) {
                error(
                        method,
                        messages.setterMethodsMustReturnVoidOrBuilder(),
                        messages.inheritedSetterMethodsMustReturnVoidOrBuilder());
            }

            //方法不允许使用泛型类型
            validateNotGeneric(method);

            VariableElement parameter = method.getParameters().get(0);

            boolean methodIsBindsInstance = isAnnotationPresent(method, TypeNames.BINDS_INSTANCE);
            boolean parameterIsBindsInstance = isAnnotationPresent(parameter, TypeNames.BINDS_INSTANCE);
            boolean bindsInstance = methodIsBindsInstance || parameterIsBindsInstance;

            //该方法和方法参数不能同时使用@BindsInstance注解修饰
            if (methodIsBindsInstance && parameterIsBindsInstance) {
                error(
                        method,
                        messages.bindsInstanceNotAllowedOnBothSetterMethodAndParameter(),
                        messages.inheritedBindsInstanceNotAllowedOnBothSetterMethodAndParameter());
            }

            //方法或方法参数都没有使用了@BindsInstance注解 && 如果参数是原始类型，则会报错。
            if (!bindsInstance && parameter.asType().getKind().isPrimitive()) {
                error(
                        method,
                        messages.nonBindsInstanceParametersMayNotBePrimitives(),
                        messages.inheritedNonBindsInstanceParametersMayNotBePrimitives());
            }
        }

        //Factory注解修饰的节点校验
        private void validateFactory() {
            //非private、非static、abstract修饰的（包括继承的）方法
            ImmutableList<ExecutableElement> abstractMethods =
                    elements.getUnimplementedMethods(type).asList();

            switch (abstractMethods.size()) {
                case 0:
                    report.addError(messages.missingFactoryMethod());
                    return;
                case 1:
                    break; // good
                default:
                    error(
                            abstractMethods.get(1),
                            messages.twoFactoryMethods(),
                            messages.inheritedTwoFactoryMethods(),
                            abstractMethods.get(0));
                    return;
            }

            validateFactoryMethod(getOnlyElement(abstractMethods));
        }

        /**
         * Validates that the given {@code method} is a valid component factory method.
         * <p>
         * Factory里面的方法校验
         */
        private void validateFactoryMethod(ExecutableElement method) {
            //方法不允许使用泛型类型
            validateNotGeneric(method);

            //校验方法返回类型
            if (!validateFactoryMethodReturnType(method)) {
                // If we can't determine that the single method is a valid factory method, don't bother
                // validating its parameters.
                return;
            }

            // factoryMethod方法参数要么是@BindsInstance修饰 || 要么不使用原始类型
            for (VariableElement parameter : method.getParameters()) {
                if (!isAnnotationPresent(parameter, TypeNames.BINDS_INSTANCE)
                        && parameter.asType().getKind().isPrimitive()) {
                    error(
                            method,
                            messages.nonBindsInstanceParametersMayNotBePrimitives(),
                            messages.inheritedNonBindsInstanceParametersMayNotBePrimitives());
                }
            }
        }

        /**
         * Validates that the factory method that actually returns a new component instance. Returns
         * true if the return type was valid.
         * <p>
         * 校验方法返回类型
         */
        private boolean validateFactoryMethodReturnType(ExecutableElement method) {

            TypeMirror returnType = types.resolveExecutableType(method, type.asType()).getReturnType();

            //isSameType(componentType.asType(), returnType)：returnType是component类型
            //isSubtype(component.asType(), returnType):returnType是component或其子类

            //方法返回类型必须是component节点或其子类，否则报错
            if (!types.isSubtype(component.asType(), returnType)) {
                error(
                        method,
                        messages.factoryMethodMustReturnComponentType(),
                        messages.inheritedFactoryMethodMustReturnComponentType());
                return false;
            }

            //方法不能使用@BindsInstance注解修饰
            if (isAnnotationPresent(method, TypeNames.BINDS_INSTANCE)) {
                error(
                        method,
                        messages.factoryMethodMayNotBeAnnotatedWithBindsInstance(),
                        messages.inheritedFactoryMethodMayNotBeAnnotatedWithBindsInstance());
                return false;
            }

            TypeElement componentType = MoreElements.asType(component);

            //如果方法返回类型是componentAll的子类
            if (!types.isSameType(componentType.asType(), returnType)) {

                //当前componentAll节点的方法
                ImmutableSet<ExecutableElement> methodsOnlyInComponent =
                        methodsOnlyInComponent(componentType);

                if (!methodsOnlyInComponent.isEmpty()) {
                    report.addWarning(
                            messages.factoryMethodReturnsSupertypeWithMissingMethods(
                                    componentType, type, returnType, method, methodsOnlyInComponent),
                            method);
                }
            }
            return true;
        }

        /**
         * Generates one of two error messages. If the method is enclosed in the subject, we target the
         * error to the method itself. Otherwise we target the error to the subject and list the method
         * as an argument. (Otherwise we have no way of knowing if the method is being compiled in this
         * pass too, so javac might not be able to pinpoint it's line of code.)
         */
        /*
         * For Component.Builder, the prototypical example would be if someone had:
         *    libfoo: interface SharedBuilder { void badSetter(A a, B b); }
         *    libbar: BarComponent { BarBuilder extends SharedBuilder } }
         * ... the compiler only validates BarBuilder when compiling libbar, but it fails because
         * of libfoo's SharedBuilder (which could have been compiled in a previous pass).
         * So we can't point to SharedBuilder#badSetter as the subject of the BarBuilder validation
         * failure.
         *
         * This check is a little more strict than necessary -- ideally we'd check if method's enclosing
         * class was included in this compile run.  But that's hard, and this is close enough.
         */
        private void error(
                ExecutableElement method,
                String enclosedError,
                String inheritedError,
                Object... extraArgs) {

            if (method.getEnclosingElement().equals(type)) {
                report.addError(String.format(enclosedError, extraArgs), method);
            } else {
                report.addError(String.format(inheritedError, ObjectArrays.concat(extraArgs, method)));
            }
        }


        /**
         * Validates that the given {@code method} is not generic. *
         */
        private void validateNotGeneric(ExecutableElement method) {
            //方法不允许使用泛型类型
            if (!method.getTypeParameters().isEmpty()) {
                error(
                        method,
                        messages.methodsMayNotHaveTypeParameters(),
                        messages.inheritedMethodsMayNotHaveTypeParameters());
            }
        }

        /**
         * Returns all methods defind in {@code componentType} which are not inherited from a supertype.
         * <p>
         * 返回 {@code componentType} 中定义的所有不是从超类型继承的方法。
         */
        private ImmutableSet<ExecutableElement> methodsOnlyInComponent(TypeElement componentType) {
            // TODO(ronshapiro): Ideally this shouldn't return methods which are redeclared from a
            // supertype, but do not change the return type. We don't have a good/simple way of checking
            // that, and it doesn't seem likely, so the warning won't be too bad.
            return ImmutableSet.copyOf(methodsIn(componentType.getEnclosedElements()));
        }
    }

}
