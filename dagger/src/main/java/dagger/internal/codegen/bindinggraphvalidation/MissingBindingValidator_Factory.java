package dagger.internal.codegen.bindinggraphvalidation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.DependencyRequestFormatter;
import dagger.internal.codegen.binding.InjectBindingRegistry;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.internal.codegen.validation.DiagnosticMessageGenerator;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class MissingBindingValidator_Factory implements Factory<MissingBindingValidator> {
    private final Provider<DaggerTypes> typesProvider;

    private final Provider<InjectBindingRegistry> injectBindingRegistryProvider;

    private final Provider<DependencyRequestFormatter> dependencyRequestFormatterProvider;

    private final Provider<DiagnosticMessageGenerator.Factory> diagnosticMessageGeneratorFactoryProvider;

    public MissingBindingValidator_Factory(Provider<DaggerTypes> typesProvider,
                                           Provider<InjectBindingRegistry> injectBindingRegistryProvider,
                                           Provider<DependencyRequestFormatter> dependencyRequestFormatterProvider,
                                           Provider<DiagnosticMessageGenerator.Factory> diagnosticMessageGeneratorFactoryProvider) {
        this.typesProvider = typesProvider;
        this.injectBindingRegistryProvider = injectBindingRegistryProvider;
        this.dependencyRequestFormatterProvider = dependencyRequestFormatterProvider;
        this.diagnosticMessageGeneratorFactoryProvider = diagnosticMessageGeneratorFactoryProvider;
    }

    @Override
    public MissingBindingValidator get() {
        return newInstance(typesProvider.get(), injectBindingRegistryProvider.get(), dependencyRequestFormatterProvider.get(), diagnosticMessageGeneratorFactoryProvider.get());
    }

    public static MissingBindingValidator_Factory create(Provider<DaggerTypes> typesProvider,
                                                         Provider<InjectBindingRegistry> injectBindingRegistryProvider,
                                                         Provider<DependencyRequestFormatter> dependencyRequestFormatterProvider,
                                                         Provider<DiagnosticMessageGenerator.Factory> diagnosticMessageGeneratorFactoryProvider) {
        return new MissingBindingValidator_Factory(typesProvider, injectBindingRegistryProvider, dependencyRequestFormatterProvider, diagnosticMessageGeneratorFactoryProvider);
    }

    public static MissingBindingValidator newInstance(DaggerTypes types,
                                                      InjectBindingRegistry injectBindingRegistry,
                                                      DependencyRequestFormatter dependencyRequestFormatter,
                                                      DiagnosticMessageGenerator.Factory diagnosticMessageGeneratorFactory) {
        return new MissingBindingValidator(types, injectBindingRegistry, dependencyRequestFormatter, diagnosticMessageGeneratorFactory);
    }
}
