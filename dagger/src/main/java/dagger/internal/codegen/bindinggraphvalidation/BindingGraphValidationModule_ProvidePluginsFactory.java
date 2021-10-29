package dagger.internal.codegen.bindinggraphvalidation;


import com.google.common.collect.ImmutableSet;

import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.validation.CompositeBindingGraphPlugin;
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
public final class BindingGraphValidationModule_ProvidePluginsFactory implements Factory<ImmutableSet<BindingGraphPlugin>> {
    private final Provider<CompositeBindingGraphPlugin.Factory> factoryProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    private final Provider<DependencyCycleValidator> validation1Provider;

    private final Provider<DependsOnProductionExecutorValidator> validation2Provider;

    private final Provider<DuplicateBindingsValidator> validation3Provider;

    private final Provider<IncompatiblyScopedBindingsValidator> validation4Provider;

    private final Provider<InjectBindingValidator> validation5Provider;

    private final Provider<MapMultibindingValidator> validation6Provider;

    private final Provider<MissingBindingValidator> validation7Provider;

    private final Provider<NullableBindingValidator> validation8Provider;

    private final Provider<ProvisionDependencyOnProducerBindingValidator> validation9Provider;

    private final Provider<SetMultibindingValidator> validation10Provider;

    private final Provider<SubcomponentFactoryMethodValidator> validation11Provider;

    public BindingGraphValidationModule_ProvidePluginsFactory(
            Provider<CompositeBindingGraphPlugin.Factory> factoryProvider,
            Provider<CompilerOptions> compilerOptionsProvider,
            Provider<DependencyCycleValidator> validation1Provider,
            Provider<DependsOnProductionExecutorValidator> validation2Provider,
            Provider<DuplicateBindingsValidator> validation3Provider,
            Provider<IncompatiblyScopedBindingsValidator> validation4Provider,
            Provider<InjectBindingValidator> validation5Provider,
            Provider<MapMultibindingValidator> validation6Provider,
            Provider<MissingBindingValidator> validation7Provider,
            Provider<NullableBindingValidator> validation8Provider,
            Provider<ProvisionDependencyOnProducerBindingValidator> validation9Provider,
            Provider<SetMultibindingValidator> validation10Provider,
            Provider<SubcomponentFactoryMethodValidator> validation11Provider) {
        this.factoryProvider = factoryProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
        this.validation1Provider = validation1Provider;
        this.validation2Provider = validation2Provider;
        this.validation3Provider = validation3Provider;
        this.validation4Provider = validation4Provider;
        this.validation5Provider = validation5Provider;
        this.validation6Provider = validation6Provider;
        this.validation7Provider = validation7Provider;
        this.validation8Provider = validation8Provider;
        this.validation9Provider = validation9Provider;
        this.validation10Provider = validation10Provider;
        this.validation11Provider = validation11Provider;
    }

    @Override
    public ImmutableSet<BindingGraphPlugin> get() {
        return providePlugins(factoryProvider.get(), compilerOptionsProvider.get(), validation1Provider.get(), validation2Provider.get(), validation3Provider.get(), validation4Provider.get(), validation5Provider.get(), validation6Provider.get(), validation7Provider.get(), validation8Provider.get(), validation9Provider.get(), validation10Provider.get(), validation11Provider.get());
    }

    public static BindingGraphValidationModule_ProvidePluginsFactory create(
            Provider<CompositeBindingGraphPlugin.Factory> factoryProvider,
            Provider<CompilerOptions> compilerOptionsProvider,
            Provider<DependencyCycleValidator> validation1Provider,
            Provider<DependsOnProductionExecutorValidator> validation2Provider,
            Provider<DuplicateBindingsValidator> validation3Provider,
            Provider<IncompatiblyScopedBindingsValidator> validation4Provider,
            Provider<InjectBindingValidator> validation5Provider,
            Provider<MapMultibindingValidator> validation6Provider,
            Provider<MissingBindingValidator> validation7Provider,
            Provider<NullableBindingValidator> validation8Provider,
            Provider<ProvisionDependencyOnProducerBindingValidator> validation9Provider,
            Provider<SetMultibindingValidator> validation10Provider,
            Provider<SubcomponentFactoryMethodValidator> validation11Provider) {
        return new BindingGraphValidationModule_ProvidePluginsFactory(factoryProvider, compilerOptionsProvider, validation1Provider, validation2Provider, validation3Provider, validation4Provider, validation5Provider, validation6Provider, validation7Provider, validation8Provider, validation9Provider, validation10Provider, validation11Provider);
    }

    public static ImmutableSet<BindingGraphPlugin> providePlugins(
            CompositeBindingGraphPlugin.Factory factory, CompilerOptions compilerOptions,
            Object validation1, Object validation2, Object validation3, Object validation4,
            Object validation5, Object validation6, Object validation7, Object validation8,
            Object validation9, Object validation10, Object validation11) {
        return Preconditions.checkNotNullFromProvides(BindingGraphValidationModule.providePlugins(factory, compilerOptions, (DependencyCycleValidator) validation1, (DependsOnProductionExecutorValidator) validation2, (DuplicateBindingsValidator) validation3, (IncompatiblyScopedBindingsValidator) validation4, (InjectBindingValidator) validation5, (MapMultibindingValidator) validation6, (MissingBindingValidator) validation7, (NullableBindingValidator) validation8, (ProvisionDependencyOnProducerBindingValidator) validation9, (SetMultibindingValidator) validation10, (SubcomponentFactoryMethodValidator) validation11));
    }
}
