package dagger.internal.codegen.writing;


import com.squareup.javapoet.CodeBlock;

import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.javapoet.CodeBlocks;
import dagger.spi.model.DependencyRequest;

import static com.google.common.base.Preconditions.checkNotNull;

/** An abstract factory creation expression for multibindings. */
abstract class MultibindingFactoryCreationExpression
        implements FrameworkFieldInitializer.FrameworkInstanceCreationExpression {
    private final ComponentImplementation.ShardImplementation shardImplementation;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final ContributionBinding binding;

    MultibindingFactoryCreationExpression(
            ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations) {
        this.binding = checkNotNull(binding);
        this.shardImplementation = checkNotNull(componentImplementation).shardImplementation(binding);
        this.componentRequestRepresentations = checkNotNull(componentRequestRepresentations);
    }

    /** Returns the expression for a dependency of this multibinding. */
    protected final CodeBlock multibindingDependencyExpression(DependencyRequest dependency) {
        CodeBlock expression =
                componentRequestRepresentations
                        .getDependencyExpression(
                                BindingRequest.bindingRequest(dependency.key(), binding.frameworkType()),
                                shardImplementation.name())
                        .codeBlock();

        return useRawType()
                ? CodeBlocks.cast(expression, binding.frameworkType().frameworkClass())
                : expression;
    }

    /** The binding request for this framework instance. */
    protected final BindingRequest bindingRequest() {
        return BindingRequest.bindingRequest(binding.key(), binding.frameworkType());
    }

    /**
     * Returns true if the {@linkplain ContributionBinding#key() key type} is inaccessible from the
     * component, and therefore a raw type must be used.
     */
    protected final boolean useRawType() {
        return !shardImplementation.isTypeAccessible(binding.key().type().java());
    }

    @Override
    public final boolean useSwitchingProvider() {
        return !binding.dependencies().isEmpty();
    }
}
