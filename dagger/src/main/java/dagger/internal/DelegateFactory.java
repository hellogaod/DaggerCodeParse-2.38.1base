package dagger.internal;


import javax.inject.Provider;

import static dagger.internal.Preconditions.checkNotNull;

/**
 * A DelegateFactory that is used to stitch Provider/Lazy indirection based dependency cycles.
 *
 * @since 2.0.1
 */
public final class DelegateFactory<T> implements Factory<T> {

    private Provider<T> delegate;

    @Override
    public T get() {
        if (delegate == null) {
            throw new IllegalStateException();
        }
        return delegate.get();
    }

    // TODO(ronshapiro): remove this once we can reasonably expect generated code is no longer using
    // this method
    @Deprecated
    public void setDelegatedProvider(Provider<T> delegate) {
        setDelegate(this, delegate);
    }

    /**
     * Sets {@code delegateFactory}'s delegate provider to {@code delegate}.
     *
     * <p>{@code delegateFactory} must be an instance of {@link DelegateFactory}, otherwise this
     * method will throw a {@link ClassCastException}.
     */
    public static <T> void setDelegate(Provider<T> delegateFactory, Provider<T> delegate) {
        checkNotNull(delegate);
        DelegateFactory<T> asDelegateFactory = (DelegateFactory<T>) delegateFactory;
        if (asDelegateFactory.delegate != null) {
            throw new IllegalStateException();
        }
        asDelegateFactory.delegate = delegate;
    }

    /**
     * Returns the factory's delegate.
     *
     * @throws NullPointerException if the delegate has not been set
     */
    Provider<T> getDelegate() {
        return checkNotNull(delegate);
    }
}
