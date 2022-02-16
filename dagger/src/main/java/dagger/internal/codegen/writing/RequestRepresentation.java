package dagger.internal.codegen.writing;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.javapoet.Expression;

/** A factory of code expressions used to access a single request for a binding in a component. */
// TODO(bcorso): Rename this to RequestExpression?
abstract class RequestRepresentation {

    /**
     * Returns an expression that evaluates to the value of a request based on the given requesting
     * class.
     *
     * @param requestingClass the class that will contain the expression
     */
    abstract Expression getDependencyExpression(ClassName requestingClass);

    /**
     * Equivalent to {@link #getDependencyExpression} that is used only when the request is for an
     * implementation of a component method. By default, just delegates to {@link
     * #getDependencyExpression}.
     */
    Expression getDependencyExpressionForComponentMethod(
            ComponentDescriptor.ComponentMethodDescriptor componentMethod, ComponentImplementation component) {
        return getDependencyExpression(component.name());
    }

    /** Returns {@code true} if this binding expression should be encapsulated in a method. */
    boolean requiresMethodEncapsulation() {
        return false;
    }

    /**
     * Returns an expression for the implementation of a component method with the given request.
     *
     * @param component the component that will contain the implemented method
     */
    CodeBlock getComponentMethodImplementation(
            ComponentDescriptor.ComponentMethodDescriptor componentMethod, ComponentImplementation component) {
        // By default, just delegate to #getDependencyExpression().
        return CodeBlock.of(
                "return $L;",
                getDependencyExpressionForComponentMethod(componentMethod, component).codeBlock());
    }
}
