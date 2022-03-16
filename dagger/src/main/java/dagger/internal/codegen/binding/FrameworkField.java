package dagger.internal.codegen.binding;

import com.google.auto.value.AutoValue;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor8;

import static dagger.spi.model.BindingKind.MEMBERS_INJECTOR;

/**
 * A value object that represents a field in the generated Component class.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>{@code Provider<String>}
 *   <li>{@code Producer<Widget>}
 *   <li>{@code Provider<Map<SomeMapKey, MapValue>>}.
 * </ul>
 */
@AutoValue
public abstract class FrameworkField {

    /**
     * Creates a framework field.
     * <p>
     * 1.使用frameworkClassName<valueTypeName>作为type类型；2.使用fieldName作为name
     *
     * @param frameworkClassName the name of the framework class (e.g., {@link javax.inject.Provider})
     * @param valueTypeName      the name of the type parameter of the framework class (e.g., {@code Foo}
     *                           for {@code Provider<Foo>}
     * @param fieldName          the name of the field
     */
    public static FrameworkField create(
            ClassName frameworkClassName,
            TypeName valueTypeName,
            String fieldName
    ) {
        //当前变量类型被frameworkClassName<valueTypeName> 包裹
        //如果名称后缀不存在suffix，那么给当前变量名 + suffix后缀，例如injectBindingRegistryImplProvider
        String suffix = frameworkClassName.simpleName();
        return new AutoValue_FrameworkField(
                ParameterizedTypeName.get(frameworkClassName, valueTypeName),
                fieldName.endsWith(suffix) ? fieldName : fieldName + suffix);
    }

    /**
     * A framework field for a {@link ContributionBinding}.
     *
     * @param frameworkClass if present, the field will use this framework class instead of the normal
     *                       one for the binding's type.
     */
    public static FrameworkField forBinding(
            ContributionBinding binding,
            Optional<ClassName> frameworkClass
    ) {
        //使用frameworkClass包裹当前binding的key的type，e.g.Provider<T>
        //命名规则在create方法中处理
        return create(
                //根据binding.bindingType确定使用哪一种框架类型
                frameworkClass.orElse(
                        //binding.bindingType()决定FrameworkType，FrameworkType决定frameworkClass
                        ClassName.get(
                                FrameworkType.forBindingType(binding.bindingType()).frameworkClass()
                        )),
                //被包裹的value
                TypeName.get(fieldValueType(binding)),

                frameworkFieldName(binding));
    }

    //如果是多重绑定，那么返回binding.key()的实际类型，例如Map<K,V>,则返回V；否则返回binding.key().type().java()即可；
    private static TypeMirror fieldValueType(ContributionBinding binding) {
        return binding.contributionType().isMultibinding()
                ? binding.contributedType()
                : binding.key().type().java();
    }

    private static String frameworkFieldName(ContributionBinding binding) {

        //如果绑定节点存，如果是注入成员，绑定节点 + "MembersInjector"；否则直接返回绑定节点
        if (binding.bindingElement().isPresent()) {
            String name = BINDING_ELEMENT_NAME.visit(binding.bindingElement().get(), binding);
            return binding.kind().equals(MEMBERS_INJECTOR) ? name + "MembersInjector" : name;
        }
        //如果绑定节点不存在，对绑定key进行key命名
        return KeyVariableNamer.name(binding.key());
    }

    //命名绑定节点
    private static final ElementVisitor<String, Binding> BINDING_ELEMENT_NAME =
            new ElementKindVisitor8<String, Binding>() {

                @Override
                protected String defaultAction(Element e, Binding p) {
                    throw new IllegalArgumentException("Unexpected binding " + p);
                }

                @Override
                public String visitExecutableAsConstructor(ExecutableElement e, Binding p) {//构造函数
                    return visit(e.getEnclosingElement(), p);
                }

                @Override
                public String visitExecutableAsMethod(ExecutableElement e, Binding p) {//普通方法
                    return e.getSimpleName().toString();
                }

                @Override
                public String visitType(TypeElement e, Binding p) {//类或接口
                    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, e.getSimpleName().toString());
                }

                @Override
                public String visitVariableAsParameter(VariableElement e, Binding p) {//参数
                    return e.getSimpleName().toString();
                }
            };


    public abstract ParameterizedTypeName type();

    public abstract String name();
}
