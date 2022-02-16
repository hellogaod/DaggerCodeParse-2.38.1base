package dagger.internal.codegen.binding;

import com.google.auto.common.MoreElements;
import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.base.ContributionType;
import dagger.internal.codegen.base.MapType;
import dagger.internal.codegen.base.SetType;
import dagger.spi.model.BindingKind;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.Key;

import static dagger.internal.codegen.base.MoreAnnotationMirrors.unwrapOptionalEquivalence;
import static dagger.internal.codegen.binding.ContributionBinding.FactoryCreationStrategy.CLASS_CONSTRUCTOR;
import static dagger.internal.codegen.binding.ContributionBinding.FactoryCreationStrategy.DELEGATE;
import static dagger.internal.codegen.binding.ContributionBinding.FactoryCreationStrategy.SINGLETON_INSTANCE;
import static java.util.Arrays.asList;

/**
 * An abstract class for a value object representing the mechanism by which a {@link Key} can be
 * contributed to a dependency graph.
 * <p>
 * 值对象的抽象类，表示 {@link Key} 可以用于依赖图的机制。
 */
public abstract class ContributionBinding extends Binding implements ContributionType.HasContributionType {

    /**
     * Returns the type that specifies this' nullability, absent if not nullable.
     */
    public abstract Optional<DeclaredType> nullableType();

    //使用的MapKey注解修饰的注解
    public abstract Optional<Equivalence.Wrapper<AnnotationMirror>> wrappedMapKeyAnnotation();

    public final Optional<AnnotationMirror> mapKeyAnnotation() {
        return unwrapOptionalEquivalence(wrappedMapKeyAnnotation());
    }

    /**
     * If {@link #bindingElement()} is a method that returns a primitive type, returns that type.
     * <p>
     * 如果bindingElement是一个方法，并且返回类型是原始类型，那么使用该type
     */
    public final Optional<TypeMirror> contributedPrimitiveType() {
        return bindingElement()
                .filter(bindingElement -> bindingElement instanceof ExecutableElement)
                .map(bindingElement -> MoreElements.asExecutable(bindingElement).getReturnType())
                .filter(type -> type.getKind().isPrimitive());
    }


    //如果是Kotlin对象，额外添加条件
    @Override
    public boolean requiresModuleInstance() {
        return !isContributingModuleKotlinObject().orElse(false) && super.requiresModuleInstance();
    }

    @Override
    public final boolean isNullable() {
        return nullableType().isPresent();
    }

    /**
     * Returns {@code true} if the contributing module is a Kotlin object. Note that a companion
     * object is also considered a Kotlin object.
     */
    abstract Optional<Boolean> isContributingModuleKotlinObject();

    /**
     * The strategy for getting an instance of a factory for a {@link ContributionBinding}.
     * <p>
     * 针对ContributionBinding获取一个工厂实例对象的策略
     */
    public enum FactoryCreationStrategy {
        /**
         * The factory class is a single instance.
         */
        SINGLETON_INSTANCE,
        /**
         * The factory must be created by calling the constructor.
         */
        CLASS_CONSTRUCTOR,
        /**
         * The factory is simply delegated to another.
         */
        DELEGATE,
    }

