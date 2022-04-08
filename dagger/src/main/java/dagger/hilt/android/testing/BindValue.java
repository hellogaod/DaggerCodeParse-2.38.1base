package dagger.hilt.android.testing;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dagger.hilt.GeneratesRootInput;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * An annotation that can be used on a test field to contribute the value into the {@link
 * dagger.hilt.components.SingletonComponent}. Example usage:
 *
 * <pre><code>
 * public class FooTest{
 *   ...
 *   {@literal @}BindValue Bar boundBar = new Bar();
 *   ...
 * }
 * </code></pre>
 *
 * Here {@code boundBar} will be accessible to the entire application for your test.
 */
@Retention(CLASS)
@Target({ElementType.FIELD})
@GeneratesRootInput
public @interface BindValue {}

