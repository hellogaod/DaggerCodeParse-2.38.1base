package dagger.internal.codegen.binding;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.graph.ImmutableNetwork;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import dagger.spi.model.ComponentPath;
import dagger.spi.model.DaggerExecutableElement;
import dagger.spi.model.DaggerTypeElement;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.Key;

import static com.google.auto.common.MoreTypes.asTypeElement;
import static com.google.common.base.Verify.verify;
import static dagger.internal.codegen.binding.BindingRequest.bindingRequest;
import static dagger.internal.codegen.extension.DaggerGraphs.unreachableNodes;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.spi.model.BindingKind.SUBCOMPONENT_CREATOR;

/**
 * Converts {@link BindingGraph}s to {@link dagger.spi.model.BindingGraph}s.
 */
final class BindingGraphConverter {
    private final BindingDeclarationFormatter bindingDeclarationFormatter;

    @Inject
    BindingGraphConverter(BindingDeclarationFormatter bindingDeclarationFormatter) {
        this.bindingDeclarationFormatter = bindingDeclarationFormatter;
    }

    /**
     * Creates the external {@link dagger.spi.model.BindingGraph} representing the given internal
     * {@link BindingGraph}.
     */
    BindingGraph convert(LegacyBindingGraph legacyBindingGraph, boolean isFullBindingGraph) {


        MutableNetwork<dagger.spi.model.BindingGraph.Node, dagger.spi.model.BindingGraph.Edge> network = asNetwork(legacyBindingGraph);

        //??????????????????rootComponent???????????????
        dagger.spi.model.BindingGraph.ComponentNode rootNode = rootComponentNode(network);

        // When bindings are copied down into child graphs because they transitively depend on local
        // multibindings or optional bindings, the parent-owned binding is still there. If that
        // parent-owned binding is not reachable from its component, it doesn't need to be in the graph
        // because it will never be used. So remove all nodes that are not reachable from the root
        // component???unless we're converting a full binding graph.
        if (!isFullBindingGraph) {
            //??????graph?????????????????????rootNode????????????????????????
            unreachableNodes(network.asGraph(), rootNode).forEach(network::removeNode);
        }

        BindingGraph.TopLevelBindingGraph topLevelBindingGraph =
                BindingGraph.TopLevelBindingGraph.create(ImmutableNetwork.copyOf(network), isFullBindingGraph);


        return BindingGraph.create(rootNode, topLevelBindingGraph);
    }

    private MutableNetwork<dagger.spi.model.BindingGraph.Node, dagger.spi.model.BindingGraph.Edge> asNetwork(LegacyBindingGraph graph) {
        Converter converter = new Converter(bindingDeclarationFormatter);
        converter.visitRootComponent(graph);
        return converter.network;
    }

    // TODO(dpb): Example of BindingGraph logic applied to derived networks.
    private dagger.spi.model.BindingGraph.ComponentNode rootComponentNode(Network<dagger.spi.model.BindingGraph.Node, dagger.spi.model.BindingGraph.Edge> network) {
        return (dagger.spi.model.BindingGraph.ComponentNode)
                Iterables.find(
                        network.nodes(),
                        node -> node instanceof dagger.spi.model.BindingGraph.ComponentNode && node.componentPath().atRoot());
    }

    /**
     * Used as a cache key to make sure resolved bindings are cached per component path.
     * This is required so that binding nodes are not reused across different branches of the
     * graph since the ResolvedBindings class only contains the component and not the path.
     */
    @AutoValue
    abstract static class ResolvedBindingsWithPath {
        abstract ResolvedBindings resolvedBindings();

        abstract ComponentPath componentPath();

        static ResolvedBindingsWithPath create(
                ResolvedBindings resolvedBindings, ComponentPath componentPath) {
            return new AutoValue_BindingGraphConverter_ResolvedBindingsWithPath(
                    resolvedBindings, componentPath);
        }
    }

    private static final class Converter {
        /**
         * The path from the root graph to the currently visited graph.
         */
        private final Deque<LegacyBindingGraph> bindingGraphPath = new ArrayDeque<>();

        /**
         * The {@link ComponentPath} for each component in {@link #bindingGraphPath}.
         */
        private final Deque<ComponentPath> componentPaths = new ArrayDeque<>();

