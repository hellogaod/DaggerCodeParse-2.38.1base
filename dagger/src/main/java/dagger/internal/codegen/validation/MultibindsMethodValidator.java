package dagger.internal.codegen.validation;


import javax.inject.Inject;

import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * A validator for {@link dagger.multibindings.Multibinds} methods.
 */
class MultibindsMethodValidator extends BindingMethodValidator {

    /**
     * Creates a validator for {@link dagger.multibindings.Multibinds @Multibinds} methods.
     */
    @Inject
    MultibindsMethodValidator(
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil kotlinMetadataUtil,
            DependencyRequestValidator dependencyRequestValidator,
            InjectionAnnotations injectionAnnotations) {
        super(TypeNames.MULTIBINDS);
    }
}
