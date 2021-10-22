package dagger;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * A subcomponent that inherits the bindings from a parent {@link Component} or
 * {@link Subcomponent}. The details of how to associate a subcomponent with a parent are described
 * in the documentation for {@link Component}.
 *
 * @since 2.0
 */
@Retention(RUNTIME) // Allows runtimes to have specialized behavior interoperating with Dagger.
@Target(TYPE)
@Documented
public @interface Subcomponent {
    /**
     * A list of classes annotated with {@link Module} whose bindings are used to generate the
     * subcomponent implementation.  Note that through the use of {@link Module#includes} the full set
     * of modules used to implement the subcomponent may include more modules that just those listed
     * here.
     */
    Class<?>[] modules() default {};

    /**
     * A builder for a subcomponent.
     *
     * <p>This follows all the rules of {@link Component.Builder}, except it must appear in classes
     * annotated with {@link Subcomponent} instead of {@code Component}.
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
     * A factory for a subcomponent.
     *
     * <p>This follows all the rules of {@link Component.Factory}, except it must appear in classes
     * annotated with {@link Subcomponent} instead of {@code Component}.
     *
     * <p>If a subcomponent defines a factory, its parent component(s) will have a binding for that
     * factory type, allowing an instance of that factory to be injected or returned from a method on
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
