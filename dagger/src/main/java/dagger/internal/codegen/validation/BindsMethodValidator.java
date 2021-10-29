package dagger.internal.codegen.validation;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: BindsMethodValidator
 * Author: 佛学徒
 * Date: 2021/10/26 8:54
 * Description:
 * History:
 */

import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;

import dagger.internal.codegen.binding.BindsTypeChecker;
import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/** A validator for {@link dagger.Binds} methods. */
final class BindsMethodValidator extends BindingMethodValidator {
    private final DaggerTypes types;
    private final BindsTypeChecker bindsTypeChecker;

    @Inject
    BindsMethodValidator(
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil kotlinMetadataUtil,
            BindsTypeChecker bindsTypeChecker,
            DependencyRequestValidator dependencyRequestValidator,
            InjectionAnnotations injectionAnnotations) {
        super(TypeNames.BINDS);
        this.types = types;
        this.bindsTypeChecker = bindsTypeChecker;
    }
}
