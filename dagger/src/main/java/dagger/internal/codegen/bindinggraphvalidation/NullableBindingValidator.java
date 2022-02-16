package dagger.internal.codegen.bindinggraphvalidation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;

import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.spi.model.Binding;
import dagger.spi.model.BindingGraph;
import dagger.spi.model.BindingGraphPlugin;
import dagger.spi.model.DiagnosticReporter;

import static dagger.internal.codegen.extension.DaggerStreams.instancesOf;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/**
 * Reports errors or warnings (depending on the {@code -Adagger.nullableValidation} value) for each
 * non-nullable dependency request that is satisfied by a nullable binding.
 */
final class NullableBindingValidator implements BindingGraphPlugin {

    private final CompilerOptions compilerOptions;

    @Inject
    NullableBindingValidator(CompilerOptions compilerOptions) {
        this.compilerOptions = compilerOptions;
    }

    @Override
    public void visitGraph(BindingGraph bindingGraph, DiagnosticReporter diagnosticReporter) {
        for (Binding binding : nullableBindings(bindingGraph)) {
            for (BindingGraph.DependencyEdge dependencyEdge : nonNullableDependencies(bindingGraph, binding)) {
                diagnosticReporter.reportDependency(
                        compilerOptions.nullableValidationKind(),
                        dependencyEdge,
                        nullableToNonNullable(
                                binding.key().toString(),
                                binding.toString())); // binding.toString() will include the @Nullable
            }
        }
    }

    @Override
    public String pluginName() {
        return "Dagger/Nullable";
    }

    private ImmutableList<Binding> nullableBindings(BindingGraph bindingGraph) {
        return bindingGraph.bindings().stream()
                .filter(binding -> binding.isNullable())
                .collect(toImmutableList());
    }

    private ImmutableSet<BindingGraph.DependencyEdge> nonNullableDependencies(
            BindingGraph bindingGraph, Binding binding) {
        return bindingGraph.network().inEdges(binding).stream()
                .flatMap(instancesOf(BindingGraph.DependencyEdge.class))
                .filter(edge -> !edge.dependencyRequest().isNullable())
                .collect(toImmutableSet());
    }

    @VisibleForTesting
    static String nullableToNonNullable(String key, String binding) {
        return String.format("%s is not nullable, but is being provided by %s", key, binding);
    }
}
