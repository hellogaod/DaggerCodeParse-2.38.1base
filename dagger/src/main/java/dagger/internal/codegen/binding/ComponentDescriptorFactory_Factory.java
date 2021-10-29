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
public final class ComponentDescriptorFactory_Factory implements Factory<ComponentDescriptorFactory> {
    private final Provider<DaggerElements> elementsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DependencyRequestFactory> dependencyRequestFactoryProvider;

    private final Provider<ModuleDescriptor.Factory> moduleDescriptorFactoryProvider;

    private final Provider<InjectionAnnotations> injectionAnnotationsProvider;

    public ComponentDescriptorFactory_Factory(Provider<DaggerElements> elementsProvider,
                                              Provider<DaggerTypes> typesProvider,
                                              Provider<DependencyRequestFactory> dependencyRequestFactoryProvider,
                                              Provider<ModuleDescriptor.Factory> moduleDescriptorFactoryProvider,
                                              Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        this.elementsProvider = elementsProvider;
        this.typesProvider = typesProvider;
        this.dependencyRequestFactoryProvider = dependencyRequestFactoryProvider;
        this.moduleDescriptorFactoryProvider = moduleDescriptorFactoryProvider;
        this.injectionAnnotationsProvider = injectionAnnotationsProvider;
    }

    @Override
    public ComponentDescriptorFactory get() {
        return newInstance(elementsProvider.get(), typesProvider.get(), dependencyRequestFactoryProvider.get(), moduleDescriptorFactoryProvider.get(), injectionAnnotationsProvider.get());
    }

    public static ComponentDescriptorFactory_Factory create(Provider<DaggerElements> elementsProvider,
                                                            Provider<DaggerTypes> typesProvider,
                                                            Provider<DependencyRequestFactory> dependencyRequestFactoryProvider,
                                                            Provider<ModuleDescriptor.Factory> moduleDescriptorFactoryProvider,
                                                            Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        return new ComponentDescriptorFactory_Factory(elementsProvider, typesProvider, dependencyRequestFactoryProvider, moduleDescriptorFactoryProvider, injectionAnnotationsProvider);
    }

    public static ComponentDescriptorFactory newInstance(DaggerElements elements, DaggerTypes types,
                                                         DependencyRequestFactory dependencyRequestFactory,
                                                         ModuleDescriptor.Factory moduleDescriptorFactory, InjectionAnnotations injectionAnnotations) {
        return new ComponentDescriptorFactory(elements, types, dependencyRequestFactory, moduleDescriptorFactory, injectionAnnotations);
    }
}
