package dagger.internal.codegen.validation;


import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.base.MapType;
import dagger.internal.codegen.base.SetType;
import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static dagger.internal.codegen.base.FrameworkTypes.isFrameworkType;
import static dagger.internal.codegen.validation.BindingElementValidator.AllowsMultibindings.NO_MULTIBINDINGS;
import static dagger.internal.codegen.validation.BindingElementValidator.AllowsScoping.NO_SCOPING;
import static dagger.internal.codegen.validation.BindingMethodValidator.Abstractness.MUST_BE_ABSTRACT;
import static dagger.internal.codegen.validation.BindingMethodValidator.ExceptionSuperclass.NO_EXCEPTIONS;

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
        super(
                elements,
                types,
                kotlinMetadataUtil,
                TypeNames.MULTIBINDS,
                ImmutableSet.of(TypeNames.MODULE, TypeNames.PRODUCER_MODULE),
                dependencyRequestValidator,
                MUST_BE_ABSTRACT,
                NO_EXCEPTIONS,
                NO_MULTIBINDINGS,
                NO_SCOPING,
                injectionAnnotations);
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
        protected void checkParameters() {
            //??????????????????
            if (!element.getParameters().isEmpty()) {
                report.addError(bindingMethods("cannot have parameters"));
            }
        }

        /** Adds an error unless the method returns a {@code Map<K, V>} or {@code Set<T>}. */
        @Override
        protected void checkType() {
            //Map<K, V> or Set<T>?????????V???T?????????FrameworkType?????????Provider,Lazy...???
            if (!isPlainMap(element.getReturnType())
                    && !isPlainSet(element.getReturnType())) {
                report.addError(bindingMethods("must return Map<K, V> or Set<T>"));
            }
        }

        //?????????????????????Map<K,V>?????????V?????????FrameworkType?????????Provider,Lazy...????????????true
        private boolean isPlainMap(TypeMirror returnType) {
            if (!MapType.isMap(returnType)) {
                return false;
            }
            MapType mapType = MapType.from(returnType);
            return !mapType.isRawType()
                    && MoreTypes.isType(mapType.valueType()) // No wildcards.
                    && !isFrameworkType(mapType.valueType());
        }

        //?????????????????????Set<T>?????????T?????????FrameworkType?????????Provider,Lazy...????????????true
        private boolean isPlainSet(TypeMirror returnType) {
            //??????Set???????????????false
            if (!SetType.isSet(returnType)) {
                return false;
            }
            SetType setType = SetType.from(returnType);
            return !setType.isRawType()//??????????????????
                    && MoreTypes.isType(setType.elementType()) // No wildcards.
                    && !isFrameworkType(setType.elementType());//
        }
    }
}
