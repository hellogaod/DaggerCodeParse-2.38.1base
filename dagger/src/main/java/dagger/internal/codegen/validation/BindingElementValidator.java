package dagger.internal.codegen.validation;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.FormatMethod;
import com.squareup.javapoet.ClassName;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Qualifier;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.base.ContributionType;
import dagger.internal.codegen.base.FrameworkTypes;
import dagger.internal.codegen.base.MultibindingAnnotations;
import dagger.internal.codegen.base.SetType;
import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.binding.MapKeys;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.spi.model.Key;
import dagger.spi.model.Scope;

import static com.google.auto.common.MoreTypes.asTypeElement;
import static com.google.common.base.Verify.verifyNotNull;
import static dagger.internal.codegen.base.Scopes.scopesOf;
import static dagger.internal.codegen.base.Util.reentrantComputeIfAbsent;
import static dagger.internal.codegen.binding.AssistedInjectionAnnotations.isAssistedFactoryType;
import static dagger.internal.codegen.binding.AssistedInjectionAnnotations.isAssistedInjectionType;
import static dagger.internal.codegen.langmodel.DaggerElements.getAnnotationMirror;
import static javax.lang.model.type.TypeKind.ARRAY;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.TYPEVAR;
import static javax.lang.model.type.TypeKind.VOID;

/**
 * A validator for elements that represent binding declarations.
 */
public abstract class BindingElementValidator<E extends Element> {

    private final ClassName bindingAnnotation;//绑定使用的注解
    private final AllowsMultibindings allowsMultibindings;//允许多重绑定（@IntoSet，@IntoMap，@ElementSet修饰）
    private final AllowsScoping allowsScoping;//是否允许使用Scope注解修饰的注解修饰
    private final Map<E, ValidationReport> cache = new HashMap<>();
    private final InjectionAnnotations injectionAnnotations;

    /**
     * Creates a validator object.
     *
     * @param bindingAnnotation the annotation on an element that identifies it as a binding element
     */
    protected BindingElementValidator(
            ClassName bindingAnnotation,
            AllowsMultibindings allowsMultibindings,
            AllowsScoping allowsScoping,
            InjectionAnnotations injectionAnnotations) {
        this.bindingAnnotation = bindingAnnotation;
        this.allowsMultibindings = allowsMultibindings;
        this.allowsScoping = allowsScoping;
        this.injectionAnnotations = injectionAnnotations;
    }

    /**
     * Returns a {@link ValidationReport} for {@code element}.
     * <p>
     * 节点校验入口
     */
    final ValidationReport validate(E element) {
        return reentrantComputeIfAbsent(cache, element, this::validateUncached);
    }

    private ValidationReport validateUncached(E element) {
        return elementValidator(element).validate();
    }

    /**
     * Returns an error message of the form "&lt;{@link #bindingElements()}&gt; <i>rule</i>", where
     * <i>rule</i> comes from calling {@link String#format(String, Object...)} on {@code ruleFormat}
     * and the other arguments.
     */
    @FormatMethod
    protected final String bindingElements(String ruleFormat, Object... args) {
        return new Formatter().format("%s ", bindingElements()).format(ruleFormat, args).toString();
    }

    /**
     * The kind of elements that this validator validates. Should be plural. Used for error reporting.
     */
    protected abstract String bindingElements();

    /**
     * The verb describing the {@link ElementValidator#bindingElementType()} in error messages.
     */
    // TODO(ronshapiro,dpb): improve the name of this method and it's documentation.
    protected abstract String bindingElementTypeVerb();

    /**
     * The error message when a binding element has a bad type.
     */
    protected String badTypeMessage() {
        return bindingElements(
                "must %s a primitive, an array, a type variable, or a declared type",
                bindingElementTypeVerb());
    }

    /**
     * The error message when a the type for a binding element with {@link
     * dagger.multibindings.ElementsIntoSet @ElementsIntoSet} or {@code SET_VALUES} is a not set type.
     */
    protected String elementsIntoSetNotASetMessage() {
        return bindingElements(
                "annotated with @ElementsIntoSet must %s a Set", bindingElementTypeVerb());
    }

    /**
     * The error message when a the type for a binding element with {@link
     * dagger.multibindings.ElementsIntoSet @ElementsIntoSet} or {@code SET_VALUES} is a raw set.
     */
    protected String elementsIntoSetRawSetMessage() {
        return bindingElements(
                "annotated with @ElementsIntoSet cannot %s a raw Set", bindingElementTypeVerb());
    }

    /*** Returns an {@link ElementValidator} for validating the given {@code element}. */
    protected abstract ElementValidator elementValidator(E element);


