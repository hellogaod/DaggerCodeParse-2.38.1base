package dagger.hilt.android.testing;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dagger.hilt.GeneratesRootInput;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Annotation used for marking an Android emulator tests that require injection. */
// Set the retention to RUNTIME because we check it via reflection in the HiltAndroidRule.
@Retention(RUNTIME)
@Target({ElementType.TYPE})
@GeneratesRootInput
public @interface HiltAndroidTest {}
