package dagger.hilt.internal.definecomponent;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * An annotation used to aggregate {@link dagger.hilt.DefineComponent} types in a common location.
 *
 * <p>Note: The types are given using {@link String} rather than {@link Class} since the {@link
 * dagger.hilt.DefineComponent} type is not necessarily in the same package and not necessarily
 * public.
 */
@Retention(CLASS)
@Target(TYPE)
public @interface DefineComponentClasses {
    /**
     * Returns the fully qualified name of the {@link dagger.hilt.DefineComponent} type, or an empty
     * string if it wasn't given.
     */
    String component() default "";

    /**
     * Returns the fully qualified name of the {@link dagger.hilt.DefineComponent.Builder} type, or an
     * empty string if it wasn't given.
     */
    String builder() default "";
}

