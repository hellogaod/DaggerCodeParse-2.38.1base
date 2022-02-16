package dagger.internal.codegen.bindinggraphvalidation;

import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.tools.Diagnostic;

import dagger.internal.codegen.base.Scopes;
import dagger.internal.codegen.binding.MethodSignatureFormatter;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.spi.model.Binding;
import dagger.spi.model.BindingGraph;
import dagger.spi.model.BindingGraphPlugin;
import dagger.spi.model.DiagnosticReporter;

import static dagger.internal.codegen.base.Formatter.INDENT;
import static dagger.internal.codegen.base.Scopes.getReadableSource;
import static dagger.internal.codegen.langmodel.DaggerElements.closestEnclosingTypeElement;
import static dagger.spi.model.BindingKind.INJECTION;
import static java.util.stream.Collectors.joining;
import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Reports an error for any component that uses bindings with scopes that are not assigned to the
 * component.
 */
final class IncompatiblyScopedBindingsValidator implements BindingGraphPlugin {

    private final MethodSignatureFormatter methodSignatureFormatter;
    private final CompilerOptions compilerOptions;

    @Inject
    IncompatiblyScopedBindingsValidator(
            MethodSignatureFormatter methodSignatureFormatter,
            CompilerOptions compilerOptions) {
        this.methodSignatureFormatter = methodSignatureFormatter;
        this.compilerOptions = compilerOptions;
    }

    @Override
    public String pluginName() {
        return "Dagger/IncompatiblyScopedBindings";
    }

    @Override
    public void visitGraph(BindingGraph bindingGraph, DiagnosticReporter diagnosticReporter) {
        ImmutableSetMultimap.Builder<BindingGraph.ComponentNode, dagger.spi.model.Binding> incompatibleBindings =
                ImmutableSetMultimap.builder();
        for (dagger.spi.model.Binding binding : bindingGraph.bindings()) {
            binding
                    .scope()
                    .filter(scope -> !scope.isReusable())
                    .ifPresent(
                            scope -> {
                                BindingGraph.ComponentNode componentNode =
                                        bindingGraph.componentNode(binding.componentPath()).get();
                                if (!componentNode.scopes().contains(scope)) {
                                    // @Inject bindings in module or subcomponent binding graphs will appear at the
                                    // properly scoped ancestor component, so ignore them here.
                                    if (binding.kind().equals(INJECTION)
                                            && (bindingGraph.rootComponentNode().isSubcomponent()
                                            || !bindingGraph.rootComponentNode().isRealComponent())) {
                                        return;
                                    }
                                    incompatibleBindings.put(componentNode, binding);
                                }
                            });
        }
        Multimaps.asMap(incompatibleBindings.build())
                .forEach((componentNode, bindings) -> report(componentNode, bindings, diagnosticReporter));
    }

    private void report(
            BindingGraph.ComponentNode componentNode,
            Set<Binding> bindings,
            DiagnosticReporter diagnosticReporter) {
        Diagnostic.Kind diagnosticKind = ERROR;
        StringBuilder message =
                new StringBuilder(
                        componentNode.componentPath().currentComponent().className().canonicalName());

        if (!componentNode.isRealComponent()) {
            // If the "component" is really a module, it will have no scopes attached. We want to report
            // if there is more than one scope in that component.
            if (bindings.stream().map(Binding::scope).map(Optional::get).distinct().count() <= 1) {
                return;
            }
            message.append(" contains bindings with different scopes:");
            diagnosticKind = compilerOptions.moduleHasDifferentScopesDiagnosticKind();
        } else if (componentNode.scopes().isEmpty()) {
            message.append(" (unscoped) may not reference scoped bindings:");
        } else {
            message
                    .append(" scoped with ")
                    .append(
                            componentNode.scopes().stream().map(Scopes::getReadableSource).collect(joining(" ")))
                    .append(" may not reference bindings with different scopes:");
        }

        // TODO(ronshapiro): Should we group by scope?
        for (Binding binding : bindings) {
            message.append('\n').append(INDENT);

            // TODO(dpb): Use BindingDeclarationFormatter.
            // But that doesn't print scopes for @Inject-constructed types.
            switch (binding.kind()) {
                case DELEGATE:
                case PROVISION:
                    message.append(
                            methodSignatureFormatter.format(
                                    MoreElements.asExecutable(binding.bindingElement().get().java())));
                    break;

                case INJECTION:
                    message
                            .append(getReadableSource(binding.scope().get()))
                            .append(" class ")
                            .append(
                                    closestEnclosingTypeElement(
                                            binding.bindingElement().get().java()).getQualifiedName());
                    break;

                default:
                    throw new AssertionError(binding);
            }
        }
        diagnosticReporter.reportComponent(diagnosticKind, componentNode, message.toString());
    }
}
