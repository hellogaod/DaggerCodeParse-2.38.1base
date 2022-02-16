package dagger.internal.codegen.writing;

import com.squareup.javapoet.CodeBlock;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.javapoet.CodeBlocks;
import dagger.spi.model.DependencyRequest;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.binding.BindingRequest.bindingRequest;

/** A framework instance creation expression for a {@link dagger.Binds @Binds} binding. */
final class DelegatingFrameworkInstanceCreationExpression
        implements FrameworkFieldInitializer.FrameworkInstanceCreationExpression {

    private final ContributionBinding binding;
    private final ComponentImplementation componentImplementation;
    private final ComponentRequestRepresentations componentRequestRepresentations;

    @AssistedInject
    DelegatingFrameworkInstanceCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations,
            CompilerOptions compilerOptions) {
        this.binding = checkNotNull(binding);
        this.componentImplementation = componentImplementation;
        this.componentRequestRepresentations = componentRequestRepresentations;
    }

    @Override
    public CodeBlock creationExpression() {
        DependencyRequest dependency = getOnlyElement(binding.dependencies());
        return CodeBlocks.cast(
                componentRequestRepresentations
                        .getDependencyExpression(
                                bindingRequest(dependency.key(), binding.frameworkType()),
                                componentImplementation.shardImplementation(binding).name())
                        .codeBlock(),
                binding.frameworkType().frameworkClass());
    }

    @Override
    public boolean useSwitchingProvider() {
        // For delegate expressions, we just want to return the delegate field directly using the above
        // creationExpression(). Using SwitchingProviders would be less efficient because it would
        // create a new SwitchingProvider that just returns "delegateField.get()".
        return false;
    }

    @AssistedFactory
    static interface Factory {
        DelegatingFrameworkInstanceCreationExpression create(ContributionBinding binding);
    }
}
