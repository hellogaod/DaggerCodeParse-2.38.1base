package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.javapoet.Expression;

import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.BindingRequest.bindingRequest;
import static dagger.internal.codegen.javapoet.CodeBlocks.anonymousProvider;
import static dagger.spi.model.RequestKind.INSTANCE;

/**
 * A {@link javax.inject.Provider} creation expression for an anonymous inner class whose
 * {@code get()} method returns the expression for an instance binding request for its key.
 */
final class AnonymousProviderCreationExpression
        implements FrameworkFieldInitializer.FrameworkInstanceCreationExpression {
    private final ContributionBinding binding;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final ClassName requestingClass;

    @AssistedInject
    AnonymousProviderCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentRequestRepresentations componentRequestRepresentations,
            ComponentImplementation componentImplementation) {
        this.binding = checkNotNull(binding);
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.requestingClass = componentImplementation.name();
    }

    @Override
    public CodeBlock creationExpression() {
        BindingRequest instanceExpressionRequest = bindingRequest(binding.key(), INSTANCE);
        Expression instanceExpression =
                componentRequestRepresentations.getDependencyExpression(
                        instanceExpressionRequest,
                        // Not a real class name, but the actual requestingClass is an inner class within the
                        // given class, not that class itself.
                        requestingClass.nestedClass("Anonymous"));
        return anonymousProvider(instanceExpression);
    }


    @AssistedFactory
    static interface Factory {
        AnonymousProviderCreationExpression create(ContributionBinding binding);
    }
}
