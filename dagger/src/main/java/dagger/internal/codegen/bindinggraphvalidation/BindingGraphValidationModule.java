package dagger.internal.codegen.bindinggraphvalidation;

import com.google.common.collect.ImmutableSet;

import dagger.Module;
import dagger.Provides;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.validation.CompositeBindingGraphPlugin;
import dagger.internal.codegen.validation.Validation;
import dagger.spi.model.BindingGraphPlugin;

/**
 * Binds the set of {@link BindingGraphPlugin}s used to implement Dagger validation.
 */
@Module
public interface BindingGraphValidationModule {

    @Provides
    @Validation
    static ImmutableSet<BindingGraphPlugin> providePlugins(
            CompositeBindingGraphPlugin.Factory factory,
            CompilerOptions compilerOptions,
            DependencyCycleValidator validation1,
            DependsOnProductionExecutorValidator validation2,
            DuplicateBindingsValidator validation3,
            IncompatiblyScopedBindingsValidator validation4,
            InjectBindingValidator validation5,
            MapMultibindingValidator validation6,
            MissingBindingValidator validation7,
            NullableBindingValidator validation8,
            ProvisionDependencyOnProducerBindingValidator validation9,
            SetMultibindingValidator validation10,
            SubcomponentFactoryMethodValidator validation11) {
        ImmutableSet<BindingGraphPlugin> plugins = ImmutableSet.of(
                validation1,
                validation2,
                validation3,
                validation4,
                validation5,
                validation6,
                validation7,
                validation8,
                validation9,
                validation10,
                validation11);
        if (compilerOptions.experimentalDaggerErrorMessages()) {
            return ImmutableSet.of(factory.create(plugins, "Dagger/Validation"));
        } else {
            return plugins;
        }
    }
}
