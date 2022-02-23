package dagger.internal.codegen.bindinggraphvalidation;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;

import dagger.internal.codegen.base.Util;
import dagger.internal.codegen.binding.ComponentNodeImpl;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.BindingGraph;
import dagger.spi.model.BindingGraphPlugin;
import dagger.spi.model.DiagnosticReporter;

import static com.google.auto.common.MoreTypes.asDeclared;
import static com.google.auto.common.MoreTypes.asExecutable;
import static com.google.auto.common.MoreTypes.asTypeElements;
import static com.google.common.collect.Sets.union;
import static dagger.internal.codegen.binding.ComponentRequirement.componentCanMakeNewInstances;
import static dagger.internal.codegen.extension.DaggerStreams.instancesOf;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static javax.tools.Diagnostic.Kind.ERROR;

/** Reports an error if a subcomponent factory method is missing required modules. */
final class SubcomponentFactoryMethodValidator implements BindingGraphPlugin {


    private final DaggerTypes types;
    private final KotlinMetadataUtil metadataUtil;
    private final Map<BindingGraph.ComponentNode, Set<TypeElement>> inheritedModulesCache = new HashMap<>();

    @Inject
    SubcomponentFactoryMethodValidator(DaggerTypes types, KotlinMetadataUtil metadataUtil) {
        this.types = types;
        this.metadataUtil = metadataUtil;
    }

    @Override
    public String pluginName() {
        return "Dagger/SubcomponentFactoryMethodMissingModule";
    }

    @Override
    public void visitGraph(BindingGraph bindingGraph, DiagnosticReporter diagnosticReporter) {
        if (!bindingGraph.rootComponentNode().isRealComponent()
                || bindingGraph.rootComponentNode().isSubcomponent()) {
            // We don't know all the modules that might be owned by the child until we know the real root
            // component, which we don't if the root component node is really a module or a subcomponent.
            return;
        }
        bindingGraph.network().edges().stream()
                //收集BindingGraph所有的ChildFactoryMethodEdge边：表示component节点中的返回类型是subcomponent节点的componentMethod方法生成的edge边，
                // 该edge边的源Node节点是component节点，目标Node节点是subcomponent节点；
                .flatMap(instancesOf(BindingGraph.ChildFactoryMethodEdge.class))
                .forEach(
                        edge -> {
                            ImmutableSet<TypeElement> missingModules = findMissingModules(edge, bindingGraph);
                            if (!missingModules.isEmpty()) {
                                reportMissingModuleParameters(
                                        edge, missingModules, bindingGraph, diagnosticReporter);
                            }
                        });
    }

    private ImmutableSet<TypeElement> findMissingModules(
            BindingGraph.ChildFactoryMethodEdge edge, BindingGraph graph) {
        //当前componentMethod方法的参数：返回类型是subcomponent，参数肯定是module节点
        ImmutableSet<TypeElement> factoryMethodParameters =
                subgraphFactoryMethodParameters(edge, graph);
        //表示当前componentMethod方法的返回类型subcomponent生成的target目标Node节点
        BindingGraph.ComponentNode child = (BindingGraph.ComponentNode) graph.network().incidentNodes(edge).target();

        //child及其父级component收集所有module节点
        Sets.SetView<TypeElement> modulesOwnedByChild = ownedModules(child, graph);

        //表示当前返回类型是subcomponent的componentMethod方法，该方法参数是module节点，
        // 那么module节点必须存在于subcomponent及其父级component关联的module集合中，
        // 并且这里面的module集合必须表示需要实例化；
        return graph.bindings().stream()
                // bindings owned by child
                .filter(binding -> binding.componentPath().equals(child.componentPath()))
                // that require a module instance
                .filter(binding -> binding.requiresModuleInstance())
                .map(binding -> binding.contributingModule().get().java())
                .distinct()
                // module owned by child
                .filter(module -> modulesOwnedByChild.contains(module))
                // module not in the method parameters
                .filter(module -> !factoryMethodParameters.contains(module))
                // module doesn't have an accessible no-arg constructor
                .filter(moduleType -> !componentCanMakeNewInstances(moduleType, metadataUtil))
                .collect(toImmutableSet());
    }

    private ImmutableSet<TypeElement> subgraphFactoryMethodParameters(
            BindingGraph.ChildFactoryMethodEdge edge, BindingGraph bindingGraph) {
        BindingGraph.ComponentNode parent = (BindingGraph.ComponentNode) bindingGraph.network().incidentNodes(edge).source();
        DeclaredType parentType = asDeclared(parent.componentPath().currentComponent().java().asType());
        ExecutableType factoryMethodType =
                asExecutable(types.asMemberOf(parentType, edge.factoryMethod().java()));
        return asTypeElements(factoryMethodType.getParameterTypes());
    }

    private Sets.SetView<TypeElement> ownedModules(BindingGraph.ComponentNode component, BindingGraph graph) {
        return Sets.difference(
                ((ComponentNodeImpl) component).componentDescriptor().moduleTypes(),
                inheritedModules(component, graph));
    }

    private Set<TypeElement> inheritedModules(BindingGraph.ComponentNode component, BindingGraph graph) {
        return Util.reentrantComputeIfAbsent(
                inheritedModulesCache, component, uncachedInheritedModules(graph));
    }

    private Function<BindingGraph.ComponentNode, Set<TypeElement>> uncachedInheritedModules(BindingGraph graph) {
        return componentNode ->
                componentNode.componentPath().atRoot()
                        ? ImmutableSet.of()
                        : graph
                        .componentNode(componentNode.componentPath().parent())
                        .map(parent -> union(ownedModules(parent, graph), inheritedModules(parent, graph)))
                        .get();
    }

    private void reportMissingModuleParameters(
            BindingGraph.ChildFactoryMethodEdge edge,
            ImmutableSet<TypeElement> missingModules,
            BindingGraph graph,
            DiagnosticReporter diagnosticReporter) {
        diagnosticReporter.reportSubcomponentFactoryMethod(
                ERROR,
                edge,
                "%s requires modules which have no visible default constructors. "
                        + "Add the following modules as parameters to this method: %s",
                graph
                        .network()
                        .incidentNodes(edge)
                        .target()
                        .componentPath()
                        .currentComponent()
                        .className()
                        .canonicalName(),
                Joiner.on(", ").join(missingModules));
    }
}
