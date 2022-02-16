package dagger.internal.codegen.writing;

import com.squareup.javapoet.CodeBlock;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;

import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.binding.BindingRequest.bindingRequest;

/**
 * A {@link FrameworkFieldInitializer.FrameworkInstanceCreationExpression} for {@link dagger.spi.model.BindingKind#OPTIONAL
 * optional bindings}.
 */
final class OptionalFactoryInstanceCreationExpression
        implements FrameworkFieldInitializer.FrameworkInstanceCreationExpression {
    private final OptionalFactories optionalFactories;
    private final ContributionBinding binding;
    private final ComponentImplementation componentImplementation;
    private final ComponentRequestRepresentations componentRequestRepresentations;

    @AssistedInject
    OptionalFactoryInstanceCreationExpression(
            @Assisted ContributionBinding binding,
            OptionalFactories optionalFactories,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations) {
        this.optionalFactories = optionalFactories;
        this.binding = binding;
        this.componentImplementation = componentImplementation;
        this.componentRequestRepresentations = componentRequestRepresentations;
    }

    @Override
    public CodeBlock creationExpression() {
        return binding.dependencies().isEmpty()
                ? optionalFactories.absentOptionalProvider(binding)
                : optionalFactories.presentOptionalFactory(
                binding,
                componentRequestRepresentations
                        .getDependencyExpression(
                                bindingRequest(
                                        getOnlyElement(binding.dependencies()).key(), binding.frameworkType()),
                                componentImplementation.shardImplementation(binding).name())
                        .codeBlock());
    }

    @Override
    public boolean useSwitchingProvider() {
        // Share providers for empty optionals from OptionalFactories so we don't have numerous
        // switch cases that all return Optional.empty().
        return !binding.dependencies().isEmpty();
    }

    @AssistedFactory
    static interface Factory {
        OptionalFactoryInstanceCreationExpression create(ContributionBinding binding);
    }
}
