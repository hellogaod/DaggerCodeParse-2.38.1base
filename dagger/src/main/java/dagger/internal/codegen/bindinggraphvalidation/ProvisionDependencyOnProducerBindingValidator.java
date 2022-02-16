package dagger.internal.codegen.bindinggraphvalidation;

import java.util.stream.Stream;

import javax.inject.Inject;

import dagger.spi.model.Binding;
import dagger.spi.model.BindingGraph;
import dagger.spi.model.BindingGraphPlugin;
import dagger.spi.model.DiagnosticReporter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static dagger.internal.codegen.base.RequestKinds.canBeSatisfiedByProductionBinding;
import static dagger.internal.codegen.extension.DaggerStreams.instancesOf;
import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Reports an error for each provision-only dependency request that is satisfied by a production
 * binding.
 */
// TODO(b/29509141): Clarify the error.
final class ProvisionDependencyOnProducerBindingValidator implements BindingGraphPlugin {


    @Inject
    ProvisionDependencyOnProducerBindingValidator() {}

    @Override
    public String pluginName() {
        return "Dagger/ProviderDependsOnProducer";
    }

    @Override
    public void visitGraph(BindingGraph bindingGraph, DiagnosticReporter diagnosticReporter) {
        provisionDependenciesOnProductionBindings(bindingGraph)
                .forEach(
                        provisionDependent ->
                                diagnosticReporter.reportDependency(
                                        ERROR,
                                        provisionDependent,
                                        provisionDependent.isEntryPoint()
                                                ? entryPointErrorMessage(provisionDependent)
                                                : dependencyErrorMessage(provisionDependent, bindingGraph)));
    }

    private Stream<BindingGraph.DependencyEdge> provisionDependenciesOnProductionBindings(
            BindingGraph bindingGraph) {
        return bindingGraph.bindings().stream()
                .filter(binding -> binding.isProduction())
                .flatMap(binding -> incomingDependencies(binding, bindingGraph))
                .filter(edge -> !dependencyCanUseProduction(edge, bindingGraph));
    }

    /** Returns the dependencies on {@code binding}. */
    // TODO(dpb): Move to BindingGraph.
    private Stream<BindingGraph.DependencyEdge> incomingDependencies(Binding binding, BindingGraph bindingGraph) {
        return bindingGraph.network().inEdges(binding).stream()
                .flatMap(instancesOf(BindingGraph.DependencyEdge.class));
    }

    // TODO(ronshapiro): merge with MissingBindingValidator.dependencyCanUseProduction
    private boolean dependencyCanUseProduction(BindingGraph.DependencyEdge edge, BindingGraph bindingGraph) {
        return edge.isEntryPoint()
                ? canBeSatisfiedByProductionBinding(edge.dependencyRequest().kind())
                : bindingRequestingDependency(edge, bindingGraph).isProduction();
    }

    /**
     * Returns the binding that requests a dependency.
     *
     * @throws IllegalArgumentException if {@code dependency} is an {@linkplain
     *     BindingGraph.DependencyEdge#isEntryPoint() entry point}.
     */
    // TODO(dpb): Move to BindingGraph.
    private Binding bindingRequestingDependency(
            BindingGraph.DependencyEdge dependency, BindingGraph bindingGraph) {
        checkArgument(!dependency.isEntryPoint());
        BindingGraph.Node source = bindingGraph.network().incidentNodes(dependency).source();
        verify(
                source instanceof Binding,
                "expected source of %s to be a binding, but was: %s",
                dependency,
                source);
        return (Binding) source;
    }

    private String entryPointErrorMessage(BindingGraph.DependencyEdge entryPoint) {
        return String.format(
                "%s is a provision entry-point, which cannot depend on a production.",
                entryPoint.dependencyRequest().key());
    }

    private String dependencyErrorMessage(
            BindingGraph.DependencyEdge dependencyOnProduction, BindingGraph bindingGraph) {
        return String.format(
                "%s is a provision, which cannot depend on a production.",
                bindingRequestingDependency(dependencyOnProduction, bindingGraph).key());
    }
}
