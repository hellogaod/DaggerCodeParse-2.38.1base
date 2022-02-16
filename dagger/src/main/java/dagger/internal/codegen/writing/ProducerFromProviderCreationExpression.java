package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import java.util.Optional;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.FrameworkType;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.producers.Producer;
import dagger.spi.model.RequestKind;

import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.BindingRequest.bindingRequest;

/**
 * An {@link Producer} creation expression for provision bindings.
 */
final class ProducerFromProviderCreationExpression implements FrameworkFieldInitializer.FrameworkInstanceCreationExpression {
    private final ContributionBinding binding;
    private final ComponentImplementation componentImplementation;
    private final ComponentRequestRepresentations componentRequestRepresentations;

    @AssistedInject
    ProducerFromProviderCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations) {
        this.binding = checkNotNull(binding);
        this.componentImplementation = componentImplementation;
        this.componentRequestRepresentations = componentRequestRepresentations;
    }

    @Override
    public CodeBlock creationExpression() {
        return FrameworkType.PROVIDER.to(
                RequestKind.PRODUCER,
                componentRequestRepresentations
                        .getDependencyExpression(
                                bindingRequest(binding.key(), FrameworkType.PROVIDER),
                                componentImplementation.shardImplementation(binding).name())
                        .codeBlock());
    }

    @Override
    public Optional<ClassName> alternativeFrameworkClass() {
        return Optional.of(TypeNames.PRODUCER);
    }

    @AssistedFactory
    static interface Factory {
        ProducerFromProviderCreationExpression create(ContributionBinding binding);
    }
}

