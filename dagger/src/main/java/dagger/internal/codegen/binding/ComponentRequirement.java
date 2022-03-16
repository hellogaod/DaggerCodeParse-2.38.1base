package dagger.internal.codegen.binding;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.common.base.Equivalence;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.spi.model.BindingKind;
import dagger.spi.model.Key;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.SourceFiles.simpleVariableName;
import static dagger.internal.codegen.langmodel.DaggerElements.isAnyAnnotationPresent;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A type that a component needs an instance of.
 */
@AutoValue
public abstract class ComponentRequirement {
    /**
     * The kind of the {@link ComponentRequirement}.
     */
    public enum Kind {
        /**
         * A type listed in the component's {@code dependencies} attribute.
         */
        DEPENDENCY,

        /**
         * A type listed in the component or subcomponent's {@code modules} attribute.
         */
        MODULE,

        /**
         * An object that is passed to a builder's {@link dagger.BindsInstance @BindsInstance} method.
         */
        BOUND_INSTANCE,
        ;

        public boolean isBoundInstance() {
            return equals(BOUND_INSTANCE);
        }

        public boolean isModule() {
            return equals(MODULE);
        }
    }

    /**
     * The kind of requirement.
     */
    public abstract Kind kind();

    /**
     * Returns true if this is a {@link Kind#BOUND_INSTANCE} requirement.
     */
    // TODO(ronshapiro): consider removing this and inlining the usages
    final boolean isBoundInstance() {
        return kind().isBoundInstance();
    }

    /**
     * The type of the instance the component must have, wrapped so that requirements can be used as
     * value types.
     */
    public abstract Equivalence.Wrapper<TypeMirror> wrappedType();

    /**
     * The type of the instance the component must have.
     */
    public TypeMirror type() {
        return wrappedType().get();
    }

    /**
     * The element associated with the type of this requirement.
     */
    public TypeElement typeElement() {
        return MoreTypes.asTypeElement(type());
    }

    /**
     * The action a component builder should take if it {@code null} is passed.
     */
    public enum NullPolicy {
        /**
         * Make a new instance.
         */
        NEW,
        /**
         * Throw an exception.
         */
        THROW,
        /**
         * Allow use of null values.
         */
        ALLOW,
    }

    /**
     * An override for the requirement's null policy. If set, this is used as the null policy instead
     * of the default behavior in {@link #nullPolicy}.
     *
     * <p>Some implementations' null policy can be determined upon construction (e.g., for binding
     * instances), but others' require Elements which must wait until {@link #nullPolicy} is called.
     */
    abstract Optional<NullPolicy> overrideNullPolicy();

    /**
     * The requirement's null policy.
     */
    public NullPolicy nullPolicy(DaggerElements elements, KotlinMetadataUtil metadataUtil) {

        //如果存在，返回，只有当前类型是BOUND_INSTANCE，并且传递过来的参数允许nullable（参数使用了Nullable修饰 ||  参数类型 ！= RequestKind.INSTANCE）情况下才会存在NullPolicy.ALLOW
        if (overrideNullPolicy().isPresent()) {
            return overrideNullPolicy().get();
        }
        switch (kind()) {
            case MODULE:
                //当前节点是否可被直接实例化：必须是一个实体（非abstract修饰）类，非接口，并且有非private的构造函数，并且如果是一个内部类必须是static修饰
                return componentCanMakeNewInstances(typeElement(), metadataUtil)
                        ? NullPolicy.NEW
                        //如果当前typeElement不能被直接实例化 && typeElement里面的所有bindingMethod方法都没有被abstract和static修饰
                        : requiresAPassedInstance(elements, metadataUtil) ? NullPolicy.THROW : NullPolicy.ALLOW;
            case DEPENDENCY:
            case BOUND_INSTANCE:
                return NullPolicy.THROW;
        }
        throw new AssertionError();
    }

    /**
     * Returns true if the passed {@link ComponentRequirement} requires a passed instance in order to
     * be used within a component.
     * <p>
     * 如果传递的 {@link ComponentRequirement} 需要传递的实例才能在组件中使用，则返回 true。
     * <p>
     * 如果类型不是Module，直接返回true；否则进行module是否可以实例化校验，
     * 只有在module类中所有方法不存在abstract和static修饰才表示当前module类可以实例化
     */
    public boolean requiresAPassedInstance(DaggerElements elements, KotlinMetadataUtil metadataUtil) {
        if (!kind().isModule()) {
            // Bound instances and dependencies always require the user to provide an instance.
            return true;
        }
        //如果typeElement是abstact修饰 && typeElement所有的bindingMethod绑定方法都没有使用abstract和static修饰  ，返回true
        return requiresModuleInstance(elements, metadataUtil)
                && !componentCanMakeNewInstances(typeElement(), metadataUtil);
    }

