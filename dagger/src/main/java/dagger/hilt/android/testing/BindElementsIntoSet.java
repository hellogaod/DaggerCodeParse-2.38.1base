package dagger.hilt.android.testing;


import static java.lang.annotation.RetentionPolicy.CLASS;

import dagger.hilt.GeneratesRootInput;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation that can be used on a test field to contribute the value into the {@link
 * dagger.hilt.components.SingletonComponent} as an {@link
 * dagger.multibindings.ElementsIntoSet} for the given type. Example usage:
 *
 * <pre><code>
 * public class FooTest{
 *   ...
 *   {@literal @}BindElementsIntoSet Set<String> bindedSet = ImmutableSet.of("bar", "baz");
 *   ...
 * }
 * </code></pre>
 *
 * Here, bindedSet will be accessible to the entire application for your test. This is functionally
 * equivalent to installing the following module in your test:
 *
 * <pre><code>
 * {@literal @}Module
 * {@literal @}InstallIn
 * interface MyModule {
 *  {@literal @}Provides
 *  {@literal @}ElementsIntoSet
 *  Set<String> bindSet() {
 *    return ImmutableSet.of("bar", "baz");
 *  }
 * }
 * </code></pre>
 *
 * Also see {@link BindValueIntoSet}, where you can gather individual elements into one set and bind
 * it to the application.
 */
@Retention(CLASS)
@Target({ElementType.FIELD})
@GeneratesRootInput
public @interface BindElementsIntoSet {}

