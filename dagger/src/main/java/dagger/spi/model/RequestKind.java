package dagger.spi.model;

import javax.inject.Provider;

import dagger.Lazy;
import dagger.producers.Produced;
import dagger.producers.Producer;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

/**
 * Represents the different kinds of {@link javax.lang.model.type.TypeMirror types} that may be
 * requested as dependencies for the same key. For example, {@code String}, {@code
 * Provider<String>}, and {@code Lazy<String>} can all be requested if a key exists for {@code
 * String}; they have the {@link #INSTANCE}, {@link #PROVIDER}, and {@link #LAZY} request kinds,
 * respectively.
 * <p>
 * 用于相同的值呈现不同的状态，例如一个String类型，如果是INSTANCE类型，那么就是一个String；如果是PROVIDER，则会
 * 使用Provider<String>来表示
 */
public enum RequestKind {
    /**
     * A default request for an instance. E.g.: {@code FooType}
     * <p>
     * （1）使用T；没有使用任何类型对象包裹；
     * （2）@Provides或@Produces或@Binds修饰的bindingMethod使用@IntoSet或@ElementIntoSet生成的依赖的kind属性；
     */
    INSTANCE,

    /**
     * A request for a {@link Provider}. E.g.: {@code Provider<FooType>}
     * <p>
     * （1）Provider< T>类型对象；
     * （2）Produces修饰的bindingMethod方法生成的ProductionBinding对象里面会
     * 生成两个属性：monitorRequest 和executorRequest，这两个依赖的kind类型都是
     * PROVIDER类型；
     * （3）如果key的type使用了AssistedFactory修饰，该type生成ProvisionBinding对象的
     * provisionDependencies依赖的kind属性；
     */
    PROVIDER,

    /**
     * A request for a {@link Lazy}. E.g.: {@code Lazy<FooType>}
     */
    LAZY,

    /**
     * A request for a {@link Provider} of a {@link Lazy}. E.g.: {@code Provider<Lazy<FooType>>}
     */
    PROVIDER_OF_LAZY,

    /**
     * A request for a members injection. E.g. {@code void injectMembers(FooType);}. Can only be
     * requested by component interfaces.
     * <p>
     * componentMethod返回类型不是subcomponent，
     * 并且有且仅有一个参数，该方法生成的依赖RequestKind类型；
     */
    MEMBERS_INJECTION,

    /**
     * A request for a {@link Producer}. E.g.: {@code Producer<FooType>}
     */
    PRODUCER,

    /**
     * A request for a {@link Produced}. E.g.: {@code Produced<FooType>}
     */
    PRODUCED,

    /**
     * A request for a {@link com.google.common.util.concurrent.ListenableFuture}. E.g.: {@code
     * ListenableFuture<FooType>}. These can only be requested by component interfaces.
     * <p>
     * 使用ListenableFuture< T>，只有在componentMethod方法所在
     * component节点是production类型才可以使用该ListenableFuture< T>返回类型
     */
    FUTURE,
    ;


    /**
     * Returns a string that represents requests of this kind for a key.
     */
    public String format(Key key) {
        switch (this) {
            case INSTANCE:
                return key.toString();

            case PROVIDER_OF_LAZY:
                return String.format("Provider<Lazy<%s>>", key);

            case MEMBERS_INJECTION:
                return String.format("injectMembers(%s)", key);

            case FUTURE:
                return String.format("ListenableFuture<%s>", key);

            default:
                return String.format("%s<%s>", UPPER_UNDERSCORE.to(UPPER_CAMEL, name()), key);
        }
    }
}
