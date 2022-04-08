package dagger.hilt.android.testing;

import static java.lang.annotation.RetentionPolicy.CLASS;

import dagger.hilt.GeneratesRootInput;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation that can be used on a test field to contribute the value into the {@link
 * dagger.hilt.components.SingletonComponent} as an {@link dagger.multibindings.IntoSet}
 * for the given type. Example usage:
 *
 * <pre><code>
 * public class FooTest{
 *   ...
 *   {@literal @}BindValueIntoSet String boundBar = "bar";
 *   {@literal @}BindValueIntoSet String boundBaz = "baz";
 *   ...
 * }
 * </code></pre>
 *
 * Here the set that contains all the bound elements (in this case "bar" and "baz") will be
 * accessible to the entire application for your test. Also see {@link BindElementsIntoSet}, where
 * you can gather individual elements into one set and bind it to the application.
 */
@Retention(CLASS)
@Target({ElementType.FIELD})
@GeneratesRootInput
public @interface BindValueIntoSet {}
