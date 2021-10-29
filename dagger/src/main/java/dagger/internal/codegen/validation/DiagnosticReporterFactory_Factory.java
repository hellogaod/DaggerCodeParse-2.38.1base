package dagger.internal.codegen.validation;


import javax.annotation.Generated;
import javax.inject.Provider;

import androidx.room.compiler.processing.XMessager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class DiagnosticReporterFactory_Factory implements Factory<DiagnosticReporterFactory> {
    private final Provider<XMessager> messagerProvider;

    private final Provider<DiagnosticMessageGenerator.Factory> diagnosticMessageGeneratorFactoryProvider;

    public DiagnosticReporterFactory_Factory(Provider<XMessager> messagerProvider,
                                             Provider<DiagnosticMessageGenerator.Factory> diagnosticMessageGeneratorFactoryProvider) {
        this.messagerProvider = messagerProvider;
        this.diagnosticMessageGeneratorFactoryProvider = diagnosticMessageGeneratorFactoryProvider;
    }

    @Override
    public DiagnosticReporterFactory get() {
        return newInstance(messagerProvider.get(), diagnosticMessageGeneratorFactoryProvider.get());
    }

    public static DiagnosticReporterFactory_Factory create(Provider<XMessager> messagerProvider,
                                                           Provider<DiagnosticMessageGenerator.Factory> diagnosticMessageGeneratorFactoryProvider) {
        return new DiagnosticReporterFactory_Factory(messagerProvider, diagnosticMessageGeneratorFactoryProvider);
    }

    public static DiagnosticReporterFactory newInstance(XMessager messager,
                                                        DiagnosticMessageGenerator.Factory diagnosticMessageGeneratorFactory) {
        return new DiagnosticReporterFactory(messager, diagnosticMessageGeneratorFactory);
    }
}
