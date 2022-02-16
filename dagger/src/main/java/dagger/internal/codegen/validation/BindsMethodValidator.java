package dagger.internal.codegen.validation;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: BindsMethodValidator
 * Author: 佛学徒
 * Date: 2021/10/26 8:54
 * Description:
 * History:
 */

import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.base.ContributionType;
import dagger.internal.codegen.base.SetType;
import dagger.internal.codegen.binding.BindsTypeChecker;
import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static dagger.internal.codegen.validation.BindingElementValidator.AllowsMultibindings.ALLOWS_MULTIBINDINGS;
import static dagger.internal.codegen.validation.BindingElementValidator.AllowsScoping.ALLOWS_SCOPING;
import static dagger.internal.codegen.validation.BindingMethodValidator.Abstractness.MUST_BE_ABSTRACT;
import static dagger.internal.codegen.validation.BindingMethodValidator.ExceptionSuperclass.NO_EXCEPTIONS;
import static dagger.internal.codegen.validation.TypeHierarchyValidator.validateTypeHierarchy;

/**
 * A validator for {@link dagger.Binds} methods.
 */
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
        super(
                elements,
                types,
                kotlinMetadataUtil,
                TypeNames.BINDS,
                ImmutableSet.of(TypeNames.MODULE, TypeNames.PRODUCER_MODULE),
                dependencyRequestValidator,
                MUST_BE_ABSTRACT,
                NO_EXCEPTIONS,
                ALLOWS_MULTIBINDINGS,
                ALLOWS_SCOPING,
                injectionAnnotations);

        this.types = types;
        this.bindsTypeChecker = bindsTypeChecker;
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
            //方法参数有且仅有一个，否则报错
            if (element.getParameters().size() != 1) {
                report.addError(
                        bindingMethods(
                                "must have exactly one parameter, whose type is assignable to the return type"));
            } else {
                super.checkParameters();
            }
        }

        @Override
        protected void checkParameter(VariableElement parameter) {
            super.checkParameter(parameter);

            TypeMirror leftHandSide = boxIfNecessary(element.getReturnType());
            TypeMirror rightHandSide = parameter.asType();
            ContributionType contributionType = ContributionType.fromBindingElement(element);
            //1.如果使用@Binds和@ElementsIntoSet修饰了方法，那么方法的返回类型必须是Set<T>，并且方法参数T
            if (contributionType.equals(ContributionType.SET_VALUES) && !SetType.isSet(leftHandSide)) {
                report.addError(
                        "@Binds @ElementsIntoSet methods must return a Set and take a Set parameter");
            }

            //2.ContributionType.UUNIQUE或SET或MAP，参数必须是返回类型的子类；
            if (!bindsTypeChecker.isAssignable(rightHandSide, leftHandSide, contributionType)) {
                // Validate the type hierarchy of both sides to make sure they're both valid.
                // If one of the types isn't valid it means we need to delay validation to the next round.
                // Note: BasicAnnotationProcessor only performs superficial validation on the referenced
                // types within the module. Thus, we're guaranteed that the types in the @Binds method are
                // valid, but it says nothing about their supertypes, which are needed for isAssignable.
                validateTypeHierarchy(leftHandSide, types);
                validateTypeHierarchy(rightHandSide, types);
                // TODO(ronshapiro): clarify this error message for @ElementsIntoSet cases, where the
                // right-hand-side might not be assignable to the left-hand-side, but still compatible with
                // Set.addAll(Collection<? extends E>)
                report.addError("@Binds methods' parameter type must be assignable to the return type");
            }
        }

        //如果是原始类型，则进行包装，例如int包装成Integer
        private TypeMirror boxIfNecessary(TypeMirror maybePrimitive) {
            if (maybePrimitive.getKind().isPrimitive()) {
                return types.boxedClass(MoreTypes.asPrimitiveType(maybePrimitive)).asType();
            }
            return maybePrimitive;
        }
    }

}
