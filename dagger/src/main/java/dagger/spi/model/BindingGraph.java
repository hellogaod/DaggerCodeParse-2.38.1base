package dagger.spi.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ImmutableNetwork;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;

import java.util.Optional;
import java.util.stream.Stream;

import dagger.Module;

import static com.google.common.collect.Sets.intersection;
import static com.google.common.graph.Graphs.inducedSubgraph;
import static com.google.common.graph.Graphs.reachableNodes;
import static com.google.common.graph.Graphs.transpose;
import static dagger.internal.codegen.extension.DaggerStreams.instancesOf;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSetMultimap;

/**
 * A graph of bindings, dependency requests, and components.
 * <p>
 * 绑定，依赖请求和components形成的有向图
 *
 * <p>A {@link BindingGraph} represents one of the following:
 * <p>
 * 下面表示的是有向图三种呈现形式
 *
 * <ul>
 *   <li>an entire component hierarchy rooted at a {@link dagger.Component} or {@link
 *       dagger.producers.ProductionComponent}
 * <p>
 *       从根Component或ProductionComponent开始关联的整个结构生成有向图
 *
 *   <li>a partial component hierarchy rooted at a {@link dagger.Subcomponent} or {@link
 *       dagger.producers.ProductionSubcomponent} (only when the value of {@code
 *       -Adagger.fullBindingGraphValidation} is not {@code NONE})
 * <p>
 *      以Subcomponent或ProductionSubcomponent为根关联的部分结构生成有向图
 *
 *   <li>the bindings installed by a {@link Module} or {@link dagger.producers.ProducerModule},
 *       including all subcomponents generated by {@link Module#subcomponents()} ()} and {@link
 *       dagger.producers.ProducerModule#subcomponents()} ()}
 * <p>
 *      Module或ProducerModule#subcomponents里面所有subcomponent组件的实例化构建有向图
 * </ul>
 * <p>
 * In the case of a {@link BindingGraph} representing a module, the root {@link ComponentNode} will
 * actually represent the module type. The graph will also be a {@linkplain #isFullBindingGraph()
 * full binding graph}, which means it will contain all bindings in all modules, as well as nodes
 * for their dependencies. Otherwise it will contain only bindings that are reachable from at least
 * one {@linkplain #entryPointEdges() entry point}.
 * <p>
 * 在 {@link BindingGraph} 代表模块的情况下，根 {@link ComponentNode} 将实际代表模块类型。
 * 该图还将是一个 {@linkplain #isFullBindingGraph() 完整绑定图}，这意味着它将包含所有模块中的所有绑定，以及它们的依赖项的节点。
 * 否则，它将仅包含可从至少一个 {@linkplain #entryPointEdges() 入口点}访问的绑定。
 *
 * <h3>Nodes</h3>
 *
 * <p>There is a <b>{@link Binding}</b> for each owned binding in the graph. If a binding is owned
 * by more than one component, there is one binding object for that binding for every owning
 * component.
 * <p>
 * 对于图中的每个拥有的绑定，都有一个 <b>{@link Binding}</b>。 如果一个绑定被多个组件拥有，那么每个拥有组件的绑定都有一个绑定对象。
 *
 * <p>There is a <b>{@linkplain ComponentNode component node}</b> (without a binding) for each
 * component in the graph.
 * <p>
 * 图中的每个组件都有一个<b>{@linkplain ComponentNode 组件节点}</b>（没有绑定）。
 *
 * <h3>Edges</h3>
 *
 * <p>There is a <b>{@linkplain DependencyEdge dependency edge}</b> for each dependency request in
 * the graph. Its target node is the binding for the binding that satisfies the request. For entry
 * point dependency requests, the source node is the component node for the component for which it
 * is an entry point. For other dependency requests, the source node is the binding for the binding
 * that contains the request.
 * <p>
 * 对于图中的每个依赖请求，都有一个 <b>{@linkplain DependencyEdge 依赖边}</b>。它的目标节点是满足请求的绑定的绑定。
 * 对于入口点依赖请求，源节点是它作为入口点的组件的组件节点。对于其他依赖请求，源节点是包含请求的绑定的绑定。
 *
 * <p>There is a <b>subcomponent edge</b> for each parent-child component relationship in the graph.
 * The target node is the component node for the child component. For subcomponents defined by a
 * {@linkplain SubcomponentCreatorBindingEdge subcomponent creator binding} (either a method on the
 * component or a set of {@code @Module.subcomponents} annotation values), the source node is the
 * binding for the {@code @Subcomponent.Builder} type. For subcomponents defined by {@linkplain
 * ChildFactoryMethodEdge subcomponent factory methods}, the source node is the component node for
 * the parent.
 * <p>
 * 对于图中的每个父子组件关系，都有一个<b>子组件边</b>。目标节点是子组件的组件节点。
 * 对于由 {@linkplain SubcomponentCreatorBindingEdge 子组件创建者绑定}（组件上的方法或一组 {@code @Module.subcomponents} 注释值）定义的子组件，
 * 源节点是 {@code @Subcomponent 的绑定。生成器} 类型。对于由 {@linkplain ChildFactoryMethodEdge subcomponent factory methods} 定义的子组件，
 * 源节点是父节点的组件节点。
 *
 * <p><b>Note that this API is experimental and will change.</b>
 *  <p><b>请注意，此 API 是实验性的，会发生变化。</b>
 */
