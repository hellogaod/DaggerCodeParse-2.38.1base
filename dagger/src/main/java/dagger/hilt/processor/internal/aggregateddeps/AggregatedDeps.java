
package dagger.hilt.processor.internal.aggregateddeps;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.CLASS;

// TODO(bcorso): Change this API to clearly represent that each AggeregatedDeps should only contain
// a single module, entry point, or component entry point.

/** Annotation for propagating dependency information through javac runs. */
@Retention(CLASS)
public @interface AggregatedDeps {
  /** Returns the components that this dependency will be installed in. */
  String[] components();

  /** Returns the test this dependency is associated with, otherwise an empty string. */
  String test() default "";

  /** Returns the deps that this dep replaces. */
  String[] replaces() default {};

  String[] modules() default {};

  String[] entryPoints() default {};

  String[] componentEntryPoints() default {};
}
