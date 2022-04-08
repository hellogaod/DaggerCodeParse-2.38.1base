package dagger.hilt.android.testing;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dagger.hilt.GeneratesRootInput;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * An annotation that can be used on a test field to contribute the value into the {@link
 * dagger.hilt.components.SingletonComponent} as an {@link dagger.multibindings.IntoMap}
 * for the given type. Example usage:
 *
 * <pre><code>
 * public class FooTest{
 *   ...
 *   {@literal @}BindValueIntoMap
 *   {@literal @}MyMapKey(KEY1)
 *   String boundBar = "bar";
 *
 *   {@literal @}BindValueIntoMap
 *   {@literal @}MyMapKey(KEY2)
 *   String boundBaz = "baz";
 *   ...
 * }
 * </code></pre>
 *
 * Here the map that contains all the bound elements (in this case "bar" and "baz") will be
 * accessible to the entire application for your test.
 */
@Retention(CLASS)
@Target({ElementType.FIELD})
@GeneratesRootInput
public @interface BindValueIntoMap {}
