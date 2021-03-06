package dagger.internal.codegen.binding;


import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValuesWithDefaults;
import static dagger.internal.codegen.binding.SourceFiles.classFileName;
import static dagger.internal.codegen.javapoet.CodeBlocks.makeParametersCodeBlock;
import static java.util.stream.Collectors.toList;

/**
 * Returns an expression creating an instance of the visited annotation type. Its parameter must be
 * a class as generated by {@link dagger.internal.codegen.writing.AnnotationCreatorGenerator}.
 *
 * <p>Note that {@link AnnotationValue#toString()} is the source-code representation of the value
 * <em>when used in an annotation</em>, which is not always the same as the representation needed
 * when creating the value in a method body.
 *
 * <p>For example, inside an annotation, a nested array of {@code int}s is simply {@code {1, 2, 3}},
 * but in code it would have to be {@code new int[] {1, 2, 3}}.
 */
public class AnnotationExpression
        extends SimpleAnnotationValueVisitor6<CodeBlock, AnnotationValue> {

    private final AnnotationMirror annotation;
    private final ClassName creatorClass;

    AnnotationExpression(AnnotationMirror annotation) {
        this.annotation = annotation;
        this.creatorClass =
                getAnnotationCreatorClassName(
                        MoreTypes.asTypeElement(annotation.getAnnotationType()));
    }


    /**
     * Returns the name of the generated class that contains the static {@code create} methods for an
     * annotation type.
     */
    public static ClassName getAnnotationCreatorClassName(TypeElement annotationType) {
        ClassName annotationTypeName = ClassName.get(annotationType);
        return annotationTypeName
                .topLevelClassName()
                .peerClass(classFileName(annotationTypeName) + "Creator");
    }

    public static String createMethodName(TypeElement annotationType) {
        return "create" + annotationType.getSimpleName();
    }

    /**
     * Returns an expression that evaluates to a {@code value} of a given type on an {@code
     * annotation}.
     */
    CodeBlock getValueExpression(TypeMirror valueType, AnnotationValue value) {
        return ARRAY_LITERAL_PREFIX.visit(valueType, this.visit(value, value));
    }

    /**
     * Returns an expression that calls static methods on the annotation's creator class to create an
     * annotation instance equivalent the annotation passed to the constructor.
     */
    CodeBlock getAnnotationInstanceExpression() {
        return getAnnotationInstanceExpression(annotation);
    }

    private CodeBlock getAnnotationInstanceExpression(AnnotationMirror annotation) {
        return CodeBlock.of(
                "$T.$L($L)",
                creatorClass,
                createMethodName(
                        MoreElements.asType(annotation.getAnnotationType().asElement())),
                makeParametersCodeBlock(
                        getAnnotationValuesWithDefaults(annotation)
                                .entrySet()
                                .stream()
                                .map(entry -> getValueExpression(entry.getKey().getReturnType(), entry.getValue()))
                                .collect(toList())));
    }

    /**
     * If the visited type is an array, prefixes the parameter code block with {@code new T[]}, where
     * {@code T} is the raw array component type.
     */
    private static final SimpleTypeVisitor6<CodeBlock, CodeBlock> ARRAY_LITERAL_PREFIX =
            new SimpleTypeVisitor6<CodeBlock, CodeBlock>() {

                @Override
                public CodeBlock visitArray(ArrayType t, CodeBlock p) {
                    return CodeBlock.of("new $T[] $L", RAW_TYPE_NAME.visit(t.getComponentType()), p);
                }

                @Override
                protected CodeBlock defaultAction(TypeMirror e, CodeBlock p) {
                    return p;
                }
            };

    /**
     * If the visited type is an array, returns the name of its raw component type; otherwise returns
     * the name of the type itself.
     */
    private static final SimpleTypeVisitor6<TypeName, Void> RAW_TYPE_NAME =
            new SimpleTypeVisitor6<TypeName, Void>() {
                @Override
                public TypeName visitDeclared(DeclaredType t, Void p) {
                    return ClassName.get(MoreTypes.asTypeElement(t));
                }

                @Override
                protected TypeName defaultAction(TypeMirror e, Void p) {
                    return TypeName.get(e);
                }
            };
}
