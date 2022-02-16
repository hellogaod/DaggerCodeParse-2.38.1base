package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.javapoet.Expression;

/** A binding expression for the instance of the component itself, i.e. {@code this}. */
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
