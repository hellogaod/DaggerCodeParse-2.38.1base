package dagger.internal.codegen.validation;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.FormatMethod;
import com.squareup.javapoet.ClassName;

import java.util.Optional;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.auto.common.MoreElements.asType;
import static dagger.internal.codegen.langmodel.DaggerElements.isAnyAnnotationPresent;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;


/**
 * A validator for methods that represent binding declarations.
 * <p>
 * 绑定的方法校验
 */
abstract class BindingMethodValidator extends BindingElementValidator<ExecutableElement> {


    private final DaggerElements elements;
    private final DaggerTypes types;
    private final KotlinMetadataUtil metadataUtil;
    private final DependencyRequestValidator dependencyRequestValidator;
    private final ClassName methodAnnotation;//绑定使用的注解
    private final ImmutableSet<ClassName> enclosingElementAnnotations;//绑定方法所在父类使用的注解
    private final Abstractness abstractness;//方法抽象性：1.修饰必须具体类；2.必须abstract修饰
    private final ExceptionSuperclass exceptionSuperclass;//方法上异常抛出类型判断

    /**
     * Creates a validator object.
     *
     * @param methodAnnotation           the annotation on a method that identifies it as a binding method
     * @param enclosingElementAnnotation the method must be declared in a class or interface annotated
     *                                   with this annotation
     */
    protected BindingMethodValidator(
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil metadataUtil,
            DependencyRequestValidator dependencyRequestValidator,
            ClassName methodAnnotation,
            ClassName enclosingElementAnnotation,
            Abstractness abstractness,
            ExceptionSuperclass exceptionSuperclass,
            AllowsMultibindings allowsMultibindings,
            AllowsScoping allowsScoping,
            InjectionAnnotations injectionAnnotations) {

        this(
                elements,
                types,
                metadataUtil,
                methodAnnotation,
                ImmutableSet.of(enclosingElementAnnotation),
                dependencyRequestValidator,
                abstractness,
                exceptionSuperclass,
                allowsMultibindings,
                allowsScoping,
                injectionAnnotations);
    }

    /**
     * Creates a validator object.
     *
     * @param methodAnnotation            the annotation on a method that identifies it as a binding method
     * @param enclosingElementAnnotations the method must be declared in a class or interface
     *                                    annotated with one of these annotations
     */
    protected BindingMethodValidator(
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil metadataUtil,
            ClassName methodAnnotation,
            Iterable<ClassName> enclosingElementAnnotations,
            DependencyRequestValidator dependencyRequestValidator,
            Abstractness abstractness,
            ExceptionSuperclass exceptionSuperclass,
            AllowsMultibindings allowsMultibindings,
            AllowsScoping allowsScoping,
            InjectionAnnotations injectionAnnotations) {
        super(methodAnnotation, allowsMultibindings, allowsScoping, injectionAnnotations);
        this.elements = elements;
        this.types = types;
        this.metadataUtil = metadataUtil;
        this.methodAnnotation = methodAnnotation;
        this.enclosingElementAnnotations = ImmutableSet.copyOf(enclosingElementAnnotations);
        this.dependencyRequestValidator = dependencyRequestValidator;
        this.abstractness = abstractness;
        this.exceptionSuperclass = exceptionSuperclass;
    }

    /**
     * The annotation that identifies binding methods validated by this object.
     */
    final ClassName methodAnnotation() {
        return methodAnnotation;
    }

    /**
     * Returns an error message of the form "@<i>annotation</i> methods <i>rule</i>", where
     * <i>rule</i> comes from calling {@link String#format(String, Object...)} on {@code ruleFormat}
     * and the other arguments.
     */
    @FormatMethod
    protected final String bindingMethods(String ruleFormat, Object... args) {
        return bindingElements(ruleFormat, args);
    }

    @Override
    protected final String bindingElements() {
        return String.format("@%s methods", methodAnnotation.simpleName());
    }

    @Override
    protected final String bindingElementTypeVerb() {
        return "return";
    }


    /**
     * Abstract validator for individual binding method elements.
     */
    protected abstract class MethodValidator extends ElementValidator {
        protected MethodValidator(ExecutableElement element) {
            super(element);
        }

        @Override
        protected final Optional<TypeMirror> bindingElementType() {
            return Optional.of(element.getReturnType());
        }

