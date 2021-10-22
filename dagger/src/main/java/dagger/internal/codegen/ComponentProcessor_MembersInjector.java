package dagger.internal.codegen;


import com.google.common.collect.ImmutableList;

import java.util.Set;

import javax.annotation.Generated;
import javax.inject.Provider;

import androidx.room.compiler.processing.XProcessingStep;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.codegen.base.ClearableCache;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.InjectBindingRegistry;
import dagger.internal.codegen.binding.MembersInjectionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.validation.ExternalBindingGraphPlugins;
import dagger.internal.codegen.validation.ValidationBindingGraphPlugins;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class ComponentProcessor_MembersInjector implements MembersInjector<ComponentProcessor> {

    private final Provider<InjectBindingRegistry> injectBindingRegistryProvider;

    private final Provider<SourceFileGenerator<ProvisionBinding>> factoryGeneratorProvider;

    private final Provider<SourceFileGenerator<MembersInjectionBinding>> membersInjectorGeneratorProvider;

    private final Provider<ImmutableList<XProcessingStep>> processingStepsProvider;

    private final Provider<ValidationBindingGraphPlugins> validationBindingGraphPluginsProvider;

    private final Provider<ExternalBindingGraphPlugins> externalBindingGraphPluginsProvider;

    private final Provider<Set<ClearableCache>> clearableCachesProvider;

    public ComponentProcessor_MembersInjector(
            Provider<InjectBindingRegistry> injectBindingRegistryProvider,
            Provider<SourceFileGenerator<ProvisionBinding>> factoryGeneratorProvider,
            Provider<SourceFileGenerator<MembersInjectionBinding>> membersInjectorGeneratorProvider,
            Provider<ImmutableList<XProcessingStep>> processingStepsProvider,
            Provider<ValidationBindingGraphPlugins> validationBindingGraphPluginsProvider,
            Provider<ExternalBindingGraphPlugins> externalBindingGraphPluginsProvider,
            Provider<Set<ClearableCache>> clearableCachesProvider) {
        this.injectBindingRegistryProvider = injectBindingRegistryProvider;
        this.factoryGeneratorProvider = factoryGeneratorProvider;
        this.membersInjectorGeneratorProvider = membersInjectorGeneratorProvider;
        this.processingStepsProvider = processingStepsProvider;
        this.validationBindingGraphPluginsProvider = validationBindingGraphPluginsProvider;
        this.externalBindingGraphPluginsProvider = externalBindingGraphPluginsProvider;
        this.clearableCachesProvider = clearableCachesProvider;
    }

    public static MembersInjector<ComponentProcessor> create(
            Provider<InjectBindingRegistry> injectBindingRegistryProvider,
            Provider<SourceFileGenerator<ProvisionBinding>> factoryGeneratorProvider,
            Provider<SourceFileGenerator<MembersInjectionBinding>> membersInjectorGeneratorProvider,
            Provider<ImmutableList<XProcessingStep>> processingStepsProvider,
            Provider<ValidationBindingGraphPlugins> validationBindingGraphPluginsProvider,
            Provider<ExternalBindingGraphPlugins> externalBindingGraphPluginsProvider,
            Provider<Set<ClearableCache>> clearableCachesProvider
    ) {
        return new ComponentProcessor_MembersInjector(
                injectBindingRegistryProvider,
                factoryGeneratorProvider,
                membersInjectorGeneratorProvider,
                processingStepsProvider,
                validationBindingGraphPluginsProvider,
                externalBindingGraphPluginsProvider,
                clearableCachesProvider);
    }

    @Override
    public void injectMembers(ComponentProcessor instance) {
        injectInjectBindingRegistry(instance, injectBindingRegistryProvider.get());
        injectFactoryGenerator(instance, factoryGeneratorProvider.get());
        injectMembersInjectorGenerator(instance, membersInjectorGeneratorProvider.get());
        injectProcessingSteps(instance, processingStepsProvider.get());
        injectValidationBindingGraphPlugins(instance, validationBindingGraphPluginsProvider.get());
        injectExternalBindingGraphPlugins(instance, externalBindingGraphPluginsProvider.get());
        injectClearableCaches(instance, clearableCachesProvider.get());
    }


    @InjectedFieldSignature("dagger.internal.codegen.ComponentProcessor.injectBindingRegistry")
    public static void injectInjectBindingRegistry(ComponentProcessor instance,
                                                   InjectBindingRegistry injectBindingRegistry) {
        instance.injectBindingRegistry = injectBindingRegistry;
    }

    @InjectedFieldSignature("dagger.internal.codegen.ComponentProcessor.factoryGenerator")
    public static void injectFactoryGenerator(ComponentProcessor instance,
                                              SourceFileGenerator<ProvisionBinding> factoryGenerator) {
        instance.factoryGenerator = factoryGenerator;
    }

    @InjectedFieldSignature("dagger.internal.codegen.ComponentProcessor.membersInjectorGenerator")
    public static void injectMembersInjectorGenerator(ComponentProcessor instance,
                                                      SourceFileGenerator<MembersInjectionBinding> membersInjectorGenerator) {
        instance.membersInjectorGenerator = membersInjectorGenerator;
    }

    @InjectedFieldSignature("dagger.internal.codegen.ComponentProcessor.processingSteps")
    public static void injectProcessingSteps(ComponentProcessor instance,
                                             ImmutableList<XProcessingStep> processingSteps) {
        instance.processingSteps = processingSteps;
    }

    @InjectedFieldSignature("dagger.internal.codegen.ComponentProcessor.validationBindingGraphPlugins")
    public static void injectValidationBindingGraphPlugins(ComponentProcessor instance,
                                                           ValidationBindingGraphPlugins validationBindingGraphPlugins) {
        instance.validationBindingGraphPlugins = validationBindingGraphPlugins;
    }

    @InjectedFieldSignature("dagger.internal.codegen.ComponentProcessor.externalBindingGraphPlugins")
    public static void injectExternalBindingGraphPlugins(ComponentProcessor instance,
                                                         ExternalBindingGraphPlugins externalBindingGraphPlugins) {
        instance.externalBindingGraphPlugins = externalBindingGraphPlugins;
    }

    @InjectedFieldSignature("dagger.internal.codegen.ComponentProcessor.clearableCaches")
    public static void injectClearableCaches(ComponentProcessor instance,
                                             Set<ClearableCache> clearableCaches) {
        instance.clearableCaches = clearableCaches;
    }
}
