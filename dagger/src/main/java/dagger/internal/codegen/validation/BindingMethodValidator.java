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

            checkEnclosingElement();//校验绑定方法所在类

            checkTypeParameters();//校验方法是否使用了泛型

            checkNotPrivate();//校验方法不允许使用private修饰

            checkAbstractness();//校验方法是否允许abstract修饰

            checkThrows();//校验方法继承异常情况

            checkParameters();//校验方法参数依赖，对参数依赖校验

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