    /**
     * Returns the {@link FactoryCreationStrategy} appropriate for a binding.
     * <p>
     * 针对绑定根据kind，返回合适的新建工厂实例的策略
     *
     * <p>Delegate bindings use the {@link FactoryCreationStrategy#DELEGATE} strategy.
     *
     * <p>Bindings without dependencies that don't require a module instance use the {@link
     * FactoryCreationStrategy#SINGLETON_INSTANCE} strategy.
     *
     * <p>All other bindings use the {@link FactoryCreationStrategy#CLASS_CONSTRUCTOR} strategy.
     */
    public final FactoryCreationStrategy factoryCreationStrategy() {
        switch (kind()) {
            //1、当前绑定使用 Binds修饰，表示代理策略
            case DELEGATE:
                return DELEGATE;

            //2、当前绑定使用Provides修饰，（1）如果没有参数并且不需要实例化所在的module类，那么表示单例策略；（2）否则表示通过构造函数构建类
            case PROVISION:
                return dependencies().isEmpty() && !requiresModuleInstance()
                        ? SINGLETON_INSTANCE
                        : CLASS_CONSTRUCTOR;

            //3、如果使用Inject修饰 ，或者用了多重绑定Set，Map（1）依赖为空，使用单例；（2）否则使用通过构造函数构建类
            case INJECTION:
            case MULTIBOUND_SET:
            case MULTIBOUND_MAP:
                return dependencies().isEmpty() ? SINGLETON_INSTANCE : CLASS_CONSTRUCTOR;

            //4、其他一律采用通过构建函数构建类
            default:
                return CLASS_CONSTRUCTOR;
        }
    }


    /**
     * The {@link TypeMirror type} for the {@code Factory<T>} or {@code Producer<T>} which is created
     * for this binding. Uses the binding's key, V in the case of {@code Map<K, FrameworkClass<V>>>},
     * and E {@code Set<E>} for {@link dagger.multibindings.IntoSet @IntoSet} methods.
     * <p>
     * 绑定Key，如果是Map<K, FrameworkClass<V>>> 获取V,如果是Set<T>获取T
     */
    public final TypeMirror contributedType() {
        switch (contributionType()) {
            case MAP://Map<K, FrameworkClass<V>>> 获取V
                return MapType.from(key()).unwrappedFrameworkValueType();
            case SET://Set<T>里面的T
                return SetType.from(key()).elementType();
            case SET_VALUES:
            case UNIQUE:
                return key().type().java();
        }
        throw new AssertionError();
    }

    /**
     * Returns {@link BindingKind#MULTIBOUND_SET} or {@link
     * BindingKind#MULTIBOUND_MAP} if the key is a set or map.
     *
     * @throws IllegalArgumentException if {@code key} is neither a set nor a map
     */
    static BindingKind bindingKindForMultibindingKey(Key key) {
        if (SetType.isSet(key)) {
            return BindingKind.MULTIBOUND_SET;
        } else if (MapType.isMap(key)) {
            return BindingKind.MULTIBOUND_MAP;
        } else {
            throw new IllegalArgumentException(String.format("key is not for a set or map: %s", key));
        }
    }

    public abstract Builder<?, ?> toBuilder();

    /**
     * Base builder for {@link com.google.auto.value.AutoValue @AutoValue} subclasses of {@link
     * ContributionBinding}.
     */
    @CanIgnoreReturnValue
    public abstract static class Builder<C extends ContributionBinding, B extends Builder<C, B>> {
        public abstract B dependencies(Iterable<DependencyRequest> dependencies);

        public B dependencies(DependencyRequest... dependencies) {
            return dependencies(asList(dependencies));
        }

        public abstract B unresolved(C unresolved);

        public abstract B contributionType(ContributionType contributionType);

        public abstract B bindingElement(Element bindingElement);

        abstract B bindingElement(Optional<Element> bindingElement);

        public final B clearBindingElement() {
            return bindingElement(Optional.empty());
        }

        ;

        abstract B contributingModule(TypeElement contributingModule);

        abstract B isContributingModuleKotlinObject(boolean isModuleKotlinObject);

        public abstract B key(Key key);

        public abstract B nullableType(Optional<DeclaredType> nullableType);

        abstract B wrappedMapKeyAnnotation(
                Optional<Equivalence.Wrapper<AnnotationMirror>> wrappedMapKeyAnnotation);

        public abstract B kind(BindingKind kind);

        @CheckReturnValue
        abstract C autoBuild();

        @CheckReturnValue
        public C build() {
            C binding = autoBuild();
            Preconditions.checkState(
                    binding.contributingModule().isPresent()
                            == binding.isContributingModuleKotlinObject().isPresent(),
                    "The contributionModule and isModuleKotlinObject must both be set together.");
            return binding;
        }

    }
}
