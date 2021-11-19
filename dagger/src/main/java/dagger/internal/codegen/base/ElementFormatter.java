package dagger.internal.codegen.base;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementKindVisitor8;

import static com.google.auto.common.MoreElements.asExecutable;
import static java.util.stream.Collectors.joining;

/**
 * Formats elements into a useful string representation.
 *
 * <p>Elements directly enclosed by a type are preceded by the enclosing type's qualified name.
 *
 * <p>Parameters are given with their enclosing executable, with other parameters elided.
 * <p>
 * <p>
 * 核心点在ELEMENT_TO_STRING，相当于if else判断节点类型，如果存在的情况下还会从当前类一级级遍历父类
 */
public final class ElementFormatter extends Formatter<Element> {
    @Inject
    ElementFormatter() {
    }

    @Override
    public String format(Element element) {
        return elementToString(element);
    }

    /**
     * Returns a useful string form for an element.
     *
     * <p>Elements directly enclosed by a type are preceded by the enclosing type's qualified name.
     *
     * <p>Parameters are given with their enclosing executable, with other parameters elided.
     */
    public static String elementToString(Element element) {
        return element.accept(ELEMENT_TO_STRING, null);
    }

    //访问节点信息（节点即类或接口），例如visitExecutable表示访问节点的方法，visitVariableAsParameter访问节点的方法参数等，具体看下面的方法
    private static final ElementVisitor<String, Void> ELEMENT_TO_STRING =
            new ElementKindVisitor8<String, Void>() {

                @Override
                public String visitExecutable(ExecutableElement executableElement, Void aVoid) {
                    //访问方法，返回信息："方法所在类" ."方法名(方法参数以逗号隔开)"

                    return enclosingTypeAndMemberName(executableElement)
                            .append(
                                    executableElement.getParameters().stream()
                                            .map(parameter -> parameter.asType().toString())
                                            .collect(joining(", ", "(", ")")))
                            .toString();
                }

                @Override
                public String visitVariableAsParameter(VariableElement parameter, Void aVoid) {
                    //访问类中的方法参数，返回信息： "所在类"."方法名(…,xxx,…)"
                    ExecutableElement methodOrConstructor = asExecutable(parameter.getEnclosingElement());
                    return enclosingTypeAndMemberName(methodOrConstructor)
                            .append('(')
                            .append(
                                    formatArgumentInList(
                                            methodOrConstructor.getParameters().indexOf(parameter),//表示当前参数所在方法的位置,假设在中间，前后都有参数
                                            methodOrConstructor.getParameters().size(),
                                            parameter.getSimpleName()))
                            .append(')')
                            .toString();
                }


                @Override
                public String visitVariableAsField(VariableElement field, Void aVoid) {
                    //访问类中的变量，返回信息： “所在类”."变量名"
                    return enclosingTypeAndMemberName(field).toString();
                }

                @Override
                public String visitType(TypeElement type, Void aVoid) {
                    //访问类或接口类型，返回当前类或接口的名称

                    return type.getQualifiedName().toString();
                }

                @Override
                protected String defaultAction(Element element, Void aVoid) {//除了以上的访问，剩余的行为

                    throw new UnsupportedOperationException(
                            "Can't determine string for " + element.getKind() + " element " + element);
                }

                private StringBuilder enclosingTypeAndMemberName(Element element) {
                    //找当前节点的父节点执行当前ElementKindVisitor8类里面的方法
                    //这里的name通过以上的使用得知，其实获取的就是类名
                    StringBuilder name = new StringBuilder(element.getEnclosingElement().accept(this, null));

                    //非<init>情况下，使用 name. 节点名称(如方法名或变量名)
                    if (!element.getSimpleName().contentEquals("<init>")) {
                        name.append('.').append(element.getSimpleName());
                    }
                    return name;
                }
            };
}
