package dagger.internal.codegen.binding;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
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
public final class BindingFactory_Factory implements Factory<BindingFactory> {
    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<KeyFactory> keyFactoryProvider;

    private final Provider<DependencyRequestFactory> dependencyRequestFactoryProvider;

    private final Provider<InjectionSiteFactory> injectionSiteFactoryProvider;

    private final Provider<InjectionAnnotations> injectionAnnotationsProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    public BindingFactory_Factory(Provider<DaggerTypes> typesProvider,
                                  Provider<DaggerElements> elementsProvider, Provider<KeyFactory> keyFactoryProvider,
                                  Provider<DependencyRequestFactory> dependencyRequestFactoryProvider,
                                  Provider<InjectionSiteFactory> injectionSiteFactoryProvider,
                                  Provider<InjectionAnnotations> injectionAnnotationsProvider,
                                  Provider<KotlinMetadataUtil> metadataUtilProvider) {
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
        this.keyFactoryProvider = keyFactoryProvider;
        this.dependencyRequestFactoryProvider = dependencyRequestFactoryProvider;
        this.injectionSiteFactoryProvider = injectionSiteFactoryProvider;
        this.injectionAnnotationsProvider = injectionAnnotationsProvider;
        this.metadataUtilProvider = metadataUtilProvider;
    }

    @Override
    public BindingFactory get() {
        return newInstance(typesProvider.get(), elementsProvider.get(), keyFactoryProvider.get(), dependencyRequestFactoryProvider.get(), injectionSiteFactoryProvider.get(), injectionAnnotationsProvider.get(), metadataUtilProvider.get());
    }

    public static BindingFactory_Factory create(Provider<DaggerTypes> typesProvider,
                                                Provider<DaggerElements> elementsProvider, Provider<KeyFactory> keyFactoryProvider,
                                                Provider<DependencyRequestFactory> dependencyRequestFactoryProvider,
                                                Provider<InjectionSiteFactory> injectionSiteFactoryProvider,
                                                Provider<InjectionAnnotations> injectionAnnotationsProvider,
                                                Provider<KotlinMetadataUtil> metadataUtilProvider) {
        return new BindingFactory_Factory(typesProvider, elementsProvider, keyFactoryProvider, dependencyRequestFactoryProvider, injectionSiteFactoryProvider, injectionAnnotationsProvider, metadataUtilProvider);
    }

    public static BindingFactory newInstance(DaggerTypes types, DaggerElements elements,
                                             KeyFactory keyFactory, DependencyRequestFactory dependencyRequestFactory,
                                             Object injectionSiteFactory, InjectionAnnotations injectionAnnotations,
                                             KotlinMetadataUtil metadataUtil) {
        return new BindingFactory(types, elements, keyFactory, dependencyRequestFactory, (InjectionSiteFactory) injectionSiteFactory, injectionAnnotations, metadataUtil);
    }
}
