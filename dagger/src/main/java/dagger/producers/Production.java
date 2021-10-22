package dagger.producers;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import dagger.internal.Beta;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifies a type that will be provided to the framework for use internally.
 *
 * <p>The only type that may be so qualified is {@link java.util.concurrent.Executor}. In this case,
 * the resulting executor is used to schedule {@linkplain Produces producer methods} in a
 * {@link ProductionComponent} or {@link ProductionSubcomponent}.
 */
@Documented
@Retention(RUNTIME)
@Qualifier
@Beta
public @interface Production {}
