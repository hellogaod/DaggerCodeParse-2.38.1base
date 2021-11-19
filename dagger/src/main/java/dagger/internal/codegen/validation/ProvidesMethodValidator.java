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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static dagger.internal.codegen.validation.BindingElementValidator.AllowsMultibindings.ALLOWS_MULTIBINDINGS;
import static dagger.internal.codegen.validation.BindingElementValidator.AllowsScoping.ALLOWS_SCOPING;
import static dagger.internal.codegen.validation.BindingMethodValidator.Abstractness.MUST_BE_CONCRETE;
import static dagger.internal.codegen.validation.BindingMethodValidator.ExceptionSuperclass.RUNTIME_EXCEPTION;

/**
 * A validator for {@link dagger.Provides} methods.
 * <p>
 * 针对Provides修饰的方法校验
 */
final class ProvidesMethodValidator extends BindingMethodValidator {

    private final DependencyRequestValidator dependencyRequestValidator;

    @Inject
    ProvidesMethodValidator(
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil kotlinMetadataUtil,
            DependencyRequestValidator dependencyRequestValidator,
            InjectionAnnotations injectionAnnotations) {

        super(
                elements,
                types,
                kotlinMetadataUtil,
                TypeNames.PROVIDES,
                ImmutableSet.of(TypeNames.MODULE, TypeNames.PRODUCER_MODULE),
                dependencyRequestValidator,
                MUST_BE_CONCRETE,
                RUNTIME_EXCEPTION,
                ALLOWS_MULTIBINDINGS,
                ALLOWS_SCOPING,
                injectionAnnotations);

        this.dependencyRequestValidator = dependencyRequestValidator;
    }

    @Override
    protected ElementValidator elementValidator(ExecutableElement element) {
        return new Validator(element);
    }

    private class Validator extends MethodValidator {

        Validator(ExecutableElement element) {
            super(element);
        }

        @Override
        protected void checkAdditionalMethodProperties() {
        }

        /**
         * Adds an error if a {@link dagger.Provides @Provides} method depends on a producer type.
         */
        @Override
        protected void checkParameter(VariableElement parameter) {
            super.checkParameter(parameter);
            //检查parameter不是Producer或Producer类型
            dependencyRequestValidator.checkNotProducer(report, parameter);
        }
    }

}