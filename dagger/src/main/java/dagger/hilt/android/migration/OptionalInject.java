package dagger.hilt.android.migration;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * When placed on an {@link dagger.hilt.android.AndroidEntryPoint}-annotated activity / fragment /
 * view / etc, allows injection to occur optionally based on whether or not the application is using
 * Hilt.
 *
 * <p>When using this annotation, you can use {@link OptionalInjectCheck#wasInjectedByHilt} to check
 * at runtime if the annotated class was injected by Hilt. Additionally, this annotation will also
 * cause a method, {@code wasInjectedByHilt} to be generated in the Hilt base class as well, that
 * behaves the same as {@link OptionalInjectCheck#wasInjectedByHilt}. The method is available to
 * users that extend the Hilt base class directly and don't use the Gradle plugin.
 *
 * <p>Example usage:
 *
 * <pre><code>
 * {@literal @}OptionalInject
 * {@literal @}AndroidEntryPoint
 * public final class MyFragment extends Fragment {
 *
 *   {@literal @}Inject Foo foo;
 *
 *   {@literal @}Override
 *   public void onAttach(Activity activity) {
 *     // Injection will happen here, but only if the Activity and the Application are also
 *     // AndroidEntryPoints and were injected by Hilt.
 *     super.onAttach(activity);
 *     if (!OptionalInjectCheck.wasInjectedByHilt(this)) {
 *       // Get Dagger components the previous way and inject.
 *     }
 *   }
 * }
 * </code></pre>
 *
 * <p>This is useful for libraries that have to support Hilt users as well as non-Hilt users.
 * Injection will happen if the parent type (e.g. the activity of a fragment) is an {@link
 * dagger.hilt.android.AndroidEntryPoint} annotated class and if that parent was also injected via
 * Hilt.
 *
 * @see OptionalInjectCheck
 * @see <a href="https://dagger.dev/hilt/optional-inject">Optional injection</a>
 */
@Target(ElementType.TYPE)
public @interface OptionalInject {}