        private final BindingDeclarationFormatter bindingDeclarationFormatter;
        private final MutableNetwork<dagger.spi.model.BindingGraph.Node, dagger.spi.model.BindingGraph.Edge> network =
                NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build();
        private final Set<BindingNode> bindings = new HashSet<>();

        private final Map<ResolvedBindingsWithPath, ImmutableSet<BindingNode>> resolvedBindingsMap =
                new HashMap<>();

        /**
         * Constructs a converter for a root (component, not subcomponent) binding graph.
         */
        private Converter(BindingDeclarationFormatter bindingDeclarationFormatter) {
            this.bindingDeclarationFormatter = bindingDeclarationFormatter;
        }

        private void visitRootComponent(LegacyBindingGraph graph) {
            visitComponent(graph, null);
        }

        /**
         * Called once for each component in a component hierarchy.
         *
         * <p>This implementation does the following:
         *
         * <ol>
         *   <li>If this component is installed in its parent by a subcomponent factory method, calls
         *       {@link #visitSubcomponentFactoryMethod(dagger.spi.model.BindingGraph.ComponentNode, dagger.spi.model.BindingGraph.ComponentNode,
         *       ExecutableElement)}.
         *   <li>For each entry point in the component, calls {@link #visitEntryPoint(dagger.spi.model.BindingGraph.ComponentNode,
         *       DependencyRequest)}.
         *   <li>For each child component, calls {@link #visitComponent(LegacyBindingGraph,
         *       dagger.spi.model.BindingGraph.ComponentNode)}, updating the traversal state.
         * </ol>
         *
         * @param graph the currently visited graph
         */
        private void visitComponent(LegacyBindingGraph graph, dagger.spi.model.BindingGraph.ComponentNode parentComponent) {
            //?????????graph?????????????????????
            bindingGraphPath.addLast(graph);

            //bindingGraphPath?????????LegacyBindingGraph????????????component????????????ComponentPath??????
            ComponentPath graphPath =
                    ComponentPath.create(
                            bindingGraphPath.stream()
                                    .map(LegacyBindingGraph::componentDescriptor)
                                    .map(ComponentDescriptor::typeElement)
                                    .map(DaggerTypeElement::fromJava)
                                    .collect(toImmutableList()));

            componentPaths.addLast(graphPath);

            //ComponentNode???????????????????????????component???????????????componentPath ??? ??????component??????????????????
            dagger.spi.model.BindingGraph.ComponentNode currentComponent =
                    ComponentNodeImpl.create(componentPath(), graph.componentDescriptor());

            //??????currentComponent???????????????network????????????
            network.addNode(currentComponent);

            //??????graph??????component???????????????component????????????private??????static???abstract??????????????????????????????????????????????????????????????????
            for (ComponentDescriptor.ComponentMethodDescriptor entryPointMethod :
                    graph.componentDescriptor().entryPointMethods()) {

                //??????component???????????????componentNode??????????????????????????????????????????
                visitEntryPoint(currentComponent, entryPointMethod.dependencyRequest().get());
            }

            for (ResolvedBindings resolvedBindings : graph.resolvedBindings()) {

                for (BindingNode binding : bindingNodes(resolvedBindings)) {

                    if (bindings.add(binding)) {
                        network.addNode(binding);
                        for (DependencyRequest dependencyRequest : binding.dependencies()) {
                            addDependencyEdges(binding, dependencyRequest);
                        }
                    }

                    if (binding.kind().equals(SUBCOMPONENT_CREATOR)
                            && binding.componentPath().equals(currentComponent.componentPath())) {
                        network.addEdge(
                                binding,
                                subcomponentNode(binding.key().type().java(), graph),
                                new SubcomponentCreatorBindingEdgeImpl(
                                        resolvedBindings.subcomponentDeclarations()));
                    }
                }
            }

            if (bindingGraphPath.size() > 1) {
                LegacyBindingGraph parent = Iterators.get(bindingGraphPath.descendingIterator(), 1);
                parent
                        .componentDescriptor()
                        .getFactoryMethodForChildComponent(graph.componentDescriptor())
                        .ifPresent(
                                childFactoryMethod ->
                                        visitSubcomponentFactoryMethod(
                                                parentComponent, currentComponent, childFactoryMethod.methodElement()));
            }

            for (LegacyBindingGraph child : graph.subgraphs()) {
                visitComponent(child, currentComponent);
            }

            verify(bindingGraphPath.removeLast().equals(graph));
            verify(componentPaths.removeLast().equals(graphPath));
        }

