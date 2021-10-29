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
public final class ProviderInstanceRequestRepresentation_Factory {
    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    public ProviderInstanceRequestRepresentation_Factory(Provider<DaggerTypes> typesProvider,
                                                         Provider<DaggerElements> elementsProvider) {
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
    }

    public ProviderInstanceRequestRepresentation get(ContributionBinding binding,
                                                     FrameworkInstanceSupplier frameworkInstanceSupplier) {
        return newInstance(binding, frameworkInstanceSupplier, typesProvider.get(), elementsProvider.get());
    }

    public static ProviderInstanceRequestRepresentation_Factory create(
            Provider<DaggerTypes> typesProvider, Provider<DaggerElements> elementsProvider) {
        return new ProviderInstanceRequestRepresentation_Factory(typesProvider, elementsProvider);
    }

    public static ProviderInstanceRequestRepresentation newInstance(ContributionBinding binding,
                                                                    Object frameworkInstanceSupplier, DaggerTypes types, DaggerElements elements) {
        return new ProviderInstanceRequestRepresentation(binding, (FrameworkInstanceSupplier) frameworkInstanceSupplier, types, elements);
    }
}
