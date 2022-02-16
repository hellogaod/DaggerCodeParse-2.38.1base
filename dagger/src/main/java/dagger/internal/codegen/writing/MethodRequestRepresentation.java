package dagger.internal.codegen.writing;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerTypes;

/** A binding expression that wraps another in a nullary method on the component. */
abstract class MethodRequestRepresentation extends RequestRepresentation {
    private final ComponentImplementation.ShardImplementation shardImplementation;
    private final ProducerEntryPointView producerEntryPointView;

    protected MethodRequestRepresentation(
            ComponentImplementation.ShardImplementation shardImplementation, DaggerTypes types) {
        this.shardImplementation = shardImplementation;
        this.producerEntryPointView = new ProducerEntryPointView(shardImplementation, types);
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {

        return Expression.create(
                returnType(),
                requestingClass.equals(shardImplementation.name())
                        ? methodCall()
                        : CodeBlock.of("$L.$L", shardImplementation.shardFieldReference(), methodCall()));
    }

    @Override
    Expression getDependencyExpressionForComponentMethod(
            ComponentDescriptor.ComponentMethodDescriptor componentMethod, ComponentImplementation component) {
        return producerEntryPointView
                .getProducerEntryPointField(this, componentMethod, component.name())
                .orElseGet(
                        () -> super.getDependencyExpressionForComponentMethod(componentMethod, component));
    }

    /** Returns the return type for the dependency request. */
    protected abstract TypeMirror returnType();

    /** Returns the method call. */
    protected abstract CodeBlock methodCall();
}
