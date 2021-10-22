package dagger.internal;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Scope;

import dagger.Provides;

/**
 * An {@linkplain Scope unscoped} {@link Provider}. While a {@link Provider} <i>may</i> apply
 * scoping semantics while providing an instance, a factory implementation is guaranteed to exercise
 * the binding logic ({@link Inject} constructors, {@link Provides} methods) upon each call to
 * {@link #get}.
 *
 * <p>Note that while subsequent calls to {@link #get} will create new instances for bindings such
 * as those created by {@link Inject} constructors, a new instance is not guaranteed by all
 * bindings. For example, {@link Provides} methods may be implemented in ways that return the same
 * instance for each call.
 * <p>
 * Factory代码层面没有实际意义。Factory的意义反应在思想模式上，即工厂模式
 */
public interface Factory<T> extends Provider<T> {
}