        @Override
        protected final void checkAdditionalProperties() {

            //5. bindingMethod方法如果是@Produces修饰，那么其所在modue节点只能使用ProducerModule注解，如果是@Provides、@Binds、@BindsOptionalOf、@Multibinds修饰的bindingMethod方法，那么所在的父级module节点可以使用Module或ProducerModule注解；
            // - 如果bindingMethod方法所在父级节点是Kotlin Component Object类型，那么对该父级节点的父级节点作为module节点校验；
            checkEnclosingElement();

            //6. bindingMethod方法不允许使用泛型,bindingMethod方法不允许使用private修饰；
            checkTypeParameters();
            checkNotPrivate();

            //7. bindingMethod方法如果是@Provides或@Produces修饰，那么当前方法必须使用实现方法；如果方法使用@Binds、@BindsOptionalOf或@Multibinds修饰，那么必须是抽象方法（abstract修饰或接口非default修饰）；
            checkAbstractness();

            //8. bindingMethod方法如果使用@Binds、@BindsOptionalOf或@Multibinds修饰，那么该方法不允许throws异常；
            // 如果方法使用@Provides修饰允许throws RuntimeException或Error及其子类型的异常；
            // 如果方法使用@Produces修饰允许throws Exception或Error及其子类型的异常;
            checkThrows();

            //9. 对bindingMethod方法参数节点和参数类型节点做依赖校验,针对参数类型剥离RequestKind<T>获得T作为keyType（如果是RequestKind.INSTANCE，keyType就是参数类型）,：
            // - 注1：①BindsOptionalOf或@Multibindings修饰的bindingMethod方法是没有参数的，所以不会进行下面的校验；②@Binds修饰的bindingMethod方法有且仅有一个参数继续下面的校验；
            // - 注2：如果是@Provides修饰的bindingMethod方法，那么该方法参数不能是Produced<T>或Producer<T>并且继续下面的校验；
            // - 注释3：如果是@Binds修饰的bindingMethod方法，①如果还是用了@ElementsIntoSet修饰，那么当前方法返回类型必须是Set<T>,而且T必须是当前方法返回类型或其子类；②当前方法参数必须是返回类型的子类，继续下面的校验；
            // - (1) 如果方法参数使用@Assisted修饰，那么不继续往下校验；
            // - (2) 当前方法参数节点如果使用了Qualifier修饰的注解修饰，那么最多只能存在一个；
            // - (3) 如果当前方法参数节点没有使用Qualifier注解修饰，那么当前参数节点的构造函数不允许使用@AssistedInject修饰；
            // - (4) 如果当前方法参数节点没有使用Qualifier注解修饰，并且keyType节点使用了@AssistedFactory修饰，那么参数类型要么是T要么是Provider<T>,而不能使用Lazy<T>、Producer<T>或Produced<T>；
            // - (5) keyType不允许是通配符格式
            // - (6) 如果keyType其实是MembersInjector<T>(必须存在T)类型对T做成员注入校验：
            //  - ① T节点不能使用Qualifier注解修饰的注解修饰；
            //  - ② T类型只能是类或接口，并且如果是泛型，泛型类型只能是类或接口或数组，数组又只能是类或接口或原始类型或数组，不允许出现例如T是List而不是List<T>的情况；
            checkParameters();

            //10. @Produces修饰的bindingMethod方法，如果出现@Nullable注解，则警告
            checkAdditionalMethodProperties();//附加
        }

        /**
         * Checks additional properties of the binding method.
         */
        protected void checkAdditionalMethodProperties() {
        }

        /**
         * Adds an error if the method is not declared in a class or interface annotated with one of the
         * {@link #enclosingElementAnnotations}.
         * <p>
         * 如果绑定方法所在父类，没有使用enclosingElementAnnotations中的注解，那么报错
         */
        private void checkEnclosingElement() {

            TypeElement enclosingElement = asType(element.getEnclosingElement());

            //如果是Kotlin文件并且是Component Object类型，获取其所在父类
            if (metadataUtil.isCompanionObjectClass(enclosingElement)) {
                // Binding method is in companion object, use companion object's enclosing class instead.
                enclosingElement = asType(enclosingElement.getEnclosingElement());
            }

            //如果绑定方法所在父类，没有使用enclosingElementAnnotations中的注解，那么报错
            if (!isAnyAnnotationPresent(enclosingElement, enclosingElementAnnotations)) {
                report.addError(
                        bindingMethods(
                                "can only be present within a @%s",
                                enclosingElementAnnotations.stream()
                                        .map(ClassName::simpleName)
                                        .collect(joining(" or @"))));
            }
        }


        /**
         * Adds an error if the method is generic.
         */
        private void checkTypeParameters() {
            //方法不允许使用泛型
            if (!element.getTypeParameters().isEmpty()) {
                report.addError(bindingMethods("may not have type parameters"));
            }
        }

        /**
         * Adds an error if the method is private.
         * <p>
         * 绑定方法不允许被private修饰
         */
        private void checkNotPrivate() {
            if (element.getModifiers().contains(PRIVATE)) {
                report.addError(bindingMethods("cannot be private"));
            }
        }

