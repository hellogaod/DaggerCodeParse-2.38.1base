package dagger.internal.codegen.validation;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ProducesMethodValidator
 * Author: 佛学徒
 * Date: 2021/10/26 8:54
 * Description:
 * History:
 */

import com.google.auto.common.MoreTypes;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.binding.ConfigurationAnnotations;
import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.validation.BindingElementValidator.AllowsMultibindings.ALLOWS_MULTIBINDINGS;
import static dagger.internal.codegen.validation.BindingElementValidator.AllowsScoping.NO_SCOPING;
import static dagger.internal.codegen.validation.BindingMethodValidator.Abstractness.MUST_BE_CONCRETE;
import static dagger.internal.codegen.validation.BindingMethodValidator.ExceptionSuperclass.EXCEPTION;

/** A validator for {@link dagger.producers.Produces} methods. */
final class ProducesMethodValidator extends BindingMethodValidator {

    @Inject
    ProducesMethodValidator(
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil kotlinMetadataUtil,
            DependencyRequestValidator dependencyRequestValidator,
            InjectionAnnotations injectionAnnotations) {
        super(
                elements,
                types,
                kotlinMetadataUtil,
                dependencyRequestValidator,
                TypeNames.PRODUCES,
                TypeNames.PRODUCER_MODULE,
                MUST_BE_CONCRETE,
                EXCEPTION,
                ALLOWS_MULTIBINDINGS,
                NO_SCOPING,
                injectionAnnotations);
    }


    @Override
    protected String elementsIntoSetNotASetMessage() {
        return "@Produces methods of type set values must return a Set or ListenableFuture of Set";
    }

    @Override
    protected String badTypeMessage() {
        return "@Produces methods can return only a primitive, an array, a type variable, "
                + "a declared type, or a ListenableFuture of one of those types";
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
            checkNullable();
        }

        /**
         * Adds a warning if a {@link dagger.producers.Produces @Produces} method is declared nullable.
         */
        // TODO(beder): Properly handle nullable with producer methods.
        private void checkNullable() {
            //element节点上的注解有Nullable及其值,提示警告
            if (ConfigurationAnnotations.getNullableType(element).isPresent()) {
                report.addWarning("@Nullable on @Produces methods does not do anything");
            }
        }

        /**
         * {@inheritDoc}
         *
         * <p>Allows {@code keyType} to be a {@link ListenableFuture} of an otherwise-valid key type.
         */
        @Override
        protected void checkKeyType(TypeMirror keyType) {
            Optional<TypeMirror> typeToCheck = unwrapListenableFuture(keyType);
            if (typeToCheck.isPresent()) {
                super.checkKeyType(typeToCheck.get());
            }
        }

        /**
         * {@inheritDoc}
         *
         * <p>Allows an {@link dagger.multibindings.ElementsIntoSet @ElementsIntoSet} or {@code
         * SET_VALUES} method to return a {@link ListenableFuture} of a {@link Set} as well.
         */
        @Override
        protected void checkSetValuesType() {
            Optional<TypeMirror> typeToCheck = unwrapListenableFuture(element.getReturnType());
            if (typeToCheck.isPresent()) {//ListenableFuture<T>形式的T进行校验
                checkSetValuesType(typeToCheck.get());
            }
        }

        private Optional<TypeMirror> unwrapListenableFuture(TypeMirror type) {
            //type是ListenableFuture类型
            if (MoreTypes.isType(type) && MoreTypes.isTypeOf(ListenableFuture.class, type)) {

                DeclaredType declaredType = MoreTypes.asDeclared(type);
                //如果这里没有使用泛型，则报错。必须使用ListenableFuture<T>形式
                if (declaredType.getTypeArguments().isEmpty()) {
                    report.addError("@Produces methods cannot return a raw ListenableFuture");
                    return Optional.empty();
                } else {
                    return Optional.of((TypeMirror) getOnlyElement(declaredType.getTypeArguments()));
                }
            }
            return Optional.of(type);
        }
    }
}
