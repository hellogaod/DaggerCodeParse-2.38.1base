package dagger.internal.codegen.validation;

import com.google.auto.common.MoreElements;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.base.ComponentAnnotation;
import dagger.internal.codegen.base.ModuleAnnotation;
import dagger.internal.codegen.binding.InjectionAnnotations;

import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.base.ModuleAnnotation.moduleAnnotation;
import static javax.lang.model.element.Modifier.ABSTRACT;

//@BindsInstance注解修饰的方法校验
final class BindsInstanceMethodValidator extends BindsInstanceElementValidator<ExecutableElement> {
    @Inject
    BindsInstanceMethodValidator(InjectionAnnotations injectionAnnotations) {
        super(injectionAnnotations);
    }


    @Override
    protected ElementValidator elementValidator(ExecutableElement element) {
        return new Validator(element);
    }

    private class Validator extends ElementValidator {

        Validator(ExecutableElement element) {
            super(element);
        }

        @Override
        protected void checkAdditionalProperties() {

            //element方法（@BindsInstance修饰的方法）必须使用abstract修饰
            if (!element.getModifiers().contains(ABSTRACT)) {
                report.addError("@BindsInstance methods must be abstract");
            }

            //element方法（@BindsInstance修饰的方法）有且仅有一个参数
            if (element.getParameters().size() != 1) {
                report.addError(
                        "@BindsInstance methods should have exactly one parameter for the bound type");
            }

            // @BindsInstance修饰的方法所在父节点不允许是module节点，也不允许是componentAll节点；
            TypeElement enclosingType = MoreElements.asType(element.getEnclosingElement());
            moduleAnnotation(enclosingType)
                    .ifPresent(moduleAnnotation -> report.addError(didYouMeanBinds(moduleAnnotation)));

            ComponentAnnotation.anyComponentAnnotation(enclosingType)
                    .ifPresent(
                            componentAnnotation ->
                                    report.addError(
                                            String.format(
                                                    "@BindsInstance methods should not be included in @%1$ss. "
                                                            + "Did you mean to put it in a @%1$s.Builder?",
                                                    componentAnnotation.simpleName())));
        }

        @Override
        protected Optional<TypeMirror> bindingElementType() {
            //绑定节点类型：@BindsInstance修饰的方法参数有且仅有一个，使用当前参数类型

            List<? extends VariableElement> parameters =
                    MoreElements.asExecutable(element).getParameters();
            return parameters.size() == 1
                    ? Optional.of(getOnlyElement(parameters).asType())
                    : Optional.empty();
        }
    }

    private static String didYouMeanBinds(ModuleAnnotation moduleAnnotation) {
        return String.format(
                "@BindsInstance methods should not be included in @%ss. Did you mean @Binds?",
                moduleAnnotation.annotationName());
    }
}
