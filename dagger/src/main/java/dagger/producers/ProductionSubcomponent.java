package dagger.producers;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dagger.Component;
import dagger.Module;
import dagger.Subcomponent;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A subcomponent that inherits the bindings from a parent {@link Component}, {@link Subcomponent},
 * {@link ProductionComponent}, or {@link ProductionSubcomponent}. The details of how to associate a
 * subcomponent with a parent are described in the documentation for {@link Component}.
 *
 * <p>The executor for a production subcomponent is supplied by binding
 * <code>{@literal @}Production Executor</code>, similar to {@link ProductionComponent}. Note that
 * this binding may be in an ancestor component.
 *
 * @since 2.1
 */
@Retention(RUNTIME) // Allows runtimes to have specialized behavior interoperating with Dagger.
@Target(TYPE)
@Documented
public @interface ProductionSubcomponent {
    /**
     * A list of classes annotated with {@link Module} or {@link ProducerModule} whose bindings are
     * used to generate the subcomponent implementation.  Note that through the use of
     * {@link Module#includes} or {@link ProducerModule#includes} the full set of modules used to
     * implement the subcomponent may include more modules that just those listed here.
     */
    Class<?>[] modules() default {};

    /**
     * A builder for a production subcomponent.
     *
     * <p>This follows all the rules of {@link Component.Builder}, except it must appear in classes
     * annotated with {@link ProductionSubcomponent} instead of {@code Component}.
     *
     * <p>If a subcomponent defines a builder, its parent component(s) will have a binding for that
     * builder type, allowing an instance or {@code Provider} of that builder to be injected or
     * returned from a method on that component like any other binding.
     */
    @Retention(RUNTIME) // Allows runtimes to have specialized behavior interoperating with Dagger.
    @Target(TYPE)
    @Documented
    @interface Builder {
    }

    /**
     * A factory for a production subcomponent.
     *
     * <p>This follows all the rules of {@link Component.Factory}, except it must appear in classes
     * annotated with {@link ProductionSubcomponent} instead of {@code Component}.
     *
     * <p>If a subcomponent defines a factory, its parent component(s) will have a binding for that
     * factory type, allowing an instance that factory to be injected or returned from a method on
     * that component like any other binding.
     *
     * @since 2.22
     */
    @Retention(RUNTIME) // Allows runtimes to have specialized behavior interoperating with Dagger.
    @Target(TYPE)
    @Documented
    @interface Factory {
    }
}
