package dagger.internal.codegen.validation;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

import dagger.assisted.AssistedInject;
import dagger.internal.codegen.base.ClearableCache;
import dagger.internal.codegen.base.Util;
import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.Accessibility;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.Scope;

import static com.google.auto.common.MoreElements.asType;
import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static dagger.internal.codegen.base.Scopes.scopesOf;
import static dagger.internal.codegen.binding.AssistedInjectionAnnotations.assistedInjectedConstructors;
import static dagger.internal.codegen.binding.InjectionAnnotations.injectedConstructors;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A {@linkplain ValidationReport validator} for {@link Inject}-annotated elements and the types
 * that contain them.
 */
@Singleton
public final class InjectValidator implements ClearableCache {

    private final DaggerTypes types;
    private final DaggerElements elements;
    private final CompilerOptions compilerOptions;
    private final DependencyRequestValidator dependencyRequestValidator;
    private final Optional<Diagnostic.Kind> privateAndStaticInjectionDiagnosticKind;
    private final InjectionAnnotations injectionAnnotations;
    private final KotlinMetadataUtil metadataUtil;
    private final Map<ExecutableElement, ValidationReport> reports = new HashMap<>();

    @Inject
    InjectValidator(
            DaggerTypes types,
            DaggerElements elements,
            DependencyRequestValidator dependencyRequestValidator,
            CompilerOptions compilerOptions,
            InjectionAnnotations injectionAnnotations,
            KotlinMetadataUtil metadataUtil
    ) {
        this(
                types,
                elements,
                compilerOptions,
                dependencyRequestValidator,
                Optional.empty(),
                injectionAnnotations,
                metadataUtil);
    }

    private InjectValidator(
            DaggerTypes types,
            DaggerElements elements,
            CompilerOptions compilerOptions,
            DependencyRequestValidator dependencyRequestValidator,
            Optional<Diagnostic.Kind> privateAndStaticInjectionDiagnosticKind,
            InjectionAnnotations injectionAnnotations,
            KotlinMetadataUtil metadataUtil) {
        this.types = types;
        this.elements = elements;
        this.compilerOptions = compilerOptions;
        this.dependencyRequestValidator = dependencyRequestValidator;
        this.privateAndStaticInjectionDiagnosticKind = privateAndStaticInjectionDiagnosticKind;
        this.injectionAnnotations = injectionAnnotations;
        this.metadataUtil = metadataUtil;
    }

    @Override
    public void clearCache() {
        reports.clear();
    }

    /**
     * Returns a new validator that performs the same validation as this one, but is strict about
     * rejecting optionally-specified JSR 330 behavior that Dagger doesn't support (unless {@code
     * -Adagger.ignorePrivateAndStaticInjectionForComponent=enabled} was set in the javac options).
     * <p>
     * 编译时命令行是否输入了忽略private和static修饰的Inject（或AssistedInject）检查。
     * <p>
     * 我们这边默认肯定没有忽略，所有生成了一个新的InjectValidator对象
     */
    public InjectValidator whenGeneratingCode() {
        return compilerOptions.ignorePrivateAndStaticInjectionForComponent()
                ? this
                : new InjectValidator(
                types,
                elements,
                compilerOptions,
                dependencyRequestValidator,
                Optional.of(Diagnostic.Kind.ERROR),
                injectionAnnotations,
                metadataUtil);
    }

    //入口1：从如果节点是构造函数开始校验
    public ValidationReport validateConstructor(ExecutableElement constructorElement) {
        return Util.reentrantComputeIfAbsent(reports, constructorElement, this::validateConstructorUncached);
    }

