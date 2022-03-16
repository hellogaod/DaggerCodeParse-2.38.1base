package dagger.internal.codegen.writing;

import com.google.auto.common.MoreElements;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import androidx.room.compiler.processing.XMessager;
import dagger.internal.Preconditions;
import dagger.internal.codegen.base.UniqueNameSet;
import dagger.internal.codegen.binding.Binding;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.BindingNode;
import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.ComponentCreatorDescriptor;
import dagger.internal.codegen.binding.ComponentCreatorKind;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.ComponentRequirement;
import dagger.internal.codegen.binding.KeyVariableNamer;
import dagger.internal.codegen.binding.MethodSignature;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.javapoet.CodeBlocks;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.javapoet.TypeSpecs;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.Key;
import dagger.spi.model.RequestKind;

import static com.google.auto.common.MoreTypes.asDeclared;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static dagger.internal.codegen.binding.ComponentCreatorKind.BUILDER;
import static dagger.internal.codegen.binding.SourceFiles.simpleVariableName;
import static dagger.internal.codegen.extension.DaggerStreams.instancesOf;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableMap;
import static dagger.internal.codegen.javapoet.AnnotationSpecs.Suppression.UNCHECKED;
import static dagger.internal.codegen.javapoet.AnnotationSpecs.suppressWarnings;
import static dagger.internal.codegen.javapoet.CodeBlocks.parameterNames;
import static dagger.internal.codegen.langmodel.Accessibility.isTypeAccessibleFrom;
import static dagger.internal.codegen.writing.ComponentImplementation.MethodSpecKind.COMPONENT_METHOD;
import static dagger.producers.CancellationPolicy.Propagation.PROPAGATE;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * The implementation of a component type.
 */
@PerComponentImplementation
public final class ComponentImplementation {

    /**
     * A factory for creating a {@link ComponentImplementation}.
     */
    public interface ChildComponentImplementationFactory {
        /**
         * Creates a {@link ComponentImplementation} for the given {@code childGraph}.
         */
        ComponentImplementation create(BindingGraph childGraph);
    }

    /**
     * A type of field that this component can contain.
     */
    public enum FieldSpecKind {
        /**
         * A field for a component shard.
         */
        COMPONENT_SHARD_FIELD,

        /**
         * A field required by the component, e.g. module instances.
         */
        COMPONENT_REQUIREMENT_FIELD,

        /**
         * A framework field for type T, e.g. {@code Provider<T>}.
         * <p>
         * 变量
         */
        FRAMEWORK_FIELD,

        /**
         * A static field that always returns an absent {@code Optional} value for the binding.
         */
        ABSENT_OPTIONAL_FIELD
    }

    /**
     * A type of method that this component can contain.
     */
    // TODO(bcorso, dpb): Change the oder to constructor, initialize, component, then private
    // (including MIM and AOM—why treat those separately?).
    public enum MethodSpecKind {
        /**
         * The component constructor.
         */
        CONSTRUCTOR,

        /**
         * A builder method for the component. (Only used by the root component.)
         */
        BUILDER_METHOD,

        /**
         * A private method that wraps dependency expressions.
         * <p>
         * 生成的方法类型
         */
        PRIVATE_METHOD,

        /**
         * An initialization method that initializes component requirements and framework types.
         */
        INITIALIZE_METHOD,

        /**
         * An implementation of a component interface method.
         * <p>
         * 表示component节点的入口方法
         */
        COMPONENT_METHOD,

        /**
         * A private method that encapsulates members injection logic for a binding.
         */
        MEMBERS_INJECTION_METHOD,

        /**
         * A static method that always returns an absent {@code Optional} value for the binding.
         */
        ABSENT_OPTIONAL_METHOD,

        /**
         * The {@link dagger.producers.internal.CancellationListener#onProducerFutureCancelled(boolean)}
         * method for a production component.
         */
        CANCELLATION_LISTENER_METHOD
    }

    /**
     * A type of nested class that this component can contain.
     */
    public enum TypeSpecKind {
        /**
         * A factory class for a present optional binding.
         */
        PRESENT_FACTORY,

        /**
         * A class for the component creator (only used by the root component.)
         * <p>
         * 表示creator类型的类
         */
        COMPONENT_CREATOR,

        /**
         * A provider class for a component provision.
         */
        COMPONENT_PROVISION_FACTORY,

        /**
         * A class for a component shard.
         */
        COMPONENT_SHARD_TYPE,

        /**
         * A class for the subcomponent or subcomponent builder.
         * <p>
         * subcomponent生成的Component类的类型
         */
        SUBCOMPONENT
    }

    /**
     * Returns the {@link ShardImplementation} for each binding in this graph.
     *
     * <p>Each shard contains approximately {@link CompilerOptions#keysPerComponentShard()} bindings.
     *
     * <p>If more than 1 shard is needed, we iterate the strongly connected nodes to make sure of two
     * things: 1) bindings are put in shards in reverse topological order (i.e., bindings in Shard{i}
     * do not depend on bindings in Shard{i+j}) and 2) bindings belonging to the same cycle are put in
     * the same shard. These two guarantees allow us to initialize each shard in a well defined order.
     */
    private static ImmutableMap<Binding, ShardImplementation> createShardsByBinding(
            ShardImplementation componentShard, BindingGraph graph, CompilerOptions compilerOptions) {

        ImmutableList<ImmutableList<Binding>> partitions = bindingPartitions(graph, compilerOptions);

        ImmutableMap.Builder<Binding, ShardImplementation> builder = ImmutableMap.builder();

        //如果当前BindingGraph有向图的BindingNode节点在3500以内，那么使用一个component类初始化即可；
        // 否则多余的BindingNode节点以每3500为一个单位在重新创建的componentShared+ i类中完成实例化
        for (int i = 0; i < partitions.size(); i++) {
            //表示binding绑定对象基于的component节点
            ShardImplementation shard = i == 0 ? componentShard : componentShard.createShard("Shard" + i);
            partitions.get(i).forEach(binding -> builder.put(binding, shard));
        }
        return builder.build();
    }

