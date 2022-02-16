package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.FrameworkType;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.BindingRequest.bindingRequest;

/**
 * A binding expression that depends on a framework instance.
 */
final class DerivedFromFrameworkInstanceRequestRepresentation extends RequestRepresentation {
    private final BindingRequest bindingRequest;
    private final BindingRequest frameworkRequest;
    private final FrameworkType frameworkType;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerTypes types;

    @AssistedInject
    DerivedFromFrameworkInstanceRequestRepresentation(
            @Assisted BindingRequest bindingRequest,
            @Assisted FrameworkType frameworkType,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types) {
        this.bindingRequest = checkNotNull(bindingRequest);
        this.frameworkType = checkNotNull(frameworkType);
        this.frameworkRequest = bindingRequest(bindingRequest.key(), frameworkType);
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.types = types;
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {
        return frameworkType.to(
                bindingRequest.requestKind(),
                componentRequestRepresentations.getDependencyExpression(frameworkRequest, requestingClass),
                types);
    }

    @Override
    Expression getDependencyExpressionForComponentMethod(
            ComponentDescriptor.ComponentMethodDescriptor componentMethod, ComponentImplementation component) {
        Expression frameworkInstance =
                componentRequestRepresentations.getDependencyExpressionForComponentMethod(
                        frameworkRequest, componentMethod, component);
        return frameworkType.to(bindingRequest.requestKind(), frameworkInstance, types);
    }

    @AssistedFactory
    static interface Factory {
        DerivedFromFrameworkInstanceRequestRepresentation create(
                BindingRequest request, FrameworkType frameworkType);
    }

}
