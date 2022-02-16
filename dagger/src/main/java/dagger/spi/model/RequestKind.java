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
     */
    INSTANCE,

    /**
     * A request for a {@link Provider}. E.g.: {@code Provider<FooType>}
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
     * 作为成员注入该component：component类中的方法有且仅有一个参数,返回类型是void或参数类型和方法返回类型必须一致
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