    //绑定图形的BindingNode节点中的Binding属性，并且判断当前BindingNode节点是否超出最大阀门（默认是3500），
    // 如果是则按照最大阀门进行分区收集。
    private static ImmutableList<ImmutableList<Binding>> bindingPartitions(
            BindingGraph graph, CompilerOptions compilerOptions) {
        int bindingsPerShard = compilerOptions.keysPerComponentShard(graph.componentTypeElement());
        int maxPartitions = (graph.localBindingNodes().size() / bindingsPerShard) + 1;

        //没有超过最大值阀门，直接返回当前BindingNode节点中的Binding属性
        if (maxPartitions <= 1) {
            return ImmutableList.of(
                    graph.localBindingNodes().stream().map(BindingNode::delegate).collect(toImmutableList()));
        }

        // Iterate through all SCCs in order until all bindings local to this component are partitioned.
        List<Binding> currPartition = new ArrayList<>(bindingsPerShard);
        //以bindingsPerShard个作为一个区间，存放在当前partitions中
        ImmutableList.Builder<ImmutableList<Binding>> partitions =
                ImmutableList.builderWithExpectedSize(maxPartitions);
        for (ImmutableSet<dagger.spi.model.BindingGraph.Node> nodes : graph.topLevelBindingGraph().stronglyConnectedNodes()) {
            nodes.stream()
                    .flatMap(instancesOf(BindingNode.class))
                    .filter(bindingNode -> bindingNode.componentPath().equals(graph.componentPath()))
                    .map(BindingNode::delegate)
                    .forEach(currPartition::add);
            if (currPartition.size() >= bindingsPerShard) {
                partitions.add(ImmutableList.copyOf(currPartition));
                currPartition = new ArrayList<>(bindingsPerShard);
            }
        }
        if (!currPartition.isEmpty()) {
            partitions.add(ImmutableList.copyOf(currPartition));
        }
        return partitions.build();
    }

    /**
     * The boolean parameter of the onProducerFutureCancelled method.
     * <p>
     * 方法参数：boolean mayInterruptIfRunning
     */
    public static final ParameterSpec MAY_INTERRUPT_IF_RUNNING_PARAM =
            ParameterSpec.builder(boolean.class, "mayInterruptIfRunning").build();

    private static final String CANCELLATION_LISTENER_METHOD_NAME = "onProducerFutureCancelled";

    /**
     * How many statements per {@code initialize()} or {@code onProducerFutureCancelled()} method
     * before they get partitioned.
     */
    private static final int STATEMENTS_PER_METHOD = 100;

    //当前正在处理的component需要一个ShardImplementation对象处理
    private final ShardImplementation componentShard;
    //当前component中的绑定对象如果超过了3500个，那么以3500为一个单位（最后那个不满3500作为一个单位），每个单位使用一个item
    private final ImmutableMap<Binding, ShardImplementation> shardsByBinding;
    //当前在处理的component收集到的变量
    private final Map<ShardImplementation, FieldSpec> shardFieldsByImplementation = new HashMap<>();

    private final List<CodeBlock> shardInitializations = new ArrayList<>();
    private final List<CodeBlock> shardCancellations = new ArrayList<>();
    //当前在处理的currentcomponent所在的父级parentcomponent节点
    private final Optional<ComponentImplementation> parent;
    //用于生成子componentImplementation
    private final ChildComponentImplementationFactory childComponentImplementationFactory;
    private final Provider<ComponentRequestRepresentations> bindingExpressionsProvider;
    private final Provider<ComponentCreatorImplementationFactory>
            componentCreatorImplementationFactoryProvider;
    private final BindingGraph graph;
    private final ComponentNames componentNames;
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final KotlinMetadataUtil metadataUtil;
    private final ImmutableMap<ComponentImplementation, FieldSpec> componentFieldsByImplementation;
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
        //如果当前component是root，那么使用rootName，否则使用rootName.currentCompoonent + "Impl"
        //初始化了当前component生成的构造函数的参数
        this.componentShard = new ShardImplementation(componentNames.get(graph.componentPath()));

        // Claim the method names for all local and inherited methods on the component type.
        //确保生成的component类中不存在重复的方法名
        elements
                .getLocalAndInheritedMethods(graph.componentTypeElement())
                .forEach(method -> componentShard.componentMethodNames.claim(method.getSimpleName()));

        // Create the shards for this component, indexed by binding.
        //当前BindingGraph有向图的BindingNode节点在3500以内，那么使用一个component类初始化即可;否则重新创建处理多余的BindingNode节点（以3500为一个单位）
        this.shardsByBinding = createShardsByBinding(componentShard, graph, compilerOptions);