public abstract class BindingGraph {
    /**
     * Returns the graph in its {@link Network} representation.
     */
    public abstract ImmutableNetwork<Node, Edge> network();

    @Override
    public String toString() {
        return network().toString();
    }

    /**
     * Returns {@code true} if this graph was constructed from a module for full binding graph
     * validation.
     * <p>
     * 如果此图是从用于完整绑定图验证的模块构建的。
     *
     * @deprecated use {@link #isFullBindingGraph()} to tell if this is a full binding graph, or
     * {@link ComponentNode#isRealComponent() rootComponentNode().isRealComponent()} to tell if
     * the root component node is really a component or derived from a module. Dagger can generate
     * full binding graphs for components and subcomponents as well as modules.
     */
    @Deprecated
    public boolean isModuleBindingGraph() {
        return !rootComponentNode().isRealComponent();
    }

    /**
     * Returns {@code true} if this is a full binding graph, which contains all bindings installed in
     * the component, or {@code false} if it is a reachable binding graph, which contains only
     * bindings that are reachable from at least one {@linkplain #entryPointEdges() entry point}.
     * <p>
     * 包含组件中安装的所有绑定为true;返回false表示它是一个可达的绑定图，仅仅包含至少一个入口可达绑定
     *
     * @see <a href="https://dagger.dev/compiler-options#full-binding-graph-validation">Full binding
     * graph validation</a>
     */
    public abstract boolean isFullBindingGraph();

    /**
     * Returns {@code true} if the {@link #rootComponentNode()} is a subcomponent. This occurs in
     * when {@code -Adagger.fullBindingGraphValidation} is used in a compilation with a subcomponent.
     *
     * @deprecated use {@link ComponentNode#isSubcomponent() rootComponentNode().isSubcomponent()}
     * instead
     */
    @Deprecated
    public boolean isPartialBindingGraph() {
        return rootComponentNode().isSubcomponent();
    }

    /**
     * Returns the bindings.
     * <p>
     * Binding类型节点收集
     */
    public ImmutableSet<Binding> bindings() {
        return nodes(Binding.class);
    }

    /**
     * Returns the bindings for a key.
     */
    public ImmutableSet<Binding> bindings(Key key) {
        return nodes(Binding.class).stream()
                .filter(binding -> binding.key().equals(key))
                .collect(toImmutableSet());
    }

    /** Returns the bindings that directly request a given binding as a dependency. */
    public ImmutableSet<Binding> requestingBindings(MaybeBinding binding) {
        return network().predecessors(binding).stream()
                .flatMap(instancesOf(Binding.class))
                .collect(toImmutableSet());
    }

