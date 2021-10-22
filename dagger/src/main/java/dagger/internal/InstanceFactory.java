package dagger.internal;

import dagger.Lazy;

import static dagger.internal.Preconditions.checkNotNull;


/**
 * A {@link Factory} implementation that returns a single instance for all invocations of {@link
 * #get}.
 *
 * <p>Note that while this is a {@link Factory} implementation, and thus unscoped, each call to
 * {@link #get} will always return the same instance. As such, any scoping applied to this factory
 * is redundant and unnecessary. However, using this with {@link DoubleCheck#provider} is valid and
 * may be desired for testing or contractual guarantees.
 */
public final class InstanceFactory<T> implements Factory<T>, Lazy<T> {
    public static <T> Factory<T> create(T instance) {
        return new InstanceFactory<T>(checkNotNull(instance, "instance cannot be null"));
    }

    public static <T> Factory<T> createNullable(T instance) {
        return instance == null
                ? InstanceFactory.<T>nullInstanceFactory()
                : new InstanceFactory<T>(instance);
    }

    @SuppressWarnings("unchecked") // bivariant implementation
    private static <T> InstanceFactory<T> nullInstanceFactory() {
        return (InstanceFactory<T>) NULL_INSTANCE_FACTORY;
    }

    private static final InstanceFactory<Object> NULL_INSTANCE_FACTORY =
            new InstanceFactory<Object>(null);

    private final T instance;

    private InstanceFactory(T instance) {
        this.instance = instance;
    }

    @Override
    public T get() {
        return instance;
    }
}
