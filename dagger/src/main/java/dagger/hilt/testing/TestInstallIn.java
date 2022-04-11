package dagger.hilt.testing;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dagger.hilt.GeneratesRootInput;
import dagger.hilt.InstallIn;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * An annotation that replaces one or more {@link InstallIn} modules with the annotated
 * module in tests.
 *
 * <p>The annotated class must also be annotated with {@link dagger.Module}.
 *
 * <p>Example:
 *
 * <pre><code>
 *   // Replaces FooModule with FakeFooModule, and installs it into the same component as FooModule.
 *   {@literal @}Module
 *   {@literal @}TestInstallIn(components = SingletonComponent.class, replaces = FooModule.class)
 *   public final class FakeFooModule {
 *     {@literal @}Provides
 *     static Foo provideFoo() {
 *       return new FakeFoo();
 *     }
 *   }
 * </code></pre>
 *
 * @see <a href="https://dagger.dev/hilt/modules">Hilt Modules</a>
 */
@Retention(CLASS)
@Target({ElementType.TYPE})
@GeneratesRootInput
public @interface TestInstallIn {
    /**
     * Returns the component(s) into which the annotated module will be installed.
     */
    Class<?>[] components();

    /**
     * Returns the {@link InstallIn} module(s) that the annotated class will replace in tests.
     */
    Class<?>[] replaces();
}