    //未缓存情况下执行该方法，并且存储于reports
    //从节点构造函数校验
    private ValidationReport validateConstructorUncached(ExecutableElement constructorElement) {

        ValidationReport.Builder builder =
                ValidationReport.about(asType(constructorElement.getEnclosingElement()));

        //1.构造函数不允许同时使用Inject注解和AssistedInject注解
        if (isAnnotationPresent(constructorElement, Inject.class)
                && isAnnotationPresent(constructorElement, AssistedInject.class)) {
            builder.addError("Constructors cannot be annotated with both @Inject and @AssistedInject");
        }

        //isAnnotationPresent()：构造函数节点被Inject或AssistedInject修饰
        Class<?> injectAnnotation =
                isAnnotationPresent(constructorElement, Inject.class) ? Inject.class : AssistedInject.class;

        //2.被Inject或AssistedInject修饰的构造函数不允许使用private修饰
        if (constructorElement.getModifiers().contains(PRIVATE)) {
            builder.addError(
                    "Dagger does not support injection into private constructors", constructorElement);
        }

        //3.被Inject或AssistedInject修饰的构造函数不能被Qualifier修饰过的注解修饰
        for (AnnotationMirror qualifier : injectionAnnotations.getQualifiers(constructorElement)) {
            builder.addError(
                    String.format(
                            "@Qualifier annotations are not allowed on @%s constructors",
                            injectAnnotation.getSimpleName()),
                    constructorElement,
                    qualifier);
        }

        String scopeErrorMsg =
                String.format(
                        "@Scope annotations are not allowed on @%s constructors",
                        injectAnnotation.getSimpleName());

        if (injectAnnotation == Inject.class) {
            scopeErrorMsg += "; annotate the class instead";
        }

        //4.被Inject或AssistedInject修饰的构造函数不能被使用Scope注解修饰的注解修饰
        for (Scope scope : scopesOf(constructorElement)) {
            builder.addError(scopeErrorMsg, constructorElement, scope.scopeAnnotation().java());
        }

        //5.校验被Inject或AssistedInject修饰的构造函数里面的参数（参数即依赖）
        for (VariableElement parameter : constructorElement.getParameters()) {
            validateDependencyRequest(builder, parameter);
        }

        //6.被Inject或AssistedInject修饰的构造函数如果继承异常，那么异常一定要继承RuntimeException或Error，否则报错
        if (throwsCheckedExceptions(constructorElement)) {
            builder.addItem(
                    String.format(
                            "Dagger does not support checked exceptions on @%s constructors",
                            injectAnnotation.getSimpleName()),
                    privateMemberDiagnosticKind(),//根据传递的参数判断当前判断是什么类型，错误还是警告又或者其他
                    constructorElement);
        }

        //7.使用了Inject或AssistedInject修饰的元素所在类不可以被private类使用
        checkInjectIntoPrivateClass(constructorElement, builder);

        TypeElement enclosingElement =
                MoreElements.asType(constructorElement.getEnclosingElement());

        Set<Modifier> typeModifiers = enclosingElement.getModifiers();
        //8.使用了Inject或AssistedInject修饰的元素所在类不能是Abstract抽象类，否则报错
        if (typeModifiers.contains(ABSTRACT)) {
            builder.addError(
                    String.format(
                            "@%s is nonsense on the constructor of an abstract class",
                            injectAnnotation.getSimpleName()),
                    constructorElement);
        }

        //9.如果使用Inject或AssistedInject修饰的构造函数所在类是一个内部类，那么必须使用Static修饰。
        if (enclosingElement.getNestingKind().isNested()
                && !typeModifiers.contains(STATIC)) {
            builder.addError(
                    String.format(
                            "@%s constructors are invalid on inner classes. "
                                    + "Did you mean to make the class static?",
                            injectAnnotation.getSimpleName()),
                    constructorElement);
        }

        // This is computationally expensive, but probably preferable to a giant index
        ImmutableSet<ExecutableElement> injectConstructors =
                ImmutableSet.<ExecutableElement>builder()
                        .addAll(injectedConstructors(enclosingElement))
                        .addAll(assistedInjectedConstructors(enclosingElement))
                        .build();

        //10.一个类最多只能有一个构造函数被Inject或AssitedInject修饰
        if (injectConstructors.size() > 1) {
            builder.addError("Types may only contain one injected constructor", constructorElement);
        }

        ImmutableSet<Scope> scopes = scopesOf(enclosingElement);

        //11.使用AssistedInject修饰的构造函数所在类，该类不能被使用Scope注解修饰的注解修饰
        if (injectAnnotation == AssistedInject.class) {
            for (Scope scope : scopes) {
                builder.addError(
                        "A type with an @AssistedInject-annotated constructor cannot be scoped",
                        enclosingElement,
                        scope.scopeAnnotation().java());
            }
        }
        //12.使用Inject修饰的构造函数所在类，该类最多只能有一个使用Scope注解修饰的注解修饰
        else if (scopes.size() > 1) {
            for (Scope scope : scopes) {
                builder.addError(
                        "A single binding may not declare more than one @Scope",
                        enclosingElement,
                        scope.scopeAnnotation().java());
            }
        }
        return builder.build();
    }

