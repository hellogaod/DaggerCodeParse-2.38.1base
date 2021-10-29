package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.langmodel.DaggerTypes;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class ComponentMethodRequestRepresentation_Factory {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<DaggerTypes> typesProvider;

    public ComponentMethodRequestRepresentation_Factory(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<DaggerTypes> typesProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
        this.typesProvider = typesProvider;
    }

    public ComponentMethodRequestRepresentation get(
            RequestRepresentation wrappedRequestRepresentation,
            ComponentDescriptor.ComponentMethodDescriptor componentMethod) {
        return newInstance(wrappedRequestRepresentation, componentMethod, componentImplementationProvider.get(), typesProvider.get());
    }

    public static ComponentMethodRequestRepresentation_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<DaggerTypes> typesProvider) {
        return new ComponentMethodRequestRepresentation_Factory(componentImplementationProvider, typesProvider);
    }

    public static ComponentMethodRequestRepresentation newInstance(
            Object wrappedRequestRepresentation,
            ComponentDescriptor.ComponentMethodDescriptor componentMethod,
            ComponentImplementation componentImplementation, DaggerTypes types) {
        return new ComponentMethodRequestRepresentation((RequestRepresentation) wrappedRequestRepresentation, componentMethod, componentImplementation, types);
    }
}