    /**
     * Returns the bindings that a given binding directly requests as a dependency. Does not include
     * any {@link MissingBinding}s.
     *
     * @see #requestedMaybeMissingBindings(Binding)
     */
    public ImmutableSet<Binding> requestedBindings(Binding binding) {
        return network().successors(binding).stream()
                .flatMap(instancesOf(Binding.class))
                .collect(toImmutableSet());
    }

    /**
     * Returns the nodes that represent missing bindings.
     */
    public ImmutableSet<MissingBinding> missingBindings() {
        return nodes(MissingBinding.class);
    }

    /**
     * Returns the component nodes.
     * <p>
     * ComponentNode类型节点
     */
    public ImmutableSet<ComponentNode> componentNodes() {
        return nodes(ComponentNode.class);
    }

    /**
     * Returns the component node for a component.
     */
    public Optional<ComponentNode> componentNode(ComponentPath component) {
        return componentNodes().stream()
                .filter(node -> node.componentPath().equals(component))
                .findFirst();
    }

    /**
     * Returns the component nodes for a component.
     */
    public ImmutableSet<ComponentNode> componentNodes(DaggerTypeElement component) {
        return componentNodes().stream()
                .filter(node -> node.componentPath().currentComponent().equals(component))
                .collect(toImmutableSet());
    }

    /**
     * Returns the component node for the root component.
     */
    public ComponentNode rootComponentNode() {
        return componentNodes().stream()
                .filter(node -> node.componentPath().atRoot())
                .findFirst()
                .get();
    }

    /**
     * Returns the dependency edges.
     * <p>
     * 图形上所有DependencyEdge边
     */
    public ImmutableSet<DependencyEdge> dependencyEdges() {
        return dependencyEdgeStream().collect(toImmutableSet());
    }

    /**
     * Returns the dependency edges for the dependencies of a binding. For valid graphs, each {@link
     * DependencyRequest} will map to a single {@link DependencyEdge}. When conflicting bindings exist
     * for a key, the multimap will have several edges for that {@link DependencyRequest}. Graphs that
     * have no binding for a key will have an edge whose {@linkplain EndpointPair#target() target
     * node} is a {@link MissingBinding}.
     */
    public ImmutableSetMultimap<DependencyRequest, DependencyEdge> dependencyEdges(
            Binding binding) {
        //当前Binding节点指向其他节点的边，如果该边是DependencyEdge那么收集
        //Map<K,V> K:表示该边里面的依赖，V：DependencyEdge边
        return dependencyEdgeStream(binding)
                .collect(toImmutableSetMultimap(DependencyEdge::dependencyRequest, edge -> edge));
    }

    /**
     * Returns the dependency edges for a dependency request.
     */
    public ImmutableSet<DependencyEdge> dependencyEdges(DependencyRequest dependencyRequest) {
        return dependencyEdgeStream()
                .filter(edge -> edge.dependencyRequest().equals(dependencyRequest))
                .collect(toImmutableSet());
    }

    /**
     * Returns the dependency edges for the entry points of a given {@code component}. Each edge's
     * source node is that component's component node.
     *
     * <p>
     * 入口边：
     * 收集ComponentNode所有节点中找到component匹配的节点，该节点指向其他的节点的所有边，在这些边中收集DependencyEdge边
     */
    public ImmutableSet<DependencyEdge> entryPointEdges(ComponentPath component) {
        return dependencyEdgeStream(componentNode(component).get()).collect(toImmutableSet());
    }

    //当前node节点指向其他节点的边，并且该边是一个DependencyEdge边
    private Stream<DependencyEdge> dependencyEdgeStream(Node node) {
        return network().outEdges(node).stream().flatMap(instancesOf(DependencyEdge.class));
    }

    /**
     * Returns the dependency edges for all entry points for all components and subcomponents. Each
     * edge's source node is a component node.
     * <p>
     * 所有DependencyEdge边，筛选isEntryPoint = true
     */
    public ImmutableSet<DependencyEdge> entryPointEdges() {
        return entryPointEdgeStream().collect(toImmutableSet());
    }

