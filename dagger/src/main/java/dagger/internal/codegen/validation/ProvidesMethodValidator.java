package dagger.internal.codegen.validation;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ProvidesMethodValidator
 * Author: 佛学徒
 * Date: 2021/10/26 8:53
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

/** A validator for {@link dagger.Provides} methods. */
final class ProvidesMethodValidator extends BindingMethodValidator {

    private final DependencyRequestValidator dependencyRequestValidator;

    @Inject
    ProvidesMethodValidator(
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil kotlinMetadataUtil,
            DependencyRequestValidator dependencyRequestValidator,
            InjectionAnnotations injectionAnnotations) {

        super(TypeNames.PROVIDES);
        this.dependencyRequestValidator = dependencyRequestValidator;
    }
}