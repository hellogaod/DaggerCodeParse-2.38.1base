package dagger.hilt.codegen;


/**
 * An annotation used to specify the originating element that triggered the code generation of a
 * type. This annotation should only be used on generated code and is meant to be used by code
 * generators that generate Hilt modules, entry points, etc. Failure to use this annotation may mean
 * improper test isolation for generated classes.
 *
 * <p>This annotation should be used on any generated top-level class that either contains generated
 * modules (or entry points) or contains annotations that will generate modules (or entry points).
 *
 * <p>Example: Suppose we have the following use of an annotation, {@code MyAnnotation}.
 *
 * <pre><code>
 *   class Outer {
 *     static class Inner {
 *       {@literal @}MyAnnotation Foo foo;
 *     }
 *   }
 * </code></pre>
 *
 * <p>If {@code MyAnnotation} generates an entry point, it should be annotated as follows:
 *
 * <pre><code>
 *   {@literal @}OriginatingElement(topLevelClass = Outer.class)
 *   {@literal @}EntryPoint
 *   {@literal @}InstallIn(SingletonComponent.class) {
 *       ...
 *   }
 * </code></pre>
 */
// TODO(bcorso): Consider just advising/enforcing that all top-level classes use this annotation.
public @interface OriginatingElement {
    /** Returns the top-level class enclosing the originating element. */
    Class<?> topLevelClass();
}
