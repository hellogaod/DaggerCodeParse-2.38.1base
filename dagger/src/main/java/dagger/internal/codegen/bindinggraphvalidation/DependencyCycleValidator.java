package dagger.internal.codegen.bindinggraphvalidation;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableNetwork;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.base.MapType;
import dagger.internal.codegen.base.OptionalType;
import dagger.internal.codegen.binding.DependencyRequestFormatter;
import dagger.spi.model.Binding;
import dagger.spi.model.BindingGraph;
import dagger.spi.model.BindingGraphPlugin;
import dagger.spi.model.BindingKind;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.DiagnosticReporter;
import dagger.spi.model.RequestKind;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Iterables.limit;
import static com.google.common.collect.Iterables.skip;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static dagger.internal.codegen.base.RequestKinds.extractKeyType;
import static dagger.internal.codegen.base.RequestKinds.getRequestKind;
import static dagger.internal.codegen.extension.DaggerGraphs.shortestPath;
import static dagger.internal.codegen.extension.DaggerStreams.instancesOf;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static javax.tools.Diagnostic.Kind.ERROR;

/** Reports errors for dependency cycles. */
class DependencyCycleValidator implements BindingGraphPlugin {


    private final DependencyRequestFormatter dependencyRequestFormatter;

    @Inject
    DependencyCycleValidator(DependencyRequestFormatter dependencyRequestFormatter) {
        this.dependencyRequestFormatter = dependencyRequestFormatter;
    }

    @Override
    public String pluginName() {
        return "Dagger/DependencyCycle";
    }

    @Override
    public void visitGraph(BindingGraph bindingGraph, DiagnosticReporter diagnosticReporter) {
        ImmutableNetwork<BindingGraph.Node, BindingGraph.DependencyEdge> dependencyGraph =
                nonCycleBreakingDependencyGraph(bindingGraph);
        // First check the graph for a cycle. If there is one, then we'll do more work to report where.
        if (!Graphs.hasCycle(dependencyGraph)) {
            return;
        }
        // Check each endpoint pair only once, no matter how many parallel edges connect them.
        Set<EndpointPair<BindingGraph.Node>> dependencyEndpointPairs = dependencyGraph.asGraph().edges();
        Set<EndpointPair<BindingGraph.Node>> visited = newHashSetWithExpectedSize(dependencyEndpointPairs.size());
        for (EndpointPair<BindingGraph.Node> endpointPair : dependencyEndpointPairs) {
            cycleContainingEndpointPair(endpointPair, dependencyGraph, visited)
                    .ifPresent(cycle -> reportCycle(cycle, bindingGraph, diagnosticReporter));
        }
    }

    private Optional<Cycle<BindingGraph.Node>> cycleContainingEndpointPair(
            EndpointPair<BindingGraph.Node> endpoints,
            ImmutableNetwork<BindingGraph.Node, BindingGraph.DependencyEdge> dependencyGraph,
            Set<EndpointPair<BindingGraph.Node>> visited) {
        if (!visited.add(endpoints)) {
            // don't recheck endpoints we already know are part of a cycle
            return Optional.empty();
        }

        // If there's a path from the target back to the source, there's a cycle.
        ImmutableList<BindingGraph.Node> cycleNodes =
                shortestPath(dependencyGraph, endpoints.target(), endpoints.source());
        if (cycleNodes.isEmpty()) {
            return Optional.empty();
        }

        Cycle<BindingGraph.Node> cycle = Cycle.fromPath(cycleNodes);
        visited.addAll(cycle.endpointPairs()); // no need to check any edge in this cycle again
        return Optional.of(cycle);
    }

