package dagger.internal.codegen;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.Map;
import java.util.Set;

import javax.annotation.Generated;
import javax.inject.Provider;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XProcessingEnv;
import androidx.room.compiler.processing.XProcessingStep;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.InstanceFactory;
import dagger.internal.Preconditions;
import dagger.internal.SingleCheck;
import dagger.internal.codegen.base.ClearableCache;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.InjectBindingRegistry;
import dagger.internal.codegen.binding.MembersInjectionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions;
import dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions_Factory;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.internal.codegen.validation.BindingMethodProcessingStep;
import dagger.internal.codegen.validation.BindingMethodProcessingStep_Factory;
import dagger.internal.codegen.validation.BindsInstanceProcessingStep;
import dagger.internal.codegen.validation.BindsInstanceProcessingStep_Factory;
import dagger.internal.codegen.validation.ExternalBindingGraphPlugins;
import dagger.internal.codegen.validation.ExternalBindingGraphPlugins_Factory;
import dagger.internal.codegen.validation.InjectBindingRegistryImpl_Factory;
import dagger.internal.codegen.validation.InjectValidator;
import dagger.internal.codegen.validation.InjectValidator_Factory;
import dagger.internal.codegen.validation.MonitoringModuleProcessingStep;
import dagger.internal.codegen.validation.MonitoringModuleProcessingStep_Factory;
import dagger.internal.codegen.validation.MultibindingAnnotationsProcessingStep;
import dagger.internal.codegen.validation.MultibindingAnnotationsProcessingStep_Factory;
import dagger.internal.codegen.validation.ValidationBindingGraphPlugins;
import dagger.internal.codegen.validation.ValidationBindingGraphPlugins_Factory;
import dagger.internal.codegen.writing.FactoryGenerator;
import dagger.internal.codegen.writing.FactoryGenerator_Factory;
import dagger.internal.codegen.writing.MembersInjectorGenerator;
import dagger.internal.codegen.writing.MembersInjectorGenerator_Factory;
import dagger.spi.BindingGraphPlugin;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
final class DaggerComponentProcessor_ProcessorComponent implements ComponentProcessor.ProcessorComponent {

    private final XProcessingEnv xProcessingEnv;

    private final ImmutableSet<BindingGraphPlugin> externalPlugins;

    private final DaggerComponentProcessor_ProcessorComponent processorComponent = this;

    private Provider<XProcessingEnv> xProcessingEnvProvider;

    private Provider<DaggerElements> daggerElementsProvider;

    private Provider<DaggerTypes> daggerTypesProvider;

    private Provider<XMessager> messagerProvider;

    private Provider<Map<String, String>> processingOptionsProvider;

    private Provider<ProcessingEnvironmentCompilerOptions> processingEnvironmentCompilerOptionsProvider;

    private Provider<CompilerOptions> bindCompilerOptionsProvider;

    private Provider<InjectValidator> injectValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider injectBindingRegistryImplProvider;

    private Provider<XFiler> filerProvider;

    private Provider<ValidationBindingGraphPlugins> validationBindingGraphPluginsProvider;

    private Provider<ImmutableSet<BindingGraphPlugin>> externalPluginsProvider;

    private Provider<ExternalBindingGraphPlugins> externalBindingGraphPluginsProvider;

    private Provider<SourceVersion> sourceVersionProvider;

    private DaggerComponentProcessor_ProcessorComponent(XProcessingEnv xProcessingEnvParam,
                                                        ImmutableSet<BindingGraphPlugin> externalPluginsParam) {
        this.xProcessingEnv = xProcessingEnvParam;
        this.externalPlugins = externalPluginsParam;
        initialize(xProcessingEnvParam, externalPluginsParam);

    }

    private XFiler xFiler() {
        return ProcessingEnvironmentModule_FilerFactory.filer(bindCompilerOptionsProvider.get(), xProcessingEnv);
    }

    private SourceVersion sourceVersion() {
        return ProcessingEnvironmentModule_SourceVersionFactory.sourceVersion(xProcessingEnv);
    }

