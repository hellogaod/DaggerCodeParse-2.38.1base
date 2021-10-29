package dagger.internal.codegen.binding;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.langmodel.DaggerElements;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class BindingGraphFactory_Factory implements Factory<BindingGraphFactory> {
    private final Provider<DaggerElements> elementsProvider;

    private final Provider<InjectBindingRegistry> injectBindingRegistryProvider;

    private final Provider<KeyFactory> keyFactoryProvider;

    private final Provider<BindingFactory> bindingFactoryProvider;

    private final Provider<ModuleDescriptor.Factory> moduleDescriptorFactoryProvider;

    private final Provider<BindingGraphConverter> bindingGraphConverterProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    public BindingGraphFactory_Factory(Provider<DaggerElements> elementsProvider,
                                       Provider<InjectBindingRegistry> injectBindingRegistryProvider,
                                       Provider<KeyFactory> keyFactoryProvider, Provider<BindingFactory> bindingFactoryProvider,
                                       Provider<ModuleDescriptor.Factory> moduleDescriptorFactoryProvider,
                                       Provider<BindingGraphConverter> bindingGraphConverterProvider,
                                       Provider<CompilerOptions> compilerOptionsProvider) {
        this.elementsProvider = elementsProvider;
        this.injectBindingRegistryProvider = injectBindingRegistryProvider;
        this.keyFactoryProvider = keyFactoryProvider;
        this.bindingFactoryProvider = bindingFactoryProvider;
        this.moduleDescriptorFactoryProvider = moduleDescriptorFactoryProvider;
        this.bindingGraphConverterProvider = bindingGraphConverterProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    @Override
    public BindingGraphFactory get() {
        return newInstance(elementsProvider.get(), injectBindingRegistryProvider.get(), keyFactoryProvider.get(), bindingFactoryProvider.get(), moduleDescriptorFactoryProvider.get(), bindingGraphConverterProvider.get(), compilerOptionsProvider.get());
    }

    public static BindingGraphFactory_Factory create(Provider<DaggerElements> elementsProvider,
                                                     Provider<InjectBindingRegistry> injectBindingRegistryProvider,
                                                     Provider<KeyFactory> keyFactoryProvider, Provider<BindingFactory> bindingFactoryProvider,
                                                     Provider<ModuleDescriptor.Factory> moduleDescriptorFactoryProvider,
                                                     Provider<BindingGraphConverter> bindingGraphConverterProvider,
                                                     Provider<CompilerOptions> compilerOptionsProvider) {
        return new BindingGraphFactory_Factory(elementsProvider, injectBindingRegistryProvider, keyFactoryProvider, bindingFactoryProvider, moduleDescriptorFactoryProvider, bindingGraphConverterProvider, compilerOptionsProvider);
    }

    public static BindingGraphFactory newInstance(DaggerElements elements,
                                                  InjectBindingRegistry injectBindingRegistry, KeyFactory keyFactory,
                                                  BindingFactory bindingFactory, ModuleDescriptor.Factory moduleDescriptorFactory,
                                                  Object bindingGraphConverter, CompilerOptions compilerOptions) {
        return new BindingGraphFactory(elements, injectBindingRegistry, keyFactory, bindingFactory, moduleDescriptorFactory, (BindingGraphConverter) bindingGraphConverter, compilerOptions);
    }
}
