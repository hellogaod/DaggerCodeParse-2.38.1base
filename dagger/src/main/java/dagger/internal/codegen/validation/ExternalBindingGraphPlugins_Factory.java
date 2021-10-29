package dagger.internal.codegen.validation;

import com.google.common.collect.ImmutableSet;

import java.util.Map;

import javax.annotation.Generated;
import javax.inject.Provider;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
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
public final class ExternalBindingGraphPlugins_Factory implements Factory<ExternalBindingGraphPlugins> {
    private final Provider<ImmutableSet<BindingGraphPlugin>> pluginsProvider;

    private final Provider<DiagnosticReporterFactory> diagnosticReporterFactoryProvider;

    private final Provider<XFiler> filerProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<Map<String, String>> processingOptionsProvider;

    public ExternalBindingGraphPlugins_Factory(
            Provider<ImmutableSet<BindingGraphPlugin>> pluginsProvider,
            Provider<DiagnosticReporterFactory> diagnosticReporterFactoryProvider,
            Provider<XFiler> filerProvider, Provider<DaggerTypes> typesProvider,
            Provider<DaggerElements> elementsProvider,
            Provider<Map<String, String>> processingOptionsProvider) {
        this.pluginsProvider = pluginsProvider;
        this.diagnosticReporterFactoryProvider = diagnosticReporterFactoryProvider;
        this.filerProvider = filerProvider;
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
        this.processingOptionsProvider = processingOptionsProvider;
    }

    @Override
    public ExternalBindingGraphPlugins get() {
        return newInstance(pluginsProvider.get(), diagnosticReporterFactoryProvider.get(), filerProvider.get(), typesProvider.get(), elementsProvider.get(), processingOptionsProvider.get());
    }

    public static ExternalBindingGraphPlugins_Factory create(
            Provider<ImmutableSet<BindingGraphPlugin>> pluginsProvider,
            Provider<DiagnosticReporterFactory> diagnosticReporterFactoryProvider,
            Provider<XFiler> filerProvider, Provider<DaggerTypes> typesProvider,
            Provider<DaggerElements> elementsProvider,
            Provider<Map<String, String>> processingOptionsProvider) {
        return new ExternalBindingGraphPlugins_Factory(pluginsProvider, diagnosticReporterFactoryProvider, filerProvider, typesProvider, elementsProvider, processingOptionsProvider);
    }

    public static ExternalBindingGraphPlugins newInstance(ImmutableSet<BindingGraphPlugin> plugins,
                                                          Object diagnosticReporterFactory, XFiler filer, DaggerTypes types, DaggerElements elements,
                                                          Map<String, String> processingOptions) {
        return new ExternalBindingGraphPlugins(plugins, (DiagnosticReporterFactory) diagnosticReporterFactory, filer, types, elements, processingOptions);
    }
}
