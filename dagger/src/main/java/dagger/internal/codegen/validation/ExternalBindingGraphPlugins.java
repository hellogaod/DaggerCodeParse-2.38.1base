package dagger.internal.codegen.validation;


import com.google.common.collect.ImmutableSet;

import java.util.Map;

import javax.inject.Inject;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.codegen.compileroption.ProcessingOptions;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.BindingGraphPlugin;

/**
 * Initializes {@link BindingGraphPlugin}s.
 */
public final class ExternalBindingGraphPlugins {

    private final ImmutableSet<BindingGraphPlugin> plugins;
    private final DiagnosticReporterFactory diagnosticReporterFactory;
    private final XFiler filer;
    private final DaggerTypes types;
    private final DaggerElements elements;
    private final Map<String, String> processingOptions;

    @Inject
    ExternalBindingGraphPlugins(
            ImmutableSet<BindingGraphPlugin> plugins,
            DiagnosticReporterFactory diagnosticReporterFactory,
            XFiler filer,
            DaggerTypes types,
            DaggerElements elements,
            @ProcessingOptions Map<String, String> processingOptions) {
        this.plugins = plugins;
        this.diagnosticReporterFactory = diagnosticReporterFactory;
        this.filer = filer;
        this.types = types;
        this.elements = elements;
        this.processingOptions = processingOptions;
    }
}
