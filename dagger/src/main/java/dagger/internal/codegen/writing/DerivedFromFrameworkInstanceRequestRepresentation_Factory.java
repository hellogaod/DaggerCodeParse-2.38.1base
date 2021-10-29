package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.FrameworkType;
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
public final class DerivedFromFrameworkInstanceRequestRepresentation_Factory {
    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    private final Provider<DaggerTypes> typesProvider;

    public DerivedFromFrameworkInstanceRequestRepresentation_Factory(
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<DaggerTypes> typesProvider) {
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
        this.typesProvider = typesProvider;
    }

    public DerivedFromFrameworkInstanceRequestRepresentation get(BindingRequest bindingRequest,
                                                                 FrameworkType frameworkType) {
        return newInstance(bindingRequest, frameworkType, componentRequestRepresentationsProvider.get(), typesProvider.get());
    }

    public static DerivedFromFrameworkInstanceRequestRepresentation_Factory create(
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<DaggerTypes> typesProvider) {
        return new DerivedFromFrameworkInstanceRequestRepresentation_Factory(componentRequestRepresentationsProvider, typesProvider);
    }

    public static DerivedFromFrameworkInstanceRequestRepresentation newInstance(
            BindingRequest bindingRequest, FrameworkType frameworkType,
            ComponentRequestRepresentations componentRequestRepresentations, DaggerTypes types) {
        return new DerivedFromFrameworkInstanceRequestRepresentation(bindingRequest, frameworkType, componentRequestRepresentations, types);
    }
}