    //校验被Inject修饰的变量
    private ValidationReport validateField(VariableElement fieldElement) {
        ValidationReport.Builder builder = ValidationReport.about(fieldElement);
        Set<Modifier> modifiers = fieldElement.getModifiers();

        //1.不能被final修饰
        if (modifiers.contains(FINAL)) {
            builder.addError("@Inject fields may not be final", fieldElement);
        }

        //2.不能被private修饰(当然了根据privateMemberDiagnosticKind，是不能还是警告，又或者其他)
        if (modifiers.contains(PRIVATE)) {
            builder.addItem(
                    "Dagger does not support injection into private fields",
                    privateMemberDiagnosticKind(),
                    fieldElement);
        }

        //3.不能被Static修饰(当然了根据privateMemberDiagnosticKind，是错误还是警告，又或者其他)
        if (modifiers.contains(STATIC)) {
            builder.addItem(
                    "Dagger does not support injection into static fields",
                    staticMemberDiagnosticKind(),
                    fieldElement);
        }

        //4.校验被Inject修饰的变量作为依赖的校验
        validateDependencyRequest(builder, fieldElement);

        return builder.build();
    }

    //校验被Inject修饰的方法
    private ValidationReport validateMethod(ExecutableElement methodElement) {
        ValidationReport.Builder builder = ValidationReport.about(methodElement);
        Set<Modifier> modifiers = methodElement.getModifiers();
        //1.方法不能被abstract修饰
        if (modifiers.contains(ABSTRACT)) {
            builder.addError("Methods with @Inject may not be abstract", methodElement);
        }

        //2.不能被private修饰(当然了根据privateMemberDiagnosticKind，是不能还是警告，又或者其他)
        if (modifiers.contains(PRIVATE)) {
            builder.addItem(
                    "Dagger does not support injection into private methods",
                    privateMemberDiagnosticKind(),
                    methodElement);
        }

        //3.不能被Static修饰(当然了根据privateMemberDiagnosticKind，是错误还是警告，又或者其他)
        if (modifiers.contains(STATIC)) {
            builder.addItem(
                    "Dagger does not support injection into static methods",
                    staticMemberDiagnosticKind(),
                    methodElement);
        }

        //4.使用Inject修饰的方法不能存在泛型类型
        if (!methodElement.getTypeParameters().isEmpty()) {//getTypeParameters():所有泛型类型
            builder.addError("Methods with @Inject may not declare type parameters", methodElement);
        }

        //5.使用Inject修饰的方法不能在方法上实现抛异常功能
        if (!methodElement.getThrownTypes().isEmpty()) {
            builder.addError("Methods with @Inject may not throw checked exceptions. "
                    + "Please wrap your exceptions in a RuntimeException instead.", methodElement);
        }

        //6.方法上的参数作为依赖，需要做依赖校验
        for (VariableElement parameter : methodElement.getParameters()) {
            validateDependencyRequest(builder, parameter);
        }

        return builder.build();
    }

    private void validateDependencyRequest(
            ValidationReport.Builder builder, VariableElement parameter) {
        dependencyRequestValidator.validateDependencyRequest(builder, parameter, parameter.asType());
        //不能是Produced或Producer类型
        dependencyRequestValidator.checkNotProducer(builder, parameter);
    }

