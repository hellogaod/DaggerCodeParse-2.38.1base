package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import dagger.internal.codegen.binding.ComponentDescriptor;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class ComponentMethodRequestRepresentation_Factory_Impl implements ComponentMethodRequestRepresentation.Factory {
    private final ComponentMethodRequestRepresentation_Factory delegateFactory;

    ComponentMethodRequestRepresentation_Factory_Impl(
            ComponentMethodRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public ComponentMethodRequestRepresentation create(
            RequestRepresentation wrappedRequestRepresentation,
            ComponentDescriptor.ComponentMethodDescriptor componentMethod) {
        return delegateFactory.get(wrappedRequestRepresentation, componentMethod);
    }

    public static Provider<ComponentMethodRequestRepresentation.Factory> create(
            ComponentMethodRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new ComponentMethodRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
