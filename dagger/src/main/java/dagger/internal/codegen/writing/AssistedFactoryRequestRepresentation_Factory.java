package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.langmodel.DaggerElements;
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
public final class AssistedFactoryRequestRepresentation_Factory {
    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    public AssistedFactoryRequestRepresentation_Factory(
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<DaggerTypes> typesProvider, Provider<DaggerElements> elementsProvider) {
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
    }

    public AssistedFactoryRequestRepresentation get(ProvisionBinding binding) {
        return newInstance(binding, componentRequestRepresentationsProvider.get(), typesProvider.get(), elementsProvider.get());
    }

    public static AssistedFactoryRequestRepresentation_Factory create(
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<DaggerTypes> typesProvider, Provider<DaggerElements> elementsProvider) {
        return new AssistedFactoryRequestRepresentation_Factory(componentRequestRepresentationsProvider, typesProvider, elementsProvider);
    }

    public static AssistedFactoryRequestRepresentation newInstance(ProvisionBinding binding,
                                                                   ComponentRequestRepresentations componentRequestRepresentations, DaggerTypes types,
                                                                   DaggerElements elements) {
        return new AssistedFactoryRequestRepresentation(binding, componentRequestRepresentations, types, elements);
    }
}
