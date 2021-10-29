package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.RequestKind;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class DelegateRequestRepresentation_Factory {
    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    public DelegateRequestRepresentation_Factory(
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<DaggerTypes> typesProvider, Provider<DaggerElements> elementsProvider) {
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
    }

    public DelegateRequestRepresentation get(ContributionBinding binding, RequestKind requestKind) {
        return newInstance(binding, requestKind, componentRequestRepresentationsProvider.get(), typesProvider.get(), elementsProvider.get());
    }

    public static DelegateRequestRepresentation_Factory create(
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<DaggerTypes> typesProvider, Provider<DaggerElements> elementsProvider) {
        return new DelegateRequestRepresentation_Factory(componentRequestRepresentationsProvider, typesProvider, elementsProvider);
    }

    public static DelegateRequestRepresentation newInstance(ContributionBinding binding,
                                                            RequestKind requestKind, ComponentRequestRepresentations componentRequestRepresentations,
                                                            DaggerTypes types, DaggerElements elements) {
        return new DelegateRequestRepresentation(binding, requestKind, componentRequestRepresentations, types, elements);
    }
}
