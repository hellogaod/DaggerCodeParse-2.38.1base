package dagger.internal.codegen.javapoet;


import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

/**
 * Convenience methods for use with JavaPoet's {@link TypeSpec}.
 */
public final class TypeSpecs {

    /**
     * If {@code supertype} is a class, adds it as a superclass for {@code typeBuilder}; if it is an
     * interface, adds it as a superinterface.
     *
     * @return {@code typeBuilder}
     */
    @CanIgnoreReturnValue
    public static TypeSpec.Builder addSupertype(TypeSpec.Builder typeBuilder, TypeElement supertype) {
        switch (supertype.getKind()) {
            case CLASS:
                return typeBuilder.superclass(ClassName.get(supertype));
            case INTERFACE:
                return typeBuilder.addSuperinterface(ClassName.get(supertype));
            default:
                throw new AssertionError(supertype + " is neither a class nor an interface.");
        }
    }

    private TypeSpecs() {
    }
}

