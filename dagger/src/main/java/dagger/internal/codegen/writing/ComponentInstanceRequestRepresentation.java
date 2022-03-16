package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.javapoet.Expression;

/**
 * A binding expression for the instance of the component itself, i.e. {@code this}.
 * <p>
 * component节点生成的ProvisionBinding对象
 */
final class ComponentInstanceRequestRepresentation extends SimpleInvocationRequestRepresentation {
    private final ComponentImplementation componentImplementation;
    private final ContributionBinding binding;

    @AssistedInject
    ComponentInstanceRequestRepresentation(
            @Assisted ContributionBinding binding, ComponentImplementation componentImplementation) {
        super(binding);
        this.componentImplementation = componentImplementation;
        this.binding = binding;
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {
        //type：当前绑定key的type类型，代码块：this
        return Expression.create(
                binding.key().type().java(),
                componentImplementation.name().equals(requestingClass)
                        ? CodeBlock.of("this")
                        : componentImplementation.componentFieldReference());
    }

    @AssistedFactory
    static interface Factory {
        ComponentInstanceRequestRepresentation create(ContributionBinding binding);
    }
}
