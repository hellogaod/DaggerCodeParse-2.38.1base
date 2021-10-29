package dagger.internal.codegen.binding;


import javax.inject.Inject;

/**
 * Factory for {@link DependencyRequest}s.
 *
 * <p>Any factory method may throw {@link TypeNotPresentException} if a type is not available, which
 * may mean that the type will be generated in a later round of processing.
 */
public final class DependencyRequestFactory {
    private final KeyFactory keyFactory;
    private final InjectionAnnotations injectionAnnotations;

    @Inject
    DependencyRequestFactory(
            KeyFactory keyFactory,
            InjectionAnnotations injectionAnnotations
    ) {
        this.keyFactory = keyFactory;
        this.injectionAnnotations = injectionAnnotations;
    }
}
