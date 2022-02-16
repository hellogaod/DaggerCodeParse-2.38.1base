package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.javapoet.CodeBlocks;
import dagger.internal.codegen.javapoet.Expression;

/** A binding expression for a subcomponent creator that just invokes the constructor. */
final class SubcomponentCreatorRequestRepresentation extends SimpleInvocationRequestRepresentation {
    private final ComponentImplementation.ShardImplementation shardImplementation;
    private final ContributionBinding binding;

    @AssistedInject
    SubcomponentCreatorRequestRepresentation(
            @Assisted ContributionBinding binding, ComponentImplementation componentImplementation) {
        super(binding);
        this.binding = binding;
        this.shardImplementation = componentImplementation.shardImplementation(binding);
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {
        return Expression.create(
                binding.key().type().java(),
                "new $T($L)",
                shardImplementation.getSubcomponentCreatorSimpleName(binding.key()),
                shardImplementation.componentFieldsByImplementation().values().stream()
                        .map(field -> CodeBlock.of("$N", field))
                        .collect(CodeBlocks.toParametersCodeBlock()));
    }

    @AssistedFactory
    static interface Factory {
        SubcomponentCreatorRequestRepresentation create(ContributionBinding binding);
    }
}
