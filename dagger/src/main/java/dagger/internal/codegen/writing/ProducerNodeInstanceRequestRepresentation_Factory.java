package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.ContributionBinding;
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
public final class ProducerNodeInstanceRequestRepresentation_Factory {
    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<ComponentImplementation> componentImplementationProvider;

    public ProducerNodeInstanceRequestRepresentation_Factory(Provider<DaggerTypes> typesProvider,
                                                             Provider<DaggerElements> elementsProvider,
                                                             Provider<ComponentImplementation> componentImplementationProvider) {
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
        this.componentImplementationProvider = componentImplementationProvider;
    }

    public ProducerNodeInstanceRequestRepresentation get(ContributionBinding binding,
                                                         FrameworkInstanceSupplier frameworkInstanceSupplier) {
        return newInstance(binding, frameworkInstanceSupplier, typesProvider.get(), elementsProvider.get(), componentImplementationProvider.get());
    }

    public static ProducerNodeInstanceRequestRepresentation_Factory create(
            Provider<DaggerTypes> typesProvider, Provider<DaggerElements> elementsProvider,
            Provider<ComponentImplementation> componentImplementationProvider) {
        return new ProducerNodeInstanceRequestRepresentation_Factory(typesProvider, elementsProvider, componentImplementationProvider);
    }

    public static ProducerNodeInstanceRequestRepresentation newInstance(ContributionBinding binding,
                                                                        Object frameworkInstanceSupplier, DaggerTypes types, DaggerElements elements,
                                                                        ComponentImplementation componentImplementation) {
        return new ProducerNodeInstanceRequestRepresentation(binding, (FrameworkInstanceSupplier) frameworkInstanceSupplier, types, elements, componentImplementation);
    }
}
