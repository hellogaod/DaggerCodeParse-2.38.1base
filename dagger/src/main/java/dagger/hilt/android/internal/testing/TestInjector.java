package dagger.hilt.android.internal.testing;

/**
 * Interface to expose a method for members injection for use in tests.
 */
public interface TestInjector<T> {
    void injectTest(T t);
}