        // Create and claim the fields for this and all ancestor components stored as fields.
        this.componentFieldsByImplementation =
                createComponentFieldsByImplementation(this, compilerOptions);
        this.messager = messager;
    }

    /**
     * Returns the shard for a given {@link Binding}.
     * <p>
     * 返回给定 {@link Binding} 的分片。
     *
     * <p>Each set of {@link CompilerOptions#keysPerShard()} will get its own shard instance.
     */
    public ShardImplementation shardImplementation(Binding binding) {
        checkState(shardsByBinding.containsKey(binding), "No shard in %s for: %s", name(), binding);
        return shardsByBinding.get(binding);
    }

    /**
     * Returns the root {@link ComponentImplementation}.
     */
    ComponentImplementation rootComponentImplementation() {
        return parent.map(ComponentImplementation::rootComponentImplementation).orElse(this);
    }

    /**
     * Returns a reference to this implementation when called from a different class.
     */
    public CodeBlock componentFieldReference() {
        // TODO(bcorso): This currently relies on all requesting classes having a reference to the
        // component with the same name, which is kind of sketchy. Try to think of a better way that
        // can accomodate the component missing in some classes if it's not used.
        return CodeBlock.of("$N", componentFieldsByImplementation.get(this));
    }

    /**
     * Returns the fields for all components in the component path.
     * <p>
     * 返回组件路径中所有组件的字段。
     */
    public ImmutableList<FieldSpec> componentFields() {
        return ImmutableList.copyOf(componentFieldsByImplementation.values());
    }

    /**
     * Returns the fields for all components in the component path except the current component.
     * <p>
     * currentComponent及其父级currentComponent生成的变量，但是要排除当前currentComponent生成的变量：
     */
    public ImmutableList<FieldSpec> creatorComponentFields() {
        return componentFieldsByImplementation.entrySet().stream()
                .filter(entry -> !this.equals(entry.getKey()))//排除当前currentComponent生成的变量：
                .map(Map.Entry::getValue)
                .collect(toImmutableList());
    }

    private static ImmutableMap<ComponentImplementation, FieldSpec>
    createComponentFieldsByImplementation(
            ComponentImplementation componentImplementation, CompilerOptions compilerOptions) {

        checkArgument(
                componentImplementation.componentShard != null,
                "The component shard must be set before computing the component fields.");

        //收集当前componentImplementation对象以及当前对象的父级对象，直到父级componentImplementation对象不存在。
        ImmutableList.Builder<ComponentImplementation> builder = ImmutableList.builder();
        for (ComponentImplementation curr = componentImplementation;
             curr != null;
             curr = curr.parent.orElse(null)) {
            builder.add(curr);
        }
        // for (int i = 0; i < 2; i++) {
        // }
        // For better readability when adding these fields/parameters to generated code, we collect the
        // component implementations in reverse order so that parents appear before children.
        // Map<K,V>,K:正在处理的componentImplementation对象；
        // V：正在处理的componentImplementation对象的有向图中的当前currentComponent节点生成的一个private和final修饰的变量，
        // 并且当前currentComponent节点根据是否内部类生成不同的变量名存在于正在处理的componentImplementation对象的componentShard的componentFieldNames集合中。
        return builder.build().reverse().stream()
                .collect(
                        toImmutableMap(
                                componentImpl -> componentImpl,
                                componentImpl -> {
                                    ClassName component =
                                            componentImpl.graph.componentPath().currentComponent().className();
                                    ClassName fieldType = componentImpl.name();
                                    String fieldName =
                                            componentImpl.isNested()
                                                    ? simpleVariableName(componentImpl.name())
                                                    : simpleVariableName(component);
                                    FieldSpec.Builder field = FieldSpec.builder(fieldType, fieldName, PRIVATE, FINAL);
                                    componentImplementation.componentShard.componentFieldNames.claim(fieldName);

                                    return field.build();
                                }));
    }

    /**
     * Returns the shard representing the {@link ComponentImplementation} itself.
     */
    public ShardImplementation getComponentShard() {
        return componentShard;
    }

    /**
     * Returns the binding graph for the component being generated.
     */
    public BindingGraph graph() {
        return componentShard.graph();
    }

    /**
     * Returns the descriptor for the component being generated.
     */
    public ComponentDescriptor componentDescriptor() {
        return componentShard.componentDescriptor();
    }

    /**
     * Returns the name of the component.
     */
    public ClassName name() {
        return componentShard.name;
    }

    /**
     * Returns whether or not the implementation is nested within another class.
     */
    private boolean isNested() {
        return name().enclosingClassName() != null;
    }

    /**
     * Returns the name of the creator class for this component. It will be a sibling of this
     * generated class unless this is a top-level component, in which case it will be nested.
     * <p>
     * 返回此组件的创建者类的名称。 除非这是一个顶级组件，否则它将是这个生成的类的兄弟，在这种情况下它将被嵌套。
     */
    public ClassName getCreatorName() {
        return componentNames.getCreatorName(graph.componentPath());
    }

    /**
     * Generates the component and returns the resulting {@link TypeSpec}.
     */
    public TypeSpec generate() {
        return componentShard.generate();
    }

    /**
     * The implementation of a shard.
     * <p>
     * 分片的实现。
     *
     * <p>The purpose of a shard is to allow a component implemenation to be split into multiple
     * classes, where each class owns the creation logic for a set of keys. Sharding is useful for
     * large component implementations, where a single component implementation class can reach size
     * limitations, such as the constant pool size.
     * <p>
     * 分片的目的是允许将组件实现拆分为多个类，其中每个类拥有一组键的创建逻辑。 分片对于大型组件实现很有用，其中单个组件实现类可以达到大小限制，例如常量池大小。
     *
     * <p>When generating the actual sources, the creation logic within the first instance of {@link
     * ShardImplementation} will go into the component implementation class itself (e.g. {@code
     * MySubcomponentImpl}). Each subsequent instance of {@link ShardImplementation} will generate a
     * nested "shard" class within the component implementation (e.g. {@code
     * MySubcomponentImpl.Shard1}, {@code MySubcomponentImpl.Shard2}, etc).
     */
    public final class ShardImplementation {
        private final ClassName name;
        private final UniqueNameSet componentFieldNames = new UniqueNameSet();
        private final UniqueNameSet componentMethodNames = new UniqueNameSet();
        //生成的initialize方法中实现的代码块收集
        private final List<CodeBlock> initializations = new ArrayList<>();
        private final Map<Key, CodeBlock> cancellations = new LinkedHashMap<>();
        //收集Assisted修饰的参数
        private final Map<VariableElement, String> uniqueAssistedName = new LinkedHashMap<>();
        //存放给新生成的Component类的构造函数添加的代码块
        private final List<CodeBlock> componentRequirementInitializations = new ArrayList<>();
        private final ImmutableMap<ComponentRequirement, ParameterSpec> constructorParameters;
        private final ListMultimap<FieldSpecKind, FieldSpec> fieldSpecsMap =
                MultimapBuilder.enumKeys(FieldSpecKind.class).arrayListValues().build();
        //存储当前component的入口方法
        private final ListMultimap<MethodSpecKind, MethodSpec> methodSpecsMap =
                MultimapBuilder.enumKeys(MethodSpecKind.class).arrayListValues().build();
        //存放在ShardImplementation当前实现的内部类
        private final ListMultimap<TypeSpecKind, TypeSpec> typeSpecsMap =
                MultimapBuilder.enumKeys(TypeSpecKind.class).arrayListValues().build();
        private final List<Supplier<TypeSpec>> typeSuppliers = new ArrayList<>();

        private ShardImplementation(ClassName name) {
            this.name = name;
            //如果当前component是production类型，那么onProducerFutureCancelled方法名表示已经被用过了
            if (graph.componentDescriptor().isProduction()) {
                claimMethodName(CANCELLATION_LISTENER_METHOD_NAME);
            }

            // Build the map of constructor parameters for this shard and claim the field names to prevent
            // collisions between the constructor parameters and fields.
            //生成的Component类的构造函数的参数来源，并且参数名使用原先的参数名 + "Param"
            constructorParameters =
                    constructorRequirements(graph).stream()
                            .collect(
                                    toImmutableMap(
                                            requirement -> requirement,
                                            requirement ->
                                                    ParameterSpec.builder(
                                                            TypeName.get(requirement.type()),
                                                            getUniqueFieldName(requirement.variableName() + "Param"))
                                                            .build()));
        }

        private ShardImplementation createShard(String shardName) {
            checkState(isComponentShard(), "Only the componentShard can create other shards.");
            return new ShardImplementation(name.nestedClass(shardName));
        }

        /**
         * Returns the {@link ComponentImplementation} that owns this shard.
         */
        public ComponentImplementation getComponentImplementation() {
            return ComponentImplementation.this;
        }

        /**
         * Returns {@code true} if this shard represents the component implementation rather than a
         * separate {@code Shard} class.
         * <p>
         * 在component中的绑定个数超出3500个会用到，否则条件一直成立
         */
        public boolean isComponentShard() {
            return this == componentShard;
        }

        /**
         * Returns the fields for all components in the component path by component implementation.
         */
        public ImmutableMap<ComponentImplementation, FieldSpec> componentFieldsByImplementation() {
            return componentFieldsByImplementation;
        }

        /**
         * Returns a reference to this implementation when called from a different class.
         */
        public CodeBlock shardFieldReference() {
            if (!isComponentShard() && !shardFieldsByImplementation.containsKey(this)) {
                // Add the shard if this is the first time it's requested by something.
                String shardFieldName =
                        componentShard.getUniqueFieldName(UPPER_CAMEL.to(LOWER_CAMEL, name.simpleName()));
                FieldSpec shardField = FieldSpec.builder(name, shardFieldName, PRIVATE).build();

                shardFieldsByImplementation.put(this, shardField);
            }
            // TODO(bcorso): This currently relies on all requesting classes having a reference to the
            // component with the same name, which is kind of sketchy. Try to think of a better way that
            // can accomodate the component missing in some classes if it's not used.
            return isComponentShard()
                    ? componentFieldReference()
                    : CodeBlock.of("$L.$N", componentFieldReference(), shardFieldsByImplementation.get(this));
        }

        /**
         * Returns the binding graph for the component being generated.
         */
        public BindingGraph graph() {
            return graph;
        }

        /**
         * Returns the descriptor for the component being generated.
         */
        public ComponentDescriptor componentDescriptor() {
            return graph.componentDescriptor();
        }

        /**
         * Returns the name of the component.
         */
        public ClassName name() {
            return name;
        }

        /**
         * Returns the name of the creator implementation class for the given subcomponent creator
         * {@link Key}.
         */
        ClassName getSubcomponentCreatorSimpleName(Key creatorKey) {
            return componentNames.getSubcomponentCreatorName(graph.componentPath(), creatorKey);
        }

        /**
         * Returns {@code true} if {@code type} is accessible from the generated component.
         */
        boolean isTypeAccessible(TypeMirror type) {
            return isTypeAccessibleFrom(type, name.packageName());
        }

        // TODO(dpb): Consider taking FieldSpec, and returning identical FieldSpec with unique name?

        /**
         * Adds the given field to the component.
         */
        public void addField(FieldSpecKind fieldKind, FieldSpec fieldSpec) {
            fieldSpecsMap.put(fieldKind, fieldSpec);
        }

        // TODO(dpb): Consider taking MethodSpec, and returning identical MethodSpec with unique name?

        /**
         * Adds the given method to the component.
         */
        public void addMethod(MethodSpecKind methodKind, MethodSpec methodSpec) {
            methodSpecsMap.put(methodKind, methodSpec);
        }

        /**
         * Adds the given type to the component.
         */
        public void addType(TypeSpecKind typeKind, TypeSpec typeSpec) {
            typeSpecsMap.put(typeKind, typeSpec);
        }

        /**
         * Adds a {@link Supplier} for the SwitchingProvider for the component.
         */
        void addTypeSupplier(Supplier<TypeSpec> typeSpecSupplier) {
            typeSuppliers.add(typeSpecSupplier);
        }

        /**
         * Adds the given code block to the initialize methods of the component.
         */
        void addInitialization(CodeBlock codeBlock) {
            initializations.add(codeBlock);
        }

        /**
         * Adds the given code block that initializes a {@link ComponentRequirement}.
         */
        void addComponentRequirementInitialization(CodeBlock codeBlock) {
            componentRequirementInitializations.add(codeBlock);
        }

        /**
         * Adds the given cancellation statement to the cancellation listener method of the component.
         */
        void addCancellation(Key key, CodeBlock codeBlock) {
            // Store cancellations by key to avoid adding the same cancellation twice.
            cancellations.putIfAbsent(key, codeBlock);
        }

        /**
         * Returns a new, unique field name for the component based on the given name.
         */
        String getUniqueFieldName(String name) {
            return componentFieldNames.getUniqueName(name);
        }

        public String getUniqueFieldNameForAssistedParam(VariableElement element) {
            if (uniqueAssistedName.containsKey(element)) {
                return uniqueAssistedName.get(element);
            }
            String name = getUniqueFieldName(element.getSimpleName().toString());
            uniqueAssistedName.put(element, name);
            return name;
        }

        /**
         * Returns a new, unique method name for the component based on the given name.
         */
        public String getUniqueMethodName(String name) {
            return componentMethodNames.getUniqueName(name);
        }

        /**
         * Returns a new, unique method name for a getter method for the given request.
         */
        String getUniqueMethodName(BindingRequest request) {
            return uniqueMethodName(request, KeyVariableNamer.name(request.key()));
        }

        private String uniqueMethodName(BindingRequest request, String bindingName) {
            // This name is intentionally made to match the name for fields in fastInit
            // in order to reduce the constant pool size. b/162004246
            String baseMethodName =
                    bindingName
                            + (request.isRequestKind(RequestKind.INSTANCE)
                            ? ""
                            : UPPER_UNDERSCORE.to(UPPER_CAMEL, request.kindName()));
            return getUniqueMethodName(baseMethodName);
        }

        /**
         * Gets the parameter name to use for the given requirement for this component, starting with
         * the given base name if no parameter name has already been selected for the requirement.
         */
        public String getParameterName(ComponentRequirement requirement) {
            return constructorParameters.get(requirement).name;
        }

        /**
         * Claims a new method name for the component. Does nothing if method name already exists.
         */
        public void claimMethodName(CharSequence name) {
            componentMethodNames.claim(name);
        }

        /**
         * Generates the component and returns the resulting {@link TypeSpec.Builder}.
         * <p>
         * 根据component及其关联的各种组件生成对应的class类
         */
        private TypeSpec generate() {
            //1.component节点生成Component类
            TypeSpec.Builder builder = classBuilder(name);

            if (isComponentShard()) {//在component中的绑定个数超出3500个判断为false，否则条件一直成立

                //2. 当前生成的Component类继承component节点
                TypeSpecs.addSupertype(builder, graph.componentTypeElement());

                //3.生成Creator内部类
                addCreator();

                //4.（1）如果当前不存在parent，在Component类中生成一个新建Creator内部类的factory方法；
                // （2）存在父级parent，那么如果当前currentComponent存在返回类型是subcomponent的方法，对该方法和方法参数生成一个方法；
                addFactoryMethods();

                //5.component节点中的入口方法：返回类型不是subcomponent节点的方法，非static、非private
                addInterfaceMethods();

                //6.当前component节点中的subcomponent节点实现代码
                addChildComponents();

                //7.可以理解为如果当前currentComponent的绑定超出了3500，那么以3500为一个单位，对每个单位生成对应的类
                //但是不能理解第一个已经处理过了为什么这里还要重新处理。
                addShards();
            }

            //8.添加Component类的构造函数和initializale方法
            addConstructorAndInitializationMethods();

            if (graph.componentDescriptor().isProduction()) {
                if (isComponentShard() || !cancellations.isEmpty()) {
                    TypeSpecs.addSupertype(
                            builder, elements.getTypeElement(TypeNames.CANCELLATION_LISTENER.canonicalName()));
                    addCancellationListenerImplementation();
                }
            }

            //9.Component类的修饰
            modifiers().forEach(builder::addModifiers);

            //10. Component类中的变量
            fieldSpecsMap.asMap().values().forEach(builder::addFields);

            //11.Component类中的的方法
            methodSpecsMap.asMap().values().forEach(builder::addMethods);

            //12.Component中的内部类
            typeSpecsMap.asMap().values().forEach(builder::addTypes);

            //13.真的不知道干啥的了！！！哈哈哈
            typeSuppliers.stream().map(Supplier::get).forEach(builder::addType);

            return builder.build();
        }

        private ImmutableSet<Modifier> modifiers() {
            if (!isComponentShard()) {
                // TODO(bcorso): Consider making shards static and unnested too?
                return ImmutableSet.of(PRIVATE, FINAL);
            } else if (isNested()) {
                return ImmutableSet.of(PRIVATE, STATIC, FINAL);
            }
            return graph.componentTypeElement().getModifiers().contains(PUBLIC)
                    // TODO(ronshapiro): perhaps all generated components should be non-public?
                    ? ImmutableSet.of(PUBLIC, FINAL)
                    : ImmutableSet.of(FINAL);
        }

        private void addCreator() {
            componentCreatorImplementationFactoryProvider
                    .get()
                    .create()
                    .map(ComponentCreatorImplementation::spec)
                    .ifPresent(
                            creator ->
                                    rootComponentImplementation()
                                            .getComponentShard()
                                            .addType(TypeSpecKind.COMPONENT_CREATOR, creator));
        }

        private void addFactoryMethods() {
            if (parent.isPresent()) {//当前处理的是子component
                //当前子component是否存在返回类型是subcomponent的方法，如果存在生成一个createSubcomponentFactoryMethod方法
                graph.factoryMethod().ifPresent(this::createSubcomponentFactoryMethod);
            } else {
                createRootComponentFactoryMethod();
            }
        }

        private void createRootComponentFactoryMethod() {
            checkState(!parent.isPresent());
            // Top-level components have a static method that returns a builder or factory for the
            // component. If the user defined a @Component.Builder or @Component.Factory, an
            // implementation of their type is returned. Otherwise, an autogenerated Builder type is
            // returned.
            // TODO(cgdecker): Replace this abomination with a small class?
            // Better yet, change things so that an autogenerated builder type has a descriptor of sorts
            // just like a user-defined creator type.
            ComponentCreatorKind creatorKind;
            ClassName creatorType;
            String factoryMethodName;
            boolean noArgFactoryMethod;
            Optional<ComponentCreatorDescriptor> creatorDescriptor =
                    graph.componentDescriptor().creatorDescriptor();
            //当前currentcomponent节点的内部creator节点
            if (creatorDescriptor.isPresent()) {
                ComponentCreatorDescriptor descriptor = creatorDescriptor.get();
                creatorKind = descriptor.kind();
                creatorType = ClassName.get(descriptor.typeElement());
                //creator节点中的factoryMethod方法或buildMethod方法 名称
                factoryMethodName = descriptor.factoryMethod().getSimpleName().toString();
                //如果无参表示buildMethod方法；否则表示factoryMethod方法
                noArgFactoryMethod = descriptor.factoryParameters().isEmpty();
            } else {
                creatorKind = BUILDER;
                creatorType = getCreatorName();
                factoryMethodName = "build";
                noArgFactoryMethod = true;
            }

            //校验component中方法名
            validateMethodNameDoesNotOverrideGeneratedCreator(creatorKind.methodName());

            MethodSpec creatorFactoryMethod =
                    methodBuilder(creatorKind.methodName())
                            .addModifiers(PUBLIC, STATIC)
                            .returns(creatorType)
                            .addStatement("return new $T()", getCreatorName())
                            .build();

            addMethod(MethodSpecKind.BUILDER_METHOD, creatorFactoryMethod);

            //如果是buildMethod方法 && component节点生成的有向图可以自己完成构建，不需要依赖外部实例化
            if (noArgFactoryMethod && canInstantiateAllRequirements()) {
                validateMethodNameDoesNotOverrideGeneratedCreator("create");
                addMethod(
                        MethodSpecKind.BUILDER_METHOD,
                        methodBuilder("create")
                                .returns(ClassName.get(graph.componentTypeElement()))
                                .addModifiers(PUBLIC, STATIC)
                                .addStatement("return new $L().$L()", creatorKind.typeName(), factoryMethodName)
                                .build());
            }
        }

        //表示component不允许出现非static修饰的无参的使用build或factory作为方法名的方法
        private void validateMethodNameDoesNotOverrideGeneratedCreator(String creatorName) {
            // Check if there is any client added method has the same signature as generated creatorName.
            //当前BindingGraph中的currentComponent节点中所有的方法
            MoreElements.getAllMethods(graph.componentTypeElement(), types, elements).stream()
                    //筛选方法名是creatorName
                    .filter(method -> method.getSimpleName().contentEquals(creatorName))
                    //筛选方法无参
                    .filter(method -> method.getParameters().isEmpty())
                    //筛选方法不是使用static修饰
                    .filter(method -> !method.getModifiers().contains(Modifier.STATIC))
                    .forEach(
                            (ExecutableElement method) ->
                                    messager.printMessage(
                                            ERROR,
                                            String.format(
                                                    "Cannot override generated method: %s.%s()",
                                                    method.getEnclosingElement().getSimpleName(), method.getSimpleName())));
        }

        /**
         * {@code true} if all of the graph's required dependencies can be automatically constructed
         * <p>
         * BindingGraph有向图的依赖项 都可以自动构建
         */
        private boolean canInstantiateAllRequirements() {
            //!(graph.componentRequirements() 满足dependency -> dependency.requiresAPassedInstance(elements, metadataUtil))
            return !Iterables.any(
                    graph.componentRequirements(),
                    dependency -> dependency.requiresAPassedInstance(elements, metadataUtil));
        }

        //返回类型是subcomponent，对该方法的module参数实例化
        private void createSubcomponentFactoryMethod(ExecutableElement factoryMethod) {
            checkState(parent.isPresent());
            Collection<ParameterSpec> params =
                    Maps.transformValues(graph.factoryMethodParameters(), ParameterSpec::get).values();
            DeclaredType parentType = asDeclared(parent.get().graph().componentTypeElement().asType());
            MethodSpec.Builder method = MethodSpec.overriding(factoryMethod, parentType, types);
            params.forEach(
                    param -> method.addStatement("$T.checkNotNull($N)", Preconditions.class, param));
            method.addStatement(
                    "return new $T($L)",
                    name(),
                    parameterNames(
                            ImmutableList.<ParameterSpec>builder()
                                    .addAll(
                                            creatorComponentFields().stream()
                                                    .map(field -> ParameterSpec.builder(field.type, field.name).build())
                                                    .collect(toImmutableList()))
                                    .addAll(params)
                                    .build()));

            parent.get().getComponentShard().addMethod(COMPONENT_METHOD, method.build());
        }

        //核心部件
        private void addInterfaceMethods() {
            // Each component method may have been declared by several supertypes. We want to implement
            // only one method for each distinct signature.
            //有向图中当前正在处理的currentComponent节点
            DeclaredType componentType = asDeclared(graph.componentTypeElement().asType());

            //确保入口方法不重复
            Set<MethodSignature> signatures = Sets.newHashSet();
            //e.g.void inject(ComponentProcessor processor)
            for (ComponentDescriptor.ComponentMethodDescriptor method : graph.componentDescriptor().entryPointMethods()) {

                if (signatures.add(MethodSignature.forComponentMethod(method, componentType, types))) {

                    addMethod(COMPONENT_METHOD, bindingExpressionsProvider.get().getComponentMethod(method));
                }
            }
        }

        private void addChildComponents() {
            for (BindingGraph subgraph : graph.subgraphs()) {
                rootComponentImplementation()
                        .getComponentShard()
                        .addType(
                                TypeSpecKind.SUBCOMPONENT,
                                childComponentImplementationFactory.create(subgraph).generate());
            }
        }

        private void addShards() {
            // Generate all shards and add them to this component implementation.
            //当前CurrentComponent节点中的绑定个数超出3500才会是多个，否则只有一个
            for (ShardImplementation shard : ImmutableSet.copyOf(shardsByBinding.values())) {

                //当前currentComponent节点收集到的变量处理
                if (shardFieldsByImplementation.containsKey(shard)) {
                    addField(FieldSpecKind.COMPONENT_SHARD_FIELD, shardFieldsByImplementation.get(shard));
                    TypeSpec shardTypeSpec = shard.generate();
                    addType(TypeSpecKind.COMPONENT_SHARD_TYPE, shardTypeSpec);
                }
            }
        }

        /**
         * Creates and adds the constructor and methods needed for initializing the component.
         */
        private void addConstructorAndInitializationMethods() {
            MethodSpec.Builder constructor = constructorBuilder().addModifiers(PRIVATE);
            ImmutableList<ParameterSpec> parameters = constructorParameters.values().asList();

            if (isComponentShard()) {
                // Add a constructor parameter and initialization for each component field. We initialize
                // these fields immediately so that we don't need to be pass them to each initialize method
                // and shard constructor.
                componentFieldsByImplementation()
                        .forEach(
                                (componentImplementation, field) -> {
                                    if (componentImplementation.equals(ComponentImplementation.this)) {
                                        // For the self-referenced component field,
                                        // just initialize it in the initializer.
                                        addField(
                                                FieldSpecKind.COMPONENT_REQUIREMENT_FIELD,
                                                field.toBuilder().initializer("this").build());
                                    } else {
                                        addField(FieldSpecKind.COMPONENT_REQUIREMENT_FIELD, field);
                                        constructor.addStatement("this.$1N = $1N", field);
                                        constructor.addParameter(field.type, field.name);
                                    }
                                });
                constructor.addCode(CodeBlocks.concat(componentRequirementInitializations));
            }
            constructor.addParameters(parameters);

            // TODO(cgdecker): It's not the case that each initialize() method has need for all of the
            // given parameters. In some cases, those parameters may have already been assigned to fields
            // which could be referenced instead. In other cases, an initialize method may just not need
            // some of the parameters because the set of initializations in that partition does not
            // include any reference to them. Right now, the Dagger code has no way of getting that
            // information because, among other things, componentImplementation.getImplementations() just
            // returns a bunch of CodeBlocks with no semantic information. Additionally, we may not know
            // yet whether a field will end up needing to be created for a specific requirement, and we
            // don't want to create a field that ends up only being used during initialization.
            CodeBlock args = parameterNames(parameters);
            ImmutableList<MethodSpec> initializationMethods =
                    createPartitionedMethods(
                            "initialize",
                            // TODO(bcorso): Rather than passing in all of the constructor parameters, keep track
                            // of which parameters are used during initialization and only pass those. This could
                            // be useful for FastInit, where most of the initializations are just calling
                            // SwitchingProvider with no parameters.
                            makeFinal(parameters),
                            initializations,
                            methodName ->
                                    methodBuilder(methodName)
                                            /* TODO(gak): Strictly speaking, we only need the suppression here if we are
                                             * also initializing a raw field in this method, but the structure of this
                                             * code makes it awkward to pass that bit through.  This will be cleaned up
                                             * when we no longer separate fields and initialization as we do now. */
                                            .addAnnotation(suppressWarnings(UNCHECKED)));

            for (MethodSpec initializationMethod : initializationMethods) {
                constructor.addStatement("$N($L)", initializationMethod, args);
                addMethod(MethodSpecKind.INITIALIZE_METHOD, initializationMethod);
            }

            if (isComponentShard()) {
                constructor.addCode(CodeBlocks.concat(shardInitializations));
            } else {
                // This initialization is called from the componentShard, so we need to use those args.
                CodeBlock componentArgs =
                        parameterNames(componentShard.constructorParameters.values().asList());
                FieldSpec shardField = shardFieldsByImplementation.get(this);
                shardInitializations.add(CodeBlock.of("$N = new $T($L);", shardField, name, componentArgs));
            }

            addMethod(MethodSpecKind.CONSTRUCTOR, constructor.build());
        }

        private void addCancellationListenerImplementation() {
            MethodSpec.Builder methodBuilder =
                    methodBuilder(CANCELLATION_LISTENER_METHOD_NAME)
                            .addModifiers(PUBLIC)
                            .addAnnotation(Override.class)
                            .addParameter(MAY_INTERRUPT_IF_RUNNING_PARAM);

            // Reversing should order cancellations starting from entry points and going down to leaves
            // rather than the other way around. This shouldn't really matter but seems *slightly*
            // preferable because:
            // When a future that another future depends on is cancelled, that cancellation will propagate
            // up the future graph toward the entry point. Cancelling in reverse order should ensure that
            // everything that depends on a particular node has already been cancelled when that node is
            // cancelled, so there's no need to propagate. Otherwise, when we cancel a leaf node, it might
            // propagate through most of the graph, making most of the cancel calls that follow in the
            // onProducerFutureCancelled method do nothing.
            if (isComponentShard()) {
                methodBuilder.addCode(
                        CodeBlocks.concat(ImmutableList.copyOf(shardCancellations).reverse()));
            } else if (!cancellations.isEmpty()) {
                shardCancellations.add(
                        CodeBlock.of(
                                "$N.$N($N);",
                                shardFieldsByImplementation.get(this),
                                CANCELLATION_LISTENER_METHOD_NAME,
                                MAY_INTERRUPT_IF_RUNNING_PARAM));
            }

            ImmutableList<CodeBlock> cancellationStatements =
                    ImmutableList.copyOf(cancellations.values()).reverse();
            if (cancellationStatements.size() < STATEMENTS_PER_METHOD) {
                methodBuilder.addCode(CodeBlocks.concat(cancellationStatements)).build();
            } else {
                ImmutableList<MethodSpec> cancelProducersMethods =
                        createPartitionedMethods(
                                "cancelProducers",
                                ImmutableList.of(MAY_INTERRUPT_IF_RUNNING_PARAM),
                                cancellationStatements,
                                methodName -> methodBuilder(methodName).addModifiers(PRIVATE));
                for (MethodSpec cancelProducersMethod : cancelProducersMethods) {
                    methodBuilder.addStatement(
                            "$N($N)", cancelProducersMethod, MAY_INTERRUPT_IF_RUNNING_PARAM);
                    addMethod(MethodSpecKind.CANCELLATION_LISTENER_METHOD, cancelProducersMethod);
                }
            }

            if (isComponentShard()) {
                cancelParentStatement().ifPresent(methodBuilder::addCode);
            }

            addMethod(MethodSpecKind.CANCELLATION_LISTENER_METHOD, methodBuilder.build());
        }

        private Optional<CodeBlock> cancelParentStatement() {
            if (!shouldPropagateCancellationToParent()) {
                return Optional.empty();
            }
            //如果parent存在，并且其component节点使用CancellationPolicy修饰的注解fromSubcomponents方法属性 == PROPAGATE
            return Optional.of(
                    CodeBlock.builder()
                            .addStatement(
                                    "$L.$N($N)",
                                    parent.get().componentFieldReference(),
                                    CANCELLATION_LISTENER_METHOD_NAME,
                                    MAY_INTERRUPT_IF_RUNNING_PARAM)
                            .build());
        }

        private boolean shouldPropagateCancellationToParent() {
            return parent.isPresent()
                    && parent
                    .get()
                    //如果父级component是production类型，那么当前component节点使用CancellationPolicy修饰
                    .componentDescriptor()
                    .cancellationPolicy()
                    //匹配CancellationPolicy注解的fromSubcomponents方法属性 == PROPAGATE
                    .map(policy -> policy.fromSubcomponents().equals(PROPAGATE))
                    .orElse(false);
        }

        /**
         * Creates one or more methods, all taking the given {@code parameters}, which partition the
         * given list of {@code statements} among themselves such that no method has more than {@code
         * STATEMENTS_PER_METHOD} statements in it and such that the returned methods, if called in
         * order, will execute the {@code statements} in the given order.
         * <p>
         * 创建一个或多个方法，所有方法都采用给定的 {@code 参数}，它们将给定的 {@code statements} 列表在它们之间划分，
         * 使得没有方法在其中包含超过 {@code STATEMENTS_PER_METHOD} 语句，并且返回的方法 ，如果按顺序调用，
         * 将按给定顺序执行 {@code statements}。
         */
        private ImmutableList<MethodSpec> createPartitionedMethods(
                String methodName,
                Iterable<ParameterSpec> parameters,
                List<CodeBlock> statements,
                Function<String, MethodSpec.Builder> methodBuilderCreator) {
            return Lists.partition(statements, STATEMENTS_PER_METHOD).stream()
                    .map(
                            partition ->
                                    methodBuilderCreator
                                            .apply(getUniqueMethodName(methodName))
                                            .addModifiers(PRIVATE)
                                            .addParameters(parameters)
                                            .addCode(CodeBlocks.concat(partition))
                                            .build())
                    .collect(toImmutableList());
        }
    }

    //表示生成的component类构造函数的参数来源
    private static ImmutableList<ComponentRequirement> constructorRequirements(BindingGraph graph) {

        //当前有向图的ComponentNodeImpl节点表示的component节点是否存在creator节点。
        if (graph.componentDescriptor().hasCreator()) {

            //（1）componentAnnotation#dependencies生成的ComponentRequirement集合；
            //（2）当前graph图中需要被实例化的module生成的ComponentRequirement集合；
            //（3）使用@BindsInstance修饰的方法或方法参数生成的ComponentRequirement集合；
            //（4）component中的返回类型是ChildComponent类型存在，那么对当前方法的参数（参数肯定是module节点）生成Module类型的ComponentRequirement对象；
            return graph.componentRequirements().asList();
        } else if (graph.factoryMethod().isPresent()) {

            //component中的返回类型是ChildComponent类型，那么对当前方法的参数（参数肯定是module节点）生成Module类型的ComponentRequirement对象；
            return graph.factoryMethodParameters().keySet().asList();
        } else {
            throw new AssertionError(
                    "Expected either a component creator or factory method but found neither.");
        }
    }

    //参数类型设置成final修饰
    private static ImmutableList<ParameterSpec> makeFinal(List<ParameterSpec> parameters) {
        return parameters.stream()
                .map(param -> param.toBuilder().addModifiers(FINAL).build())
                .collect(toImmutableList());
    }
}
