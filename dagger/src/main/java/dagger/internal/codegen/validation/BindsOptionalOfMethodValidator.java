package dagger.internal.codegen.validation;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: BindsOptionalOfMethodValidator
 * Author: 佛学徒
 * Date: 2021/10/26 8:57
 * Description:
 * History:
 */

import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;

import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/** A validator for {@link dagger.BindsOptionalOf} methods. */
final class BindsOptionalOfMethodValidator extends BindingMethodValidator {

    private final DaggerTypes types;
    private final InjectionAnnotations injectionAnnotations;

    @Inject
    BindsOptionalOfMethodValidator(
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil kotlinMetadataUtil,
            DependencyRequestValidator dependencyRequestValidator,
            InjectionAnnotations injectionAnnotations) {
        super(TypeNames.BINDS_OPTIONAL_OF);
        this.types = types;
        this.injectionAnnotations = injectionAnnotations;
    }
}
