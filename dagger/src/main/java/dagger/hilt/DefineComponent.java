package dagger.hilt;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dagger.hilt.internal.definecomponent.DefineComponentNoParent;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Defines a Hilt component.
 *
 * <p>Example defining a root component, {@code ParentComponent}:
 *
 * <pre><code>
 *   {@literal @}ParentScoped
 *   {@literal @}DefineComponent
 *   interface ParentComponent {}
 * </code></pre>
 *
 * <p>Example defining a child component, {@code ChildComponent}:
 *
 * <pre><code>
 *   {@literal @}ChildScoped
 *   {@literal @}DefineComponent(parent = ParentComponent.class)
 *   interface ChildComponent {}
 * </code></pre>
 */
@Retention(CLASS)
@Target(TYPE)
@GeneratesRootInput
public @interface DefineComponent {
    /**
     * Returns the parent of this component, if it exists.
     */
    Class<?> parent() default DefineComponentNoParent.class;

    /**
     * Defines a builder for a Hilt component.
     *
     * <pre><code>
     *   {@literal @}DefineComponent.Builder
     *   interface ParentComponentBuilder {
     *     ParentComponentBuilder seedData(SeedData seed);
     *     ParentComponent build();
     *   }
     * </code></pre>
     */
    // TODO(bcorso): Consider making this a top-level class to hint that it doesn't need to be nested.
    @Retention(CLASS)
    @Target(TYPE)
    @GeneratesRootInput
    public @interface Builder {
    }
}
