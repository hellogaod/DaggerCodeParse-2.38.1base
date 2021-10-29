package dagger.internal.codegen.validation;


import javax.inject.Inject;

import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;

/**
 * Validation for dependency requests.
 */
final class DependencyRequestValidator {
    private final MembersInjectionValidator membersInjectionValidator;
    private final InjectionAnnotations injectionAnnotations;
    private final KotlinMetadataUtil metadataUtil;
    private final DaggerElements elements;

    @Inject
    DependencyRequestValidator(
            MembersInjectionValidator membersInjectionValidator,
            InjectionAnnotations injectionAnnotations,
            KotlinMetadataUtil metadataUtil,
            DaggerElements elements) {
        this.membersInjectionValidator = membersInjectionValidator;
        this.injectionAnnotations = injectionAnnotations;
        this.metadataUtil = metadataUtil;
        this.elements = elements;
    }
}