    /**
     * Validator for a single binding element.
     * <p>
     * 单个绑定元素的校验器
     */
    protected abstract class ElementValidator {
        protected final E element;//当前节点
        protected final ValidationReport.Builder report;
        private final ImmutableCollection<? extends AnnotationMirror> qualifiers;//获取Element上的所有使用Qualifier注解修饰的注解集

        protected ElementValidator(E element) {
            this.element = element;
            this.report = ValidationReport.about(element);
            qualifiers = injectionAnnotations.getQualifiers(element);
        }

        /**
         * Checks the element for validity.
         * <p>
         * 真正校验的入口：核心代码
         */
        private ValidationReport validate() {

            //判断当前element节点，是否使用了IntoMap，IntoSet或ElementsIntoSet注解，并对不同情况进行判断
            checkType();

            //Element上的使用Qualifier注解修饰的注解最多只能有一个，否则报错
            checkQualifiers();

            checkMapKeys();//校验element上MapKey注解修饰的注解逻辑判断

            checkMultibindings();//element多重绑定的校验

            checkScopes();//element使用Scope注解修饰的注解情况

            checkAdditionalProperties();//子类中可能会实现

            return report.build();
        }

        /**
         * Check any additional properties of the element. Does nothing by default.
         */
        protected void checkAdditionalProperties() {
        }

        /**
         * The type declared by this binding element. This may differ from a binding's {@link
         * Key#type()}, for example in multibindings. An {@link Optional#empty()} return value indicates
         * that the contributed type is ambiguous or missing, i.e. a {@code @BindsInstance} method with
         * zero or many parameters.
         */
        // TODO(dpb): should this be an ImmutableList<TypeMirror>, with this class checking the size?
        protected abstract Optional<TypeMirror> bindingElementType();

        /**
         * Adds an error if the {@link #bindingElementType() binding element type} is not appropriate.
         *
         * <p>Adds an error if the type is not a primitive, array, declared type, or type variable.
         *
         * <p>If the binding is not a multibinding contribution, adds an error if the type is a
         * framework type.
         *
         * <p>If the element has {@link dagger.multibindings.ElementsIntoSet @ElementsIntoSet} or {@code
         * SET_VALUES}, adds an error if the type is not a {@code Set<T>} for some {@code T}
         * <p>
         * 逻辑总结：check element类型，对不同类型进行不同校验
         * <p>
         * 1.如果element是UNIQUE类型
         * ------①不允许使用FrameworkType中的类型，FrameworkType：Produced.class, Producer.class，Provider.class, Lazy.class, MembersInjector.class
         * ------②当前节点没有使用Qulifier修饰，并且bindingElementType()存在而且还是一个类或接口情况下，不允许使用AssistdInject或AssistedFactory修饰
         * ------会继续校验下面的2（因为case UNIQUE 没有break）
         * 2.如果是SET或MAP类型；
         * ------bindingElementType()类型只能是原始类型或数组或类型变量或类或接口
         * ------不会往下校验了；
         * 3.如果是SET_VALUES类型
         * ------bindingElementType()节点必须是Set<T>格式,并且T只能是原始类型或数组或类型变量或类或接口
         */
        protected void checkType() {
            //判断当前element节点，是否使用了IntoMap，IntoSet或ElementsIntoSet注解
            switch (ContributionType.fromBindingElement(element)) {
                case UNIQUE:
                    // Validate that a unique binding is not attempting to bind a framework type. This
                    // validation is only appropriate for unique bindings because multibindings may collect
                    // framework types.  E.g. Set<Provider<Foo>> is perfectly reasonable.

                    //如果element节点没有使用IntoMap，IntoSet或ElementsIntoSet注解中的任何注解，那么不允许使用FrameworkType中的类型
                    checkFrameworkType();

                    // Validate that a unique binding is not attempting to bind an unqualified assisted type.
                    // This validation is only appropriate for unique bindings because multibindings may
                    // collect assisted types.

                    //当前节点没有使用Qulifier修饰，并且bindingElementType()存在而且还是一个类或接口情况下，不允许使用AssistdInject或AssistedFactory修饰
                    checkAssistedType();
                    // fall through

                case SET:
                case MAP:
                    //bindingElementType()类型只能是原始类型或数组或类型变量或类或接口
                    bindingElementType().ifPresent(type -> checkKeyType(type));
                    break;

                case SET_VALUES:
                    //bindingElementType()节点必须是Set<T>格式,并且T只能是原始类型或数组或类型变量或类或接口
                    checkSetValuesType();
            }
        }

