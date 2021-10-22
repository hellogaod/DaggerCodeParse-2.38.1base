package dagger.producers;

import com.google.common.util.concurrent.ListenableFuture;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Qualifier;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates an interface or abstract class for which a fully-formed, dependency-injected
 * implementation is to be generated from a set of {@linkplain #modules modules}. The generated
 * class will have the name of the type annotated with {@code @ProductionComponent} prepended with
 * {@code Dagger}. For example, {@code @ProductionComponent interface MyComponent {...}} will
 * produce an implementation named {@code DaggerMyComponent}.
 * <p>
 * 使用ProductionComponent注解的命名规则是前面加上Dagger
 *
 * <p>Each {@link Produces} method that contributes to the component will be called at most once per
 * component instance, no matter how many times that binding is used as a dependency. TODO(beder):
 * Decide on how scope works for producers.
 * <p>
 * 对组件有贡献的每个 {@link Produces} 方法在每个组件实例中最多被调用一次，无论该绑定作为依赖项使用多少次。 TODO(beder)：决定范围如何为生产者工作。
 *
 * <h2>Component methods</h2>
 *
 * <p>Every type annotated with {@code @ProductionComponent} must contain at least one abstract
 * component method. Component methods must represent {@linkplain Producer production}.
 *
 * <p>Production methods have no arguments and return either a {@link ListenableFuture} or {@link
 * Producer} of a type that is {@link Inject injected}, {@link Provides provided}, or {@link
 * Produces produced}. Each may have a {@link Qualifier} annotation as well. The following are all
 * valid production method declarations:
 *
 * <pre><code>
 *   {@literal ListenableFuture<SomeType>} getSomeType();
 *   {@literal Producer<Set<SomeType>>} getSomeTypes();
 *   {@literal @Response ListenableFuture<Html>} getResponse();
 * </code></pre>
 *
 * <h2>Exceptions</h2>
 *
 * <p>When a producer throws an exception, the exception will be propagated to its downstream
 * producers in the following way: if the downstream producer injects a type {@code T}, then that
 * downstream producer will be skipped, and the exception propagated to its downstream producers;
 * and if the downstream producer injects a {@code Produced<T>}, then the downstream producer will
 * be run with the exception stored in the {@code Produced<T>}.
 *
 * <p>If a non-execution exception is thrown (e.g., an {@code InterruptedException} or {@code
 * CancellationException}), then exception is handled as in {@link
 * com.google.common.util.concurrent.Futures#transform}.
 * <!-- TODO(beder): Explain this more thoroughly, and update the javadocs of those utilities. -->
 *
 * <h2>Executor</h2>
 *
 * <p>The component must include a binding for <code>{@literal @}{@link Production}
 * {@link java.util.concurrent.Executor}</code>; this binding will be called exactly once, and the
 * provided executor will be used by the framework to schedule all producer methods (for this
 * component, and any {@link ProductionSubcomponent} it may have.
 *
 * @since 2.0
 */
public @interface ProductionComponent {
    /**
     * A list of classes annotated with {@link Module} or {@link ProducerModule} whose bindings are
     * used to generate the component implementation.
     */
    Class<?>[] modules() default {};

    /**
     * A list of types that are to be used as component dependencies.
     */
    Class<?>[] dependencies() default {};

    /**
     * A builder for a production component.
     *
     * <p>This follows all the rules of {@link Component.Builder}, except it must appear in classes
     * annotated with {@link ProductionComponent} instead of {@code Component}.
     */
    @Retention(RUNTIME) // Allows runtimes to have specialized behavior interoperating with Dagger.
    @Target(TYPE)
    @Documented
    @interface Builder {
    }

    /**
     * A factory for a production component.
     *
     * <p>This follows all the rules of {@link Component.Factory}, except it must appear in classes
     * annotated with {@link ProductionComponent} instead of {@code Component}.
     *
     * @since 2.22
     */
    @Retention(RUNTIME) // Allows runtimes to have specialized behavior interoperating with Dagger.
    @Target(TYPE)
    @Documented
    @interface Factory {
    }
}
