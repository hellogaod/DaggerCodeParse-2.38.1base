package dagger.internal.codegen;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Generated;
import javax.inject.Provider;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import androidx.room.compiler.processing.XFiler;
import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XProcessingEnv;
import androidx.room.compiler.processing.XProcessingStep;
import dagger.internal.DaggerGenerated;
import dagger.internal.DelegateFactory;
import dagger.internal.DoubleCheck;
import dagger.internal.InstanceFactory;
import dagger.internal.Preconditions;
import dagger.internal.SetFactory;
import dagger.internal.SingleCheck;
import dagger.internal.codegen.base.ClearableCache;
import dagger.internal.codegen.base.ElementFormatter_Factory;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.BindingDeclarationFormatter;
import dagger.internal.codegen.binding.BindingDeclarationFormatter_Factory;
import dagger.internal.codegen.binding.BindingFactory;
import dagger.internal.codegen.binding.BindingFactory_Factory;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.BindingGraphConverter_Factory;
import dagger.internal.codegen.binding.BindingGraphFactory;
import dagger.internal.codegen.binding.BindingGraphFactory_Factory;
import dagger.internal.codegen.binding.BindsTypeChecker;
import dagger.internal.codegen.binding.BindsTypeChecker_Factory;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.ComponentDescriptorFactory;
import dagger.internal.codegen.binding.ComponentDescriptorFactory_Factory;
import dagger.internal.codegen.binding.DelegateDeclaration;
import dagger.internal.codegen.binding.DelegateDeclaration_Factory_Factory;
import dagger.internal.codegen.binding.DependencyRequestFactory;
import dagger.internal.codegen.binding.DependencyRequestFactory_Factory;
import dagger.internal.codegen.binding.DependencyRequestFormatter;
import dagger.internal.codegen.binding.DependencyRequestFormatter_Factory;
import dagger.internal.codegen.binding.InjectBindingRegistry;
import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.binding.InjectionAnnotations_Factory;
import dagger.internal.codegen.binding.InjectionSiteFactory_Factory;
import dagger.internal.codegen.binding.KeyFactory;
import dagger.internal.codegen.binding.KeyFactory_Factory;
import dagger.internal.codegen.binding.MembersInjectionBinding;
import dagger.internal.codegen.binding.MethodSignatureFormatter;
import dagger.internal.codegen.binding.MethodSignatureFormatter_Factory;
import dagger.internal.codegen.binding.ModuleDescriptor;
import dagger.internal.codegen.binding.ModuleDescriptor_Factory_Factory;
import dagger.internal.codegen.binding.MultibindingDeclaration;
import dagger.internal.codegen.binding.MultibindingDeclaration_Factory_Factory;
import dagger.internal.codegen.binding.OptionalBindingDeclaration_Factory_Factory;
import dagger.internal.codegen.binding.ProductionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.binding.SubcomponentDeclaration;
import dagger.internal.codegen.binding.SubcomponentDeclaration_Factory_Factory;
import dagger.internal.codegen.bindinggraphvalidation.BindingGraphValidationModule_ProvidePluginsFactory;
import dagger.internal.codegen.bindinggraphvalidation.DependencyCycleValidator_Factory;
import dagger.internal.codegen.bindinggraphvalidation.DependsOnProductionExecutorValidator_Factory;
import dagger.internal.codegen.bindinggraphvalidation.DuplicateBindingsValidator_Factory;
import dagger.internal.codegen.bindinggraphvalidation.IncompatiblyScopedBindingsValidator_Factory;
import dagger.internal.codegen.bindinggraphvalidation.InjectBindingValidator_Factory;
import dagger.internal.codegen.bindinggraphvalidation.MapMultibindingValidator_Factory;
import dagger.internal.codegen.bindinggraphvalidation.MissingBindingValidator_Factory;
import dagger.internal.codegen.bindinggraphvalidation.NullableBindingValidator_Factory;
import dagger.internal.codegen.bindinggraphvalidation.ProvisionDependencyOnProducerBindingValidator_Factory;
import dagger.internal.codegen.bindinggraphvalidation.SetMultibindingValidator_Factory;
import dagger.internal.codegen.bindinggraphvalidation.SubcomponentFactoryMethodValidator_Factory;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions;
import dagger.internal.codegen.compileroption.ProcessingEnvironmentCompilerOptions_Factory;
import dagger.internal.codegen.componentgenerator.ComponentGenerator_Factory;
import dagger.internal.codegen.componentgenerator.ComponentHjarGenerator_Factory;
import dagger.internal.codegen.componentgenerator.CurrentImplementationSubcomponent;
import dagger.internal.codegen.componentgenerator.CurrentImplementationSubcomponent_ChildComponentImplementationFactoryModule_ProvideChildComponentImplementationFactoryFactory;
import dagger.internal.codegen.componentgenerator.TopLevelImplementationComponent;
import dagger.internal.codegen.kotlin.KotlinMetadataFactory;
import dagger.internal.codegen.kotlin.KotlinMetadataFactory_Factory;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil_Factory;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.internal.codegen.validation.AnyBindingMethodValidator;
import dagger.internal.codegen.validation.AnyBindingMethodValidator_Factory;
import dagger.internal.codegen.validation.BindingGraphValidator;
import dagger.internal.codegen.validation.BindingGraphValidator_Factory;
import dagger.internal.codegen.validation.BindingMethodProcessingStep;
import dagger.internal.codegen.validation.BindingMethodProcessingStep_Factory;
import dagger.internal.codegen.validation.BindingMethodValidatorsModule_IndexValidatorsFactory;
import dagger.internal.codegen.validation.BindsInstanceMethodValidator_Factory;
import dagger.internal.codegen.validation.BindsInstanceParameterValidator_Factory;
import dagger.internal.codegen.validation.BindsInstanceProcessingStep;
import dagger.internal.codegen.validation.BindsInstanceProcessingStep_Factory;
import dagger.internal.codegen.validation.BindsMethodValidator_Factory;
import dagger.internal.codegen.validation.BindsOptionalOfMethodValidator_Factory;
import dagger.internal.codegen.validation.ComponentCreatorValidator;
import dagger.internal.codegen.validation.ComponentCreatorValidator_Factory;
import dagger.internal.codegen.validation.ComponentDescriptorValidator;
import dagger.internal.codegen.validation.ComponentDescriptorValidator_Factory;
import dagger.internal.codegen.validation.ComponentHierarchyValidator_Factory;
import dagger.internal.codegen.validation.ComponentValidator;
import dagger.internal.codegen.validation.ComponentValidator_Factory;
import dagger.internal.codegen.validation.CompositeBindingGraphPlugin;
import dagger.internal.codegen.validation.CompositeBindingGraphPlugin_Factory_Factory;
import dagger.internal.codegen.validation.DependencyRequestValidator_Factory;
import dagger.internal.codegen.validation.DiagnosticMessageGenerator;
import dagger.internal.codegen.validation.DiagnosticMessageGenerator_Factory_Factory;
import dagger.internal.codegen.validation.DiagnosticReporterFactory_Factory;
import dagger.internal.codegen.validation.ExternalBindingGraphPlugins;
import dagger.internal.codegen.validation.ExternalBindingGraphPlugins_Factory;
import dagger.internal.codegen.validation.InjectBindingRegistryImpl_Factory;
import dagger.internal.codegen.validation.InjectValidator;
import dagger.internal.codegen.validation.InjectValidator_Factory;
import dagger.internal.codegen.validation.MapKeyValidator;
import dagger.internal.codegen.validation.MapKeyValidator_Factory;
import dagger.internal.codegen.validation.MembersInjectionValidator_Factory;
import dagger.internal.codegen.validation.ModuleValidator;
import dagger.internal.codegen.validation.ModuleValidator_Factory;
import dagger.internal.codegen.validation.MonitoringModuleGenerator_Factory;
import dagger.internal.codegen.validation.MonitoringModuleProcessingStep;
import dagger.internal.codegen.validation.MonitoringModuleProcessingStep_Factory;
import dagger.internal.codegen.validation.MultibindingAnnotationsProcessingStep;
import dagger.internal.codegen.validation.MultibindingAnnotationsProcessingStep_Factory;
import dagger.internal.codegen.validation.MultibindsMethodValidator_Factory;
import dagger.internal.codegen.validation.ProducesMethodValidator_Factory;
import dagger.internal.codegen.validation.ProvidesMethodValidator_Factory;
import dagger.internal.codegen.validation.ValidationBindingGraphPlugins;
import dagger.internal.codegen.validation.ValidationBindingGraphPlugins_Factory;
import dagger.internal.codegen.writing.AnnotationCreatorGenerator;
import dagger.internal.codegen.writing.AnnotationCreatorGenerator_Factory;
import dagger.internal.codegen.writing.AnonymousProviderCreationExpression_Factory;
import dagger.internal.codegen.writing.AnonymousProviderCreationExpression_Factory_Impl;
import dagger.internal.codegen.writing.AssistedFactoryRequestRepresentation_Factory;
import dagger.internal.codegen.writing.AssistedFactoryRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.AssistedPrivateMethodRequestRepresentation_Factory;
import dagger.internal.codegen.writing.AssistedPrivateMethodRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.ComponentCreatorImplementationFactory_Factory;
import dagger.internal.codegen.writing.ComponentImplementation;
import dagger.internal.codegen.writing.ComponentImplementation_Factory;
import dagger.internal.codegen.writing.ComponentInstanceRequestRepresentation_Factory;
import dagger.internal.codegen.writing.ComponentInstanceRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.ComponentMethodRequestRepresentation_Factory;
import dagger.internal.codegen.writing.ComponentMethodRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.ComponentNames;
import dagger.internal.codegen.writing.ComponentNames_Factory;
import dagger.internal.codegen.writing.ComponentProvisionRequestRepresentation_Factory;
import dagger.internal.codegen.writing.ComponentProvisionRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.ComponentRequestRepresentations;
import dagger.internal.codegen.writing.ComponentRequestRepresentations_Factory;
import dagger.internal.codegen.writing.ComponentRequirementExpressions;
import dagger.internal.codegen.writing.ComponentRequirementExpressions_Factory;
import dagger.internal.codegen.writing.ComponentRequirementRequestRepresentation_Factory;
import dagger.internal.codegen.writing.ComponentRequirementRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.DelegateRequestRepresentation_Factory;
import dagger.internal.codegen.writing.DelegateRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.DelegatingFrameworkInstanceCreationExpression_Factory;
import dagger.internal.codegen.writing.DelegatingFrameworkInstanceCreationExpression_Factory_Impl;
import dagger.internal.codegen.writing.DependencyMethodProducerCreationExpression_Factory;
import dagger.internal.codegen.writing.DependencyMethodProducerCreationExpression_Factory_Impl;
import dagger.internal.codegen.writing.DependencyMethodProviderCreationExpression_Factory;
import dagger.internal.codegen.writing.DependencyMethodProviderCreationExpression_Factory_Impl;
import dagger.internal.codegen.writing.DerivedFromFrameworkInstanceRequestRepresentation_Factory;
import dagger.internal.codegen.writing.DerivedFromFrameworkInstanceRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.FactoryGenerator;
import dagger.internal.codegen.writing.FactoryGenerator_Factory;
import dagger.internal.codegen.writing.ImmediateFutureRequestRepresentation_Factory;
import dagger.internal.codegen.writing.ImmediateFutureRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.InaccessibleMapKeyProxyGenerator;
import dagger.internal.codegen.writing.InaccessibleMapKeyProxyGenerator_Factory;
import dagger.internal.codegen.writing.InjectionOrProvisionProviderCreationExpression_Factory;
import dagger.internal.codegen.writing.InjectionOrProvisionProviderCreationExpression_Factory_Impl;
import dagger.internal.codegen.writing.LegacyBindingRepresentation_Factory;
import dagger.internal.codegen.writing.LegacyBindingRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.MapFactoryCreationExpression_Factory;
import dagger.internal.codegen.writing.MapFactoryCreationExpression_Factory_Impl;
import dagger.internal.codegen.writing.MapRequestRepresentation_Factory;
import dagger.internal.codegen.writing.MapRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.MembersInjectionMethods_Factory;
import dagger.internal.codegen.writing.MembersInjectionRequestRepresentation_Factory;
import dagger.internal.codegen.writing.MembersInjectionRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.MembersInjectorGenerator;
import dagger.internal.codegen.writing.MembersInjectorGenerator_Factory;
import dagger.internal.codegen.writing.MembersInjectorProviderCreationExpression_Factory;
import dagger.internal.codegen.writing.MembersInjectorProviderCreationExpression_Factory_Impl;
import dagger.internal.codegen.writing.ModuleProxies;
import dagger.internal.codegen.writing.ModuleProxies_Factory;
import dagger.internal.codegen.writing.ModuleProxies_ModuleConstructorProxyGenerator_Factory;
import dagger.internal.codegen.writing.OptionalFactories_Factory;
import dagger.internal.codegen.writing.OptionalFactories_PerGeneratedFileCache_Factory;
import dagger.internal.codegen.writing.OptionalFactoryInstanceCreationExpression_Factory;
import dagger.internal.codegen.writing.OptionalFactoryInstanceCreationExpression_Factory_Impl;
import dagger.internal.codegen.writing.OptionalRequestRepresentation_Factory;
import dagger.internal.codegen.writing.OptionalRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.PrivateMethodRequestRepresentation_Factory;
import dagger.internal.codegen.writing.PrivateMethodRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.ProducerCreationExpression_Factory;
import dagger.internal.codegen.writing.ProducerCreationExpression_Factory_Impl;
import dagger.internal.codegen.writing.ProducerFactoryGenerator;
import dagger.internal.codegen.writing.ProducerFactoryGenerator_Factory;
import dagger.internal.codegen.writing.ProducerFromProviderCreationExpression_Factory;
import dagger.internal.codegen.writing.ProducerFromProviderCreationExpression_Factory_Impl;
import dagger.internal.codegen.writing.ProducerNodeInstanceRequestRepresentation_Factory;
import dagger.internal.codegen.writing.ProducerNodeInstanceRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.ProviderInstanceRequestRepresentation_Factory;
import dagger.internal.codegen.writing.ProviderInstanceRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.SetFactoryCreationExpression_Factory;
import dagger.internal.codegen.writing.SetFactoryCreationExpression_Factory_Impl;
import dagger.internal.codegen.writing.SetRequestRepresentation_Factory;
import dagger.internal.codegen.writing.SetRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.SimpleMethodRequestRepresentation_Factory;
import dagger.internal.codegen.writing.SimpleMethodRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.SubcomponentCreatorRequestRepresentation_Factory;
import dagger.internal.codegen.writing.SubcomponentCreatorRequestRepresentation_Factory_Impl;
import dagger.internal.codegen.writing.UnscopedDirectInstanceRequestRepresentationFactory_Factory;
import dagger.internal.codegen.writing.UnscopedFrameworkInstanceCreationExpressionFactory_Factory;
import dagger.internal.codegen.writing.UnwrappedMapKeyGenerator;
import dagger.internal.codegen.writing.UnwrappedMapKeyGenerator_Factory;
import dagger.spi.model.BindingGraphPlugin;

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

    private Provider<KotlinMetadataFactory> kotlinMetadataFactoryProvider;

    private Provider<KotlinMetadataUtil> kotlinMetadataUtilProvider;

    private Provider<InjectionAnnotations> injectionAnnotationsProvider;

    @SuppressWarnings("rawtypes")
    private Provider membersInjectionValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider dependencyRequestValidatorProvider;

    private Provider<Map<String, String>> processingOptionsProvider;

    private Provider<ProcessingEnvironmentCompilerOptions> processingEnvironmentCompilerOptionsProvider;

    private Provider<CompilerOptions> bindCompilerOptionsProvider;

    private Provider<InjectValidator> injectValidatorProvider;

    private Provider<KeyFactory> keyFactoryProvider;

    private Provider<DependencyRequestFactory> dependencyRequestFactoryProvider;

    @SuppressWarnings("rawtypes")
    private Provider injectionSiteFactoryProvider;

    private Provider<BindingFactory> bindingFactoryProvider;
    @SuppressWarnings("rawtypes")
    private Provider injectBindingRegistryImplProvider;

    @SuppressWarnings("rawtypes")
    private Provider providesMethodValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider producesMethodValidatorProvider;

    private Provider<BindsTypeChecker> bindsTypeCheckerProvider;

    @SuppressWarnings("rawtypes")
    private Provider bindsMethodValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider multibindsMethodValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider bindsOptionalOfMethodValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider setOfBindingMethodValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider indexValidatorsProvider;

    private Provider<AnyBindingMethodValidator> anyBindingMethodValidatorProvider;

    private Provider<MethodSignatureFormatter> methodSignatureFormatterProvider;

    private Provider<MultibindingDeclaration.Factory> factoryProvider;

    private Provider<DelegateDeclaration.Factory> factoryProvider2;

    private Provider<SubcomponentDeclaration.Factory> factoryProvider3;

    @SuppressWarnings("rawtypes")
    private Provider factoryProvider4;

    private Provider<ModuleDescriptor.Factory> factoryProvider5;

    private Provider<ComponentDescriptorFactory> componentDescriptorFactoryProvider;

    private Provider<BindingDeclarationFormatter> bindingDeclarationFormatterProvider;

    @SuppressWarnings("rawtypes")
    private Provider bindingGraphConverterProvider;

    private Provider<BindingGraphFactory> bindingGraphFactoryProvider;

    private Provider<DependencyRequestFormatter> dependencyRequestFormatterProvider;

    private Provider<DiagnosticMessageGenerator.Factory> factoryProvider6;

    private Provider<CompositeBindingGraphPlugin.Factory> factoryProvider7;

    @SuppressWarnings("rawtypes")
    private Provider dependencyCycleValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider dependsOnProductionExecutorValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider duplicateBindingsValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider incompatiblyScopedBindingsValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider injectBindingValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider mapMultibindingValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider missingBindingValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider nullableBindingValidatorProvider;

    @SuppressWarnings("rawtypes")
    private Provider subcomponentFactoryMethodValidatorProvider;

    private Provider<ImmutableSet<dagger.spi.model.BindingGraphPlugin>> providePluginsProvider;

    @SuppressWarnings("rawtypes")
    private Provider diagnosticReporterFactoryProvider;

    private Provider<XFiler> filerProvider;

    private Provider<ValidationBindingGraphPlugins> validationBindingGraphPluginsProvider;

    private Provider<ImmutableSet<BindingGraphPlugin>> externalPluginsProvider;

    private Provider<ExternalBindingGraphPlugins> externalBindingGraphPluginsProvider;

    private Provider<BindingGraphValidator> bindingGraphValidatorProvider;

    private Provider<ModuleValidator> moduleValidatorProvider;

    private Provider<ComponentCreatorValidator> componentCreatorValidatorProvider;

    private Provider<ComponentValidator> componentValidatorProvider;

    private Provider<ModuleProxies> moduleProxiesProvider;

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

    private KotlinMetadataUtil kotlinMetadataUtil() {
        return KotlinMetadataUtil_Factory.newInstance(kotlinMetadataFactoryProvider.get());
    }

    private FactoryGenerator factoryGenerator() {
        return FactoryGenerator_Factory.newInstance(
                xFiler(),
                sourceVersion(),
                daggerTypesProvider.get(),
                daggerElementsProvider.get(),
                bindCompilerOptionsProvider.get(),
                kotlinMetadataUtil()
        );
    }


    private SourceFileGenerator<ProvisionBinding> sourceFileGeneratorOfProvisionBinding() {
        return SourceFileGeneratorsModule_FactoryGeneratorFactory.factoryGenerator(factoryGenerator(), bindCompilerOptionsProvider.get());
    }

    private MembersInjectorGenerator membersInjectorGenerator() {
        return MembersInjectorGenerator_Factory.newInstance(
                xFiler(),
                daggerElementsProvider.get(),
                daggerTypesProvider.get(),
                sourceVersion(),
                kotlinMetadataUtil()
        );
    }

    private SourceFileGenerator<MembersInjectionBinding> sourceFileGeneratorOfMembersInjectionBinding(
    ) {
        return SourceFileGeneratorsModule_MembersInjectorGeneratorFactory.membersInjectorGenerator(membersInjectorGenerator(), bindCompilerOptionsProvider.get());
    }

    private XMessager xMessager() {
        return ProcessingEnvironmentModule_MessagerFactory.messager(xProcessingEnv);
    }

    private MapKeyValidator mapKeyValidator() {
        return MapKeyValidator_Factory.newInstance(daggerElementsProvider.get());
    }

    private AnnotationCreatorGenerator annotationCreatorGenerator() {
        return AnnotationCreatorGenerator_Factory.newInstance(xFiler(), daggerElementsProvider.get(), sourceVersion());
    }

    private UnwrappedMapKeyGenerator unwrappedMapKeyGenerator() {
        return UnwrappedMapKeyGenerator_Factory.newInstance(xFiler(), daggerElementsProvider.get(), sourceVersion());
    }

    private MapKeyProcessingStep mapKeyProcessingStep() {
        return new MapKeyProcessingStep(
                xMessager(),
                daggerTypesProvider.get(),
                mapKeyValidator(),
                annotationCreatorGenerator(),
                unwrappedMapKeyGenerator()
        );
    }

    private InjectProcessingStep injectProcessingStep() {
        return new InjectProcessingStep((InjectBindingRegistry) injectBindingRegistryImplProvider.get());
    }

    private AssistedInjectProcessingStep assistedInjectProcessingStep() {
        return new AssistedInjectProcessingStep(daggerTypesProvider.get(), xMessager(), xProcessingEnv);
    }


    private InjectionAnnotations injectionAnnotations() {
        return InjectionAnnotations_Factory.newInstance(daggerElementsProvider.get(), kotlinMetadataUtil());
    }

    private KeyFactory keyFactory() {
        return KeyFactory_Factory.newInstance(daggerTypesProvider.get(), daggerElementsProvider.get(), injectionAnnotations());
    }

    private DependencyRequestFactory dependencyRequestFactory() {
        return DependencyRequestFactory_Factory.newInstance(keyFactory(), injectionAnnotations());
    }

    private Object injectionSiteFactory() {
        return InjectionSiteFactory_Factory.newInstance(
                daggerTypesProvider.get(),
                daggerElementsProvider.get(),
                dependencyRequestFactory()
        );
    }

    private BindingFactory bindingFactory() {
        return BindingFactory_Factory.newInstance(
                daggerTypesProvider.get(),
                daggerElementsProvider.get(),
                keyFactory(),
                dependencyRequestFactory(),
                injectionSiteFactory(),
                injectionAnnotations(),
                kotlinMetadataUtil()
        );
    }

    private AssistedFactoryProcessingStep assistedFactoryProcessingStep() {
        return new AssistedFactoryProcessingStep(
                xProcessingEnv,
                xMessager(),
                xFiler(),
                sourceVersion(),
                daggerElementsProvider.get(),
                daggerTypesProvider.get(),
                bindingFactory()
        );

    }

    private AssistedProcessingStep assistedProcessingStep() {
        return new AssistedProcessingStep(
                kotlinMetadataUtil(),
                injectionAnnotations(),
                daggerElementsProvider.get(),
                xMessager(),
                xProcessingEnv
        );
    }

    private Object monitoringModuleGenerator() {
        return MonitoringModuleGenerator_Factory.newInstance(xFiler(), daggerElementsProvider.get(), sourceVersion());
    }

    private MonitoringModuleProcessingStep monitoringModuleProcessingStep() {
        return MonitoringModuleProcessingStep_Factory.newInstance(xMessager(), monitoringModuleGenerator());
    }

    private MultibindingAnnotationsProcessingStep multibindingAnnotationsProcessingStep() {
        return MultibindingAnnotationsProcessingStep_Factory.newInstance(anyBindingMethodValidatorProvider.get(), xMessager());
    }

    private Object bindsInstanceMethodValidator() {
        return BindsInstanceMethodValidator_Factory.newInstance(injectionAnnotations());
    }

    private Object bindsInstanceParameterValidator() {
        return BindsInstanceParameterValidator_Factory.newInstance(injectionAnnotations());
    }

    private BindsInstanceProcessingStep bindsInstanceProcessingStep() {
        return BindsInstanceProcessingStep_Factory.newInstance(
                bindsInstanceMethodValidator(),
                bindsInstanceParameterValidator(),
                xMessager()
        );
    }

    private ProducerFactoryGenerator producerFactoryGenerator() {
        return ProducerFactoryGenerator_Factory.newInstance(
                xFiler(),
                daggerElementsProvider.get(),
                sourceVersion(),
                bindCompilerOptionsProvider.get(),
                keyFactory()
        );
    }

    private SourceFileGenerator<ProductionBinding> sourceFileGeneratorOfProductionBinding() {
        return SourceFileGeneratorsModule_ProducerFactoryGeneratorFactory.producerFactoryGenerator(
                producerFactoryGenerator(),
                bindCompilerOptionsProvider.get()
        );
    }

    private ModuleProxies moduleProxies() {
        return new ModuleProxies(daggerElementsProvider.get(), kotlinMetadataUtil());
    }

    private ModuleProxies.ModuleConstructorProxyGenerator moduleConstructorProxyGenerator() {
        return ModuleProxies_ModuleConstructorProxyGenerator_Factory.newInstance(xFiler(), daggerElementsProvider.get(), sourceVersion(), moduleProxies(), kotlinMetadataUtil());
    }

    private SourceFileGenerator<TypeElement> moduleGeneratorSourceFileGeneratorOfTypeElement() {
        return SourceFileGeneratorsModule_ModuleConstructorProxyGeneratorFactory.moduleConstructorProxyGenerator(
                moduleConstructorProxyGenerator(),
                bindCompilerOptionsProvider.get()
        );
    }

    private InaccessibleMapKeyProxyGenerator inaccessibleMapKeyProxyGenerator() {
        return InaccessibleMapKeyProxyGenerator_Factory.newInstance(
                xFiler(),
                daggerTypesProvider.get(),
                daggerElementsProvider.get(),
                sourceVersion()
        );
    }

    private DelegateDeclaration.Factory delegateDeclarationFactory() {
        return DelegateDeclaration_Factory_Factory.newInstance(daggerTypesProvider.get(), keyFactory(), dependencyRequestFactory());
    }

    private ModuleProcessingStep moduleProcessingStep() {
        return new ModuleProcessingStep(
                xMessager(),
                moduleValidatorProvider.get(),
                bindingFactory(),
                sourceFileGeneratorOfProvisionBinding(),
                sourceFileGeneratorOfProductionBinding(),
                moduleGeneratorSourceFileGeneratorOfTypeElement(),
                inaccessibleMapKeyProxyGenerator(),
                delegateDeclarationFactory(),
                kotlinMetadataUtil()
        );
    }

    private MethodSignatureFormatter methodSignatureFormatter() {
        return new MethodSignatureFormatter(daggerTypesProvider.get(), injectionAnnotations());
    }

    private Object componentHierarchyValidator() {
        return ComponentHierarchyValidator_Factory.newInstance(bindCompilerOptionsProvider.get());
    }

    private ComponentDescriptorValidator componentDescriptorValidator() {
        return ComponentDescriptorValidator_Factory.newInstance(
                daggerElementsProvider.get(),
                daggerTypesProvider.get(),
                bindCompilerOptionsProvider.get(),
                methodSignatureFormatter(),
                componentHierarchyValidator(),
                kotlinMetadataUtil()
        );
    }

    private ComponentDescriptorFactory componentDescriptorFactory() {
        return ComponentDescriptorFactory_Factory.newInstance(
                daggerElementsProvider.get(),
                daggerTypesProvider.get(),
                dependencyRequestFactory(),
                factoryProvider5.get(),
                injectionAnnotations()
        );
    }

    private Object componentGenerator() {
        return ComponentGenerator_Factory.newInstance(
                xFiler(),
                daggerElementsProvider.get(),
                sourceVersion(),
                new TopLevelImplementationComponentFactory(processorComponent)
        );
    }

    private ComponentProcessingStep componentProcessingStep() {
        return new ComponentProcessingStep(
                xMessager(),
                componentValidatorProvider.get(),
                componentCreatorValidatorProvider.get(),
                componentDescriptorValidator(),
                componentDescriptorFactory(),
                bindingGraphFactoryProvider.get(),
                (SourceFileGenerator<BindingGraph>) componentGenerator(),
                bindingGraphValidatorProvider.get()
        );
    }

    private Object componentHjarGenerator() {
        return ComponentHjarGenerator_Factory.newInstance(
                xFiler(),
                daggerElementsProvider.get(),
                daggerTypesProvider.get(),
                sourceVersion(),
                kotlinMetadataUtil()
        );
    }

    private ComponentHjarProcessingStep componentHjarProcessingStep() {
        return new ComponentHjarProcessingStep(
                xMessager(),
                componentValidatorProvider.get(),
                componentCreatorValidatorProvider.get(),
                componentDescriptorFactory(),
                (SourceFileGenerator<ComponentDescriptor>) componentHjarGenerator()
        );

    }

    private BindingMethodProcessingStep bindingMethodProcessingStep() {
        return BindingMethodProcessingStep_Factory.newInstance(xMessager(), anyBindingMethodValidatorProvider.get());
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

    private DependencyRequestFormatter dependencyRequestFormatter() {
        return DependencyRequestFormatter_Factory.newInstance(daggerTypesProvider.get());
    }

    private DiagnosticMessageGenerator.Factory diagnosticMessageGeneratorFactory() {
        return DiagnosticMessageGenerator_Factory_Factory.newInstance(
                daggerTypesProvider.get(),
                dependencyRequestFormatter(),
                ElementFormatter_Factory.newInstance()
        );
    }

    private Object diagnosticReporterFactory() {
        return DiagnosticReporterFactory_Factory.newInstance(xMessager(), diagnosticMessageGeneratorFactory());
    }


    private Map<String, String> processingOptionsMapOfStringAndString() {
        return ProcessingEnvironmentModule_ProcessingOptionsFactory.processingOptions(xProcessingEnv);
    }


    private BindingDeclarationFormatter bindingDeclarationFormatter() {
        return BindingDeclarationFormatter_Factory.newInstance(methodSignatureFormatter());
    }

    private Object mapMultibindingValidator() {
        return MapMultibindingValidator_Factory.newInstance(bindingDeclarationFormatter(), keyFactory());
    }

    private Object missingBindingValidator() {
        return MissingBindingValidator_Factory.newInstance(
                daggerTypesProvider.get(),
                (InjectBindingRegistry) injectBindingRegistryImplProvider.get(),
                dependencyRequestFormatter(),
                diagnosticMessageGeneratorFactory()
        );
    }

    private Object nullableBindingValidator() {
        return NullableBindingValidator_Factory.newInstance(bindCompilerOptionsProvider.get());
    }

    private Object subcomponentFactoryMethodValidator() {
        return SubcomponentFactoryMethodValidator_Factory.newInstance(daggerTypesProvider.get(), kotlinMetadataUtil());
    }

    private Object injectBindingValidator() {
        return InjectBindingValidator_Factory.newInstance(injectValidatorProvider.get());
    }

    private Object incompatiblyScopedBindingsValidator() {
        return IncompatiblyScopedBindingsValidator_Factory.newInstance(methodSignatureFormatter(), bindCompilerOptionsProvider.get());
    }

    private Object duplicateBindingsValidator() {
        return DuplicateBindingsValidator_Factory.newInstance(bindingDeclarationFormatter(), bindCompilerOptionsProvider.get());
    }

    private Object dependencyCycleValidator() {
        return DependencyCycleValidator_Factory.newInstance(dependencyRequestFormatter());
    }

    private Object dependsOnProductionExecutorValidator() {
        return DependsOnProductionExecutorValidator_Factory.newInstance(bindCompilerOptionsProvider.get(), keyFactory());
    }

    private CompositeBindingGraphPlugin.Factory compositeBindingGraphPluginFactory() {
        return CompositeBindingGraphPlugin_Factory_Factory.newInstance(diagnosticMessageGeneratorFactory());
    }

    private ImmutableSet<dagger.spi.model.BindingGraphPlugin> validationImmutableSetOfBindingGraphPlugin(
    ) {
        return BindingGraphValidationModule_ProvidePluginsFactory.providePlugins(
                compositeBindingGraphPluginFactory(),
                bindCompilerOptionsProvider.get(),
                dependencyCycleValidator(),
                dependsOnProductionExecutorValidator(),
                duplicateBindingsValidator(),
                incompatiblyScopedBindingsValidator(),
                injectBindingValidator(),
                mapMultibindingValidator(),
                missingBindingValidator(),
                nullableBindingValidator(),
                ProvisionDependencyOnProducerBindingValidator_Factory.newInstance(),
                SetMultibindingValidator_Factory.newInstance(),
                subcomponentFactoryMethodValidator()
        );
    }

    private ValidationBindingGraphPlugins validationBindingGraphPlugins() {
        return ValidationBindingGraphPlugins_Factory.newInstance(
                validationImmutableSetOfBindingGraphPlugin(),
                diagnosticReporterFactory(),
                xFiler(),
                daggerTypesProvider.get(),
                daggerElementsProvider.get(),
                bindCompilerOptionsProvider.get(),
                processingOptionsMapOfStringAndString()
        );
    }

    private ExternalBindingGraphPlugins externalBindingGraphPlugins() {
        return ExternalBindingGraphPlugins_Factory.newInstance(
                externalPlugins,
                diagnosticReporterFactory(),
                xFiler(),
                daggerTypesProvider.get(), daggerElementsProvider.get(),
                processingOptionsMapOfStringAndString()
        );
    }


    private Set<ClearableCache> setOfClearableCache() {
        return ImmutableSet.<ClearableCache>of(
                daggerElementsProvider.get(),
                anyBindingMethodValidatorProvider.get(),
                injectValidatorProvider.get(),
                factoryProvider5.get(),
                bindingGraphFactoryProvider.get(),
                componentValidatorProvider.get(),
                componentCreatorValidatorProvider.get(),
                kotlinMetadataFactoryProvider.get()
        );
    }

    @SuppressWarnings("unchecked")
    private void initialize(final XProcessingEnv xProcessingEnvParam,
                            final ImmutableSet<BindingGraphPlugin> externalPluginsParam) {

        this.xProcessingEnvProvider = InstanceFactory.create(xProcessingEnvParam);

        this.daggerElementsProvider = DoubleCheck.provider(ProcessingEnvironmentModule_DaggerElementsFactory.create(xProcessingEnvProvider));

        this.daggerTypesProvider = DoubleCheck.provider(ProcessingEnvironmentModule_DaggerTypesFactory.create(xProcessingEnvProvider, daggerElementsProvider));

        this.messagerProvider = ProcessingEnvironmentModule_MessagerFactory.create(xProcessingEnvProvider);

        this.kotlinMetadataFactoryProvider = DoubleCheck.provider(KotlinMetadataFactory_Factory.create());

        this.kotlinMetadataUtilProvider = KotlinMetadataUtil_Factory.create(kotlinMetadataFactoryProvider);

        this.injectionAnnotationsProvider = InjectionAnnotations_Factory.create(daggerElementsProvider, kotlinMetadataUtilProvider);

        this.membersInjectionValidatorProvider = MembersInjectionValidator_Factory.create(injectionAnnotationsProvider);

        this.dependencyRequestValidatorProvider = DependencyRequestValidator_Factory.create(membersInjectionValidatorProvider, injectionAnnotationsProvider, kotlinMetadataUtilProvider, daggerElementsProvider);

        this.processingOptionsProvider = ProcessingEnvironmentModule_ProcessingOptionsFactory.create(xProcessingEnvProvider);

        this.processingEnvironmentCompilerOptionsProvider = ProcessingEnvironmentCompilerOptions_Factory.create(messagerProvider, processingOptionsProvider, daggerElementsProvider);

        this.bindCompilerOptionsProvider = SingleCheck.provider((Provider) processingEnvironmentCompilerOptionsProvider);

        this.injectValidatorProvider = DoubleCheck.provider(InjectValidator_Factory.create(daggerTypesProvider, daggerElementsProvider, dependencyRequestValidatorProvider, bindCompilerOptionsProvider, injectionAnnotationsProvider, kotlinMetadataUtilProvider));

        this.keyFactoryProvider = KeyFactory_Factory.create(daggerTypesProvider, daggerElementsProvider, injectionAnnotationsProvider);

        this.dependencyRequestFactoryProvider = DependencyRequestFactory_Factory.create(keyFactoryProvider, injectionAnnotationsProvider);

        this.injectionSiteFactoryProvider = InjectionSiteFactory_Factory.create(daggerTypesProvider, daggerElementsProvider, dependencyRequestFactoryProvider);

        this.bindingFactoryProvider = BindingFactory_Factory.create(daggerTypesProvider, daggerElementsProvider, keyFactoryProvider, dependencyRequestFactoryProvider, injectionSiteFactoryProvider, injectionAnnotationsProvider, kotlinMetadataUtilProvider);

        this.injectBindingRegistryImplProvider = DoubleCheck.provider(InjectBindingRegistryImpl_Factory.create(daggerElementsProvider, daggerTypesProvider, messagerProvider, injectValidatorProvider, keyFactoryProvider, bindingFactoryProvider, bindCompilerOptionsProvider));

        this.providesMethodValidatorProvider = ProvidesMethodValidator_Factory.create(daggerElementsProvider, daggerTypesProvider, kotlinMetadataUtilProvider, dependencyRequestValidatorProvider, injectionAnnotationsProvider);

        this.producesMethodValidatorProvider = ProducesMethodValidator_Factory.create(daggerElementsProvider, daggerTypesProvider, kotlinMetadataUtilProvider, dependencyRequestValidatorProvider, injectionAnnotationsProvider);

        this.bindsTypeCheckerProvider = BindsTypeChecker_Factory.create(daggerTypesProvider, daggerElementsProvider);

        this.bindsMethodValidatorProvider = BindsMethodValidator_Factory.create(daggerElementsProvider, daggerTypesProvider, kotlinMetadataUtilProvider, bindsTypeCheckerProvider, dependencyRequestValidatorProvider, injectionAnnotationsProvider);

        this.multibindsMethodValidatorProvider = MultibindsMethodValidator_Factory.create(daggerElementsProvider, daggerTypesProvider, kotlinMetadataUtilProvider, dependencyRequestValidatorProvider, injectionAnnotationsProvider);

        this.bindsOptionalOfMethodValidatorProvider = BindsOptionalOfMethodValidator_Factory.create(daggerElementsProvider, daggerTypesProvider, kotlinMetadataUtilProvider, dependencyRequestValidatorProvider, injectionAnnotationsProvider);

        this.setOfBindingMethodValidatorProvider = SetFactory.builder(5, 0).addProvider((Provider) providesMethodValidatorProvider).addProvider((Provider) producesMethodValidatorProvider).addProvider((Provider) bindsMethodValidatorProvider).addProvider((Provider) multibindsMethodValidatorProvider).addProvider((Provider) bindsOptionalOfMethodValidatorProvider).build();

        this.indexValidatorsProvider = BindingMethodValidatorsModule_IndexValidatorsFactory.create(setOfBindingMethodValidatorProvider);

        this.anyBindingMethodValidatorProvider = DoubleCheck.provider(AnyBindingMethodValidator_Factory.create(indexValidatorsProvider));

        this.methodSignatureFormatterProvider = MethodSignatureFormatter_Factory.create(daggerTypesProvider, injectionAnnotationsProvider);

        this.factoryProvider = MultibindingDeclaration_Factory_Factory.create(daggerTypesProvider, keyFactoryProvider);

        this.factoryProvider2 = DelegateDeclaration_Factory_Factory.create(daggerTypesProvider, keyFactoryProvider, dependencyRequestFactoryProvider);

        this.factoryProvider3 = SubcomponentDeclaration_Factory_Factory.create(keyFactoryProvider);

        this.factoryProvider4 = OptionalBindingDeclaration_Factory_Factory.create(keyFactoryProvider);

        this.factoryProvider5 = DoubleCheck.provider(ModuleDescriptor_Factory_Factory.create(daggerElementsProvider, kotlinMetadataUtilProvider, bindingFactoryProvider, factoryProvider, factoryProvider2, factoryProvider3, factoryProvider4));

        this.componentDescriptorFactoryProvider = ComponentDescriptorFactory_Factory.create(daggerElementsProvider, daggerTypesProvider, dependencyRequestFactoryProvider, factoryProvider5, injectionAnnotationsProvider);

        this.bindingDeclarationFormatterProvider = BindingDeclarationFormatter_Factory.create(methodSignatureFormatterProvider);

        this.bindingGraphConverterProvider = BindingGraphConverter_Factory.create(bindingDeclarationFormatterProvider);

        this.bindingGraphFactoryProvider = DoubleCheck.provider(BindingGraphFactory_Factory.create(daggerElementsProvider, injectBindingRegistryImplProvider, keyFactoryProvider, bindingFactoryProvider, factoryProvider5, bindingGraphConverterProvider, bindCompilerOptionsProvider));

        this.dependencyRequestFormatterProvider = DependencyRequestFormatter_Factory.create(daggerTypesProvider);

        this.factoryProvider6 = DiagnosticMessageGenerator_Factory_Factory.create(daggerTypesProvider, dependencyRequestFormatterProvider, ElementFormatter_Factory.create());

        this.factoryProvider7 = CompositeBindingGraphPlugin_Factory_Factory.create(factoryProvider6);

        this.dependencyCycleValidatorProvider = DependencyCycleValidator_Factory.create(dependencyRequestFormatterProvider);

        this.dependsOnProductionExecutorValidatorProvider = DependsOnProductionExecutorValidator_Factory.create(bindCompilerOptionsProvider, keyFactoryProvider);

        this.duplicateBindingsValidatorProvider = DuplicateBindingsValidator_Factory.create(bindingDeclarationFormatterProvider, bindCompilerOptionsProvider);

        this.incompatiblyScopedBindingsValidatorProvider = IncompatiblyScopedBindingsValidator_Factory.create(methodSignatureFormatterProvider, bindCompilerOptionsProvider);

        this.injectBindingValidatorProvider = InjectBindingValidator_Factory.create(injectValidatorProvider);

        this.mapMultibindingValidatorProvider = MapMultibindingValidator_Factory.create(bindingDeclarationFormatterProvider, keyFactoryProvider);

        this.missingBindingValidatorProvider = MissingBindingValidator_Factory.create(daggerTypesProvider, injectBindingRegistryImplProvider, dependencyRequestFormatterProvider, factoryProvider6);

        this.nullableBindingValidatorProvider = NullableBindingValidator_Factory.create(bindCompilerOptionsProvider);

        this.subcomponentFactoryMethodValidatorProvider = SubcomponentFactoryMethodValidator_Factory.create(daggerTypesProvider, kotlinMetadataUtilProvider);

        this.providePluginsProvider = BindingGraphValidationModule_ProvidePluginsFactory.create(factoryProvider7, bindCompilerOptionsProvider, dependencyCycleValidatorProvider, dependsOnProductionExecutorValidatorProvider, duplicateBindingsValidatorProvider, incompatiblyScopedBindingsValidatorProvider, injectBindingValidatorProvider, mapMultibindingValidatorProvider, missingBindingValidatorProvider, nullableBindingValidatorProvider, ProvisionDependencyOnProducerBindingValidator_Factory.create(), SetMultibindingValidator_Factory.create(), subcomponentFactoryMethodValidatorProvider);

        this.diagnosticReporterFactoryProvider = DiagnosticReporterFactory_Factory.create(messagerProvider, factoryProvider6);

        this.filerProvider = ProcessingEnvironmentModule_FilerFactory.create(bindCompilerOptionsProvider, xProcessingEnvProvider);

        this.validationBindingGraphPluginsProvider = ValidationBindingGraphPlugins_Factory.create(providePluginsProvider, diagnosticReporterFactoryProvider, filerProvider, daggerTypesProvider, daggerElementsProvider, bindCompilerOptionsProvider, processingOptionsProvider);

        this.externalPluginsProvider = InstanceFactory.create(externalPluginsParam);

        this.externalBindingGraphPluginsProvider = ExternalBindingGraphPlugins_Factory.create(externalPluginsProvider, diagnosticReporterFactoryProvider, filerProvider, daggerTypesProvider, daggerElementsProvider, processingOptionsProvider);

        this.bindingGraphValidatorProvider = DoubleCheck.provider(BindingGraphValidator_Factory.create(validationBindingGraphPluginsProvider, externalBindingGraphPluginsProvider, bindCompilerOptionsProvider));

        this.moduleValidatorProvider = DoubleCheck.provider(ModuleValidator_Factory.create(daggerTypesProvider, daggerElementsProvider, anyBindingMethodValidatorProvider, methodSignatureFormatterProvider, componentDescriptorFactoryProvider, bindingGraphFactoryProvider, bindingGraphValidatorProvider, kotlinMetadataUtilProvider));

        this.componentCreatorValidatorProvider = DoubleCheck.provider(ComponentCreatorValidator_Factory.create(daggerElementsProvider, daggerTypesProvider, kotlinMetadataUtilProvider));

        this.componentValidatorProvider = DoubleCheck.provider(ComponentValidator_Factory.create(daggerElementsProvider, daggerTypesProvider, moduleValidatorProvider, componentCreatorValidatorProvider, dependencyRequestValidatorProvider, membersInjectionValidatorProvider, methodSignatureFormatterProvider, dependencyRequestFactoryProvider, kotlinMetadataUtilProvider));

        this.moduleProxiesProvider = ModuleProxies_Factory.create(daggerElementsProvider, kotlinMetadataUtilProvider);

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

    private static final class TopLevelImplementationComponentFactory implements TopLevelImplementationComponent.Factory {
        private final DaggerComponentProcessor_ProcessorComponent processorComponent;

        private TopLevelImplementationComponentFactory(
                DaggerComponentProcessor_ProcessorComponent processorComponent) {
            this.processorComponent = processorComponent;
        }

        @Override
        public TopLevelImplementationComponent create(BindingGraph bindingGraph) {
            Preconditions.checkNotNull(bindingGraph);
            return new TopLevelImplementationComponentImpl(processorComponent, bindingGraph);
        }
    }

    private static final class CurrentImplementationSubcomponentBuilder implements CurrentImplementationSubcomponent.Builder {
        private final DaggerComponentProcessor_ProcessorComponent processorComponent;

        private final TopLevelImplementationComponentImpl topLevelImplementationComponentImpl;

        private BindingGraph bindingGraph;

        private Optional<ComponentImplementation> parentImplementation;

        private Optional<ComponentRequestRepresentations> parentRequestRepresentations;

        private Optional<ComponentRequirementExpressions> parentRequirementExpressions;

        private CurrentImplementationSubcomponentBuilder(
                DaggerComponentProcessor_ProcessorComponent processorComponent,
                TopLevelImplementationComponentImpl topLevelImplementationComponentImpl) {
            this.processorComponent = processorComponent;
            this.topLevelImplementationComponentImpl = topLevelImplementationComponentImpl;
        }


        @Override
        public CurrentImplementationSubcomponentBuilder bindingGraph(BindingGraph bindingGraph) {
            this.bindingGraph = Preconditions.checkNotNull(bindingGraph);
            return this;
        }

        @Override
        public CurrentImplementationSubcomponentBuilder parentImplementation(
                Optional<ComponentImplementation> parentImplementation) {
            this.parentImplementation = Preconditions.checkNotNull(parentImplementation);
            return this;
        }

        @Override
        public CurrentImplementationSubcomponentBuilder parentRequestRepresentations(
                Optional<ComponentRequestRepresentations> parentRequestRepresentations) {
            this.parentRequestRepresentations = Preconditions.checkNotNull(parentRequestRepresentations);
            return this;
        }

        @Override
        public CurrentImplementationSubcomponentBuilder parentRequirementExpressions(
                Optional<ComponentRequirementExpressions> parentRequirementExpressions) {
            this.parentRequirementExpressions = Preconditions.checkNotNull(parentRequirementExpressions);
            return this;
        }

        @Override
        public CurrentImplementationSubcomponent build() {
            Preconditions.checkBuilderRequirement(bindingGraph, BindingGraph.class);
            Preconditions.checkBuilderRequirement(parentImplementation, Optional.class);
            Preconditions.checkBuilderRequirement(parentRequestRepresentations, Optional.class);
            Preconditions.checkBuilderRequirement(parentRequirementExpressions, Optional.class);
            return new CurrentImplementationSubcomponentImpl(
                    processorComponent,
                    topLevelImplementationComponentImpl,
                    bindingGraph,
                    parentImplementation,
                    parentRequestRepresentations,
                    parentRequirementExpressions
            );
        }
    }


    private static final class CurrentImplementationSubcomponentImpl implements CurrentImplementationSubcomponent {
        private final DaggerComponentProcessor_ProcessorComponent processorComponent;

        private final TopLevelImplementationComponentImpl topLevelImplementationComponentImpl;

        private final CurrentImplementationSubcomponentImpl currentImplementationSubcomponentImpl = this;

        private Provider<Optional<ComponentImplementation>> parentImplementationProvider;

        private Provider<ComponentImplementation> componentImplementationProvider;

        private Provider<Optional<ComponentRequestRepresentations>> parentRequestRepresentationsProvider;

        private Provider<BindingGraph> bindingGraphProvider;

        private Provider<Optional<ComponentRequirementExpressions>> parentRequirementExpressionsProvider;

        private Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider;

        @SuppressWarnings("rawtypes")
        private ComponentMethodRequestRepresentation_Factory componentMethodRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider;

        private Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

        @SuppressWarnings("rawtypes")
        private DelegateRequestRepresentation_Factory delegateRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider2;

        @SuppressWarnings("rawtypes")
        private DerivedFromFrameworkInstanceRequestRepresentation_Factory derivedFromFrameworkInstanceRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider3;

        @SuppressWarnings("rawtypes")
        private ImmediateFutureRequestRepresentation_Factory immediateFutureRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider4;

        @SuppressWarnings("rawtypes")
        private Provider membersInjectionMethodsProvider;

        @SuppressWarnings("rawtypes")
        private MembersInjectionRequestRepresentation_Factory membersInjectionRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider5;

        @SuppressWarnings("rawtypes")
        private PrivateMethodRequestRepresentation_Factory privateMethodRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider6;

        @SuppressWarnings("rawtypes")
        private AssistedPrivateMethodRequestRepresentation_Factory assistedPrivateMethodRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider7;

        @SuppressWarnings("rawtypes")
        private ProducerNodeInstanceRequestRepresentation_Factory producerNodeInstanceRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider8;

        @SuppressWarnings("rawtypes")
        private ProviderInstanceRequestRepresentation_Factory providerInstanceRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider9;

        @SuppressWarnings("rawtypes")
        private AssistedFactoryRequestRepresentation_Factory assistedFactoryRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider10;

        @SuppressWarnings("rawtypes")
        private ComponentInstanceRequestRepresentation_Factory componentInstanceRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider11;

        @SuppressWarnings("rawtypes")
        private ComponentProvisionRequestRepresentation_Factory componentProvisionRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider12;

        @SuppressWarnings("rawtypes")
        private ComponentRequirementRequestRepresentation_Factory componentRequirementRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider13;

        @SuppressWarnings("rawtypes")
        private MapRequestRepresentation_Factory mapRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider14;

        @SuppressWarnings("rawtypes")
        private OptionalRequestRepresentation_Factory optionalRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider15;

        @SuppressWarnings("rawtypes")
        private SetRequestRepresentation_Factory setRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider16;

        @SuppressWarnings("rawtypes")
        private SimpleMethodRequestRepresentation_Factory simpleMethodRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider17;

        @SuppressWarnings("rawtypes")
        private SubcomponentCreatorRequestRepresentation_Factory subcomponentCreatorRequestRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider18;

        @SuppressWarnings("rawtypes")
        private Provider unscopedDirectInstanceRequestRepresentationFactoryProvider;

        @SuppressWarnings("rawtypes")
        private ProducerFromProviderCreationExpression_Factory producerFromProviderCreationExpressionProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider19;

        @SuppressWarnings("rawtypes")
        private AnonymousProviderCreationExpression_Factory anonymousProviderCreationExpressionProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider20;

        @SuppressWarnings("rawtypes")
        private DelegatingFrameworkInstanceCreationExpression_Factory delegatingFrameworkInstanceCreationExpressionProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider21;

        @SuppressWarnings("rawtypes")
        private DependencyMethodProducerCreationExpression_Factory dependencyMethodProducerCreationExpressionProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider22;

        @SuppressWarnings("rawtypes")
        private DependencyMethodProviderCreationExpression_Factory dependencyMethodProviderCreationExpressionProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider23;

        @SuppressWarnings("rawtypes")
        private InjectionOrProvisionProviderCreationExpression_Factory injectionOrProvisionProviderCreationExpressionProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider24;

        @SuppressWarnings("rawtypes")
        private MapFactoryCreationExpression_Factory mapFactoryCreationExpressionProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider25;

        @SuppressWarnings("rawtypes")
        private MembersInjectorProviderCreationExpression_Factory membersInjectorProviderCreationExpressionProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider26;

        @SuppressWarnings("rawtypes")
        private Provider optionalFactoriesProvider;

        @SuppressWarnings("rawtypes")
        private OptionalFactoryInstanceCreationExpression_Factory optionalFactoryInstanceCreationExpressionProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider27;

        @SuppressWarnings("rawtypes")
        private ProducerCreationExpression_Factory producerCreationExpressionProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider28;

        @SuppressWarnings("rawtypes")
        private SetFactoryCreationExpression_Factory setFactoryCreationExpressionProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider29;

        @SuppressWarnings("rawtypes")
        private Provider unscopedFrameworkInstanceCreationExpressionFactoryProvider;

        @SuppressWarnings("rawtypes")
        private LegacyBindingRepresentation_Factory legacyBindingRepresentationProvider;

        @SuppressWarnings("rawtypes")
        private Provider factoryProvider30;

        private Provider<ComponentImplementation.ChildComponentImplementationFactory> provideChildComponentImplementationFactoryProvider;

        @SuppressWarnings("rawtypes")
        private Provider componentCreatorImplementationFactoryProvider;

        private Provider<ComponentNames> componentNamesProvider;

        private CurrentImplementationSubcomponentImpl(
                DaggerComponentProcessor_ProcessorComponent processorComponent,
                TopLevelImplementationComponentImpl topLevelImplementationComponentImpl,
                BindingGraph bindingGraphParam, Optional<ComponentImplementation> parentImplementationParam,
                Optional<ComponentRequestRepresentations> parentRequestRepresentationsParam,
                Optional<ComponentRequirementExpressions> parentRequirementExpressionsParam) {
            this.processorComponent = processorComponent;
            this.topLevelImplementationComponentImpl = topLevelImplementationComponentImpl;

            initialize(bindingGraphParam, parentImplementationParam, parentRequestRepresentationsParam, parentRequirementExpressionsParam);

        }


        @SuppressWarnings("unchecked")
        private void initialize(final BindingGraph bindingGraphParam,
                                final Optional<ComponentImplementation> parentImplementationParam,
                                final Optional<ComponentRequestRepresentations> parentRequestRepresentationsParam,
                                final Optional<ComponentRequirementExpressions> parentRequirementExpressionsParam) {

            this.parentImplementationProvider = InstanceFactory.create(parentImplementationParam);

            this.componentImplementationProvider = new DelegateFactory<>();

            this.parentRequestRepresentationsProvider = InstanceFactory.create(parentRequestRepresentationsParam);

            this.bindingGraphProvider = InstanceFactory.create(bindingGraphParam);

            this.parentRequirementExpressionsProvider = InstanceFactory.create(parentRequirementExpressionsParam);

            this.componentRequirementExpressionsProvider = DoubleCheck.provider(ComponentRequirementExpressions_Factory.create(
                    parentRequirementExpressionsProvider,
                    bindingGraphProvider,
                    componentImplementationProvider,
                    processorComponent.daggerElementsProvider,
                    processorComponent.moduleProxiesProvider
            ));

            this.componentMethodRequestRepresentationProvider = ComponentMethodRequestRepresentation_Factory.create(componentImplementationProvider, processorComponent.daggerTypesProvider);
            this.factoryProvider = ComponentMethodRequestRepresentation_Factory_Impl.create(componentMethodRequestRepresentationProvider);

            this.componentRequestRepresentationsProvider = new DelegateFactory<>();

            this.delegateRequestRepresentationProvider = DelegateRequestRepresentation_Factory.create(
                    componentRequestRepresentationsProvider,
                    processorComponent.daggerTypesProvider,
                    processorComponent.daggerElementsProvider
            );

            this.factoryProvider2 = DelegateRequestRepresentation_Factory_Impl.create(delegateRequestRepresentationProvider);

            this.derivedFromFrameworkInstanceRequestRepresentationProvider = DerivedFromFrameworkInstanceRequestRepresentation_Factory.create(componentRequestRepresentationsProvider, processorComponent.daggerTypesProvider);
            this.factoryProvider3 = DerivedFromFrameworkInstanceRequestRepresentation_Factory_Impl.create(derivedFromFrameworkInstanceRequestRepresentationProvider);

            this.immediateFutureRequestRepresentationProvider = ImmediateFutureRequestRepresentation_Factory.create(componentRequestRepresentationsProvider, processorComponent.daggerTypesProvider, processorComponent.sourceVersionProvider);
            this.factoryProvider4 = ImmediateFutureRequestRepresentation_Factory_Impl.create(immediateFutureRequestRepresentationProvider);

            this.membersInjectionMethodsProvider = DoubleCheck.provider(MembersInjectionMethods_Factory.create(componentImplementationProvider, componentRequestRepresentationsProvider, bindingGraphProvider, processorComponent.daggerElementsProvider, processorComponent.daggerTypesProvider, processorComponent.kotlinMetadataUtilProvider));
            this.membersInjectionRequestRepresentationProvider = MembersInjectionRequestRepresentation_Factory.create(membersInjectionMethodsProvider);
            this.factoryProvider5 = MembersInjectionRequestRepresentation_Factory_Impl.create(membersInjectionRequestRepresentationProvider);

            this.privateMethodRequestRepresentationProvider = PrivateMethodRequestRepresentation_Factory.create(componentImplementationProvider, processorComponent.daggerTypesProvider, processorComponent.bindCompilerOptionsProvider);
            this.factoryProvider6 = PrivateMethodRequestRepresentation_Factory_Impl.create(privateMethodRequestRepresentationProvider);

            this.assistedPrivateMethodRequestRepresentationProvider = AssistedPrivateMethodRequestRepresentation_Factory.create(componentImplementationProvider, processorComponent.daggerTypesProvider, processorComponent.bindCompilerOptionsProvider);
            this.factoryProvider7 = AssistedPrivateMethodRequestRepresentation_Factory_Impl.create(assistedPrivateMethodRequestRepresentationProvider);

            this.producerNodeInstanceRequestRepresentationProvider = ProducerNodeInstanceRequestRepresentation_Factory.create(processorComponent.daggerTypesProvider, processorComponent.daggerElementsProvider, componentImplementationProvider);
            this.factoryProvider8 = ProducerNodeInstanceRequestRepresentation_Factory_Impl.create(producerNodeInstanceRequestRepresentationProvider);

            this.providerInstanceRequestRepresentationProvider = ProviderInstanceRequestRepresentation_Factory.create(processorComponent.daggerTypesProvider, processorComponent.daggerElementsProvider);
            this.factoryProvider9 = ProviderInstanceRequestRepresentation_Factory_Impl.create(providerInstanceRequestRepresentationProvider);

            this.assistedFactoryRequestRepresentationProvider = AssistedFactoryRequestRepresentation_Factory.create(componentRequestRepresentationsProvider, processorComponent.daggerTypesProvider, processorComponent.daggerElementsProvider);
            this.factoryProvider10 = AssistedFactoryRequestRepresentation_Factory_Impl.create(assistedFactoryRequestRepresentationProvider);

            this.componentInstanceRequestRepresentationProvider = ComponentInstanceRequestRepresentation_Factory.create(componentImplementationProvider);
            this.factoryProvider11 = ComponentInstanceRequestRepresentation_Factory_Impl.create(componentInstanceRequestRepresentationProvider);

            this.componentProvisionRequestRepresentationProvider = ComponentProvisionRequestRepresentation_Factory.create(bindingGraphProvider, componentRequirementExpressionsProvider, processorComponent.bindCompilerOptionsProvider);
            this.factoryProvider12 = ComponentProvisionRequestRepresentation_Factory_Impl.create(componentProvisionRequestRepresentationProvider);

            this.componentRequirementRequestRepresentationProvider = ComponentRequirementRequestRepresentation_Factory.create(componentRequirementExpressionsProvider);
            this.factoryProvider13 = ComponentRequirementRequestRepresentation_Factory_Impl.create(componentRequirementRequestRepresentationProvider);

            this.mapRequestRepresentationProvider = MapRequestRepresentation_Factory.create(bindingGraphProvider, componentRequestRepresentationsProvider, processorComponent.daggerTypesProvider, processorComponent.daggerElementsProvider);
            this.factoryProvider14 = MapRequestRepresentation_Factory_Impl.create(mapRequestRepresentationProvider);

            this.optionalRequestRepresentationProvider = OptionalRequestRepresentation_Factory.create(componentRequestRepresentationsProvider, processorComponent.daggerTypesProvider, processorComponent.sourceVersionProvider);
            this.factoryProvider15 = OptionalRequestRepresentation_Factory_Impl.create(optionalRequestRepresentationProvider);

            this.setRequestRepresentationProvider = SetRequestRepresentation_Factory.create(bindingGraphProvider, componentRequestRepresentationsProvider, processorComponent.daggerTypesProvider, processorComponent.daggerElementsProvider);
            this.factoryProvider16 = SetRequestRepresentation_Factory_Impl.create(setRequestRepresentationProvider);

            this.simpleMethodRequestRepresentationProvider = SimpleMethodRequestRepresentation_Factory.create(membersInjectionMethodsProvider, processorComponent.bindCompilerOptionsProvider, componentRequestRepresentationsProvider, componentRequirementExpressionsProvider, processorComponent.sourceVersionProvider, processorComponent.kotlinMetadataUtilProvider, componentImplementationProvider);
            this.factoryProvider17 = SimpleMethodRequestRepresentation_Factory_Impl.create(simpleMethodRequestRepresentationProvider);

            this.subcomponentCreatorRequestRepresentationProvider = SubcomponentCreatorRequestRepresentation_Factory.create(componentImplementationProvider);
            this.factoryProvider18 = SubcomponentCreatorRequestRepresentation_Factory_Impl.create(subcomponentCreatorRequestRepresentationProvider);

            this.unscopedDirectInstanceRequestRepresentationFactoryProvider = UnscopedDirectInstanceRequestRepresentationFactory_Factory.create(componentImplementationProvider, factoryProvider10, factoryProvider11, factoryProvider12, factoryProvider13, factoryProvider2, factoryProvider14, factoryProvider15, factoryProvider16, factoryProvider17, factoryProvider18);
            this.producerFromProviderCreationExpressionProvider = ProducerFromProviderCreationExpression_Factory.create(componentImplementationProvider, componentRequestRepresentationsProvider);
            this.factoryProvider19 = ProducerFromProviderCreationExpression_Factory_Impl.create(producerFromProviderCreationExpressionProvider);

            this.anonymousProviderCreationExpressionProvider = AnonymousProviderCreationExpression_Factory.create(componentRequestRepresentationsProvider, componentImplementationProvider);
            this.factoryProvider20 = AnonymousProviderCreationExpression_Factory_Impl.create(anonymousProviderCreationExpressionProvider);

            this.delegatingFrameworkInstanceCreationExpressionProvider = DelegatingFrameworkInstanceCreationExpression_Factory.create(componentImplementationProvider, componentRequestRepresentationsProvider, processorComponent.bindCompilerOptionsProvider);
            this.factoryProvider21 = DelegatingFrameworkInstanceCreationExpression_Factory_Impl.create(delegatingFrameworkInstanceCreationExpressionProvider);

            this.dependencyMethodProducerCreationExpressionProvider = DependencyMethodProducerCreationExpression_Factory.create(componentImplementationProvider, componentRequirementExpressionsProvider, bindingGraphProvider);
            this.factoryProvider22 = DependencyMethodProducerCreationExpression_Factory_Impl.create(dependencyMethodProducerCreationExpressionProvider);

            this.dependencyMethodProviderCreationExpressionProvider = DependencyMethodProviderCreationExpression_Factory.create(componentImplementationProvider, componentRequirementExpressionsProvider, processorComponent.bindCompilerOptionsProvider, bindingGraphProvider);
            this.factoryProvider23 = DependencyMethodProviderCreationExpression_Factory_Impl.create(dependencyMethodProviderCreationExpressionProvider);

            this.injectionOrProvisionProviderCreationExpressionProvider = InjectionOrProvisionProviderCreationExpression_Factory.create(componentImplementationProvider, componentRequestRepresentationsProvider);
            this.factoryProvider24 = InjectionOrProvisionProviderCreationExpression_Factory_Impl.create(injectionOrProvisionProviderCreationExpressionProvider);

            this.mapFactoryCreationExpressionProvider = MapFactoryCreationExpression_Factory.create(componentImplementationProvider, componentRequestRepresentationsProvider, bindingGraphProvider, processorComponent.daggerElementsProvider);
            this.factoryProvider25 = MapFactoryCreationExpression_Factory_Impl.create(mapFactoryCreationExpressionProvider);

            this.membersInjectorProviderCreationExpressionProvider = MembersInjectorProviderCreationExpression_Factory.create(componentImplementationProvider, componentRequestRepresentationsProvider);
            this.factoryProvider26 = MembersInjectorProviderCreationExpression_Factory_Impl.create(membersInjectorProviderCreationExpressionProvider);

            this.optionalFactoriesProvider = OptionalFactories_Factory.create(topLevelImplementationComponentImpl.perGeneratedFileCacheProvider, componentImplementationProvider);
            this.optionalFactoryInstanceCreationExpressionProvider = OptionalFactoryInstanceCreationExpression_Factory.create(optionalFactoriesProvider, componentImplementationProvider, componentRequestRepresentationsProvider);
            this.factoryProvider27 = OptionalFactoryInstanceCreationExpression_Factory_Impl.create(optionalFactoryInstanceCreationExpressionProvider);

            this.producerCreationExpressionProvider = ProducerCreationExpression_Factory.create(componentImplementationProvider, componentRequestRepresentationsProvider);
            this.factoryProvider28 = ProducerCreationExpression_Factory_Impl.create(producerCreationExpressionProvider);

            this.setFactoryCreationExpressionProvider = SetFactoryCreationExpression_Factory.create(componentImplementationProvider, componentRequestRepresentationsProvider, bindingGraphProvider);
            this.factoryProvider29 = SetFactoryCreationExpression_Factory_Impl.create(setFactoryCreationExpressionProvider);

            this.unscopedFrameworkInstanceCreationExpressionFactoryProvider = UnscopedFrameworkInstanceCreationExpressionFactory_Factory.create(componentImplementationProvider, componentRequirementExpressionsProvider, factoryProvider20, factoryProvider21, factoryProvider22, factoryProvider23, factoryProvider24, factoryProvider25, factoryProvider26, factoryProvider27, factoryProvider28, factoryProvider29);
            this.legacyBindingRepresentationProvider = LegacyBindingRepresentation_Factory.create(bindingGraphProvider, componentImplementationProvider, factoryProvider, factoryProvider2, factoryProvider3, factoryProvider4, factoryProvider5, factoryProvider6, factoryProvider7, factoryProvider8, factoryProvider9, unscopedDirectInstanceRequestRepresentationFactoryProvider, factoryProvider19, unscopedFrameworkInstanceCreationExpressionFactoryProvider, processorComponent.daggerTypesProvider);
            this.factoryProvider30 = LegacyBindingRepresentation_Factory_Impl.create(legacyBindingRepresentationProvider);

            DelegateFactory.setDelegate(componentRequestRepresentationsProvider,
                    DoubleCheck.provider(
                            ComponentRequestRepresentations_Factory.create(
                                    parentRequestRepresentationsProvider,
                                    bindingGraphProvider,
                                    componentImplementationProvider,
                                    componentRequirementExpressionsProvider,
                                    factoryProvider30,
                                    processorComponent.daggerTypesProvider,
                                    processorComponent.bindCompilerOptionsProvider
                            )
                    )
            );

            this.provideChildComponentImplementationFactoryProvider =
                    CurrentImplementationSubcomponent_ChildComponentImplementationFactoryModule_ProvideChildComponentImplementationFactoryFactory.create(
                            topLevelImplementationComponentImpl.currentImplementationSubcomponentBuilderProvider,
                            componentImplementationProvider,
                            componentRequestRepresentationsProvider,
                            componentRequirementExpressionsProvider
                    );

            this.componentCreatorImplementationFactoryProvider = ComponentCreatorImplementationFactory_Factory.create(
                    componentImplementationProvider,
                    processorComponent.daggerElementsProvider,
                    processorComponent.daggerTypesProvider,
                    processorComponent.kotlinMetadataUtilProvider,
                    processorComponent.moduleProxiesProvider
            );

            this.componentNamesProvider = ComponentNames_Factory.create(
                    topLevelImplementationComponentImpl.bindingGraphProvider,
                    processorComponent.keyFactoryProvider
            );

            DelegateFactory.setDelegate(
                    componentImplementationProvider,
                    DoubleCheck.provider(
                            ComponentImplementation_Factory.create(
                                    parentImplementationProvider,
                                    provideChildComponentImplementationFactoryProvider,
                                    componentRequestRepresentationsProvider,
                                    componentCreatorImplementationFactoryProvider,
                                    bindingGraphProvider,
                                    componentNamesProvider,
                                    processorComponent.bindCompilerOptionsProvider,
                                    processorComponent.daggerElementsProvider,
                                    processorComponent.daggerTypesProvider,
                                    processorComponent.kotlinMetadataUtilProvider,
                                    processorComponent.messagerProvider
                            )
                    )
            );

        }

        @Override
        public ComponentImplementation componentImplementation() {
            return componentImplementationProvider.get();
        }
    }

    private static final class TopLevelImplementationComponentImpl implements TopLevelImplementationComponent {

        private final DaggerComponentProcessor_ProcessorComponent processorComponent;

        private final TopLevelImplementationComponentImpl topLevelImplementationComponentImpl = this;

        private Provider<CurrentImplementationSubcomponent.Builder> currentImplementationSubcomponentBuilderProvider;

        @SuppressWarnings("rawtypes")
        private Provider perGeneratedFileCacheProvider;

        private Provider<BindingGraph> bindingGraphProvider;

        private TopLevelImplementationComponentImpl(
                DaggerComponentProcessor_ProcessorComponent processorComponent,
                BindingGraph bindingGraphParam) {
            this.processorComponent = processorComponent;

            initialize(bindingGraphParam);

        }

        @SuppressWarnings("unchecked")
        private void initialize(final BindingGraph bindingGraphParam) {
            this.currentImplementationSubcomponentBuilderProvider = new Provider<CurrentImplementationSubcomponent.Builder>() {
                @Override
                public CurrentImplementationSubcomponent.Builder get() {
                    return new CurrentImplementationSubcomponentBuilder(processorComponent, topLevelImplementationComponentImpl);
                }
            };
            this.perGeneratedFileCacheProvider = DoubleCheck.provider(OptionalFactories_PerGeneratedFileCache_Factory.create());
            this.bindingGraphProvider = InstanceFactory.create(bindingGraphParam);
        }

        @Override
        public CurrentImplementationSubcomponent.Builder currentImplementationSubcomponentBuilder() {
            return new CurrentImplementationSubcomponentBuilder(processorComponent, topLevelImplementationComponentImpl);
        }
    }
}
