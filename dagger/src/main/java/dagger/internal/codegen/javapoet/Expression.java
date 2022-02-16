package dagger.internal.codegen.javapoet;


import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Encapsulates a {@link CodeBlock} for an <a
 * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html">expression</a> and the
 * {@link TypeMirror} that it represents from the perspective of the compiler. Consider the
 * following example:
 *
 * <pre><code>
 *   {@literal @SuppressWarnings("rawtypes")}
 *   private Provider fooImplProvider = DoubleCheck.provider(FooImpl_Factory.create());
 * </code></pre>
 *
 * <p>An {@code Expression} for {@code fooImplProvider.get()} would have a {@link #type()} of {@code
 * java.lang.Object} and not {@code FooImpl}.
 */
public final class Expression {

    private final TypeMirror type;
    private final CodeBlock codeBlock;

    private Expression(TypeMirror type, CodeBlock codeBlock) {
        this.type = type;
        this.codeBlock = codeBlock;
    }

    /**
     * Creates a new {@link Expression} with a {@link TypeMirror} and {@link CodeBlock}.
     */
    public static Expression create(TypeMirror type, CodeBlock expression) {
        return new Expression(type, expression);
    }

    /**
     * Creates a new {@link Expression} with a {@link TypeMirror}, {@linkplain CodeBlock#of(String,
     * Object[]) format, and arguments}.
     */
    public static Expression create(TypeMirror type, String format, Object... args) {
        return create(type, CodeBlock.of(format, args));
    }

    /**
     * Returns a new expression that casts the current expression to {@code newType}.
     */
    // TODO(ronshapiro): consider overloads that take a Types and Elements and only cast if necessary,
    // or just embedding a Types/Elements instance in an Expression.
    public Expression castTo(TypeMirror newType) {
        return create(newType, "($T) $L", newType, codeBlock);
    }

    /**
     * Returns a new expression that {@link #castTo(TypeMirror)} casts the current expression to its
     * boxed type if this expression has a primitive type.
     */
    public Expression box(DaggerTypes types) {
        return type.getKind().isPrimitive()
                ? castTo(types.boxedClass(MoreTypes.asPrimitiveType(type)).asType())
                : this;
    }

    /** The {@link TypeMirror type} to which the expression evaluates. */
    public TypeMirror type() {
        return type;
    }

    /** The code of the expression. */
    public CodeBlock codeBlock() {
        return codeBlock;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", type, codeBlock);
    }
}