        /**
         * Adds an error if {@code keyType} is not a primitive, declared type, array, or type variable.
         * <p>
         * keyType只能是原始类型或数组或类型变量或类或接口
         */
        protected void checkKeyType(TypeMirror keyType) {
            TypeKind kind = keyType.getKind();
            if (kind.equals(VOID)) {
                report.addError(bindingElements("must %s a value (not void)", bindingElementTypeVerb()));
            } else if (!(kind.isPrimitive()
                    || kind.equals(DECLARED)
                    || kind.equals(ARRAY)
                    || kind.equals(TYPEVAR))) {
                report.addError(badTypeMessage());
            }
        }

        /**
         * Adds errors for unqualified assisted types.
         */
        private void checkAssistedType() {
            //如果当前节点没有使用Qulifier修饰的注解修饰，并且bindingElementType()存在而且还是一个类或接口
            if (qualifiers.isEmpty()
                    && bindingElementType().isPresent()
                    && bindingElementType().get().getKind() == DECLARED) {

                TypeElement keyElement = asTypeElement(bindingElementType().get());

                //bindingElementType()节点不允许使用AssistdInject修饰
                if (isAssistedInjectionType(keyElement)) {
                    report.addError("Dagger does not support providing @AssistedInject types.", keyElement);
                }
                //bindingElementType()节点不允许使用AssistedFactory注解修饰
                if (isAssistedFactoryType(keyElement)) {
                    report.addError("Dagger does not support providing @AssistedFactory types.", keyElement);
                }

            }
        }

        /**
         * Adds an error if the type for an element with {@link
         * dagger.multibindings.ElementsIntoSet @ElementsIntoSet} or {@code SET_VALUES} is not a a
         * {@code Set<T>} for a reasonable {@code T}.
         */
        // TODO(gak): should we allow "covariant return" for set values?
        protected void checkSetValuesType() {
            bindingElementType().ifPresent(keyType -> checkSetValuesType(keyType));
        }

        /**
         * Adds an error if {@code type} is not a {@code Set<T>} for a reasonable {@code T}.
         * <p>
         * bindingElementType()节点必须是Set<T>格式,并且T只能是原始类型或数组或类型变量或类或接口
         */
        protected final void checkSetValuesType(TypeMirror type) {
            if (!SetType.isSet(type)) {
                report.addError(elementsIntoSetNotASetMessage());
            } else {
                SetType setType = SetType.from(type);
                if (setType.isRawType()) {
                    report.addError(elementsIntoSetRawSetMessage());
                } else {
                    checkKeyType(setType.elementType());
                }
            }
        }

        /**
         * Adds an error if the element has more than one {@linkplain Qualifier qualifier} annotation.
         */
        private void checkQualifiers() {
            if (qualifiers.size() > 1) {
                for (AnnotationMirror qualifier : qualifiers) {
                    report.addError(
                            bindingElements("may not use more than one @Qualifier"),
                            element,
                            qualifier);
                }
            }
        }

        /**
         * Adds an error if an {@link dagger.multibindings.IntoMap @IntoMap} element doesn't have
         * exactly one {@link dagger.MapKey @MapKey} annotation, or if an element that is {@link
         * dagger.multibindings.IntoMap @IntoMap} has any.
         * <p>
         * 1.如果当前不允许多重绑定，则不需要判断MapKey修饰情况，即不需要继续下面的判断；否则继续往下判断；
         * 2.@IntoMap和@MapKey注解修饰的注解一定是同时存在于绑定节点上的，并且@MapKey修饰的注解有且仅有一个
         */
        private void checkMapKeys() {
            //不允许多重绑定，不需要往下继续校验
            if (!allowsMultibindings.allowsMultibindings()) {
                return;
            }

            //element节点上的被MapKey修饰的注解集
            ImmutableSet<? extends AnnotationMirror> mapKeys = MapKeys.getMapKeys(element);

            //element使用了@IntoMap注解修饰了
            if (ContributionType.fromBindingElement(element).equals(ContributionType.MAP)) {
                switch (mapKeys.size()) {
                    case 0:
                        report.addError(bindingElements("of type map must declare a map key"));
                        break;
                    case 1:
                        break;
                    default:
                        report.addError(bindingElements("may not have more than one map key"));
                        break;
                }
            } else if (!mapKeys.isEmpty()) {
                report.addError(bindingElements("of non map type cannot declare a map key"));
            }
        }

