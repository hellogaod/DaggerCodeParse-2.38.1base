package dagger.internal.codegen.validation;

import com.google.auto.common.MoreElements;

import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.binding.InjectionAnnotations;

import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.TYPEVAR;

//@BindsInstance修饰的参数校验
final class BindsInstanceParameterValidator extends BindsInstanceElementValidator<VariableElement> {

    @Inject
    BindsInstanceParameterValidator(InjectionAnnotations injectionAnnotations) {
        super(injectionAnnotations);
    }

    @Override
    protected ElementValidator elementValidator(VariableElement element) {
        return new Validator(element);
    }

    private class Validator extends ElementValidator {
        Validator(VariableElement element) {
            super(element);
        }

        @Override
        protected void checkAdditionalProperties() {

            Element enclosing = element.getEnclosingElement();

            //1.@BindsInstance修饰的参数必须是使用在方法中，否则报错
            if (!enclosing.getKind().equals(METHOD)) {
                report.addError(
                        "@BindsInstance should only be applied to methods or parameters of methods");
                return;
            }

            ExecutableElement method = MoreElements.asExecutable(enclosing);

            //2.@BindsInstance修饰的参数所在方法必须使用abstract修饰
            if (!method.getModifiers().contains(ABSTRACT)) {
                report.addError("@BindsInstance parameters may only be used in abstract methods");
            }

            //3.@BindsInstance修饰的参数所在方法的返回类型，只能是一个类或接口（可以是泛型），不能是void，数组又或者原始类型
            TypeKind returnKind = method.getReturnType().getKind();
            if (!(returnKind.equals(DECLARED) || returnKind.equals(TYPEVAR))) {
                report.addError(
                        "@BindsInstance parameters may not be used in methods with a void, array or primitive "
                                + "return type");
            }
        }

        @Override
        protected Optional<TypeMirror> bindingElementType() {
            return Optional.of(element.asType());
        }
    }
}