    private FactoryGenerator factoryGenerator() {
        return FactoryGenerator_Factory.newInstance(daggerTypesProvider.get(), bindCompilerOptionsProvider.get());
    }


    private SourceFileGenerator<ProvisionBinding> sourceFileGeneratorOfProvisionBinding() {
        return SourceFileGeneratorsModule_FactoryGeneratorFactory.factoryGenerator(factoryGenerator(), bindCompilerOptionsProvider.get());
    }

    private MembersInjectorGenerator membersInjectorGenerator() {
        return MembersInjectorGenerator_Factory.newInstance(daggerTypesProvider.get());
    }

    private SourceFileGenerator<MembersInjectionBinding> sourceFileGeneratorOfMembersInjectionBinding(
    ) {
        return SourceFileGeneratorsModule_MembersInjectorGeneratorFactory.membersInjectorGenerator(membersInjectorGenerator(), bindCompilerOptionsProvider.get());
    }

    private XMessager xMessager() {
        return ProcessingEnvironmentModule_MessagerFactory.messager(xProcessingEnv);
    }

    private MapKeyProcessingStep mapKeyProcessingStep() {
        return new MapKeyProcessingStep(xMessager(), daggerTypesProvider.get());
    }

    private InjectProcessingStep injectProcessingStep() {
        return new InjectProcessingStep((InjectBindingRegistry) injectBindingRegistryImplProvider.get());
    }

    private AssistedInjectProcessingStep assistedInjectProcessingStep() {
        return new AssistedInjectProcessingStep(daggerTypesProvider.get(), xMessager(), xProcessingEnv);
    }

    private AssistedFactoryProcessingStep assistedFactoryProcessingStep() {
        return new AssistedFactoryProcessingStep(
                xProcessingEnv,
                xMessager(),
                xFiler(),
                sourceVersion(),
                daggerElementsProvider.get(),
                daggerTypesProvider.get()
        );
    }

    private AssistedProcessingStep assistedProcessingStep() {
        return new AssistedProcessingStep(daggerElementsProvider.get(), xMessager(), xProcessingEnv);
    }

    private MonitoringModuleProcessingStep monitoringModuleProcessingStep() {
        return MonitoringModuleProcessingStep_Factory.newInstance(xMessager());
    }

    private MultibindingAnnotationsProcessingStep multibindingAnnotationsProcessingStep() {
        return MultibindingAnnotationsProcessingStep_Factory.newInstance(xMessager());
    }

    private BindsInstanceProcessingStep bindsInstanceProcessingStep() {
        return BindsInstanceProcessingStep_Factory.newInstance(xMessager());
    }

    private ModuleProcessingStep moduleProcessingStep() {
        return new ModuleProcessingStep(
                xMessager(),
                sourceFileGeneratorOfProvisionBinding()
        );
    }

    private ComponentProcessingStep componentProcessingStep() {
        return new ComponentProcessingStep(xMessager());
    }

    private ComponentHjarProcessingStep componentHjarProcessingStep() {
        return new ComponentHjarProcessingStep(xMessager());
    }


    private BindingMethodProcessingStep bindingMethodProcessingStep() {
        return BindingMethodProcessingStep_Factory.newInstance(xMessager());
    }

    private ImmutableList<XProcessingStep> immutableListOfXProcessingStep() {
        return ComponentProcessor_ProcessingStepsModule_ProcessingStepsFactory.processingSteps(
                mapKeyProcessingStep(),
                injectProcessingStep(),
                assistedInjectProcessingStep(),
                assistedFactoryProcessingStep(),
                assistedProcessingStep(),
                monitoringModuleProcessingStep(),
                multibindingAnnotationsProcessingStep(),
                bindsInstanceProcessingStep(),
                moduleProcessingStep(),
                componentProcessingStep(),
                componentHjarProcessingStep(),
                bindingMethodProcessingStep(),
                bindCompilerOptionsProvider.get()
        );
    }


    private ValidationBindingGraphPlugins validationBindingGraphPlugins() {
        return ValidationBindingGraphPlugins_Factory.newInstance();
    }

