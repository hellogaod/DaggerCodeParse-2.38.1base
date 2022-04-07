package dagger.hilt.android;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import dagger.hilt.GeneratesRootInput;


/**
 * Marks an Android component class to be setup for injection with the standard Hilt Dagger Android
 * components. Currently, this supports activities, fragments, views, services, and broadcast
 * receivers.
 *
 * <p>This annotation will generate a base class that the annotated class should extend, either
 * directly or via the Hilt Gradle Plugin. This base class will take care of injecting members into
 * the Android class as well as handling instantiating the proper Hilt components at the right point
 * in the lifecycle. The name of the base class will be "Hilt_<annotated class name>".
 *
 * <p>Example usage (with the Hilt Gradle Plugin):
 *
 * <pre><code>
 *   {@literal @}AndroidEntryPoint
 *   public final class FooActivity extends FragmentActivity {
 *     {@literal @}Inject Foo foo;
 *
 *     {@literal @}Override
 *     public void onCreate(Bundle savedInstanceState) {
 *       super.onCreate(savedInstanceState);  // The foo field is injected in super.onCreate()
 *     }
 *   }
 * </code></pre>
 *
 * <p>Example usage (without the Hilt Gradle Plugin):
 *
 * <pre><code>
 *   {@literal @}AndroidEntryPoint(FragmentActivity.class)
 *   public final class FooActivity extends Hilt_FooActivity {
 *     {@literal @}Inject Foo foo;
 *
 *     {@literal @}Override
 *     public void onCreate(Bundle savedInstanceState) {
 *       super.onCreate(savedInstanceState);  // The foo field is injected in super.onCreate()
 *     }
 *   }
 * </code></pre>
 *
 * @see HiltAndroidApp
 */
//修饰的类节点支持： activities, fragments, views, services, and broadcast receivers.
@Target({ElementType.TYPE})
@GeneratesRootInput
public @interface AndroidEntryPoint {

    /**
     * The base class for the generated Hilt class. When applying the Hilt Gradle Plugin this value
     * is not necessary and will be inferred from the current superclass.
     */
    Class<?> value() default Void.class;
}