    //入口2：从如果节点是成员类型（Inject或AssitedInject修饰的普通方法或变量都是成员类型）开始校验
    public ValidationReport validateMembersInjectionType(TypeElement typeElement) {

        ValidationReport.Builder builder = ValidationReport.about(typeElement);
        boolean hasInjectedMembers = false;

        //1.遍历类收集变量，如果变量被Inject修饰，校验
        for (VariableElement element : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (MoreElements.isAnnotationPresent(element, Inject.class)) {
                hasInjectedMembers = true;
                ValidationReport report = validateField(element);
                if (!report.isClean()) {
                    builder.addSubreport(report);
                }
            }
        }

        //2.遍历类收集方法，如果方法被Inject修饰，校验
        for (ExecutableElement element : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if (MoreElements.isAnnotationPresent(element, Inject.class)) {
                hasInjectedMembers = true;
                ValidationReport report = validateMethod(element);
                if (!report.isClean()) {
                    builder.addSubreport(report);
                }
            }
        }

        //3.Inject或AssitedInject修饰的类里面存在Inject修饰的变量或方法
        if (hasInjectedMembers) {
            //（1）.使用了Inject修饰的类所在类不可以被private类使用
            checkInjectIntoPrivateClass(typeElement, builder);
            //（2）.Inject注解不能在Kotlin文件中使用
            checkInjectIntoKotlinObject(typeElement, builder);
        }

        //4.当前使用Inject或AssitedInject修饰的类存在超类，那么对超类进行校验
        TypeMirror superclass = typeElement.getSuperclass();
        if (!superclass.getKind().equals(TypeKind.NONE)) {
            ValidationReport report = validateType(MoreTypes.asTypeElement(superclass));
            if (!report.isClean()) {
                builder.addSubreport(report);
            }
        }
        return builder.build();
    }

    public ValidationReport validateType(TypeElement typeElement) {
        ValidationReport.Builder builder = ValidationReport.about(typeElement);

        //1.对类成员（变量和方法）校验
        ValidationReport membersInjectionReport = validateMembersInjectionType(typeElement);
        if (!membersInjectionReport.isClean()) {
            builder.addSubreport(membersInjectionReport);
        }

        //2.校验类的构造函数
        for (ExecutableElement element :
                ElementFilter.constructorsIn(typeElement.getEnclosedElements())) {
            if (isAnnotationPresent(element, Inject.class)
                    || isAnnotationPresent(element, AssistedInject.class)) {
                ValidationReport report = validateConstructor(element);
                if (!report.isClean()) {
                    builder.addSubreport(report);
                }
            }
        }
        return builder.build();
    }

    /**
     * Returns true if the given method element declares a checked exception.
     * <p>
     * 如果用异常抛出，但是异常抛出没有继承runtimeExceptionType 并且没有继承errorType 直接返回true
     */
    private boolean throwsCheckedExceptions(ExecutableElement methodElement) {
        TypeMirror runtimeExceptionType = elements.getTypeElement(RuntimeException.class).asType();
        TypeMirror errorType = elements.getTypeElement(Error.class).asType();
        for (TypeMirror thrownType : methodElement.getThrownTypes()) {
            //如果用异常抛出，但是异常抛出没有继承runtimeExceptionType 并且没有继承errorType 直接返回true
            if (!types.isSubtype(thrownType, runtimeExceptionType)
                    && !types.isSubtype(thrownType, errorType)) {
                return true;
            }
        }
        return false;
    }

    private void checkInjectIntoPrivateClass(Element element, ValidationReport.Builder builder) {

        //使用了Inject或AssistedInject修饰的元素所在类不可以被private类使用（可能是警告，这种情况不被支持）
        if (!Accessibility.isElementAccessibleFromOwnPackage(
                DaggerElements.closestEnclosingTypeElement(element))) {
            builder.addItem(
                    "Dagger does not support injection into private classes",
                    privateMemberDiagnosticKind(),
                    element);
        }
    }

    private void checkInjectIntoKotlinObject(TypeElement element, ValidationReport.Builder builder) {
        if (metadataUtil.isObjectClass(element) || metadataUtil.isCompanionObjectClass(element)) {
            builder.addError("Dagger does not support injection into Kotlin objects", element);
        }
    }

    private Diagnostic.Kind privateMemberDiagnosticKind() {
        return privateAndStaticInjectionDiagnosticKind.orElse(
                compilerOptions.privateMemberValidationKind());
    }

    private Diagnostic.Kind staticMemberDiagnosticKind() {
        return privateAndStaticInjectionDiagnosticKind.orElse(
                compilerOptions.staticMemberValidationKind());
    }
}
