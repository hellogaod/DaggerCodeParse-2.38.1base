package dagger.internal.codegen.javapoet;


import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.util.stream.Collector;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static dagger.internal.codegen.javapoet.TypeNames.providerOf;
import static java.util.stream.StreamSupport.stream;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Convenience methods for creating {@link CodeBlock}s.
 */
public final class CodeBlocks {
    /**
     * Joins {@link CodeBlock} instances in a manner suitable for use as method parameters (or
     * arguments).
     * <p>
     * 使用逗号隔开集合信息
     */
    public static Collector<CodeBlock, ?, CodeBlock> toParametersCodeBlock() {
        // TODO(ronshapiro,jakew): consider adding zero-width spaces to help line breaking when the
        // formatter is off. If not, inline this
        return CodeBlock.joining(", ");
    }

    /**
     * Returns {@code expression} cast to a type.
     */
    public static CodeBlock cast(CodeBlock expression, ClassName castTo) {
        return CodeBlock.of("($T) $L", castTo, expression);
    }

    /**
     * Returns {@code expression} cast to a type.
     */
    public static CodeBlock cast(CodeBlock expression, Class<?> castTo) {
        return CodeBlock.of("($T) $L", castTo, expression);
    }

    public static CodeBlock type(TypeMirror type) {
        return CodeBlock.of("$T", type);
    }

    /**
     * Returns one unified {@link CodeBlock} which joins each item in {@code codeBlocks} with a
     * newline.
     */
    public static CodeBlock concat(Iterable<CodeBlock> codeBlocks) {
        return stream(codeBlocks.spliterator(), false).collect(toConcatenatedCodeBlock());
    }

    /**
     * Returns an anonymous {@link javax.inject.Provider} class with the single {@link
     * javax.inject.Provider#get()} method that returns the given {@code expression}.
     */
    public static CodeBlock anonymousProvider(Expression expression) {
        // More of a precondition check that the type Provider is parameterized with is a DeclaredType
        DeclaredType type = MoreTypes.asDeclared(expression.type());
        return anonymousProvider(
                TypeName.get(type), CodeBlock.of("return $L;", expression.codeBlock()));
    }

    /**
     * Returns an anonymous {@link javax.inject.Provider} class with the single {@link
     * javax.inject.Provider#get()} method implemented by {@code body}.
     */
    public static CodeBlock anonymousProvider(TypeName providedType, CodeBlock body) {
        return CodeBlock.of(
                "$L",
                anonymousClassBuilder("")
                        .superclass(providerOf(providedType))
                        .addMethod(
                                methodBuilder("get")
                                        .addAnnotation(Override.class)
                                        .addModifiers(PUBLIC)
                                        .returns(providedType)
                                        .addCode(body)
                                        .build())
                        .build());
    }

    /**
     * Returns a comma-separated {@link CodeBlock} using the name of every parameter in {@code
     * parameters}.
     */
    public static CodeBlock parameterNames(Iterable<ParameterSpec> parameters) {
        // TODO(ronshapiro): Add DaggerStreams.stream(Iterable)
        return stream(parameters.spliterator(), false)
                .map(p -> CodeBlock.of("$N", p))
                .collect(toParametersCodeBlock());
    }

    /**
     * Concatenates {@link CodeBlock} instances separated by newlines for readability.
     */
    public static Collector<CodeBlock, ?, CodeBlock> toConcatenatedCodeBlock() {
        return CodeBlock.joining("\n", "", "\n");
    }

    /**
     * Returns a comma-separated version of {@code codeBlocks} as one unified {@link CodeBlock}.
     */
    public static CodeBlock makeParametersCodeBlock(Iterable<CodeBlock> codeBlocks) {
        return stream(codeBlocks.spliterator(), false).collect(toParametersCodeBlock());
    }

    /**
     * Adds an annotation to a method.
     */
    public static void addAnnotation(MethodSpec.Builder method, DeclaredType nullableType) {
        method.addAnnotation(ClassName.get(MoreTypes.asTypeElement(nullableType)));
    }
}
