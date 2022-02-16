package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.FrameworkType;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.producers.internal.Producers;
import dagger.spi.model.Key;

/** Binding expression for producer node instances. */
final class ProducerNodeInstanceRequestRepresentation
        extends FrameworkInstanceRequestRepresentation {
    private final ComponentImplementation.ShardImplementation shardImplementation;
    private final Key key;
    private final ProducerEntryPointView producerEntryPointView;

    @AssistedInject
    ProducerNodeInstanceRequestRepresentation(
            @Assisted ContributionBinding binding,
            @Assisted FrameworkInstanceSupplier frameworkInstanceSupplier,
            DaggerTypes types,
            DaggerElements elements,
            ComponentImplementation componentImplementation) {
        super(binding, frameworkInstanceSupplier, types, elements);
        this.shardImplementation = componentImplementation.shardImplementation(binding);
        this.key = binding.key();
        this.producerEntryPointView = new ProducerEntryPointView(shardImplementation, types);
    }

    @Override
    protected FrameworkType frameworkType() {
        return FrameworkType.PRODUCER_NODE;
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {
        Expression result = super.getDependencyExpression(requestingClass);
        shardImplementation.addCancellation(
                key,
                CodeBlock.of(
                        "$T.cancel($L, $N);",
                        Producers.class,
                        result.codeBlock(),
                        ComponentImplementation.MAY_INTERRUPT_IF_RUNNING_PARAM));
        return result;
    }

    @Override
    Expression getDependencyExpressionForComponentMethod(
            ComponentDescriptor.ComponentMethodDescriptor componentMethod, ComponentImplementation component) {
        return producerEntryPointView
                .getProducerEntryPointField(this, componentMethod, component.name())
                .orElseGet(
                        () -> super.getDependencyExpressionForComponentMethod(componentMethod, component));
    }

    @AssistedFactory
    static interface Factory {
        ProducerNodeInstanceRequestRepresentation create(
                ContributionBinding binding,
                FrameworkInstanceSupplier frameworkInstanceSupplier
        );
    }
}
