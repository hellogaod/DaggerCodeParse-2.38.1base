package dagger.internal.codegen.writing;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import dagger.internal.codegen.binding.ComponentRequirement;

/**
 * A factory for expressions of {@link ComponentRequirement}s in the generated component. This is
 * <em>not</em> a {@link RequestRepresentation}, since {@link ComponentRequirement}s do not have a
 * {@link dagger.spi.model.Key}. See {@link ComponentRequirementRequestRepresentation} for binding
 * expressions that are themselves a component requirement.
 */
interface ComponentRequirementExpression {
    /**
     * Returns an expression for the {@link ComponentRequirement} to be used when implementing a
     * component method. This may add a field or method to the component in order to reference the
     * component requirement outside of the {@code initialize()} methods.
     */
    CodeBlock getExpression(ClassName requestingClass);

    /**
     * Returns an expression for the {@link ComponentRequirement} to be used only within {@code
     * initialize()} methods, where the constructor parameters are available.
     *
     * <p>When accessing this expression from a subcomponent, this may cause a field to be initialized
     * or a method to be added in the component that owns this {@link ComponentRequirement}.
     */
    default CodeBlock getExpressionDuringInitialization(ClassName requestingClass) {
        return getExpression(requestingClass);
    }

    /**
     * Returns the expression for the {@link ComponentRequirement} to be used when reimplementing a
     * modifiable module method.
     */
    default CodeBlock getModifiableModuleMethodExpression(ClassName requestingClass) {
        return CodeBlock.of("return $L", getExpression(requestingClass));
    }
}
