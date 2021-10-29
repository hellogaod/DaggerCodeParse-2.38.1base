package dagger.internal.codegen.validation;

import javax.inject.Inject;

import dagger.internal.codegen.binding.InjectionAnnotations;

/**
 * Validates members injection requests (members injection methods on components and requests for
 * {@code MembersInjector<Foo>}).
 */
final class MembersInjectionValidator {
    private final InjectionAnnotations injectionAnnotations;

    @Inject
    MembersInjectionValidator(
            InjectionAnnotations injectionAnnotations
    ) {
        this.injectionAnnotations = injectionAnnotations;
    }
}
