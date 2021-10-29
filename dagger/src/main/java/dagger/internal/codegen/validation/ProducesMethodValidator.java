package dagger.internal.codegen.validation;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ProducesMethodValidator
 * Author: 佛学徒
 * Date: 2021/10/26 8:54
 * Description:
 * History:
 */

import javax.inject.Inject;

import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/** A validator for {@link dagger.producers.Produces} methods. */
final class ProducesMethodValidator extends BindingMethodValidator {

    @Inject
    ProducesMethodValidator(
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil kotlinMetadataUtil,
            DependencyRequestValidator dependencyRequestValidator,
            InjectionAnnotations injectionAnnotations) {
        super(TypeNames.PRODUCES);
    }
}
