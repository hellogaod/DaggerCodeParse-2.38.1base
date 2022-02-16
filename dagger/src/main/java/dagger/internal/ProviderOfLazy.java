package dagger.internal;


import javax.inject.Provider;

import dagger.Lazy;

import static dagger.internal.Preconditions.checkNotNull;

/**
 * A {@link Provider} of {@link Lazy} instances that each delegate to a given {@link Provider}.
 */
public final class ProviderOfLazy<T> implements Provider<Lazy<T>> {

    private final Provider<T> provider;

    private ProviderOfLazy(Provider<T> provider) {
        assert provider != null;
        this.provider = provider;
    }

    /**
     * Returns a new instance of {@link Lazy Lazy&lt;T&gt;}, which calls {@link Provider#get()} at
     * most once on the {@link Provider} held by this object.
     */
    @Override
    public Lazy<T> get() {
        return DoubleCheck.lazy(provider);
    }

    /**
     * Creates a new {@link Provider Provider&lt;Lazy&lt;T&gt;&gt;} that decorates the given
     * {@link Provider}.
     *
     * @see #get()
     */
    public static <T> Provider<Lazy<T>> create(Provider<T> provider) {
        return new ProviderOfLazy<T>(checkNotNull(provider));
    }

}
