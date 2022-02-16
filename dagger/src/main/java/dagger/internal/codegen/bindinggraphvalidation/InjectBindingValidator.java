package dagger.internal.codegen.bindinggraphvalidation;

import com.google.auto.common.MoreTypes;

import javax.inject.Inject;

import dagger.internal.codegen.validation.InjectValidator;
import dagger.internal.codegen.validation.ValidationReport;
import dagger.spi.model.Binding;
import dagger.spi.model.BindingGraph;
import dagger.spi.model.BindingGraphPlugin;
import dagger.spi.model.DiagnosticReporter;

import static dagger.spi.model.BindingKind.INJECTION;

/** Validates bindings from {@code @Inject}-annotated constructors. */
final class InjectBindingValidator implements BindingGraphPlugin {

    private final InjectValidator injectValidator;

    @Inject
    InjectBindingValidator(InjectValidator injectValidator) {
        this.injectValidator = injectValidator.whenGeneratingCode();
    }

    @Override
    public String pluginName() {
        return "Dagger/InjectBinding";
    }

    @Override
    public void visitGraph(BindingGraph bindingGraph, DiagnosticReporter diagnosticReporter) {
        bindingGraph.bindings().stream()
                .filter(binding -> binding.kind().equals(INJECTION)) // TODO(dpb): Move to BindingGraph
                .forEach(binding -> validateInjectionBinding(binding, diagnosticReporter));
    }

    private void validateInjectionBinding(Binding node, DiagnosticReporter diagnosticReporter) {
        ValidationReport typeReport =
                injectValidator.validateType(MoreTypes.asTypeElement(node.key().type().java()));
        for (ValidationReport.Item item : typeReport.allItems()) {
            diagnosticReporter.reportBinding(item.kind(), node, item.message());
        }
    }
}
