package dagger.internal.codegen.validation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.compileroption.CompilerOptions;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class BindingGraphValidator_Factory implements Factory<BindingGraphValidator> {
    private final Provider<ValidationBindingGraphPlugins> validationPluginsProvider;

    private final Provider<ExternalBindingGraphPlugins> externalPluginsProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    public BindingGraphValidator_Factory(
            Provider<ValidationBindingGraphPlugins> validationPluginsProvider,
            Provider<ExternalBindingGraphPlugins> externalPluginsProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        this.validationPluginsProvider = validationPluginsProvider;
        this.externalPluginsProvider = externalPluginsProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    @Override
    public BindingGraphValidator get() {
        return newInstance(validationPluginsProvider.get(), externalPluginsProvider.get(), compilerOptionsProvider.get());
    }

    public static BindingGraphValidator_Factory create(
            Provider<ValidationBindingGraphPlugins> validationPluginsProvider,
            Provider<ExternalBindingGraphPlugins> externalPluginsProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        return new BindingGraphValidator_Factory(validationPluginsProvider, externalPluginsProvider, compilerOptionsProvider);
    }

    public static BindingGraphValidator newInstance(ValidationBindingGraphPlugins validationPlugins,
                                                    ExternalBindingGraphPlugins externalPlugins, CompilerOptions compilerOptions) {
        return new BindingGraphValidator(validationPlugins, externalPlugins, compilerOptions);
    }
}