    /**
     * Reports a dependency cycle at the dependency into the cycle that is closest to an entry point.
     *
     * <p>For cycles found in reachable binding graphs, looks for the shortest path from the component
     * that contains the cycle (all bindings in a cycle must be in the same component; see below) to
     * some binding in the cycle. Then looks for the last dependency in that path that is not in the
     * cycle; that is the dependency that will be reported, so that the dependency trace will end just
     * before the cycle.
     *
     * <p>For cycles found during full binding graph validation, just reports the component that
     * contains the cycle.
     *
     * <p>Proof (by counterexample) that all bindings in a cycle must be in the same component: Assume
     * one binding in the cycle is in a parent component. Bindings cannot depend on bindings in child
     * components, so that binding cannot depend on the next binding in the cycle.
     */
    private void reportCycle(
            Cycle<BindingGraph.Node> cycle, BindingGraph bindingGraph, DiagnosticReporter diagnosticReporter) {
        if (bindingGraph.isFullBindingGraph()) {
            diagnosticReporter.reportComponent(
                    ERROR,
                    bindingGraph.componentNode(cycle.nodes().asList().get(0).componentPath()).get(),
                    errorMessage(cycle, bindingGraph));
            return;
        }

        ImmutableList<BindingGraph.Node> path = shortestPathToCycleFromAnEntryPoint(cycle, bindingGraph);
        BindingGraph.Node cycleStartNode = path.get(path.size() - 1);
        BindingGraph.Node previousNode = path.get(path.size() - 2);
        BindingGraph.DependencyEdge dependencyToReport =
                chooseDependencyEdgeConnecting(previousNode, cycleStartNode, bindingGraph);
        diagnosticReporter.reportDependency(
                ERROR, dependencyToReport, errorMessage(cycle.shift(cycleStartNode), bindingGraph));
    }

    private ImmutableList<BindingGraph.Node> shortestPathToCycleFromAnEntryPoint(
            Cycle<BindingGraph.Node> cycle, BindingGraph bindingGraph) {
        BindingGraph.Node someCycleNode = cycle.nodes().asList().get(0);
        BindingGraph.ComponentNode componentContainingCycle =
                bindingGraph.componentNode(someCycleNode.componentPath()).get();
        ImmutableList<BindingGraph.Node> pathToCycle =
                shortestPath(bindingGraph.network(), componentContainingCycle, someCycleNode);
        return subpathToCycle(pathToCycle, cycle);
    }

    /**
     * Returns the subpath from the head of {@code path} to the first node in {@code path} that's in
     * the cycle.
     */
    private ImmutableList<BindingGraph.Node> subpathToCycle(ImmutableList<BindingGraph.Node> path, Cycle<BindingGraph.Node> cycle) {
        ImmutableList.Builder<BindingGraph.Node> subpath = ImmutableList.builder();
        for (BindingGraph.Node node : path) {
            subpath.add(node);
            if (cycle.nodes().contains(node)) {
                return subpath.build();
            }
        }
        throw new IllegalArgumentException(
                "path " + path + " doesn't contain any nodes in cycle " + cycle);
    }

    private String errorMessage(Cycle<BindingGraph.Node> cycle, BindingGraph graph) {
        StringBuilder message = new StringBuilder("Found a dependency cycle:");
        ImmutableList<DependencyRequest> cycleRequests =
                cycle.endpointPairs().stream()
                        // TODO(dpb): Would be nice to take the dependency graph here.
                        .map(endpointPair -> nonCycleBreakingEdge(endpointPair, graph))
                        .map(BindingGraph.DependencyEdge::dependencyRequest)
                        .collect(toImmutableList())
                        .reverse();
        dependencyRequestFormatter.formatIndentedList(message, cycleRequests, 0);
        return message.toString();
    }

    /**
     * Returns one of the edges between two nodes that doesn't {@linkplain
     * #breaksCycle(BindingGraph.DependencyEdge, BindingGraph) break} a cycle.
     */
    private BindingGraph.DependencyEdge nonCycleBreakingEdge(EndpointPair<BindingGraph.Node> endpointPair, BindingGraph graph) {
        return graph.network().edgesConnecting(endpointPair.source(), endpointPair.target()).stream()
                .flatMap(instancesOf(BindingGraph.DependencyEdge.class))
                .filter(edge -> !breaksCycle(edge, graph))
                .findFirst()
                .get();
    }

    private boolean breaksCycle(BindingGraph.DependencyEdge edge, BindingGraph graph) {
        // Map<K, V> multibindings depend on Map<K, Provider<V>> entries, but those don't break any
        // cycles, so ignore them.
        if (edge.dependencyRequest().key().multibindingContributionIdentifier().isPresent()) {
            return false;
        }
        if (breaksCycle(
                edge.dependencyRequest().key().type().java(), edge.dependencyRequest().kind())) {
            return true;
        }
        BindingGraph.Node target = graph.network().incidentNodes(edge).target();
        if (target instanceof Binding && ((Binding) target).kind().equals(BindingKind.OPTIONAL)) {
            /* For @BindsOptionalOf bindings, unwrap the type inside the Optional. If the unwrapped type
             * breaks the cycle, so does the optional binding. */
            TypeMirror optionalValueType = OptionalType.from(edge.dependencyRequest().key()).valueType();
            RequestKind requestKind = getRequestKind(optionalValueType);
            return breaksCycle(extractKeyType(optionalValueType), requestKind);
        }
        return false;
    }

