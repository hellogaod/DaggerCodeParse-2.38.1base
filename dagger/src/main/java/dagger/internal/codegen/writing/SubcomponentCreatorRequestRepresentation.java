package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.javapoet.CodeBlocks;
import dagger.internal.codegen.javapoet.Expression;

/**
 * A binding expression for a subcomponent creator that just invokes the constructor.
 * <p>
 * componentMethod返回类型是subcomponent.Builder或component关联的subcomponent
 */
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
        //new subcomponent.creator(参数)，参数使用的是当前currentComponent及其直到父级component节点（排除当前currentComponent节点）
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