        /**
         * Adds errors if:
         *
         * <ul>
         *   <li>the element doesn't allow {@linkplain MultibindingAnnotations multibinding annotations}
         *       and has any
         *   <li>the element does allow them but has more than one
         *   <li>the element has a multibinding annotation and its {@link dagger.Provides} or {@link
         *       dagger.producers.Produces} annotation has a {@code type} parameter.
         * </ul>
         * <p>
         * element节点上多重绑定校验逻辑整理：
         * <p>
         * 1.如果当前不允许使用多重绑定，那么IntoMap，IntoSet，ElementsIntoSet注解表示的多重绑定肯定是不允许使用的；
         * 2.如果当前允许多重绑定，那么多重绑定IntoMap，IntoSet，ElementsIntoSet注解最多element节点上只被允许使用一个
         * 3.如果element使用了Provides注解,并且也使用了多重绑定，那么@Provides.type不能被当前element使用
         */
        private void checkMultibindings() {

            //element上所有多重绑定注解（IntoMap，IntoSet，ElementsIntoSet）
            ImmutableSet<AnnotationMirror> multibindingAnnotations =
                    MultibindingAnnotations.forElement(element);

            switch (allowsMultibindings) {
                case NO_MULTIBINDINGS://如果不允许使用多重绑定，那么不应该出现多重绑定注解，否则报错
                    for (AnnotationMirror annotation : multibindingAnnotations) {
                        report.addError(
                                bindingElements("cannot have multibinding annotations"),
                                element,
                                annotation);
                    }
                    break;

                case ALLOWS_MULTIBINDINGS://如果允许多重绑定，那么最多只能有一个多重绑定类型
                    if (multibindingAnnotations.size() > 1) {
                        for (AnnotationMirror annotation : multibindingAnnotations) {
                            report.addError(
                                    bindingElements("cannot have more than one multibinding annotation"),
                                    element,
                                    annotation);
                        }
                    }
                    break;
            }

            // TODO(ronshapiro): move this into ProvidesMethodValidator
            if (bindingAnnotation.equals(TypeNames.PROVIDES)) {
                //如果element使用了Provides注解，获取该注解
                AnnotationMirror bindingAnnotationMirror =
                        getAnnotationMirror(element, bindingAnnotation).get();

                boolean usesProvidesType = false;
                for (ExecutableElement member : bindingAnnotationMirror.getElementValues().keySet()) {
                    usesProvidesType |= member.getSimpleName().contentEquals("type");
                }

                //@Provides.type不能再多重绑定中使用
                if (usesProvidesType && !multibindingAnnotations.isEmpty()) {
                    report.addError(
                            "@Provides.type cannot be used with multibinding annotations", element);
                }
            }
        }

        /**
         * Adds an error if the element has a scope but doesn't allow scoping, or if it has more than
         * one {@linkplain Scope scope} annotation.
         * <p>
         * element节点上 所有使用Scope注解修饰的注解
         * 1.如果允许使用，那么最多只能存在一个；
         * 2.如果不允许使用，则不适用，否则报错。
         */
        private void checkScopes() {
            //element节点上 所有使用Scope注解修饰的注解 转换成Scope对象
            ImmutableSet<Scope> scopes = scopesOf(element);

            String error = null;
            switch (allowsScoping) {
                case ALLOWS_SCOPING://如果当前允许scope注解修饰的注解的使用，那么最多只能使用一个
                    if (scopes.size() <= 1) {
                        return;
                    }
                    error = bindingElements("cannot use more than one @Scope");
                    break;
                case NO_SCOPING://表示不允许使用Scope注解修饰的注解
                    error = bindingElements("cannot be scoped");
                    break;
            }
            verifyNotNull(error);
            for (Scope scope : scopes) {
                report.addError(error, element, scope.scopeAnnotation().java());
            }
        }

        /**
         * Adds an error if the {@link #bindingElementType() type} is a {@linkplain FrameworkTypes
         * framework type}.
         * <p>
         * bindingElementType()不应该存在FrameworkType类型（例如Provider<T>,或Producer<T>）,否则报错
         */
        private void checkFrameworkType() {
            if (bindingElementType().filter(FrameworkTypes::isFrameworkType).isPresent()) {
                report.addError(bindingElements("must not %s framework types", bindingElementTypeVerb()));
            }
        }

    }

    /**
     * Whether to check multibinding annotations.
     * <p>
     * 是否允许多重绑定，使用IntoSet，IntoMap，ElementSet修饰
     */
    enum AllowsMultibindings {
        /**
         * This element disallows multibinding annotations, so don't bother checking for their validity.
         * {@link MultibindingAnnotationsProcessingStep} will add errors if the element has any
         * multibinding annotations.
         */
        NO_MULTIBINDINGS,

        /**
         * This element allows multibinding annotations, so validate them.
         */
        ALLOWS_MULTIBINDINGS,
        ;

        private boolean allowsMultibindings() {
            return this == ALLOWS_MULTIBINDINGS;
        }
    }

    /**
     * How to check scoping annotations.
     * <p>
     * 是否允许使用Scope注解修饰的注解修饰
     */
    enum AllowsScoping {
        /**
         * This element disallows scoping, so check that no scope annotations are present.
         */
        NO_SCOPING,

        /**
         * This element allows scoping, so validate that there's at most one scope annotation.
         */
        ALLOWS_SCOPING,
        ;
    }
}
