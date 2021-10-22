package dagger.producers;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dagger.Module;
import dagger.internal.Beta;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates a class that contributes {@link Produces} bindings to the production component.
 *
 * @since 2.0
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Beta
public @interface ProducerModule {

    /**
     * Additional {@code @ProducerModule}- or {@link Module}-annotated classes from which this module
     * is composed. The de-duplicated contributions of the modules in {@code includes}, and of their
     * inclusions recursively, are all contributed to the object graph.
     */
    Class<?>[] includes() default {};


    /**
     * Any {@link dagger.Subcomponent}- or {@link ProductionSubcomponent}-annotated classes which
     * should be children of the component in which this module is installed. A subcomponent may be
     * listed in more than one module in a component.
     *
     * @since 2.7
     */
    Class<?>[] subcomponents() default {};
}