    /**
     * Returns the binding or missing binding nodes that directly satisfy entry points.
     * <p>
     * 所有DependencyEdge边，筛选isEntryPoint = true。在这些筛选的边找边指向的节点是MaybeBinding
     */
    public ImmutableSet<MaybeBinding> entryPointBindings() {
        return entryPointEdgeStream()
                .map(edge -> (MaybeBinding) network().incidentNodes(edge).target())
                .collect(toImmutableSet());
    }

    /**
     * Returns the edges for entry points that transitively depend on a binding or missing binding for
     * a key.
     */
    public ImmutableSet<DependencyEdge> entryPointEdgesDependingOnBinding(
            MaybeBinding binding) {
        ImmutableNetwork<Node, DependencyEdge> dependencyGraph = dependencyGraph();
        Network<Node, DependencyEdge> subgraphDependingOnBinding =
                inducedSubgraph(
                        dependencyGraph, reachableNodes(transpose(dependencyGraph).asGraph(), binding));
        return intersection(entryPointEdges(), subgraphDependingOnBinding.edges()).immutableCopy();
    }

    /**
     * Returns a subnetwork that contains all nodes but only {@link DependencyEdge}s.
     * <p>
     * 返回包含所有节点但仅包含 {@link DependencyEdge}边 的子网。
     */
    // TODO(dpb): Make public. Cache.
    private ImmutableNetwork<Node, DependencyEdge> dependencyGraph() {

        MutableNetwork<Node, DependencyEdge> dependencyGraph =
                NetworkBuilder.from(network())
                        .expectedNodeCount(network().nodes().size())
                        .expectedEdgeCount((int) dependencyEdgeStream().count())
                        .build();

        //所有节点
        network().nodes().forEach(dependencyGraph::addNode); // include disconnected nodes

        //仅仅包含DependencyEdge边
        dependencyEdgeStream()
                .forEach(
                        edge -> {
                            EndpointPair<Node> endpoints = network().incidentNodes(edge);
                            dependencyGraph.addEdge(endpoints.source(), endpoints.target(), edge);
                        });

        return ImmutableNetwork.copyOf(dependencyGraph);
    }

    //收集clazz类型的节点
    @SuppressWarnings({"rawtypes", "unchecked"})
    private <N extends Node> ImmutableSet<N> nodes(Class<N> clazz) {
        return (ImmutableSet) nodesByClass().get(clazz);
    }

    private static final ImmutableSet<Class<? extends Node>> NODE_TYPES =
            ImmutableSet.of(Binding.class, MissingBinding.class, ComponentNode.class);

    //图形节点class，节点实例-Binding，MissingBinding，ComponentNode
    protected ImmutableSetMultimap<Class<? extends Node>, ? extends Node> nodesByClass() {

        return network().nodes().stream()
                .collect(
                        toImmutableSetMultimap(
                                node ->
                                        NODE_TYPES.stream().filter(clazz -> clazz.isInstance(node)).findFirst().get(),
                                node -> node));
    }

    //图形上的所有边，筛选出DependencyEdge边
    private Stream<DependencyEdge> dependencyEdgeStream() {
        return network().edges().stream().flatMap(instancesOf(DependencyEdge.class));
    }

    //DependencyEdge边，并且是入口
    private Stream<DependencyEdge> entryPointEdgeStream() {
        return dependencyEdgeStream().filter(DependencyEdge::isEntryPoint);
    }

    /**
     * An edge in the binding graph. Either a {@link DependencyEdge}, a {@link
     * ChildFactoryMethodEdge}, or a {@link SubcomponentCreatorBindingEdge}.
     * <p>
     * 绑定图形的边，有三种样式
     */
    public interface Edge {
    }

    /**
     * An edge that represents a dependency on a binding.
     * <p>
     * 一条边：代表一个绑定的一个依赖
     *
     * <p>Because one {@link DependencyRequest} may represent a dependency from two bindings (e.g., a
     * dependency of {@code Foo<String>} and {@code Foo<Number>} may have the same key and request
     * element), this class does not override {@link #equals(Object)} to use value semantics.
     *
     * <p>For entry points, the source node is the {@link ComponentNode} that contains the entry
     * point. Otherwise the source node is a {@link Binding}.
     *
     * <p>For dependencies on missing bindings, the target node is a {@link MissingBinding}. Otherwise
     * the target node is a {@link Binding}.
     */
    public interface DependencyEdge extends Edge {
        /**
         * The dependency request.
         * <p>
         * 依赖
         */
        DependencyRequest dependencyRequest();

