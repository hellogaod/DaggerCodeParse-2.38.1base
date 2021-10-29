package dagger.internal.codegen.binding;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class InjectionSiteFactory_Factory implements Factory<InjectionSiteFactory> {
    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<DependencyRequestFactory> dependencyRequestFactoryProvider;

    public InjectionSiteFactory_Factory(Provider<DaggerTypes> typesProvider,
                                        Provider<DaggerElements> elementsProvider,
                                        Provider<DependencyRequestFactory> dependencyRequestFactoryProvider) {
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
        this.dependencyRequestFactoryProvider = dependencyRequestFactoryProvider;
    }

    @Override
    public InjectionSiteFactory get() {
        return newInstance(typesProvider.get(), elementsProvider.get(), dependencyRequestFactoryProvider.get());
    }

    public static InjectionSiteFactory_Factory create(Provider<DaggerTypes> typesProvider,
                                                      Provider<DaggerElements> elementsProvider,
                                                      Provider<DependencyRequestFactory> dependencyRequestFactoryProvider) {
        return new InjectionSiteFactory_Factory(typesProvider, elementsProvider, dependencyRequestFactoryProvider);
    }

    public static InjectionSiteFactory newInstance(DaggerTypes types, DaggerElements elements,
                                                   DependencyRequestFactory dependencyRequestFactory) {
        return new InjectionSiteFactory(types, elements, dependencyRequestFactory);
    }
}
