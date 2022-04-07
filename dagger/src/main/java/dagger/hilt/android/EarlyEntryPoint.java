package dagger.hilt.android;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dagger.internal.Beta;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An escape hatch for when a Hilt entry point usage needs to be called before the singleton
 * component is available in a Hilt test.
 *
 * 当单例组件在 Hilt 测试中可用之前需要调用 Hilt 入口点使用时的逃生舱口。
 *
 * <p>Warning: Please see documentation for more details:
 * https://dagger.dev/hilt/early-entry-point
 *
 * <p>Usage:
 *
 * <p>To enable an existing entry point to be called early in a Hilt test, replace its
 * {@link dagger.hilt.EntryPoint} annotation with {@link EarlyEntryPoint}. (Note that,
 * {@link EarlyEntryPoint} is only allowed on entry points installed in the
 * {@link dagger.hilt.components.SingletonComponent}).
 *
 * <pre><code>
 * @EarlyEntryPoint  // <- This replaces @EntryPoint
 * @InstallIn(SingletonComponent.class)
 * interface FooEntryPoint {
 *   Foo getFoo();
 * }
 * </code></pre>
 *
 * <p>Then, replace any of the corresponding usages of {@link dagger.hilt.EntryPoints} with
 * {@link EarlyEntryPoints}, as shown below:
 *
 * <pre><code>
 * // EarlyEntryPoints.get() must be used with entry points annotated with @EarlyEntryPoint
 * // This entry point can now be called at any point during a test, e.g. in Application.onCreate().
 * Foo foo = EarlyEntryPoints.get(appContext, FooEntryPoint.class).getFoo();
 * </code></pre>
 */
@Beta
@Retention(RUNTIME) // Needs to be runtime for checks in EntryPoints and EarlyEntryPoints.
@Target(ElementType.TYPE)
public @interface EarlyEntryPoint {}
