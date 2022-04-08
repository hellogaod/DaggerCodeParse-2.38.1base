package dagger.hilt.android.testing;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import dagger.hilt.GeneratesRootInput;

/**
 * An annotation that creates an application with the given base type that can be used for any
 * test in the given build.
 *
 * <p>This annotation is useful for creating an application that can be used with instrumentation
 * tests in gradle, since every instrumentation test must share the same application type.
 */
@Target({ElementType.TYPE})
@GeneratesRootInput
public @interface CustomTestApplication {

    /** Returns the base {@link android.app.Application} class. */
    Class<?> value();
}
