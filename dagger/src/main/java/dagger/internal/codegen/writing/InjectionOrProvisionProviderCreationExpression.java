package dagger.internal.codegen.writing;

import com.squareup.javapoet.CodeBlock;

import javax.inject.Provider;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.javapoet.CodeBlocks;

import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.SourceFiles.generatedClassNameForBinding;
import static dagger.spi.model.BindingKind.INJECTION;

/**
 * A {@link Provider} creation expression for an {@link javax.inject.Inject @Inject}-constructed
 * class or a {@link dagger.Provides @Provides}-annotated module method.
 */
// TODO(dpb): Resolve with ProducerCreationExpression.
final class InjectionOrProvisionProviderCreationExpression
        implements FrameworkFieldInitializer.FrameworkInstanceCreationExpression {

    private final ContributionBinding binding;
    private final ComponentImplementation.ShardImplementation shardImplementation;
    private final ComponentRequestRepresentations componentRequestRepresentations;

    @AssistedInject
    InjectionOrProvisionProviderCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations) {
        this.binding = checkNotNull(binding);
        this.shardImplementation = componentImplementation.shardImplementation(binding);
        this.componentRequestRepresentations = componentRequestRepresentations;
    }

    @Override
    public CodeBlock creationExpression() {
        CodeBlock createFactory =
                CodeBlock.of(
                        "$T.create($L)",
                        generatedClassNameForBinding(binding),
                        componentRequestRepresentations.getCreateMethodArgumentsCodeBlock(
                                binding, shardImplementation.name()));

        // When scoping a parameterized factory for an @Inject class, Java 7 cannot always infer the
        // type properly, so cast to a raw framework type before scoping.
        if (binding.kind().equals(INJECTION)
                && binding.unresolved().isPresent()
                && binding.scope().isPresent()) {
            return CodeBlocks.cast(createFactory, Provider.class);
        } else {
            return createFactory;
        }
    }

    @AssistedFactory
    static interface Factory {
        InjectionOrProvisionProviderCreationExpression create(ContributionBinding binding);
    }
}
