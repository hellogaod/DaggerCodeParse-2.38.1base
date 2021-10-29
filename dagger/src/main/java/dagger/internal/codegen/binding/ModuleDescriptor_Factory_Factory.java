package dagger.internal.codegen.binding;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
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
public final class ModuleDescriptor_Factory_Factory implements Factory<ModuleDescriptor.Factory> {
    private final Provider<DaggerElements> elementsProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    private final Provider<BindingFactory> bindingFactoryProvider;

    private final Provider<MultibindingDeclaration.Factory> multibindingDeclarationFactoryProvider;

    private final Provider<DelegateDeclaration.Factory> bindingDelegateDeclarationFactoryProvider;

    private final Provider<SubcomponentDeclaration.Factory> subcomponentDeclarationFactoryProvider;

    private final Provider<OptionalBindingDeclaration.Factory> optionalBindingDeclarationFactoryProvider;

    public ModuleDescriptor_Factory_Factory(Provider<DaggerElements> elementsProvider,
                                            Provider<KotlinMetadataUtil> metadataUtilProvider,
                                            Provider<BindingFactory> bindingFactoryProvider,
                                            Provider<MultibindingDeclaration.Factory> multibindingDeclarationFactoryProvider,
                                            Provider<DelegateDeclaration.Factory> bindingDelegateDeclarationFactoryProvider,
                                            Provider<SubcomponentDeclaration.Factory> subcomponentDeclarationFactoryProvider,
                                            Provider<OptionalBindingDeclaration.Factory> optionalBindingDeclarationFactoryProvider) {
        this.elementsProvider = elementsProvider;
        this.metadataUtilProvider = metadataUtilProvider;
        this.bindingFactoryProvider = bindingFactoryProvider;
        this.multibindingDeclarationFactoryProvider = multibindingDeclarationFactoryProvider;
        this.bindingDelegateDeclarationFactoryProvider = bindingDelegateDeclarationFactoryProvider;
        this.subcomponentDeclarationFactoryProvider = subcomponentDeclarationFactoryProvider;
        this.optionalBindingDeclarationFactoryProvider = optionalBindingDeclarationFactoryProvider;
    }

    @Override
    public ModuleDescriptor.Factory get() {
        return newInstance(elementsProvider.get(), metadataUtilProvider.get(), bindingFactoryProvider.get(), multibindingDeclarationFactoryProvider.get(), bindingDelegateDeclarationFactoryProvider.get(), subcomponentDeclarationFactoryProvider.get(), optionalBindingDeclarationFactoryProvider.get());
    }

    public static ModuleDescriptor_Factory_Factory create(Provider<DaggerElements> elementsProvider,
                                                          Provider<KotlinMetadataUtil> metadataUtilProvider,
                                                          Provider<BindingFactory> bindingFactoryProvider,
                                                          Provider<MultibindingDeclaration.Factory> multibindingDeclarationFactoryProvider,
                                                          Provider<DelegateDeclaration.Factory> bindingDelegateDeclarationFactoryProvider,
                                                          Provider<SubcomponentDeclaration.Factory> subcomponentDeclarationFactoryProvider,
                                                          Provider<OptionalBindingDeclaration.Factory> optionalBindingDeclarationFactoryProvider) {
        return new ModuleDescriptor_Factory_Factory(elementsProvider, metadataUtilProvider, bindingFactoryProvider, multibindingDeclarationFactoryProvider, bindingDelegateDeclarationFactoryProvider, subcomponentDeclarationFactoryProvider, optionalBindingDeclarationFactoryProvider);
    }

    public static ModuleDescriptor.Factory newInstance(DaggerElements elements,
                                                       KotlinMetadataUtil metadataUtil, BindingFactory bindingFactory,
                                                       MultibindingDeclaration.Factory multibindingDeclarationFactory,
                                                       DelegateDeclaration.Factory bindingDelegateDeclarationFactory,
                                                       SubcomponentDeclaration.Factory subcomponentDeclarationFactory,
                                                       Object optionalBindingDeclarationFactory) {
        return new ModuleDescriptor.Factory(elements, metadataUtil, bindingFactory, multibindingDeclarationFactory, bindingDelegateDeclarationFactory, subcomponentDeclarationFactory, (OptionalBindingDeclaration.Factory) optionalBindingDeclarationFactory);
    }
}
