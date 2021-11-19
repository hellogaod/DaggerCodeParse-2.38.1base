package dagger.internal.codegen.validation;

import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.auto.common.MoreTypes.asTypeElement;
import static dagger.internal.codegen.base.Keys.isValidImplicitProvisionKey;
import static dagger.internal.codegen.binding.InjectionAnnotations.injectedConstructors;
import static dagger.internal.codegen.validation.BindingElementValidator.AllowsMultibindings.NO_MULTIBINDINGS;
import static dagger.internal.codegen.validation.BindingElementValidator.AllowsScoping.NO_SCOPING;
import static dagger.internal.codegen.validation.BindingMethodValidator.Abstractness.MUST_BE_ABSTRACT;
import static dagger.internal.codegen.validation.BindingMethodValidator.ExceptionSuperclass.NO_EXCEPTIONS;

/**
 * A validator for {@link dagger.BindsOptionalOf} methods.
 */
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
        super(
                elements,
                types,
                kotlinMetadataUtil,
                TypeNames.BINDS_OPTIONAL_OF,
                ImmutableSet.of(TypeNames.MODULE, TypeNames.PRODUCER_MODULE),
                dependencyRequestValidator,
                MUST_BE_ABSTRACT,
                NO_EXCEPTIONS,
                NO_MULTIBINDINGS,
                NO_SCOPING,
                injectionAnnotations);

        this.types = types;
        this.injectionAnnotations = injectionAnnotations;
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
        protected void checkKeyType(TypeMirror keyType) {
            super.checkKeyType(keyType);

            //（keyType没有使用Qualifier注解修饰的注解修饰 && 节点泛型为空 ） && keyType存在构造函数使用了Inject注解
            //即使用@BindsOptionalOf 修饰的方法返回类型，如果该返回类型的构造方法使用了Inject注解修饰，
            // 那么要么还需要使用Qualifier注解修饰的注解修饰,要么使用泛型类型
            if (isValidImplicitProvisionKey(
                    injectionAnnotations.getQualifiers(element).stream().findFirst(),
                    keyType,
                    types)
                    && !injectedConstructors(asTypeElement(keyType)).isEmpty()
            ) {
                report.addError(
                        "@BindsOptionalOf methods cannot return unqualified types that have an @Inject-"
                                + "annotated constructor because those are always present");
            }
        }

        @Override
        protected void checkParameters() {
            //@BindsOptionalOf修饰的方法参数必须为空
            if (!element.getParameters().isEmpty()) {
                report.addError("@BindsOptionalOf methods cannot have parameters");
            }
        }
    }
}
