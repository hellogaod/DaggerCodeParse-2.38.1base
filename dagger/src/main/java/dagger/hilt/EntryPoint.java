package dagger.hilt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Annotation for marking an interface as an entry point into a generated component. This annotation
 * must be used with {@link dagger.hilt.InstallIn} to indicate which component(s) should have this
 * entry point. When assembling components, Hilt will make the indicated components extend the
 * interface marked with this annotation.
 *
 * <p>To use the annotated interface to access Dagger objects, use {@link dagger.hilt.EntryPoints}.
 *
 * <p>Example usage:
 *
 * <pre><code>
 *   {@literal @}EntryPoint
 *   {@literal @}InstallIn(SingletonComponent.class)
 *   public interface FooEntryPoint {
 *     Foo getFoo();
 *   }
 *
 *   Foo foo = EntryPoints.get(component, FooEntryPoint.class).getFoo();
 * </code></pre>
 *
 * @see <a href="https://dagger.dev/hilt/entry-points">Entry points</a>
 */
@Retention(CLASS)
@Target(ElementType.TYPE)
@GeneratesRootInput
public @interface EntryPoint {}
