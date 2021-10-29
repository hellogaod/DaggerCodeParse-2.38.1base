package dagger.internal.codegen.writing;

import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.room.compiler.processing.XMessager;
import dagger.internal.codegen.binding.Binding;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/** The implementation of a component type. */
@PerComponentImplementation
public final class ComponentImplementation {

    /** A factory for creating a {@link ComponentImplementation}. */
    public interface ChildComponentImplementationFactory {
        /** Creates a {@link ComponentImplementation} for the given {@code childGraph}. */
        ComponentImplementation create(BindingGraph childGraph);
    }

//    private final ShardImplementation componentShard;
//    private final ImmutableMap<Binding, ShardImplementation> shardsByBinding;
//    private final Map<ShardImplementation, FieldSpec> shardFieldsByImplementation = new HashMap<>();
    private final List<CodeBlock> shardInitializations = new ArrayList<>();
    private final List<CodeBlock> shardCancellations = new ArrayList<>();
    private final Optional<ComponentImplementation> parent;
    private final ChildComponentImplementationFactory childComponentImplementationFactory;
    private final Provider<ComponentRequestRepresentations> bindingExpressionsProvider;
    private final Provider<ComponentCreatorImplementationFactory>
            componentCreatorImplementationFactoryProvider;
    private final BindingGraph graph;
    private final ComponentNames componentNames;
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final KotlinMetadataUtil metadataUtil;
//    private final ImmutableMap<ComponentImplementation, FieldSpec> componentFieldsByImplementation;
    private final XMessager messager;

    @Inject
    ComponentImplementation(
            @ParentComponent Optional<ComponentImplementation> parent,
            ChildComponentImplementationFactory childComponentImplementationFactory,
            // Inject as Provider<> to prevent a cycle.
            Provider<ComponentRequestRepresentations> bindingExpressionsProvider,
            Provider<ComponentCreatorImplementationFactory> componentCreatorImplementationFactoryProvider,
            BindingGraph graph,
            ComponentNames componentNames,
            CompilerOptions compilerOptions,
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil metadataUtil,
            XMessager messager) {
        this.parent = parent;
        this.childComponentImplementationFactory = childComponentImplementationFactory;
        this.bindingExpressionsProvider = bindingExpressionsProvider;
        this.componentCreatorImplementationFactoryProvider =
                componentCreatorImplementationFactoryProvider;
        this.graph = graph;
        this.componentNames = componentNames;
        this.elements = elements;
        this.types = types;
        this.metadataUtil = metadataUtil;

        // The first group of keys belong to the component itself. We call this the componentShard.
//        this.componentShard = new ShardImplementation(componentNames.get(graph.componentPath()));

        // Claim the method names for all local and inherited methods on the component type.
//        elements
//                .getLocalAndInheritedMethods(graph.componentTypeElement())
//                .forEach(method -> componentShard.componentMethodNames.claim(method.getSimpleName()));

        // Create the shards for this component, indexed by binding.
//        this.shardsByBinding = createShardsByBinding(componentShard, graph, compilerOptions);

        // Create and claim the fields for this and all ancestor components stored as fields.
//        this.componentFieldsByImplementation =
//                createComponentFieldsByImplementation(this, compilerOptions);
        this.messager = messager;
    }
}
