package dagger.internal.codegen.writing;


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
public final class ComponentCreatorImplementationFactory_Factory implements Factory<ComponentCreatorImplementationFactory> {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    private final Provider<ModuleProxies> moduleProxiesProvider;

    public ComponentCreatorImplementationFactory_Factory(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<DaggerElements> elementsProvider, Provider<DaggerTypes> typesProvider,
            Provider<KotlinMetadataUtil> metadataUtilProvider,
            Provider<ModuleProxies> moduleProxiesProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
        this.elementsProvider = elementsProvider;
        this.typesProvider = typesProvider;
        this.metadataUtilProvider = metadataUtilProvider;
        this.moduleProxiesProvider = moduleProxiesProvider;
    }

    @Override
    public ComponentCreatorImplementationFactory get() {
        return newInstance(componentImplementationProvider.get(), elementsProvider.get(), typesProvider.get(), metadataUtilProvider.get(), moduleProxiesProvider.get());
    }

    public static ComponentCreatorImplementationFactory_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<DaggerElements> elementsProvider, Provider<DaggerTypes> typesProvider,
            Provider<KotlinMetadataUtil> metadataUtilProvider,
            Provider<ModuleProxies> moduleProxiesProvider) {
        return new ComponentCreatorImplementationFactory_Factory(componentImplementationProvider, elementsProvider, typesProvider, metadataUtilProvider, moduleProxiesProvider);
    }

    public static ComponentCreatorImplementationFactory newInstance(
            ComponentImplementation componentImplementation, DaggerElements elements, DaggerTypes types,
            KotlinMetadataUtil metadataUtil, ModuleProxies moduleProxies) {
        return new ComponentCreatorImplementationFactory(componentImplementation, elements, types, metadataUtil, moduleProxies);
    }
}
