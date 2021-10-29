package dagger.internal.codegen.validation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.BindingGraphFactory;
import dagger.internal.codegen.binding.ComponentDescriptorFactory;
import dagger.internal.codegen.binding.MethodSignatureFormatter;
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
public final class ModuleValidator_Factory implements Factory<ModuleValidator> {
    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<AnyBindingMethodValidator> anyBindingMethodValidatorProvider;

    private final Provider<MethodSignatureFormatter> methodSignatureFormatterProvider;

    private final Provider<ComponentDescriptorFactory> componentDescriptorFactoryProvider;

    private final Provider<BindingGraphFactory> bindingGraphFactoryProvider;

    private final Provider<BindingGraphValidator> bindingGraphValidatorProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    public ModuleValidator_Factory(Provider<DaggerTypes> typesProvider,
                                   Provider<DaggerElements> elementsProvider,
                                   Provider<AnyBindingMethodValidator> anyBindingMethodValidatorProvider,
                                   Provider<MethodSignatureFormatter> methodSignatureFormatterProvider,
                                   Provider<ComponentDescriptorFactory> componentDescriptorFactoryProvider,
                                   Provider<BindingGraphFactory> bindingGraphFactoryProvider,
                                   Provider<BindingGraphValidator> bindingGraphValidatorProvider,
                                   Provider<KotlinMetadataUtil> metadataUtilProvider) {
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
        this.anyBindingMethodValidatorProvider = anyBindingMethodValidatorProvider;
        this.methodSignatureFormatterProvider = methodSignatureFormatterProvider;
        this.componentDescriptorFactoryProvider = componentDescriptorFactoryProvider;
        this.bindingGraphFactoryProvider = bindingGraphFactoryProvider;
        this.bindingGraphValidatorProvider = bindingGraphValidatorProvider;
        this.metadataUtilProvider = metadataUtilProvider;
    }

    @Override
    public ModuleValidator get() {
        return newInstance(typesProvider.get(), elementsProvider.get(), anyBindingMethodValidatorProvider.get(), methodSignatureFormatterProvider.get(), componentDescriptorFactoryProvider.get(), bindingGraphFactoryProvider.get(), bindingGraphValidatorProvider.get(), metadataUtilProvider.get());
    }

    public static ModuleValidator_Factory create(Provider<DaggerTypes> typesProvider,
                                                 Provider<DaggerElements> elementsProvider,
                                                 Provider<AnyBindingMethodValidator> anyBindingMethodValidatorProvider,
                                                 Provider<MethodSignatureFormatter> methodSignatureFormatterProvider,
                                                 Provider<ComponentDescriptorFactory> componentDescriptorFactoryProvider,
                                                 Provider<BindingGraphFactory> bindingGraphFactoryProvider,
                                                 Provider<BindingGraphValidator> bindingGraphValidatorProvider,
                                                 Provider<KotlinMetadataUtil> metadataUtilProvider) {
        return new ModuleValidator_Factory(typesProvider, elementsProvider, anyBindingMethodValidatorProvider, methodSignatureFormatterProvider, componentDescriptorFactoryProvider, bindingGraphFactoryProvider, bindingGraphValidatorProvider, metadataUtilProvider);
    }

    public static ModuleValidator newInstance(DaggerTypes types, DaggerElements elements,
                                              AnyBindingMethodValidator anyBindingMethodValidator,
                                              MethodSignatureFormatter methodSignatureFormatter,
                                              ComponentDescriptorFactory componentDescriptorFactory,
                                              BindingGraphFactory bindingGraphFactory, BindingGraphValidator bindingGraphValidator,
                                              KotlinMetadataUtil metadataUtil) {
        return new ModuleValidator(types, elements, anyBindingMethodValidator, methodSignatureFormatter, componentDescriptorFactory, bindingGraphFactory, bindingGraphValidator, metadataUtil);
    }
}
