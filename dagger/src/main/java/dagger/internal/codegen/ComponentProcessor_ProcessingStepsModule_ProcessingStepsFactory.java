package dagger.internal.codegen;


import com.google.common.collect.ImmutableList;

import javax.annotation.Generated;
import javax.inject.Provider;

import androidx.room.compiler.processing.XProcessingStep;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.validation.BindingMethodProcessingStep;
import dagger.internal.codegen.validation.BindsInstanceProcessingStep;
import dagger.internal.codegen.validation.MonitoringModuleProcessingStep;
import dagger.internal.codegen.validation.MultibindingAnnotationsProcessingStep;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class ComponentProcessor_ProcessingStepsModule_ProcessingStepsFactory implements Factory<ImmutableList<XProcessingStep>> {
    private final Provider<MapKeyProcessingStep> mapKeyProcessingStepProvider;

    private final Provider<InjectProcessingStep> injectProcessingStepProvider;

    private final Provider<AssistedInjectProcessingStep> assistedInjectProcessingStepProvider;

    private final Provider<AssistedFactoryProcessingStep> assistedFactoryProcessingStepProvider;

    private final Provider<AssistedProcessingStep> assistedProcessingStepProvider;

    private final Provider<MonitoringModuleProcessingStep> monitoringModuleProcessingStepProvider;

    private final Provider<MultibindingAnnotationsProcessingStep> multibindingAnnotationsProcessingStepProvider;

    private final Provider<BindsInstanceProcessingStep> bindsInstanceProcessingStepProvider;

    private final Provider<ModuleProcessingStep> moduleProcessingStepProvider;

    private final Provider<ComponentProcessingStep> componentProcessingStepProvider;

    private final Provider<ComponentHjarProcessingStep> componentHjarProcessingStepProvider;

    private final Provider<BindingMethodProcessingStep> bindingMethodProcessingStepProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    public ComponentProcessor_ProcessingStepsModule_ProcessingStepsFactory(
            Provider<MapKeyProcessingStep> mapKeyProcessingStepProvider,
            Provider<InjectProcessingStep> injectProcessingStepProvider,
            Provider<AssistedInjectProcessingStep> assistedInjectProcessingStepProvider,
            Provider<AssistedFactoryProcessingStep> assistedFactoryProcessingStepProvider,
            Provider<AssistedProcessingStep> assistedProcessingStepProvider,
            Provider<MonitoringModuleProcessingStep> monitoringModuleProcessingStepProvider,
            Provider<MultibindingAnnotationsProcessingStep> multibindingAnnotationsProcessingStepProvider,
            Provider<BindsInstanceProcessingStep> bindsInstanceProcessingStepProvider,
            Provider<ModuleProcessingStep> moduleProcessingStepProvider,
            Provider<ComponentProcessingStep> componentProcessingStepProvider,
            Provider<ComponentHjarProcessingStep> componentHjarProcessingStepProvider,
            Provider<BindingMethodProcessingStep> bindingMethodProcessingStepProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        this.mapKeyProcessingStepProvider = mapKeyProcessingStepProvider;
        this.injectProcessingStepProvider = injectProcessingStepProvider;
        this.assistedInjectProcessingStepProvider = assistedInjectProcessingStepProvider;
        this.assistedFactoryProcessingStepProvider = assistedFactoryProcessingStepProvider;
        this.assistedProcessingStepProvider = assistedProcessingStepProvider;
        this.monitoringModuleProcessingStepProvider = monitoringModuleProcessingStepProvider;
        this.multibindingAnnotationsProcessingStepProvider = multibindingAnnotationsProcessingStepProvider;
        this.bindsInstanceProcessingStepProvider = bindsInstanceProcessingStepProvider;
        this.moduleProcessingStepProvider = moduleProcessingStepProvider;
        this.componentProcessingStepProvider = componentProcessingStepProvider;
        this.componentHjarProcessingStepProvider = componentHjarProcessingStepProvider;
        this.bindingMethodProcessingStepProvider = bindingMethodProcessingStepProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    @Override
    public ImmutableList<XProcessingStep> get() {
        return processingSteps(mapKeyProcessingStepProvider.get(), injectProcessingStepProvider.get(), assistedInjectProcessingStepProvider.get(), assistedFactoryProcessingStepProvider.get(), assistedProcessingStepProvider.get(), monitoringModuleProcessingStepProvider.get(), multibindingAnnotationsProcessingStepProvider.get(), bindsInstanceProcessingStepProvider.get(), moduleProcessingStepProvider.get(), componentProcessingStepProvider.get(), componentHjarProcessingStepProvider.get(), bindingMethodProcessingStepProvider.get(), compilerOptionsProvider.get());
    }

    public static ComponentProcessor_ProcessingStepsModule_ProcessingStepsFactory create(
            Provider<MapKeyProcessingStep> mapKeyProcessingStepProvider,
            Provider<InjectProcessingStep> injectProcessingStepProvider,
            Provider<AssistedInjectProcessingStep> assistedInjectProcessingStepProvider,
            Provider<AssistedFactoryProcessingStep> assistedFactoryProcessingStepProvider,
            Provider<AssistedProcessingStep> assistedProcessingStepProvider,
            Provider<MonitoringModuleProcessingStep> monitoringModuleProcessingStepProvider,
            Provider<MultibindingAnnotationsProcessingStep> multibindingAnnotationsProcessingStepProvider,
            Provider<BindsInstanceProcessingStep> bindsInstanceProcessingStepProvider,
            Provider<ModuleProcessingStep> moduleProcessingStepProvider,
            Provider<ComponentProcessingStep> componentProcessingStepProvider,
            Provider<ComponentHjarProcessingStep> componentHjarProcessingStepProvider,
            Provider<BindingMethodProcessingStep> bindingMethodProcessingStepProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        return new ComponentProcessor_ProcessingStepsModule_ProcessingStepsFactory(mapKeyProcessingStepProvider, injectProcessingStepProvider, assistedInjectProcessingStepProvider, assistedFactoryProcessingStepProvider, assistedProcessingStepProvider, monitoringModuleProcessingStepProvider, multibindingAnnotationsProcessingStepProvider, bindsInstanceProcessingStepProvider, moduleProcessingStepProvider, componentProcessingStepProvider, componentHjarProcessingStepProvider, bindingMethodProcessingStepProvider, compilerOptionsProvider);
    }


    public static ImmutableList<XProcessingStep> processingSteps(Object mapKeyProcessingStep,
                                                                 Object injectProcessingStep, Object assistedInjectProcessingStep,
                                                                 Object assistedFactoryProcessingStep, Object assistedProcessingStep,
                                                                 MonitoringModuleProcessingStep monitoringModuleProcessingStep,
                                                                 MultibindingAnnotationsProcessingStep multibindingAnnotationsProcessingStep,
                                                                 BindsInstanceProcessingStep bindsInstanceProcessingStep, Object moduleProcessingStep,
                                                                 Object componentProcessingStep, Object componentHjarProcessingStep,
                                                                 BindingMethodProcessingStep bindingMethodProcessingStep, CompilerOptions compilerOptions) {
        return Preconditions.checkNotNullFromProvides(ComponentProcessor.ProcessingStepsModule.processingSteps((MapKeyProcessingStep) mapKeyProcessingStep, (InjectProcessingStep) injectProcessingStep, (AssistedInjectProcessingStep) assistedInjectProcessingStep, (AssistedFactoryProcessingStep) assistedFactoryProcessingStep, (AssistedProcessingStep) assistedProcessingStep, monitoringModuleProcessingStep, multibindingAnnotationsProcessingStep, bindsInstanceProcessingStep, (ModuleProcessingStep) moduleProcessingStep, (ComponentProcessingStep) componentProcessingStep, (ComponentHjarProcessingStep) componentHjarProcessingStep, bindingMethodProcessingStep, compilerOptions));
    }
}