        /**
         * Adds an error if the method is abstract but must not be, or is not and must be.
         */
        private void checkAbstractness() {
            boolean isAbstract = element.getModifiers().contains(ABSTRACT);
            //如果必须使用Abstract，那么必须使用；如果必须具体方法，那么绝对不能使用abstract抽象方法，否则报错
            switch (abstractness) {
                case MUST_BE_ABSTRACT:
                    if (!isAbstract) {
                        report.addError(bindingMethods("must be abstract"));
                    }
                    break;

                case MUST_BE_CONCRETE:
                    if (isAbstract) {
                        report.addError(bindingMethods("cannot be abstract"));
                    }
            }
        }

        /**
         * Adds an error if the method declares throws anything but an {@link Error} or an appropriate
         * subtype of {@link Exception}.
         */
        private void checkThrows() {
            //绑定方法是否允许继承异常，允许继承哪些异常，并且校验方法是否遵循该规则
            exceptionSuperclass.checkThrows(BindingMethodValidator.this, element, report);
        }


        /**
         * Adds errors for the method parameters.
         */
        protected void checkParameters() {
            for (VariableElement parameter : element.getParameters()) {
                checkParameter(parameter);
            }
        }

        /**
         * Adds errors for a method parameter. This implementation reports an error if the parameter has
         * more than one qualifier.
         * <p>
         * 校验绑定方法参数依赖
         */
        protected void checkParameter(VariableElement parameter) {
            dependencyRequestValidator.validateDependencyRequest(report, parameter, parameter.asType());
        }
    }

    /**
     * An abstract/concrete restriction on methods.
     */
    protected enum Abstractness {
        MUST_BE_ABSTRACT,//必须abstract修饰该绑定方法
        MUST_BE_CONCRETE//必须是具体实现类
    }


    /**
     * The exception class that all {@code throws}-declared throwables must extend, other than {@link
     * Error}.
     * <p>
     * 方法上异常抛出类型判断
     */
    protected enum ExceptionSuperclass {
        /**
         * Methods may not declare any throwable types.
         * <p>
         * 方法上不允许使用异常抛出，否则报错
         */
        NO_EXCEPTIONS {
            @Override
            protected String errorMessage(BindingMethodValidator validator) {
                return validator.bindingMethods("may not throw");
            }

            @Override
            protected void checkThrows(
                    BindingMethodValidator validator,
                    ExecutableElement element,
                    ValidationReport.Builder report) {

                if (!element.getThrownTypes().isEmpty()) {
                    report.addError(validator.bindingMethods("may not throw"));
                    return;
                }
            }
        },

        /**
         * Methods may throw checked or unchecked exceptions or errors.
         * <p>
         * 方法上只支持使用Exception和Error两种异常抛出
         */
        EXCEPTION(Exception.class) {
            @Override
            protected String errorMessage(BindingMethodValidator validator) {
                return validator.bindingMethods(
                        "may only throw unchecked exceptions or exceptions subclassing Exception");
            }
        },

        /**
         * Methods may throw unchecked exceptions or errors.
         * <p>
         * 方法上只支持使用RuntimeException和Error两种异常抛出
         */
        RUNTIME_EXCEPTION(RuntimeException.class) {
            @Override
            protected String errorMessage(BindingMethodValidator validator) {
                return validator.bindingMethods("may only throw unchecked exceptions");
            }
        },
        ;

        private final Class<? extends Exception> superclass;

        ExceptionSuperclass() {
            this(null);
        }

        ExceptionSuperclass(Class<? extends Exception> superclass) {
            this.superclass = superclass;
        }

        /**
         * Adds an error if the method declares throws anything but an {@link Error} or an appropriate
         * subtype of {@link Exception}.
         * <p>
         * 如果element方法有异常抛出类，判断是否继承Error或superclass。如果都没有，则报错。
         *
         * <p>This method is overridden in {@link #NO_EXCEPTIONS}.
         */
        protected void checkThrows(
                BindingMethodValidator validator,
                ExecutableElement element,
                ValidationReport.Builder report) {
            //superclass转换TypeMirror类型
            TypeMirror exceptionSupertype = validator.elements.getTypeElement(superclass).asType();
            TypeMirror errorType = validator.elements.getTypeElement(Error.class).asType();

            //element方法上所有的异常类型
            for (TypeMirror thrownType : element.getThrownTypes()) {
                //如果异常类型thrownType 既 没有继承 exceptionSupertype 也没继承Error，则report收集报错信息
                if (!validator.types.isSubtype(thrownType, exceptionSupertype)
                        && !validator.types.isSubtype(thrownType, errorType)) {
                    report.addError(errorMessage(validator));
                    break;
                }
            }
        }

        protected abstract String errorMessage(BindingMethodValidator validator);
    }
}
