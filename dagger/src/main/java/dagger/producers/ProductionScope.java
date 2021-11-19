package dagger.producers;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A scope annotation for provision bindings that are tied to the lifetime of a
 * {@link ProductionComponent} or {@link ProductionSubcomponent}.
 */
@Documented
@Retention(RUNTIME)
@Scope
public @interface ProductionScope {}