    /**
     * Returns {@code true} if an instance is needed for this (module) requirement.
     * <p>
     * 如果此（模块）要求需要实例，则返回 {@code true}。
     *
     * <p>An instance is only needed if there is a binding method on the module that is neither {@code
     * abstract} nor {@code static}; if all bindings are one of those, then there should be no
     * possible dependency on instance state in the module's bindings.
     *
     * <p>Alternatively, if the module is a Kotlin Object then the binding methods are considered
     * {@code static}, requiring no module instance.
     */
    private boolean requiresModuleInstance(DaggerElements elements, KotlinMetadataUtil metadataUtil) {
        boolean isKotlinObject =
                metadataUtil.isObjectClass(typeElement())
                        || metadataUtil.isCompanionObjectClass(typeElement());

        if (isKotlinObject) {//如果是一个kotlin object对象，返回false
            return false;
        }

        //当前type类型的节点上所有bindingMethod方法，如果都没有使用abstract和static修饰
        ImmutableSet<ExecutableElement> methods = elements.getLocalAndInheritedMethods(typeElement());
        return methods.stream()
                .filter(this::isBindingMethod)
                .map(ExecutableElement::getModifiers)
                .anyMatch(modifiers -> !modifiers.contains(ABSTRACT) && !modifiers.contains(STATIC));
    }

    //判断当前方法是否是绑定类型：
    private boolean isBindingMethod(ExecutableElement method) {
        // TODO(cgdecker): At the very least, we should have utility methods to consolidate this stuff
        // in one place; listing individual annotations all over the place is brittle.
        return isAnyAnnotationPresent(
                method,
                TypeNames.PROVIDES,
                TypeNames.PRODUCES,
                // TODO(ronshapiro): it would be cool to have internal meta-annotations that could describe
                // these, like @AbstractBindingMethod
                TypeNames.BINDS,
                TypeNames.MULTIBINDS,
                TypeNames.BINDS_OPTIONAL_OF);
    }

    /**
     * The key for this requirement, if one is available.
     */
    public abstract Optional<Key> key();

    /**
     * Returns the name for this requirement that could be used as a variable.
     */
    public abstract String variableName();

    /**
     * Returns a parameter spec for this requirement.
     */
    public ParameterSpec toParameterSpec() {
        return ParameterSpec.builder(TypeName.get(type()), variableName()).build();
    }

    //一个依赖生成的该对象：Component#dependencies、
    public static ComponentRequirement forDependency(TypeMirror type) {
        return new AutoValue_ComponentRequirement(
                Kind.DEPENDENCY,
                MoreTypes.equivalence().wrap(checkNotNull(type)),
                Optional.empty(),
                Optional.empty(),
                simpleVariableName(MoreTypes.asTypeElement(type)));
    }

    //一个module类生成一个ComponentRequrement对象，Component#modules里面的module类生成的对象
    public static ComponentRequirement forModule(TypeMirror type) {
        return new AutoValue_ComponentRequirement(
                Kind.MODULE,
                MoreTypes.equivalence().wrap(checkNotNull(type)),
                Optional.empty(),
                Optional.empty(),
                simpleVariableName(MoreTypes.asTypeElement(type)));
    }

    //当前绑定使用的是@BindsInstance注解
    static ComponentRequirement forBoundInstance(Key key, boolean nullable, String variableName) {
        return new AutoValue_ComponentRequirement(
                Kind.BOUND_INSTANCE,
                MoreTypes.equivalence().wrap(key.type().java()),
                nullable ? Optional.of(NullPolicy.ALLOW) : Optional.empty(),
                Optional.of(key),
                variableName);
    }

    //当前绑定使用的是@BindsInstance注解
    public static ComponentRequirement forBoundInstance(ContributionBinding binding) {
        checkArgument(binding.kind().equals(BindingKind.BOUND_INSTANCE));
        return forBoundInstance(
                binding.key(),
                binding.nullableType().isPresent(),
                binding.bindingElement().get().getSimpleName().toString());
    }

    /**
     * Returns true if and only if a component can instantiate new instances (typically of a module)
     * rather than requiring that they be passed.
     * <p>
     * 当前typeElement节点是否可以被直接实例化条件：必须是非abstract类，如果是内部类那么必须是static修饰，并且该类有一个非private修的构造函数
     */
    // TODO(bcorso): Should this method throw if its called knowing that an instance is not needed?
    public static boolean componentCanMakeNewInstances(
            TypeElement typeElement,
            KotlinMetadataUtil metadataUtil
    ) {
        switch (typeElement.getKind()) {//只有是类的情况下才会继续下面的代码判断
            case CLASS:
                break;
            case ENUM:
            case ANNOTATION_TYPE:
            case INTERFACE:
                return false;
            default:
                throw new AssertionError("TypeElement cannot have kind: " + typeElement.getKind());
        }

        if (typeElement.getModifiers().contains(ABSTRACT)) {
            return false;
        }

        if (requiresEnclosingInstance(typeElement)) {
            return false;
        }

        if (metadataUtil.isObjectClass(typeElement)
                || metadataUtil.isCompanionObjectClass(typeElement)) {
            return false;
        }

        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed.getKind().equals(CONSTRUCTOR)
                    && MoreElements.asExecutable(enclosed).getParameters().isEmpty()
                    && !enclosed.getModifiers().contains(PRIVATE)) {
                return true;
            }
        }

        // TODO(gak): still need checks for visibility

        return false;
    }

    //是否能获取到当前typeElement的父级实例
    private static boolean requiresEnclosingInstance(TypeElement typeElement) {
        switch (typeElement.getNestingKind()) {
            case TOP_LEVEL://本身就是最上级
                return false;
            case MEMBER://成员：判断当前typeElement是否使用Static就是，如果是返回false；否则返回true
                return !typeElement.getModifiers().contains(STATIC);
            case ANONYMOUS:
            case LOCAL:
                return true;
        }
        throw new AssertionError(
                "TypeElement cannot have nesting kind: " + typeElement.getNestingKind());
    }
}
