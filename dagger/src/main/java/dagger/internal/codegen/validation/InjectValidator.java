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
import static javax.lang.model.type.TypeKind.DECLARED;

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

        //1. 节点的构造函数不允许同时使用Inject注解和AssistedInject注解;
        if (isAnnotationPresent(constructorElement, Inject.class)
                && isAnnotationPresent(constructorElement, AssistedInject.class)) {
            builder.addError("Constructors cannot be annotated with both @Inject and @AssistedInject");
        }

        //isAnnotationPresent()：构造函数节点被Inject或AssistedInject修饰
        Class<?> injectAnnotation =
                isAnnotationPresent(constructorElement, Inject.class) ? Inject.class : AssistedInject.class;

        //2. 被Inject或AssistedInject修饰的构造函数不允许使用private修饰，也不能被Qualifier修饰的注解修饰；
        if (constructorElement.getModifiers().contains(PRIVATE)) {
            builder.addError(
                    "Dagger does not support injection into private constructors", constructorElement);
        }

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

        //3. 被Inject或AssistedInject修饰的构造函数不能被Scope注解修饰的注解修饰；
        for (Scope scope : scopesOf(constructorElement)) {
            builder.addError(scopeErrorMsg, constructorElement, scope.scopeAnnotation().java());
        }

        //4. Inject或AssistedInject修饰的构造函数的参数不能是Produced< T>和Producer< T>类型,并且对参数和参数类型做依赖校验，规则如下：
        //  - 注：当前参数类型剥离RequestKind< T>类型得到T作为keyType（当然如果是RequestKind.INSTANCE，那么keyType就是参数类型）
        //  - （1）如果参数节点使用了@Assiste修饰，不进行下面的依赖校验；
        //  - （2）如果参数节点使用了Qualifier修饰的注解修饰，那么该类型注解不得超过1个；
        //  - （3）如果参数节点没有使用Qualifier修饰的注解修饰，那么keytype类型的构造函数不能使用AssistedInject修饰；
        //  - （4）如果参数节点没有使用Qualifier修饰的注解修饰，并且keyType节点使用了@AssistedFactory修饰，那么当前参数要么是T要么是Provider< T>，不能是Lazy< T>、Producer< T>或Produced< T>；
        //  - （5）keyType不能使用通配符；
        //  - （6）如果keyType是MembersInjector< T>（必须存在T）类型，那么对T进行成员注入校验:a.不能使用Qualifier注解修饰的注解修饰;b.T只能是类或接口，并且如果是泛型，那么泛型类型只能是类或接口或数组，数组只能是类或接口或数组，并且T不允许出现例如List类型（必须是List< T>类型）；
        for (VariableElement parameter : constructorElement.getParameters()) {
            validateDependencyRequest(builder, parameter);
        }

        //5. 被Inject或AssistedInject修饰的构造函数如果throws异常，那么异常一定要是RuntimeException或Error或两者子类；
        if (throwsCheckedExceptions(constructorElement)) {
            builder.addItem(
                    String.format(
                            "Dagger does not support checked exceptions on @%s constructors",
                            injectAnnotation.getSimpleName()),
                    privateMemberDiagnosticKind(),//根据传递的参数判断当前判断是什么类型，错误还是警告又或者其他
                    constructorElement);
        }

        //6. 使用了Inject或AssistedInject修饰的构造函数所在父节点不可以被private类使用,该构造函数所在父节点也不要使用abstract修饰,并且如果构造函数所在父节点是一个内部类，那么该内部类必须使用static修饰；
        checkInjectIntoPrivateClass(constructorElement, builder);
        TypeElement enclosingElement =
                MoreElements.asType(constructorElement.getEnclosingElement());
        Set<Modifier> typeModifiers = enclosingElement.getModifiers();
        if (typeModifiers.contains(ABSTRACT)) {
            builder.addError(
                    String.format(
                            "@%s is nonsense on the constructor of an abstract class",
                            injectAnnotation.getSimpleName()),
                    constructorElement);
        }
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

        //7. 一个类最多只能有一个构造函数被Inject或AssitedInject修饰；
        if (injectConstructors.size() > 1) {
            builder.addError("Types may only contain one injected constructor", constructorElement);
        }

        ImmutableSet<Scope> scopes = scopesOf(enclosingElement);

        //8. 使用AssistedInject修饰的构造函数所在的父节点不能被使用Scope注解修饰的注解修饰；
        if (injectAnnotation == AssistedInject.class) {
            for (Scope scope : scopes) {
                builder.addError(
                        "A type with an @AssistedInject-annotated constructor cannot be scoped",
                        enclosingElement,
                        scope.scopeAnnotation().java());
            }
        }
        //9. 使用Inject修饰的构造函数所在父节点最多只能有一个使用Scope注解修饰的注解修饰。
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

        //不能被final修饰
        if (modifiers.contains(FINAL)) {
            builder.addError("@Inject fields may not be final", fieldElement);
        }

        //不能被private修饰(当然了根据privateMemberDiagnosticKind，是不能还是警告，又或者其他)
        if (modifiers.contains(PRIVATE)) {
            builder.addItem(
                    "Dagger does not support injection into private fields",
                    privateMemberDiagnosticKind(),
                    fieldElement);
        }

        //不能被Static修饰(当然了根据privateMemberDiagnosticKind，是错误还是警告，又或者其他)
        if (modifiers.contains(STATIC)) {
            builder.addItem(
                    "Dagger does not support injection into static fields",
                    staticMemberDiagnosticKind(),
                    fieldElement);
        }

        //校验被Inject修饰的变量作为依赖的校验
        validateDependencyRequest(builder, fieldElement);

        return builder.build();
    }

    //校验被Inject修饰的方法
    private ValidationReport validateMethod(ExecutableElement methodElement) {
        ValidationReport.Builder builder = ValidationReport.about(methodElement);
        Set<Modifier> modifiers = methodElement.getModifiers();
        //方法不能被abstract修饰
        if (modifiers.contains(ABSTRACT)) {
            builder.addError("Methods with @Inject may not be abstract", methodElement);
        }

        //不能被private修饰(当然了根据privateMemberDiagnosticKind，是不能还是警告，又或者其他)
        if (modifiers.contains(PRIVATE)) {
            builder.addItem(
                    "Dagger does not support injection into private methods",
                    privateMemberDiagnosticKind(),
                    methodElement);
        }

        //不能被Static修饰(当然了根据privateMemberDiagnosticKind，是错误还是警告，又或者其他)
        if (modifiers.contains(STATIC)) {
            builder.addItem(
                    "Dagger does not support injection into static methods",
                    staticMemberDiagnosticKind(),
                    methodElement);
        }

        //使用Inject修饰的方法不能存在泛型类型
        if (!methodElement.getTypeParameters().isEmpty()) {//getTypeParameters():所有泛型类型
            builder.addError("Methods with @Inject may not declare type parameters", methodElement);
        }

        //使用Inject修饰的方法不能在方法上实现抛异常功能
        if (!methodElement.getThrownTypes().isEmpty()) {
            builder.addError("Methods with @Inject may not throw checked exceptions. "
                    + "Please wrap your exceptions in a RuntimeException instead.", methodElement);
        }

        //方法上的参数作为依赖，需要做依赖校验
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

        //1. 对使用Inject修饰变量校验：
        // - （1）Inject修饰的变量节点不能使用final修饰；也不要使用private和static修饰（可能警告可能报错）；
        // - （2）当前变量和变量类型做依赖校验：
        // - 注：当前变量类型剥离RequestKind< T>类型得到T作为keyType（当然如果是RequestKind.INSTANCE，那么keyType就是变量类型）
        //  - ① 当前变量如果使用了Qulifiers注解修饰的注解，那么该类型的注解最多只能使用1个；
        //  - ② 如果变量节点没有使用Qualifier修饰的注解修饰，那么keytype类型的构造函数不能使用AssistedInject修饰；
        //  - ③ 如果变量节点没有使用Qualifier修饰的注解修饰，并且keyType节点使用了@AssistedFactory修饰，那么当前参数要么是T要么是Provider< T>，不能是Lazy< T>、Producer< T>或Produced< T>；
        //  - ④ keyType不能使用通配符；
        //  - ⑤ 如果keyType是MembersInjector< T>（必须存在T）类型，那么对T进行成员注入校验:a.T不能使用Qualifier注解修饰的注解修饰;b.T只能是类或接口，并且如果是泛型，那么泛型类型只能是类或接口或数组，数组只能是类或接口或数组，并且T不允许出现例如List类型（必须是List< T>类型）
        //  - ⑥ Inject修饰的变量节点不能是Produced< T>或Producer< T>类型；
        for (VariableElement element : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (MoreElements.isAnnotationPresent(element, Inject.class)) {
                hasInjectedMembers = true;
                ValidationReport report = validateField(element);
                if (!report.isClean()) {
                    builder.addSubreport(report);
                }
            }
        }

        //2. 对使用Inject修饰的普通方法校验：
        // - （1）Inject修饰的普通方法必须是实现类，不能是abstract修饰的抽象类或接口方法；
        // - （2）Inject修饰的普通方法不要使用private和static修饰（可能报错可能警告）；
        // - （3）Inject修饰的普通方法不能使用泛型类型,并且不能throws异常；
        // - （4）Inject修饰的普通方法的参数合参数类型做依赖校验：
        // - 注1：当前参数类型剥离RequestKind< T>类型得到T作为keyType（当然如果是RequestKind.INSTANCE，那么keyType就是参数类型）；
        // - 注2：如果参数使用了@Assisted修饰，不进行下面的依赖校验；
        //  - ① 当前参数如果使用了Qulifiers注解修饰的注解，那么该类型的注解最多只能使用1个；
        //  - ② 如果参数节点没有使用Qualifier修饰的注解修饰，那么keytype类型的构造函数不能使用AssistedInject修饰；
        //  - ③ 如果参数节点没有使用Qualifier修饰的注解修饰，并且keyType节点使用了@AssistedFactory修饰，那么当前参数要么是T要么是Provider< T>，不能是Lazy< T>、Producer< T>或Produced< T>；
        //  - ④ keyType不能使用通配符；
        //  - ⑤ 如果keyType是MembersInjector< T>（必须存在T）类型，那么对T进行成员注入校验:a.T不能使用Qualifier注解修饰的注解修饰;b.T只能是类或接口，并且如果是泛型，那么泛型类型只能是类或接口或数组，数组只能是类或接口或数组，并且T不允许出现例如List类型（必须是List< T>类型）
        //  - ⑥ Inject修饰的普通方法的参数类型不能是Produced< T>或Producer< T>类型；
        for (ExecutableElement element : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if (MoreElements.isAnnotationPresent(element, Inject.class)) {
                hasInjectedMembers = true;
                ValidationReport report = validateMethod(element);
                if (!report.isClean()) {
                    builder.addSubreport(report);
                }
            }
        }

        //3. Inject修饰的节点所在父节点最好不要被private修饰（可能警告可能报错）；并且Inject修饰的节点所在父节点不能是Kotlin Object或Kotlin Companion Object对象；
        if (hasInjectedMembers) {
            checkInjectIntoPrivateClass(typeElement, builder);
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

    public boolean isValidType(TypeMirror type) {
        if (!type.getKind().equals(DECLARED)) {
            return true;
        }
        return validateType(MoreTypes.asTypeElement(type)).isClean();
    }

    /**
     * Returns true if the given method element declares a checked exception.
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
