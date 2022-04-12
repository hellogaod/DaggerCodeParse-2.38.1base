package dagger.hilt.android.internal.testing;


import android.app.Application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dagger.hilt.GeneratesRootInput;

import static java.lang.annotation.RetentionPolicy.CLASS;

/** Annotation that generates a Hilt test application. */
@Retention(CLASS)
@Target({ElementType.TYPE})
@GeneratesRootInput
public @interface InternalTestRoot {

    /** Returns the test class. */
    Class<?> testClass();

    /** Returns the base {@link Application} class.  */
    Class<? extends Application> applicationBaseClass();
}
