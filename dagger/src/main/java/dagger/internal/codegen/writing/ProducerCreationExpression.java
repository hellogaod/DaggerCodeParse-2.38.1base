package dagger.internal.codegen.writing;

import com.squareup.javapoet.CodeBlock;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;

import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.SourceFiles.generatedClassNameForBinding;

/**
 * A {@link dagger.producers.Producer} creation expression for a {@link
 * dagger.producers.Produces @Produces}-annotated module method.
 */
// TODO(dpb): Resolve with InjectionOrProvisionProviderCreationExpression.
final class ProducerCreationExpression implements FrameworkFieldInitializer.FrameworkInstanceCreationExpression {

    private final ComponentImplementation.ShardImplementation shardImplementation;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final ContributionBinding binding;

    @AssistedInject
    ProducerCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations) {
        this.binding = checkNotNull(binding);
        this.shardImplementation = componentImplementation.shardImplementation(binding);
        this.componentRequestRepresentations = checkNotNull(componentRequestRepresentations);
    }

    @Override
    public CodeBlock creationExpression() {
        return CodeBlock.of(
                "$T.create($L)",
                generatedClassNameForBinding(binding),
                componentRequestRepresentations.getCreateMethodArgumentsCodeBlock(
                        binding, shardImplementation.name()));
    }

    @AssistedFactory
    static interface Factory {
        ProducerCreationExpression create(ContributionBinding binding);
    }
}
