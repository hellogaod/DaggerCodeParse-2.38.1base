package dagger.internal.codegen.validation;


import java.util.List;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import dagger.MapKey;
import dagger.internal.codegen.langmodel.DaggerElements;

import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * A validator for {@link MapKey} annotations.
 * <p>
 * 对使用MapKey注解的节点（其实该节点还是一个注解）的校验，校验入口validate方法
 */
// TODO(dpb,gak): Should unwrapped MapKeys be required to have their single member be named "value"?
public final class MapKeyValidator {
    private final DaggerElements elements;

    @Inject
    MapKeyValidator(DaggerElements elements) {
        this.elements = elements;
    }

    //校验入口，使用MapKey注解修饰的注解规则如下，
    //1.被修饰的注解类必须有方法
    //2.①如果MapKey.unwrapValue() = true的情况下，被修饰的注解类方法有且仅有一个，并且该方法的返回类型不可以是TypeKind.ARRAY数组；
    //  ②如果MapKey.unwrapValue() = false，被修饰的注解类方法可以有多个，并且当前项目必须引用com.google.auto.value.AutoAnnotation,
    public ValidationReport validate(Element element) {
        ValidationReport.Builder builder = ValidationReport.about(element);
        List<ExecutableElement> members = methodsIn(((TypeElement) element).getEnclosedElements());

        if (members.isEmpty()) {
            builder.addError("Map key annotations must have members", element);
        } else if (element.getAnnotation(MapKey.class).unwrapValue()) {
            if (members.size() > 1) {
                builder.addError(
                        "Map key annotations with unwrapped values must have exactly one member", element);
            } else if (members.get(0).getReturnType().getKind() == TypeKind.ARRAY) {
                builder.addError("Map key annotations with unwrapped values cannot use arrays", element);
            }
        } else if (autoAnnotationIsMissing()) {//必须有@MapKey(unwrapValue = false)写法
            builder.addError(
                    "@AutoAnnotation is a necessary dependency if @MapKey(unwrapValue = false). Add a "
                            + "dependency on com.google.auto.value:auto-value:<current version>");
        }
        return builder.build();
    }

    private boolean autoAnnotationIsMissing() {
        return elements.getTypeElement("com.google.auto.value.AutoAnnotation") == null;
    }
}
