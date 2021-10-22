package dagger.producers;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dagger.internal.Beta;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates methods of a producer module to create a production binding. If the method returns a
 * {@link com.google.common.util.concurrent.ListenableFuture} or {@link
 * com.google.common.util.concurrent.FluentFuture}, then the parameter type of the future is bound
 * to the value that the future produces; otherwise, the return type is bound to the returned value.
 * The production component will pass dependencies to the method as parameters.
 *
 * @since 2.0
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
@Beta
public @interface Produces {}
