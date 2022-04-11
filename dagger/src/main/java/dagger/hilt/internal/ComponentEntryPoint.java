package dagger.hilt.internal;


import static java.lang.annotation.RetentionPolicy.CLASS;

import dagger.hilt.GeneratesRootInput;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation marking generated interfaces for entry points for which there is also a corresponding
 * generated Component. Component entry points differ from normal entry points in that they may be
 * filtered out in tests.
 */
@Target(ElementType.TYPE)
@Retention(CLASS)
@GeneratesRootInput
// TODO(bcorso): Rename and publicly strip these references out of hilt.
public @interface ComponentEntryPoint {}