        /**
         * Called once for each entry point in a component.
         *
         * @param componentNode the component that contains the entry point
         * @param entryPoint    the entry point to visit
         */
        private void visitEntryPoint(dagger.spi.model.BindingGraph.ComponentNode componentNode, DependencyRequest entryPoint) {
            addDependencyEdges(componentNode, entryPoint);
        }

        /**
         * Called if this component was installed in its parent by a subcomponent factory method.
         *
         * @param parentComponent  the parent graph
         * @param currentComponent the currently visited graph
         * @param factoryMethod    the factory method in the parent component that declares that the
         *                         current component is a child
         */
        private void visitSubcomponentFactoryMethod(
                dagger.spi.model.BindingGraph.ComponentNode parentComponent,
                dagger.spi.model.BindingGraph.ComponentNode currentComponent,
                ExecutableElement factoryMethod) {
            network.addEdge(
                    parentComponent,
                    currentComponent,
                    new ChildFactoryMethodEdgeImpl(DaggerExecutableElement.fromJava(factoryMethod)));
        }

        /**
         * Returns an immutable snapshot of the path from the root component to the currently visited
         * component.
         */
        private ComponentPath componentPath() {
            return componentPaths.getLast();
        }

        /**
         * Returns the subpath from the root component to the matching {@code ancestor} of the current
         * component.
         */
        private ComponentPath pathFromRootToAncestor(TypeElement ancestor) {
            for (ComponentPath componentPath : componentPaths) {
                if (componentPath.currentComponent().java().equals(ancestor)) {
                    return componentPath;
                }
            }
            throw new IllegalArgumentException(
                    String.format(
                            "%s is not in the current path: %s", ancestor.getQualifiedName(), componentPath()));
        }

        /**
         * Returns the LegacyBindingGraph for {@code ancestor}, where {@code ancestor} is in the
         * component path of the current traversal.
         * <p>
         * ???bindingGraphPath????????????ancestor???LegacyBindingGraph
         */
        private LegacyBindingGraph graphForAncestor(TypeElement ancestor) {
            for (LegacyBindingGraph graph : bindingGraphPath) {
                if (graph.componentDescriptor().typeElement().equals(ancestor)) {
                    return graph;
                }
            }
            throw new IllegalArgumentException(
                    String.format(
                            "%s is not in the current path: %s", ancestor.getQualifiedName(), componentPath()));
        }

        /**
         * Adds a {@link dagger.spi.model.BindingGraph.DependencyEdge} from a node to the binding(s)
         * that satisfy a dependency request.
         */
        private void addDependencyEdges(dagger.spi.model.BindingGraph.Node source, DependencyRequest dependencyRequest) {

            ResolvedBindings dependencies = resolvedDependencies(source, dependencyRequest);

            if (dependencies.isEmpty()) {
                addDependencyEdge(source, dependencyRequest, missingBindingNode(dependencies));
            } else {
                for (BindingNode dependency : bindingNodes(dependencies)) {
                    addDependencyEdge(source, dependencyRequest, dependency);
                }
            }
        }

        private void addDependencyEdge(
                dagger.spi.model.BindingGraph.Node source,
                DependencyRequest dependencyRequest,
                dagger.spi.model.BindingGraph.Node dependency) {

            //???????????????????????????
            network.addNode(dependency);

            //??????source->dependency??????????????????????????????
            if (!hasDependencyEdge(source, dependency, dependencyRequest)) {
                network.addEdge(
                        source,
                        dependency,
                        new DependencyEdgeImpl(dependencyRequest, source instanceof dagger.spi.model.BindingGraph.ComponentNode));
            }
        }

        private boolean hasDependencyEdge(
                dagger.spi.model.BindingGraph.Node source, dagger.spi.model.BindingGraph.Node dependency, DependencyRequest dependencyRequest) {
            // An iterative approach is used instead of a Stream because this method is called in a hot
            // loop, and the Stream calculates the size of network.edgesConnecting(), which is slow. This
            // seems to be because caculating the edges connecting two nodes in a Network that supports
            // parallel edges is must check the equality of many nodes, and BindingNode's equality
            // semantics drag in the equality of many other expensive objects
            for (dagger.spi.model.BindingGraph.Edge edge : network.edgesConnecting(source, dependency)) {
                if (edge instanceof dagger.spi.model.BindingGraph.DependencyEdge) {
                    if (((dagger.spi.model.BindingGraph.DependencyEdge) edge).dependencyRequest().equals(dependencyRequest)) {
                        return true;
                    }
                }
            }
            return false;
        }

