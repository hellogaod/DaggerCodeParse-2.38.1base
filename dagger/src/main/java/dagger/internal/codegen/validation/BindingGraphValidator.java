package dagger.internal.codegen.validation;


import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.compileroption.ValidationType;
import dagger.spi.model.BindingGraph;

/**
 * Validates a {@link BindingGraph}.
 */
@Singleton
public final class BindingGraphValidator {
    private final ValidationBindingGraphPlugins validationPlugins;
    private final ExternalBindingGraphPlugins externalPlugins;
    private final CompilerOptions compilerOptions;

    @Inject
    BindingGraphValidator(
            ValidationBindingGraphPlugins validationPlugins,
            ExternalBindingGraphPlugins externalPlugins,
            CompilerOptions compilerOptions) {
        this.validationPlugins = validationPlugins;
        this.externalPlugins = externalPlugins;
        this.compilerOptions = compilerOptions;
    }

    /**
     * Returns {@code true} if validation or analysis is required on the full binding graph.
     * <p>
     * 如果需要对完整绑定图进行验证或分析，则返回 {@code true}。
     */
    public boolean shouldDoFullBindingGraphValidation(TypeElement component) {
        return requiresFullBindingGraphValidation()
                || compilerOptions.pluginsVisitFullBindingGraphs(component);
    }

    private boolean requiresFullBindingGraphValidation() {
        return !compilerOptions.fullBindingGraphValidationType().equals(ValidationType.NONE);
    }

    /** Returns {@code true} if no errors are reported for {@code graph}. */
    public boolean isValid(BindingGraph graph) {
        return visitValidationPlugins(graph) && visitExternalPlugins(graph);
    }

    /** Returns {@code true} if validation plugins report no errors. */
    private boolean visitValidationPlugins(BindingGraph graph) {
        if (graph.isFullBindingGraph() && !requiresFullBindingGraphValidation()) {
            return true;
        }

        return validationPlugins.visit(graph);
    }

    /** Returns {@code true} if external plugins report no errors. */
    private boolean visitExternalPlugins(BindingGraph graph) {
        TypeElement component = graph.rootComponentNode().componentPath().currentComponent().java();
        if (graph.isFullBindingGraph()
                // TODO(b/135938915): Consider not visiting plugins if only
                // fullBindingGraphValidation is enabled.
                && !requiresFullBindingGraphValidation()
                && !compilerOptions.pluginsVisitFullBindingGraphs(component)) {
            return true;
        }

        return externalPlugins.visit(graph);
    }
}