    private ExternalBindingGraphPlugins externalBindingGraphPlugins() {
        return ExternalBindingGraphPlugins_Factory.newInstance();
    }


    private Set<ClearableCache> setOfClearableCache() {
        return ImmutableSet.<ClearableCache>of(daggerElementsProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final XProcessingEnv xProcessingEnvParam,
                            final ImmutableSet<BindingGraphPlugin> externalPluginsParam) {

        this.xProcessingEnvProvider = InstanceFactory.create(xProcessingEnvParam);

        this.daggerElementsProvider = DoubleCheck.provider(ProcessingEnvironmentModule_DaggerElementsFactory.create(xProcessingEnvProvider));

        this.daggerTypesProvider = DoubleCheck.provider(ProcessingEnvironmentModule_DaggerTypesFactory.create(xProcessingEnvProvider, daggerElementsProvider));

        this.messagerProvider = ProcessingEnvironmentModule_MessagerFactory.create(xProcessingEnvProvider);

        this.processingOptionsProvider = ProcessingEnvironmentModule_ProcessingOptionsFactory.create(xProcessingEnvProvider);

        this.processingEnvironmentCompilerOptionsProvider = ProcessingEnvironmentCompilerOptions_Factory.create(processingOptionsProvider);

        this.bindCompilerOptionsProvider = SingleCheck.provider((Provider) processingEnvironmentCompilerOptionsProvider);

        this.injectValidatorProvider = DoubleCheck.provider(InjectValidator_Factory.create());

        this.injectBindingRegistryImplProvider = DoubleCheck.provider(InjectBindingRegistryImpl_Factory.create(daggerElementsProvider, daggerTypesProvider, messagerProvider, injectValidatorProvider, bindCompilerOptionsProvider));

        this.filerProvider = ProcessingEnvironmentModule_FilerFactory.create(bindCompilerOptionsProvider, xProcessingEnvProvider);

        this.validationBindingGraphPluginsProvider = ValidationBindingGraphPlugins_Factory.create();

        this.externalPluginsProvider = InstanceFactory.create(externalPluginsParam);

        this.externalBindingGraphPluginsProvider = ExternalBindingGraphPlugins_Factory.create();

        this.sourceVersionProvider = ProcessingEnvironmentModule_SourceVersionFactory.create(xProcessingEnvProvider);
    }

    @Override
    public void inject(ComponentProcessor processor) {
        injectComponentProcessor(processor);
    }


    @CanIgnoreReturnValue
    private ComponentProcessor injectComponentProcessor(ComponentProcessor instance) {
        ComponentProcessor_MembersInjector.injectInjectBindingRegistry(instance, (InjectBindingRegistry) injectBindingRegistryImplProvider.get());
        ComponentProcessor_MembersInjector.injectFactoryGenerator(instance, sourceFileGeneratorOfProvisionBinding());
        ComponentProcessor_MembersInjector.injectMembersInjectorGenerator(instance, sourceFileGeneratorOfMembersInjectionBinding());
        ComponentProcessor_MembersInjector.injectProcessingSteps(instance, immutableListOfXProcessingStep());
        ComponentProcessor_MembersInjector.injectValidationBindingGraphPlugins(instance, validationBindingGraphPlugins());
        ComponentProcessor_MembersInjector.injectExternalBindingGraphPlugins(instance, externalBindingGraphPlugins());
        ComponentProcessor_MembersInjector.injectClearableCaches(instance, setOfClearableCache());
        return instance;
    }

    public static ComponentProcessor.ProcessorComponent.Factory factory() {
        return new Factory();
    }

    private static final class Factory implements ComponentProcessor.ProcessorComponent.Factory {


        @Override
        public ComponentProcessor.ProcessorComponent create(XProcessingEnv xProcessingEnv, ImmutableSet<BindingGraphPlugin> externalPlugins) {
            Preconditions.checkNotNull(xProcessingEnv);
            Preconditions.checkNotNull(externalPlugins);
            return new DaggerComponentProcessor_ProcessorComponent(xProcessingEnv, externalPlugins);
        }
    }

}