        //???ProcessorComponent?????????inject??????????????????????????????ResolvedBindings?????????allMembersInjectionBindings?????????ComponentProcessor????????????MembersInjectionBinding??????
        private ResolvedBindings resolvedDependencies(
                dagger.spi.model.BindingGraph.Node source, DependencyRequest dependencyRequest) {

            //1.graphForAncestor?????????????????????source??????????????????componentPath??????????????????component???????????????LegacyBindingGraph??????
            //2.bindingRequest?????????component?????????????????????dependencyRequest???????????????key???kind??????BindingRequest??????
            //3.resolvedBindings???????????????RequestKind??????MEMBERS_INJECTION(?????????????????????????????????????????????????????????component????????????????????????void???????????????????????????????????????????????????)
            //(1)??????????????????LegacyBindingGraph???membersInjectionBindings???????????????
            //(2)?????????????????????LegacyBindingGraph???contributionBindings???????????????
            return graphForAncestor(source.componentPath().currentComponent().java())
                    .resolvedBindings(bindingRequest(dependencyRequest));
        }

        private ImmutableSet<BindingNode> bindingNodes(ResolvedBindings resolvedBindings) {
            //???ProcessorComponent?????????inject??????????????????????????????ResolvedBindings?????????allMembersInjectionBindings?????????ComponentProcessor????????????MembersInjectionBinding??????
            //ResolvedBindingsWithPath???????????????ResolvedBindings???ProcessorComponent???????????????ComponentPath??????
            ResolvedBindingsWithPath resolvedBindingsWithPath =
                    ResolvedBindingsWithPath.create(resolvedBindings, componentPath());

            return resolvedBindingsMap.computeIfAbsent(
                    resolvedBindingsWithPath, this::uncachedBindingNodes);
        }

        private ImmutableSet<BindingNode> uncachedBindingNodes(
                ResolvedBindingsWithPath resolvedBindingsWithPath) {

            ImmutableSet.Builder<BindingNode> bindingNodes = ImmutableSet.builder();
            resolvedBindingsWithPath.resolvedBindings()

                    .allBindings()
                    .asMap()
                    .forEach(
                            (component, bindings) -> {
                                for (Binding binding : bindings) {
                                    bindingNodes.add(
                                            bindingNode(resolvedBindingsWithPath.resolvedBindings(), binding, component));
                                }
                            });
            return bindingNodes.build();
        }

        private BindingNode bindingNode(
                ResolvedBindings resolvedBindings, Binding binding, TypeElement owningComponent) {
            return BindingNode.create(
                    pathFromRootToAncestor(owningComponent),
                    binding,
                    resolvedBindings.multibindingDeclarations(),
                    resolvedBindings.optionalBindingDeclarations(),
                    resolvedBindings.subcomponentDeclarations(),
                    bindingDeclarationFormatter);
        }

        private dagger.spi.model.BindingGraph.MissingBinding missingBindingNode(ResolvedBindings dependencies) {
            // Put all missing binding nodes in the root component. This simplifies the binding graph
            // and produces better error messages for users since all dependents point to the same node.
            return MissingBindingImpl.create(
                    ComponentPath.create(ImmutableList.of(componentPath().rootComponent())),
                    dependencies.key());
        }

        private dagger.spi.model.BindingGraph.ComponentNode subcomponentNode(
                TypeMirror subcomponentBuilderType, LegacyBindingGraph graph) {
            TypeElement subcomponentBuilderElement = asTypeElement(subcomponentBuilderType);
            ComponentDescriptor subcomponent =
                    graph.componentDescriptor().getChildComponentWithBuilderType(subcomponentBuilderElement);
            return ComponentNodeImpl.create(
                    componentPath().childPath(DaggerTypeElement.fromJava(subcomponent.typeElement())),
                    subcomponent);
        }
    }

    @AutoValue
    abstract static class MissingBindingImpl extends dagger.spi.model.BindingGraph.MissingBinding {
        static dagger.spi.model.BindingGraph.MissingBinding create(ComponentPath component, Key key) {
            return new AutoValue_BindingGraphConverter_MissingBindingImpl(component, key);
        }

        @Memoized
        @Override
        public abstract int hashCode();

        @Override
        public abstract boolean equals(Object o);
    }
}