        /**
         * Returns {@code true} if this edge represents an entry point.
         * <p>
         * 表示当前依赖边作为入口
         */
        boolean isEntryPoint();
    }

    /**
     * An edge that represents a subcomponent factory method linking a parent component to a child
     * subcomponent.
     * <p>
     * 一条边：表示将父组件链接到子子组件的子组件工厂方法的边。
     */
    public interface ChildFactoryMethodEdge extends Edge {
        /**
         * The subcomponent factory method element.
         * <p>
         * subcomponent工厂方法节点
         */
        DaggerExecutableElement factoryMethod();
    }

    /**
     * An edge that represents the link between a parent component and a child subcomponent implied by
     * a subcomponent creator ({@linkplain dagger.Subcomponent.Builder builder} or {@linkplain
     * dagger.Subcomponent.Factory factory}) binding.
     *
     * <p>The {@linkplain com.google.common.graph.EndpointPair#source() source node} of this edge is a
     * {@link Binding} for the subcomponent creator {@link Key} and the {@linkplain
     * com.google.common.graph.EndpointPair#target() target node} is a {@link ComponentNode} for the
     * child subcomponent.
     */
    public interface SubcomponentCreatorBindingEdge extends Edge {
        /**
         * The modules that {@linkplain Module#subcomponents() declare the subcomponent} that generated
         * this edge. Empty if the parent component has a subcomponent creator method and there are no
         * declaring modules.
         * <p>
         * 当前subcomponent所在的module类节点。如果父component直接有一个返回subcomponent creator类型的方法，
         * 那么表示不需要在module#subcomponents中声明，该方法及为空
         */
        ImmutableSet<DaggerTypeElement> declaringModules();
    }

    /**
     * A node in the binding graph. Either a {@link Binding} or a {@link ComponentNode}.
     */
    // TODO(dpb): Make all the node/edge types top-level.
    public interface Node {
        /**
         * The component this node belongs to.
         */
        ComponentPath componentPath();
    }

    /**
     * A node in the binding graph that is either a {@link Binding} or a {@link MissingBinding}.
     */
    public interface MaybeBinding extends Node {

        /**
         * The component that owns the binding, or in which the binding is missing.
         */
        @Override
        ComponentPath componentPath();

        /**
         * The key of the binding, or for which there is no binding.
         */
        Key key();

        /**
         * The binding, or empty if missing.
         */
        Optional<Binding> binding();
    }

    /**
     * A node in the binding graph that represents a missing binding for a key in a component.
     */
    public abstract static class MissingBinding implements MaybeBinding {
        /**
         * The component in which the binding is missing.
         */
        @Override
        public abstract ComponentPath componentPath();

        /**
         * The key for which there is no binding.
         */
        @Override
        public abstract Key key();

        /**
         * @deprecated This always returns {@code Optional.empty()}.
         */
        @Override
        @Deprecated
        public Optional<Binding> binding() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return String.format("missing binding for %s in %s", key(), componentPath());
        }
    }

    /**
     * A <b>component node</b> in the graph. Every entry point {@linkplain DependencyEdge dependency
     * edge}'s source node is a component node for the component containing the entry point.
     */
    public interface ComponentNode extends Node {

        /**
         * The component represented by this node.
         */
        @Override
        ComponentPath componentPath();

        /**
         * Returns {@code true} if the component is a {@code @Subcomponent} or
         * {@code @ProductionSubcomponent}.
         */
        boolean isSubcomponent();

        /**
         * Returns {@code true} if the component is a real component, or {@code false} if it is a
         * fictional component based on a module.
         */
        boolean isRealComponent();

        /**
         * The entry points on this component.
         */
        ImmutableSet<DependencyRequest> entryPoints();

        /**
         * The scopes declared on this component.
         */
        ImmutableSet<Scope> scopes();
    }
}