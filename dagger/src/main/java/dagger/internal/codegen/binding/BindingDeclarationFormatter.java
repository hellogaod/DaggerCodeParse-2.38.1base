package dagger.internal.codegen.binding;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import dagger.internal.codegen.base.Formatter;

import static com.google.common.collect.Sets.immutableEnumSet;
import static dagger.internal.codegen.base.DiagnosticFormatting.stripCommonTypePrefixes;
import static dagger.internal.codegen.base.ElementFormatter.elementToString;
import static dagger.internal.codegen.base.Formatter.formatArgumentInList;
import static javax.lang.model.element.ElementKind.PARAMETER;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.EXECUTABLE;

/**
 * Formats a {@link BindingDeclaration} into a {@link String} suitable for use in error messages.
 */
public class BindingDeclarationFormatter extends Formatter<BindingDeclaration> {
    private static final ImmutableSet<TypeKind> FORMATTABLE_ELEMENT_TYPE_KINDS =
            immutableEnumSet(EXECUTABLE, DECLARED);

    private final MethodSignatureFormatter methodSignatureFormatter;

    @Inject
    BindingDeclarationFormatter(MethodSignatureFormatter methodSignatureFormatter) {
        this.methodSignatureFormatter = methodSignatureFormatter;
    }

    /**
     * Returns {@code true} for declarations that this formatter can format. Specifically bindings
     * from subcomponent declarations or those with {@linkplain BindingDeclaration#bindingElement()
     * binding elements} that are methods, constructors, or types.
     * <p>
     * 对于此格式化程序可以格式化的声明，返回 {@code true}
     */
    public boolean canFormat(BindingDeclaration bindingDeclaration) {

        //是Subcomponent声明返回true
        if (bindingDeclaration instanceof SubcomponentDeclaration) {
            return true;
        }

        //如果声明的绑定节点存在，当绑定类型是方法或类或接口或者是方法参数类型，返回true
        if (bindingDeclaration.bindingElement().isPresent()) {

            Element bindingElement = bindingDeclaration.bindingElement().get();
            return bindingElement.getKind().equals(PARAMETER)
                    || FORMATTABLE_ELEMENT_TYPE_KINDS.contains(bindingElement.asType().getKind());
        }
        // TODO(dpb): validate whether what this is doing is correct
        return false;
    }

    @Override
    public String format(BindingDeclaration bindingDeclaration) {

        if (bindingDeclaration instanceof SubcomponentDeclaration) {
            return formatSubcomponentDeclaration((SubcomponentDeclaration) bindingDeclaration);
        }

        if (bindingDeclaration.bindingElement().isPresent()) {

            Element bindingElement = bindingDeclaration.bindingElement().get();

            //如果方法参数，返回的格式化字符串
            if (bindingElement.getKind().equals(PARAMETER)) {
                return elementToString(bindingElement);
            }

            switch (bindingElement.asType().getKind()) {
                case EXECUTABLE:
                    return methodSignatureFormatter.format(
                            MoreElements.asExecutable(bindingElement),
                            bindingDeclaration
                                    .contributingModule()
                                    .map(module -> MoreTypes.asDeclared(module.asType())));

                case DECLARED:
                    return stripCommonTypePrefixes(bindingElement.asType().toString());

                default:
                    throw new IllegalArgumentException(
                            "Formatting unsupported for element: " + bindingElement);
            }
        }

        return String.format(
                "Dagger-generated binding for %s",
                stripCommonTypePrefixes(bindingDeclaration.key().toString()));
    }

    private String formatSubcomponentDeclaration(SubcomponentDeclaration subcomponentDeclaration) {

        //当前subcomponentDeclaration所在的(Producer)Moduel#subcomponent里面所有的值
        ImmutableList<TypeElement> moduleSubcomponents =
                subcomponentDeclaration.moduleAnnotation().subcomponents();

        //当前subcomponentDeclaration在moduleSubcomponents的位置
        int index = moduleSubcomponents.indexOf(subcomponentDeclaration.subcomponentType());

        StringBuilder annotationValue = new StringBuilder();
        if (moduleSubcomponents.size() != 1) {
            annotationValue.append("{");
        }
        annotationValue.append(
                formatArgumentInList(
                        index,
                        moduleSubcomponents.size(),
                        subcomponentDeclaration.subcomponentType().getQualifiedName() + ".class"));
        if (moduleSubcomponents.size() != 1) {
            annotationValue.append("}");
        }

        return String.format(
                "@%s(subcomponents = %s) for %s",
                subcomponentDeclaration.moduleAnnotation().annotationName(),
                annotationValue,
                subcomponentDeclaration.contributingModule().get());
    }
}
