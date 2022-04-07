package dagger.hilt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * An annotation that declares which component(s) the annotated class should be included in when
 * Hilt generates the components. This may only be used with classes annotated with
 * {@literal @}{@link dagger.Module} or {@literal @}{@link dagger.hilt.EntryPoint}.
 *
 * <p>Example usage for installing a module in the generated {@code ApplicationComponent}:
 *
 * <pre><code>
 *   {@literal @}Module
 *   {@literal @}InstallIn(SingletonComponent.class)
 *   public final class FooModule {
 *     {@literal @}Provides
 *     static Foo provideFoo() {
 *       return new Foo();
 *     }
 *   }
 * </code></pre>
 *
 * @see <a href="https://dagger.dev/hilt/modules">Hilt Modules</a>
 */
@Retention(CLASS)
@Target({ElementType.TYPE})
@GeneratesRootInput
public @interface InstallIn {
    Class<?>[] value();
}
