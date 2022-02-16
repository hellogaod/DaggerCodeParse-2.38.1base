package dagger.internal.codegen.writing;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A binding expression that implements and uses a component method.
 *
 * <p>Dependents of this binding expression will just call the component method.
 */
final class ComponentMethodRequestRepresentation extends MethodRequestRepresentation {
    private final RequestRepresentation wrappedRequestRepresentation;
    private final ComponentImplementation componentImplementation;
    private final ComponentDescriptor.ComponentMethodDescriptor componentMethod;
    private final DaggerTypes types;

    @AssistedInject
    ComponentMethodRequestRepresentation(
            @Assisted RequestRepresentation wrappedRequestRepresentation,
            @Assisted ComponentDescriptor.ComponentMethodDescriptor componentMethod,
            ComponentImplementation componentImplementation,
            DaggerTypes types) {
        super(componentImplementation.getComponentShard(), types);
        this.wrappedRequestRepresentation = checkNotNull(wrappedRequestRepresentation);
        this.componentMethod = checkNotNull(componentMethod);
        this.componentImplementation = componentImplementation;
        this.types = types;
    }

    @Override
    protected CodeBlock getComponentMethodImplementation(
            ComponentDescriptor.ComponentMethodDescriptor componentMethod, ComponentImplementation component) {
        // There could be several methods on the component for the same request key and kind.
        // Only one should use the BindingMethodImplementation; the others can delegate that one.
        // Separately, the method might be defined on a supertype that is also a supertype of some
        // parent component. In that case, the same ComponentMethodDescriptor will be used to add a CMBE
        // for the parent and the child. Only the parent's should use the BindingMethodImplementation;
        // the child's can delegate to the parent. So use methodImplementation.body() only if
        // componentName equals the component for this instance.
        return componentMethod.equals(this.componentMethod) && component.equals(componentImplementation)
                ? CodeBlock.of(
                "return $L;",
                wrappedRequestRepresentation
                        .getDependencyExpressionForComponentMethod(componentMethod, componentImplementation)
                        .codeBlock())
                : super.getComponentMethodImplementation(componentMethod, component);
    }

    @Override
    protected CodeBlock methodCall() {
        return CodeBlock.of("$N()", componentMethod.methodElement().getSimpleName());
    }

    @Override
    protected TypeMirror returnType() {
        return componentMethod.resolvedReturnType(types);
    }

    @AssistedFactory
    static interface Factory {
        ComponentMethodRequestRepresentation create(
                RequestRepresentation wrappedRequestRepresentation,
                ComponentDescriptor.ComponentMethodDescriptor componentMethod);
    }
}