    private boolean breaksCycle(TypeMirror requestedType, RequestKind requestKind) {
        switch (requestKind) {
            case PROVIDER:
            case LAZY:
            case PROVIDER_OF_LAZY:
                return true;

            case INSTANCE:
                if (MapType.isMap(requestedType)) {
                    MapType mapType = MapType.from(requestedType);
                    return !mapType.isRawType() && mapType.valuesAreTypeOf(Provider.class);
                }
                // fall through

            default:
                return false;
        }
    }

    private BindingGraph.DependencyEdge chooseDependencyEdgeConnecting(
            BindingGraph.Node source, BindingGraph.Node target, BindingGraph bindingGraph) {
        return bindingGraph.network().edgesConnecting(source, target).stream()
                .flatMap(instancesOf(BindingGraph.DependencyEdge.class))
                .findFirst()
                .get();
    }

    /** Returns the subgraph containing only {@link BindingGraph.DependencyEdge}s that would not break a cycle. */
    // TODO(dpb): Return a network containing only Binding nodes.
    private ImmutableNetwork<BindingGraph.Node, BindingGraph.DependencyEdge> nonCycleBreakingDependencyGraph(
            BindingGraph bindingGraph) {
        MutableNetwork<BindingGraph.Node, BindingGraph.DependencyEdge> dependencyNetwork =
                NetworkBuilder.from(bindingGraph.network())
                        .expectedNodeCount(bindingGraph.network().nodes().size())
                        .expectedEdgeCount(bindingGraph.dependencyEdges().size())
                        .build();
        bindingGraph.dependencyEdges().stream()
                .filter(edge -> !breaksCycle(edge, bindingGraph))
                .forEach(
                        edge -> {
                            EndpointPair<BindingGraph.Node> endpoints = bindingGraph.network().incidentNodes(edge);
                            dependencyNetwork.addEdge(endpoints.source(), endpoints.target(), edge);
                        });
        return ImmutableNetwork.copyOf(dependencyNetwork);
    }

    /**
     * An ordered set of endpoint pairs representing the edges in the cycle. The target of each pair
     * is the source of the next pair. The target of the last pair is the source of the first pair.
     */
    @AutoValue
    abstract static class Cycle<N> {
        /**
         * The ordered set of endpoint pairs representing the edges in the cycle. The target of each
         * pair is the source of the next pair. The target of the last pair is the source of the first
         * pair.
         */
        abstract ImmutableSet<EndpointPair<N>> endpointPairs();

        /** Returns the nodes that participate in the cycle. */
        ImmutableSet<N> nodes() {
            return endpointPairs().stream()
                    .flatMap(pair -> Stream.of(pair.source(), pair.target()))
                    .collect(toImmutableSet());
        }

        /** Returns the number of edges in the cycle. */
        int size() {
            return endpointPairs().size();
        }

        /**
         * Shifts this cycle so that it starts with a specific node.
         *
         * @return a cycle equivalent to this one but whose first pair starts with {@code startNode}
         */
        Cycle<N> shift(N startNode) {
            int startIndex = Iterables.indexOf(endpointPairs(), pair -> pair.source().equals(startNode));
            checkArgument(
                    startIndex >= 0, "startNode (%s) is not part of this cycle: %s", startNode, this);
            if (startIndex == 0) {
                return this;
            }
            ImmutableSet.Builder<EndpointPair<N>> shifted = ImmutableSet.builder();
            shifted.addAll(skip(endpointPairs(), startIndex));
            shifted.addAll(limit(endpointPairs(), size() - startIndex));
            return new AutoValue_DependencyCycleValidator_Cycle<>(shifted.build());
        }

        @Override
        public final String toString() {
            return endpointPairs().toString();
        }

        /**
         * Creates a {@link Cycle} from a nonempty list of nodes, assuming there is an edge between each
         * pair of nodes as well as an edge from the last node to the first.
         */
        static <N> Cycle<N> fromPath(List<N> nodes) {
            checkArgument(!nodes.isEmpty());
            ImmutableSet.Builder<EndpointPair<N>> cycle = ImmutableSet.builder();
            cycle.add(EndpointPair.ordered(getLast(nodes), nodes.get(0)));
            for (int i = 0; i < nodes.size() - 1; i++) {
                cycle.add(EndpointPair.ordered(nodes.get(i), nodes.get(i + 1)));
            }
            return new AutoValue_DependencyCycleValidator_Cycle<>(cycle.build());
        }
    }
}
