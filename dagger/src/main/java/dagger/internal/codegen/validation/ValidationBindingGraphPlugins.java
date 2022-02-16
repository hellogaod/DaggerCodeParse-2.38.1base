package dagger.internal.codegen.validation;

import com.google.common.collect.ImmutableSet;

import java.util.Map;

import javax.inject.Inject;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.compileroption.ProcessingOptions;
import dagger.internal.codegen.compileroption.ValidationType;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.BindingGraph;
import dagger.spi.model.BindingGraphPlugin;

import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Initializes {@link BindingGraphPlugin}s.
 * <p>
 * 初始化BindingGraphPlugin对象，并且校验
 */
public final class ValidationBindingGraphPlugins {
    private final ImmutableSet<BindingGraphPlugin> plugins;
    private final DiagnosticReporterFactory diagnosticReporterFactory;
    private final XFiler filer;
    private final DaggerTypes types;
    private final DaggerElements elements;
    private final CompilerOptions compilerOptions;
    private final Map<String, String> processingOptions;

    @Inject
    ValidationBindingGraphPlugins(
            @Validation ImmutableSet<BindingGraphPlugin> plugins,
            DiagnosticReporterFactory diagnosticReporterFactory,
            XFiler filer,
            DaggerTypes types,
            DaggerElements elements,
            CompilerOptions compilerOptions,
            @ProcessingOptions Map<String, String> processingOptions) {
        this.plugins = plugins;
        this.diagnosticReporterFactory = diagnosticReporterFactory;
        this.filer = filer;
        this.types = types;
        this.elements = elements;
        this.compilerOptions = compilerOptions;
        this.processingOptions = processingOptions;
    }

    /** Returns {@code false} if any of the plugins reported an error. */
    boolean visit(BindingGraph graph) {
        boolean errorsAsWarnings =
                graph.isFullBindingGraph()
                        && compilerOptions.fullBindingGraphValidationType().equals(ValidationType.WARNING);

        boolean isClean = true;
        for (BindingGraphPlugin plugin : plugins) {
            DiagnosticReporterFactory.DiagnosticReporterImpl reporter =
                    diagnosticReporterFactory.reporter(graph, plugin.pluginName(), errorsAsWarnings);
            plugin.visitGraph(graph, reporter);
            if (reporter.reportedDiagnosticKinds().contains(ERROR)) {
                isClean = false;
            }
        }
        return isClean;
    }
}
