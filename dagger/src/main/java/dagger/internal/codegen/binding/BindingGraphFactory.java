package dagger.internal.codegen.binding;

import com.google.auto.common.MoreTypes;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import dagger.MembersInjector;
import dagger.Reusable;
import dagger.internal.ProductionExecutorModule;
import dagger.internal.codegen.base.ClearableCache;
import dagger.internal.codegen.base.ContributionType;
import dagger.internal.codegen.base.Keys;
import dagger.internal.codegen.base.MapType;
import dagger.internal.codegen.base.OptionalType;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.producers.Produced;
import dagger.producers.Producer;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.Key;
import dagger.spi.model.Scope;

import static com.google.auto.common.MoreTypes.asTypeElement;
import static com.google.auto.common.MoreTypes.isType;
import static com.google.auto.common.MoreTypes.isTypeOf;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.base.RequestKinds.getRequestKind;
import static dagger.internal.codegen.base.Util.reentrantComputeIfAbsent;
import static dagger.internal.codegen.binding.AssistedInjectionAnnotations.isAssistedFactoryType;
import static dagger.internal.codegen.binding.ComponentDescriptor.isComponentContributionMethod;
import static dagger.internal.codegen.binding.SourceFiles.generatedMonitoringModuleName;
import static dagger.spi.model.BindingKind.ASSISTED_INJECTION;
import static dagger.spi.model.BindingKind.DELEGATE;
import static dagger.spi.model.BindingKind.INJECTION;
import static dagger.spi.model.BindingKind.OPTIONAL;
import static dagger.spi.model.BindingKind.SUBCOMPONENT_CREATOR;
import static dagger.spi.model.RequestKind.MEMBERS_INJECTION;
import static java.util.function.Predicate.isEqual;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * A factory for {@link BindingGraph} objects.
 */
@Singleton
public final class BindingGraphFactory implements ClearableCache {

    private final DaggerElements elements;
    private final InjectBindingRegistry injectBindingRegistry;
    private final KeyFactory keyFactory;
    private final BindingFactory bindingFactory;
    private final ModuleDescriptor.Factory moduleDescriptorFactory;
    private final BindingGraphConverter bindingGraphConverter;
    private final Map<Key, ImmutableSet<Key>> keysMatchingRequestCache = new HashMap<>();
    private final CompilerOptions compilerOptions;

    @Inject
    BindingGraphFactory(
            DaggerElements elements,
            InjectBindingRegistry injectBindingRegistry,
            KeyFactory keyFactory,
            BindingFactory bindingFactory,
            ModuleDescriptor.Factory moduleDescriptorFactory,
            BindingGraphConverter bindingGraphConverter,
            CompilerOptions compilerOptions) {
        this.elements = elements;
        this.injectBindingRegistry = injectBindingRegistry;
        this.keyFactory = keyFactory;
        this.bindingFactory = bindingFactory;
        this.moduleDescriptorFactory = moduleDescriptorFactory;
        this.bindingGraphConverter = bindingGraphConverter;
        this.compilerOptions = compilerOptions;
    }

    /**
     * Creates a binding graph for a component.
     *
     * @param createFullBindingGraph if {@code true}, the binding graph will include all bindings;
     *                               otherwise it will include only bindings reachable from at least one entry point
     */
    public BindingGraph create(
            ComponentDescriptor componentDescriptor,
            boolean createFullBindingGraph
    ) {

        return bindingGraphConverter.convert(

                createLegacyBindingGraph(Optional.empty(),
                        componentDescriptor,
                        createFullBindingGraph),
                createFullBindingGraph);
    }

    private LegacyBindingGraph createLegacyBindingGraph(
            Optional<Resolver> parentResolver,
            ComponentDescriptor componentDescriptor,
            boolean createFullBindingGraph) {

        //收集明确的绑定
        //1. 如果是component类，对该component类生成一个ProvisionBinding绑定,Key的type类型是component类
        //2.对component#dependencies里面的类生成ProvisionBinding绑定，Key的type类型是dependency类
        //3.对component#dependencies里面的类的无参、返回类型不是void的所有方法生成ContributionBinding绑定对象，Key的type类型是方法返回类型
        //4.在creator类中找方法或方法参数使用 BindsInstance修饰过的，如果存在，当前方法生成ProvisionBinding绑定，Key的type类型是当前方法参数
        //5.component类的方法返回类型是subcomponent.Builder类，并且subcomponent不包含在父类component关联的moduleAnnotation#subcomponents当中，
        // 该方法节点和该方法所在component类生成一个ProvisionBinding绑定对象，Key的type类型是当前方法的返回类型
        //6.component关联的module类使用Provides或Produces修饰的方法生成ContributionBinding，Key的type类型是方法返回类型根据注解决定是否外裹一层框架类型，如Provider<T>
        ImmutableSet.Builder<ContributionBinding> explicitBindingsBuilder = ImmutableSet.builder();

        //component关联的module类使用Binds修饰的方法生成的DelegateDeclaration
        ImmutableSet.Builder<DelegateDeclaration> delegatesBuilder = ImmutableSet.builder();

        //component关联的module类使用BindsOptionalOf生成的OptionalBindingDeclaration
        ImmutableSet.Builder<OptionalBindingDeclaration> optionalsBuilder = ImmutableSet.builder();

        //如果是真的compoent类（module类生成的该描述类表示不是真实的component描述类）
        if (componentDescriptor.isRealComponent()) {
            // binding for the component itself
            //component类生成一个ProvisionBinding对象
            explicitBindingsBuilder.add(
                    bindingFactory.componentBinding(componentDescriptor.typeElement()));
        }

        // Collect Component dependencies.
        for (ComponentRequirement dependency : componentDescriptor.dependencies()) {

            //component#dependencies里面的类生成ProvisionBinding绑定
            explicitBindingsBuilder.add(bindingFactory.componentDependencyBinding(dependency));

            List<ExecutableElement> dependencyMethods =
                    methodsIn(elements.getAllMembers(dependency.typeElement()));

            // Within a component dependency, we want to allow the same method to appear multiple
            // times assuming it is the exact same method. We do this by tracking a set of bindings
            // we've already added with the binding element removed since that is the only thing
            // allowed to differ.
            //在组件依赖项中，我们希望允许相同的方法多次出现，假设它是完全相同的方法。 我们通过跟踪一组我们已经添加的绑定来实现这一点，并删除了绑定元素，因为这是唯一允许不同的东西。
            HashMultimap<String, ContributionBinding> dedupeBindings = HashMultimap.create();
            for (ExecutableElement method : dependencyMethods) {
                // MembersInjection methods aren't "provided" explicitly, so ignore them.
                //component#dependencies里面的类的无参返回类型不是void的所有方法遍历
                if (isComponentContributionMethod(elements, method)) {

                    ContributionBinding binding = bindingFactory.componentDependencyMethodBinding(
                            componentDescriptor, method);

                    if (dedupeBindings.put(
                            method.getSimpleName().toString(),
                            // Remove the binding element since we know that will be different, but everything
                            // else we want to be the same to consider it a duplicate.
                            //删除绑定元素，因为我们知道这会有所不同，但我们希望其他所有内容都相同以将其视为重复项。
                            binding.toBuilder().clearBindingElement().build())) {

                        //对component#dependencies里面的dependency类的无参返回类型不是void的所有方法生成ContributionBinding绑定对象
                        explicitBindingsBuilder.add(binding);
                    }
                }
            }
        }

        // Collect bindings on the creator.
        componentDescriptor
                .creatorDescriptor()
                .ifPresent(
                        creatorDescriptor ->
                                //component.creator中被@BindsInstance修饰过的方法或方法参数，该方法参数（方法参数根据RequestKind剥离外层框架），生成ProvisionBinding对象
                                creatorDescriptor.boundInstanceRequirements().stream()
                                        .map(
                                                requirement ->
                                                        bindingFactory.boundInstanceBinding(
                                                                requirement, creatorDescriptor.elementForRequirement(requirement)))
                                        .forEach(explicitBindingsBuilder::add));

        componentDescriptor
                .childComponentsDeclaredByBuilderEntryPoints()//当前component中的方法的返回类型是一个subcomponent.creator(Builder)
                .forEach(
                        (builderEntryPoint, childComponent) -> {

                            //该方法返回类型subcomponent类不包含在父类component的module#subcomponents当中
                            if (!componentDescriptor
                                    .childComponentsDeclaredByModules()
                                    .contains(childComponent)) {

                                //该方法节点和该方法所在component类生成一个ProvisionBinding绑定对象
                                explicitBindingsBuilder.add(
                                        bindingFactory.subcomponentCreatorBinding(
                                                builderEntryPoint.methodElement(), componentDescriptor.typeElement()));
                            }

                        });

        //component关联的module类使用Multibinds修饰的方法生成MultibindingDeclaration对象
        ImmutableSet.Builder<MultibindingDeclaration> multibindingDeclarations = ImmutableSet.builder();

        //component关联的module类的注解moduleAnnotation#subcomponents()里面的类生成的SubcomponentDeclaration
        ImmutableSet.Builder<SubcomponentDeclaration> subcomponentDeclarations = ImmutableSet.builder();

        // Collect transitive module bindings and multibinding declarations.

        //component#modules里面item以及该item注解module#includes所有module类
        for (ModuleDescriptor moduleDescriptor : modules(componentDescriptor, parentResolver)) {

            //component关联的module类使用Provides或Produces修饰的方法生成ContributionBinding
            explicitBindingsBuilder.addAll(moduleDescriptor.bindings());

            multibindingDeclarations.addAll(moduleDescriptor.multibindingDeclarations());

            subcomponentDeclarations.addAll(moduleDescriptor.subcomponentDeclarations());

            delegatesBuilder.addAll(moduleDescriptor.delegateDeclarations());
            optionalsBuilder.addAll(moduleDescriptor.optionalDeclarations());
        }

        final Resolver requestResolver =
                new Resolver(
                        //父级Resolver,componentAll节点只有componentMethod方法返回类型是subcomponent或subcomponent.creator，该subcomponent生成的Resolver对象才会存在由componentAll节点生成的parentPesolver
                        parentResolver,
                        componentDescriptor,//当前component的描述类
                        //K：BindingDeclaration对象中的Key，
                        // V：①如果是component类，对该component类生成一个ProvisionBinding绑定
                        //②对component#dependencies里面的类生成ProvisionBinding绑定
                        //③对component#dependencies里面的类的无参返回类型不是void的所有方法生成ContributionBinding绑定对象
                        //④在creator类中找方法或方法参数使用 BindsInstance修饰过的，如果存在，生成ProvisionBinding绑定
                        //⑤component类中方法返回类型subcomponent类不包含在component的module#subcomponents当中，该方法节点和该方法所在component类生成一个ProvisionBinding绑定对象
                        //⑥component#modules和该item使用的注解modules#incluedes所有的module类使用Provides或Produces修饰的方法生成ContributionBinding
                        indexBindingDeclarationsByKey(explicitBindingsBuilder.build()),

                        //K：BindingDeclaration对象中的Key，
                        // V：component#modules和该item使用的注解modules#incluedes所有的module类使用Multibinds修饰的方法生成MultibindingDeclaration对象
                        indexBindingDeclarationsByKey(multibindingDeclarations.build()),

                        //K：BindingDeclaration对象中的Key
                        // V：component#modules和该item使用的注解modules#incluedes所有的module类的注解Module#subcomponents()里面的类生成的SubcomponentDeclaration
                        indexBindingDeclarationsByKey(subcomponentDeclarations.build()),

                        //K：BindingDeclaration对象中的Key
                        // V：component#modules和该item使用的注解modules#incluedes所有的module类使用Binds修饰的方法生成的DelegateDeclaration
                        indexBindingDeclarationsByKey(delegatesBuilder.build()),

                        //K：BindingDeclaration对象中的Key
                        // V：component#modules和该item使用的注解modules#incluedes所有的module类使用BindsOptionalOf生成的OptionalBindingDeclaration
                        indexBindingDeclarationsByKey(optionalsBuilder.build()));

        //component节点上非private、非static、abstract(接口除外)的有参数的方法集合
        componentDescriptor.entryPointMethods().stream()
                .map(method -> method.dependencyRequest().get())
                .forEach(
                        entryPoint -> {
                            //作为成员注入该component：component类中的方法有且仅有一个参数,返回类型是void或参数类型和方法返回类型必须一致
                            if (entryPoint.kind().equals(MEMBERS_INJECTION)) {
                                //将该方法生成的依赖(来源：dependencyRequestFactory.forComponentMembersInjectionMethod)
                                //key：type方法参数类型
                                requestResolver.resolveMembersInjection(entryPoint.key());
                            } else {
                                //key：方法参数
                                requestResolver.resolve(entryPoint.key());
                            }
                        });

        if (createFullBindingGraph) {
            // Resolve the keys for all bindings in all modules, stripping any multibinding contribution
            // identifier so that the multibinding itself is resolved.
            modules(componentDescriptor, parentResolver).stream()
                    .flatMap(module -> module.allBindingKeys().stream())
                    .map(key -> key.toBuilder().multibindingContributionIdentifier(Optional.empty()).build())
                    .forEach(requestResolver::resolve);
        }

        // Resolve all bindings for subcomponents, creating subgraphs for all subcomponents that have
        // been detected during binding resolution. If a binding for a subcomponent is never resolved,
        // no BindingGraph will be created for it and no implementation will be generated. This is
        // done in a queue since resolving one subcomponent might resolve a key for a subcomponent
        // from a parent graph. This is done until no more new subcomponents are resolved.
        Set<ComponentDescriptor> resolvedSubcomponents = new HashSet<>();
        ImmutableList.Builder<LegacyBindingGraph> subgraphs = ImmutableList.builder();
        for (ComponentDescriptor subcomponent :
                Iterables.consumingIterable(requestResolver.subcomponentsToResolve)) {
            if (resolvedSubcomponents.add(subcomponent)) {
                subgraphs.add(
                        createLegacyBindingGraph(
                                Optional.of(requestResolver), subcomponent, createFullBindingGraph));
            }
        }

        return new LegacyBindingGraph(
                componentDescriptor,
                ImmutableMap.copyOf(requestResolver.getResolvedContributionBindings()),
                ImmutableMap.copyOf(requestResolver.getResolvedMembersInjectionBindings()),
                ImmutableList.copyOf(subgraphs.build()));
    }

    /**
     * Returns all the modules that should be installed in the component. For production components
     * and production subcomponents that have a parent that is not a production component or
     * subcomponent, also includes the production monitoring module for the component and the
     * production executor module.
     */
    private ImmutableSet<ModuleDescriptor> modules(
            ComponentDescriptor componentDescriptor, Optional<Resolver> parentResolver) {

        //是否需要加入隐式的module描述类
        return shouldIncludeImplicitProductionModules(componentDescriptor, parentResolver)
                ? new ImmutableSet.Builder<ModuleDescriptor>()
                .addAll(componentDescriptor.modules())
                .add(descriptorForMonitoringModule(componentDescriptor.typeElement()))
                .add(descriptorForProductionExecutorModule())
                .build()
                : componentDescriptor.modules();
    }


    private boolean shouldIncludeImplicitProductionModules(
            ComponentDescriptor component, Optional<Resolver> parentResolver) {
        //是production类型的component && (不是subcomponent && 并且是真正的component类(不是module类)) || （parentResolver存在 && parentResolver中的component是一个production）
        return component.isProduction()
                && ((!component.isSubcomponent() && component.isRealComponent())
                || (parentResolver.isPresent()
                && !parentResolver.get().componentDescriptor.isProduction()));
    }

    /**
     * Returns a descriptor for a generated module that handles monitoring for production components.
     * This module is generated in the {@link
     * dagger.internal.codegen.validation.MonitoringModuleProcessingStep}.
     *
     * @throws TypeNotPresentException if the module has not been generated yet. This will cause the
     *                                 processor to retry in a later processing round.
     */
    private ModuleDescriptor descriptorForMonitoringModule(TypeElement componentDefinitionType) {
        return moduleDescriptorFactory.create(
                elements.checkTypePresent(
                        generatedMonitoringModuleName(componentDefinitionType).toString()));
    }

    /**
     * Returns a descriptor {@link ProductionExecutorModule}.
     */
    private ModuleDescriptor descriptorForProductionExecutorModule() {
        return moduleDescriptorFactory.create(elements.getTypeElement(ProductionExecutorModule.class));
    }

    /**
     * Indexes {@code bindingDeclarations} by {@link BindingDeclaration#key()}.
     */
    private static <T extends BindingDeclaration>
    ImmutableSetMultimap<Key, T> indexBindingDeclarationsByKey(Iterable<T> declarations) {
        return ImmutableSetMultimap.copyOf(Multimaps.index(declarations, BindingDeclaration::key));
    }

    @Override
    public void clearCache() {
        keysMatchingRequestCache.clear();
    }


    private final class Resolver {
        //如果表示的是subcomponent节点：是父级component的方法返回类型是subcomponent或subcomponent.creator的subcomponent节点，该parentResolver表示component生成的Resolver解析器对象
        final Optional<Resolver> parentResolver;

        //当前component节点描述类（或者module节点生成component描述类）
        final ComponentDescriptor componentDescriptor;

        //(1)如果是component节点（非module），当前component节点生成的ProvisionBinding绑定对象
        //(2)componentAnnotation#dependencies中的dependency生成ProvisionBinding对象
        //(3)componentAnnotation#dependencies中的dependency节点中无参返回类型不是void的方法生成ContributionBinding对象，当前dependency所在component如果使用的是ProductionComponent注解生成的是ProductionBinding对象，Component注解生成ProvisionBinding对象
        //(4)component.creator中被@BindsInstance修饰过的方法或方法参数，该方法参数（方法参数根据RequestKind剥离外层框架），生成ProvisionBinding对象
        //(5)如果当前component节点方法返回类型是一个childcomponent.creator（Builder），并且childcomponent不在componentAnnotation#modules以及moduleAnnotation#includes里面的所有module节点上的moduleAnnotation#subcomponents里面的subcomponent节点集合中
        //(6)component关联的module类使用Provides或Produces修饰的方法生成ContributionBinding
        final ImmutableSetMultimap<Key, ContributionBinding> explicitBindings;

        //explicitBindings参数
        final ImmutableSet<ContributionBinding> explicitBindingsSet;

        //explicitBindings 筛选多重绑定：使用@Provides或@Produces修饰的bindingMethod也是使用了@IntoMap，@IntoSet，@ElementsIntoSet修饰的bindingMethod
        final ImmutableSetMultimap<Key, ContributionBinding> explicitMultibindings;

        //component关联的module类使用Multibinds修饰的方法生成MultibindingDeclaration对象
        final ImmutableSetMultimap<Key, MultibindingDeclaration> multibindingDeclarations;

        //component关联的module类的注解moduleAnnotation#subcomponents()里面的类生成的SubcomponentDeclaration
        final ImmutableSetMultimap<Key, SubcomponentDeclaration> subcomponentDeclarations;

        //component关联的module类使用Binds修饰的方法生成的DelegateDeclaration
        final ImmutableSetMultimap<Key, DelegateDeclaration> delegateDeclarations;

        //component关联的module类使用BindsOptionalOf生成的OptionalBindingDeclaration
        final ImmutableSetMultimap<Key, OptionalBindingDeclaration> optionalBindingDeclarations;

        //optionalBindingDeclarations Binds绑定筛选出还使用@IntoMap，@IntoSet，@ElementsIntoSet注解修饰的绑定方法
        final ImmutableSetMultimap<Key, DelegateDeclaration> delegateMultibindingDeclarations;

        //
        final Map<Key, ResolvedBindings> resolvedContributionBindings = new LinkedHashMap<>();

        //K：key需要注入实例的类生成的对象，例如ComponentProcessor类中使用@Inject修饰的变量表示需要注入这些变量实例；
        // V：ResolvedBindings，当前需要注入实例的类生成MembersInjectionBinding对象注册到injectBindingRegistryimpl中，
        // 并且MembersInjectionBinding作为allMembersInjectionBindings属性的V，（K：当前所属component节点）
        final Map<Key, ResolvedBindings> resolvedMembersInjectionBindings = new LinkedHashMap<>();

        final Deque<Key> cycleStack = new ArrayDeque<>();
        final Map<Key, Boolean> keyDependsOnLocalBindingsCache = new HashMap<>();
        final Map<Binding, Boolean> bindingDependsOnLocalBindingsCache = new HashMap<>();
        final Queue<ComponentDescriptor> subcomponentsToResolve = new ArrayDeque<>();

        Resolver(
                Optional<Resolver> parentResolver,
                ComponentDescriptor componentDescriptor,
                ImmutableSetMultimap<Key, ContributionBinding> explicitBindings,
                ImmutableSetMultimap<Key, MultibindingDeclaration> multibindingDeclarations,
                ImmutableSetMultimap<Key, SubcomponentDeclaration> subcomponentDeclarations,
                ImmutableSetMultimap<Key, DelegateDeclaration> delegateDeclarations,
                ImmutableSetMultimap<Key, OptionalBindingDeclaration> optionalBindingDeclarations) {

            this.parentResolver = parentResolver;
            this.componentDescriptor = checkNotNull(componentDescriptor);
            this.explicitBindings = checkNotNull(explicitBindings);
            this.explicitBindingsSet = ImmutableSet.copyOf(explicitBindings.values());
            this.multibindingDeclarations = checkNotNull(multibindingDeclarations);
            this.subcomponentDeclarations = checkNotNull(subcomponentDeclarations);
            this.delegateDeclarations = checkNotNull(delegateDeclarations);
            this.optionalBindingDeclarations = checkNotNull(optionalBindingDeclarations);

            //绑定方法上使用了@IntoMap或@IntoSet或@ElementsIntoSet注解中的任何一个
            this.explicitMultibindings = multibindingContributionsByMultibindingKey(explicitBindingsSet);
            this.delegateMultibindingDeclarations =
                    multibindingContributionsByMultibindingKey(delegateDeclarations.values());

            //component节点方法返回类型是subcomponent节点（使用subcomponentAnnotation注解）
            subcomponentsToResolve.addAll(
                    componentDescriptor.childComponentsDeclaredByFactoryMethods().values());

            //component节点的方法返回类型是subcomponent.builder节点（使用subcomponentAnnotation.creatorAnnotation注解）
            subcomponentsToResolve.addAll(
                    componentDescriptor.childComponentsDeclaredByBuilderEntryPoints().values());
        }

        /**
         * Returns the resolved contribution bindings for the given {@link Key}:
         *
         * <ul>
         *   <li>All explicit bindings for:
         *       <ul>
         *         <li>the requested key
         *         <li>{@code Set<T>} if the requested key's type is {@code Set<Produced<T>>}
         *         <li>{@code Map<K, Provider<V>>} if the requested key's type is {@code Map<K,
         *             Producer<V>>}.
         *       </ul>
         *   <li>An implicit {@link Inject @Inject}-annotated constructor binding if there is one and
         *       there are no explicit bindings or synthetic bindings.
         * </ul>
         */
        ResolvedBindings lookUpBindings(Key requestKey) {

            Set<ContributionBinding> bindings = new LinkedHashSet<>();

            //1.explicitMultibindings表示使用@Provides或@Produces修饰的bindingMethod也是使用了@IntoMap，@IntoSet，@ElementsIntoSet修饰的bindingMethod
            // 生成的ContributionBinding对象(当前对象的key属性的type属性：要么是Set<返回类型>集合，要么是Map<K,Provider<方法返回类型>>),匹配key对象
            //2.delegateMultibindingDeclarations表示使用@Binds && @IntoSet、@IntoMap、@ElementsIntoSet中任意一个注解 的方法生成的DelegateDeclaration对象(key的type属性：要么是Set<方法返回类型>，要么是Map<K,方法返回类型>)
            // 参数key处理的type（如果是Map<K, Provider<V>>,Map<K, Producer<V>>，Map<K, Produced<V>>返回Map<K, V>;否则还是使用type）作为新的key匹配收集DelegateDeclaration对象；
            //筛选出的delegateDeclarations生成ContributionBinding对象
            Set<ContributionBinding> multibindingContributions = new LinkedHashSet<>();

            //component关联的module类使用Multibinds修饰的方法生成MultibindingDeclaration对象
            // (key对象type属性：如果bindingMetghod方法returnType不是Map类型，直接使用bindingMethod方法的返回类型returnType；如果returnType是Map<K,V>类型，使用Map<K,Provider<V>>类型)
            //keysMatchingRequest匹配MultibindingDeclaration对象对象的key
            Set<MultibindingDeclaration> multibindingDeclarations = new LinkedHashSet<>();

            //在component关联的module关联的@BindsOptionalOf修饰的bindingMethod方法生成的OptionalBindingDeclaration对象（bindingMethod方法返回类型作为type生成的key）中查找：
            //如果keysMatchingRequest的type是Optional<T>类型，那么重新生成一个type是T类型的key对象，匹配OptionalBindingDeclaration的key属性
            Set<OptionalBindingDeclaration> optionalBindingDeclarations = new LinkedHashSet<>();

            //在component节点关联的module - moduleAnnotation#subcomponent的subcomponent节点生成的SubcomponentDeclaration对象
            // （key：将当前subcomponent.creator节点类型作为type）中查找和keysMatchingRequest匹配的SubcomponentDeclaration对象
            Set<SubcomponentDeclaration> subcomponentDeclarations = new LinkedHashSet<>();

            // Gather all bindings, multibindings, optional, and subcomponent declarations/contributions.
            //1.requestKey本身
            //2.如果requestKey type类型是Set<Produced<T>>返回Set<T>
            //3.如果requestKey type类型是Map<K, Producer<T>>返回Map<K, Producer<T>>
            //4.如果requestKey type类型是Map<K, Provider<T>>返回Map<K, Producer<T>>
            //5.如果requestKey type类型是 Map<K, V> 或   Map<K, Produced<V>>改成Map<K, Provider<V>> 和 {@code Map<K, Producer<V>>；
            ImmutableSet<Key> keysMatchingRequest = keysMatchingRequest(requestKey);

            //从当前resolver一直到父级
            for (Resolver resolver : getResolverLineage()) {

                //1.explicitBindings匹配requestKey
                //2.(1)key表示type类型（如果是Map<K, Provider<V>>,Map<K, Producer<V>>，Map<K, Produced<V>>返回Map<K, V>）生成新的key,否则沿用之前的key对象；
                // （2）在delegateDeclarations（component关联的module关联的使用@Binds修饰的bindingMethod方法生成的对象，requestKey的type是bindingMethod方法返回类型）中匹配（1）中的key对象
                //（3）（2）中收集的delegateDeclaration对象生成ContributionBinding绑定对象
                bindings.addAll(resolver.getLocalExplicitBindings(requestKey));

                for (Key key : keysMatchingRequest) {
                    //1.explicitMultibindings表示使用@Provides或@Produces修饰的bindingMethod也是使用了@IntoMap，@IntoSet，@ElementsIntoSet修饰的bindingMethod
                    // 生成的ContributionBinding对象(当前对象的key属性的type属性：要么是Set<返回类型>集合，要么是Map<K,Provider<方法返回类型>>),匹配keysMatchingRequest
                    //2. (1)keysMatchingRequest，根据type类型筛选：(① 不是Map类型) || (Map(而不是Map<K,V>)) || (Map<K,V>，V是Produced、Producer、Provider、Lazy、MembersInjector)
                    // (2)delegateMultibindingDeclarations表示使用@Binds && @IntoSet、@IntoMap、@ElementsIntoSet中任意一个注解 的方法生成的DelegateDeclaration对象(key的type属性：要么是Set<方法返回类型>，要么是Map<K,方法返回类型>)
                    // (3)在（1）中筛选出的keysMatchingRequest（如果是Map<K, Provider<V>>,Map<K, Producer<V>>，Map<K, Produced<V>>返回Map<K, V>;否则还是使用type）生成新的key匹配（2）；
                    //（4）在（3）中筛选出的delegateDeclarations生成ContributionBinding对象；
                    multibindingContributions.addAll(resolver.getLocalExplicitMultibindings(key));

                    //component关联的module类使用Multibinds修饰的方法生成MultibindingDeclaration对象
                    // (key对象type属性：如果bindingMetghod方法returnType不是Map类型，直接使用bindingMethod方法的返回类型returnType；如果returnType是Map<K,V>类型，使用Map<K,Provider<V>>类型)
                    //keysMatchingRequest匹配MultibindingDeclaration对象对象的key
                    multibindingDeclarations.addAll(resolver.multibindingDeclarations.get(key));
                    //在component节点关联的module - moduleAnnotation#subcomponent的subcomponent节点生成的SubcomponentDeclaration对象（key：将当前subcomponent.creator节点类型作为type）中查找和keysMatchingRequest匹配的SubcomponentDeclaration对象
                    subcomponentDeclarations.addAll(resolver.subcomponentDeclarations.get(key));
                    // The optional binding declarations are keyed by the unwrapped type.
                    //在component关联的module关联的@BindsOptionalOf修饰的bindingMethod方法生成的OptionalBindingDeclaration对象（bindingMethod方法返回类型作为type生成的key）中查找：
                    //如果requestKey的type是Optional<T>类型，那么重新生成一个type是T类型的key对象，匹配OptionalBindingDeclaration的key属性
                    keyFactory.unwrapOptional(key)
                            .map(resolver.optionalBindingDeclarations::get)
                            .ifPresent(optionalBindingDeclarations::addAll);
                }
            }

            // Add synthetic（合成的） multibinding
            //(1)使用@Provides或@Produces修饰的bindingMethod也是使用了@IntoMap，@IntoSet，@ElementsIntoSet修饰的bindingMethod生成的生成的ContributionBinding对象(当前对象的key属性的type属性：要么是Set<返回类型>集合，要么是Map<K,Provider<方法返回类型>>)
            //(2)使用@Binds && @IntoSet、@IntoMap、@ElementsIntoSet中任意一个注解 的方法生成的DelegateDeclaration对象(key的type属性：要么是Set<方法返回类型>，要么是Map<K,方法返回类型>)
            //(3) 使用Multibinds修饰的bindingMethod方法生成MultibindingDeclaration对象 (key对象type属性：如果bindingMetghod方法returnType不是Map类型，直接使用bindingMethod方法的返回类型returnType；如果returnType是Map<K,V>类型，使用Map<K,Provider<V>>类型)
            //如果（1），（2），（3）中匹配上keysMatchingRequest,那么将收集（1）和（2）中匹配的ContributionBinding对象
            if (!multibindingContributions.isEmpty() || !multibindingDeclarations.isEmpty()) {
                bindings.add(bindingFactory.syntheticMultibinding(requestKey, multibindingContributions));
            }

            // Add synthetic optional binding
            if (!optionalBindingDeclarations.isEmpty()) {
                bindings.add(
                        bindingFactory.syntheticOptionalBinding(
                                requestKey,
                                getRequestKind(OptionalType.from(requestKey).valueType()),
                                lookUpBindings(keyFactory.unwrapOptional(requestKey).get()).bindings()));
            }

            // Add subcomponent creator binding
            if (!subcomponentDeclarations.isEmpty()) {
                ProvisionBinding binding =
                        bindingFactory.subcomponentCreatorBinding(
                                ImmutableSet.copyOf(subcomponentDeclarations));
                bindings.add(binding);
                addSubcomponentToOwningResolver(binding);
            }

            // Add members injector binding
            if (isType(requestKey.type().java())
                    && isTypeOf(MembersInjector.class, requestKey.type().java())) {
                injectBindingRegistry
                        .getOrFindMembersInjectorProvisionBinding(requestKey)
                        .ifPresent(bindings::add);
            }

            // Add Assisted Factory binding
            if (isType(requestKey.type().java())
                    && requestKey.type().java().getKind() == TypeKind.DECLARED
                    && isAssistedFactoryType(asTypeElement(requestKey.type().java()))) {
                bindings.add(
                        bindingFactory.assistedFactoryBinding(
                                asTypeElement(requestKey.type().java()), Optional.of(requestKey.type().java())));
            }

            // If there are no bindings, add the implicit @Inject-constructed binding if there is one.
            if (bindings.isEmpty()) {
                injectBindingRegistry
                        .getOrFindProvisionBinding(requestKey)
                        .filter(this::isCorrectlyScopedInSubcomponent)
                        .ifPresent(bindings::add);
            }

            return ResolvedBindings.forContributionBindings(
                    requestKey,
                    Multimaps.index(bindings, binding -> getOwningComponent(requestKey, binding)),
                    multibindingDeclarations,
                    subcomponentDeclarations,
                    optionalBindingDeclarations);
        }

        /**
         * Returns true if this binding graph resolution is for a subcomponent and the {@code @Inject}
         * binding's scope correctly matches one of the components in the current component ancestry.
         * If not, it means the binding is not owned by any of the currently known components, and will
         * be owned by a future ancestor (or, if never owned, will result in an incompatibly scoped
         * binding error at the root component).
         * <p>
         * 检查当前绑定的key的type使用了Inject修饰或AssistedInject修饰
         */
        private boolean isCorrectlyScopedInSubcomponent(ProvisionBinding binding) {
            checkArgument(binding.kind() == INJECTION || binding.kind() == ASSISTED_INJECTION);

            //resolved最root的component不是subcomponent节点 || 当前绑定的key的type不存在Scope修饰的注解修饰 || 当前绑定的key的type使用了Reusable注解修饰了
            if (!rootComponent().isSubcomponent()
                    || !binding.scope().isPresent()
                    || binding.scope().get().isReusable()) {
                return true;
            }

            Resolver owningResolver = getOwningResolver(binding).orElse(this);
            ComponentDescriptor owningComponent = owningResolver.componentDescriptor;
            return owningComponent.scopes().contains(binding.scope().get());
        }

        private ComponentDescriptor rootComponent() {
            return parentResolver.map(Resolver::rootComponent).orElse(componentDescriptor);
        }

        /**
         * Returns the resolved members injection bindings for the given {@link Key}.
         * <p>
         * component类中存在一个参数的方法（并且返回类型是void或返回类型和参数类型一致），该方法参数表示一个成员注入，需要去injectBindingRegistry注册该成员绑定
         * <p>
         * 注：如果该方法参数有非Object父级节点，那么将当前父级节点继续作为成员注入，去injectBindingRegistry注册该成员绑定；
         * 并且这里得到的binding一定是最父级（最接近Object的节点）生成的那个MembersInjectionBinding
         * <p>
         * （1）如果注册成功（校验成功，并且注册）：生成一个MembersInjectionBinding；
         * （2）注册失败（校验失败）：生成一个noBindings对象
         */
        ResolvedBindings lookUpMembersInjectionBinding(Key requestKey) {
            // no explicit deps for members injection, so just look it up
            Optional<MembersInjectionBinding> binding =
                    injectBindingRegistry.getOrFindMembersInjectionBinding(requestKey);

            //requestKey表示component中的方法参数
            return binding.isPresent()
                    ? ResolvedBindings.forMembersInjectionBinding(
                    requestKey, componentDescriptor, binding.get())
                    : ResolvedBindings.noBindings(requestKey);
        }

        /**
         * When a binding is resolved for a {@link SubcomponentDeclaration}, adds corresponding {@link
         * ComponentDescriptor subcomponent} to a queue in the owning component's resolver. The queue
         * will be used to detect which subcomponents need to be resolved.
         */
        private void addSubcomponentToOwningResolver(ProvisionBinding subcomponentCreatorBinding) {
            checkArgument(subcomponentCreatorBinding.kind().equals(SUBCOMPONENT_CREATOR));
            Resolver owningResolver = getOwningResolver(subcomponentCreatorBinding).get();

            TypeElement builderType =
                    MoreTypes.asTypeElement(subcomponentCreatorBinding.key().type().java());
            owningResolver.subcomponentsToResolve.add(
                    owningResolver.componentDescriptor.getChildComponentWithBuilderType(builderType));
        }

        /**
         * Profiling has determined that computing the keys matching {@code requestKey} has measurable
         * performance impact. It is called repeatedly (at least 3 times per key resolved per {@link
         * BindingGraph}. {@code javac}'s name-checking performance seems suboptimal (converting byte
         * strings to Strings repeatedly), and the matching keys creations relies on that. This also
         * ensures that the resulting keys have their hash codes cached on successive calls to this
         * method.
         *
         * <p>This caching may become obsolete if:
         *
         * <ul>
         *   <li>We decide to intern all {@link Key} instances
         *   <li>We fix javac's name-checking peformance (though we may want to keep this for older
         *       javac users)
         * </ul>
         */
        private ImmutableSet<Key> keysMatchingRequest(Key requestKey) {
            return keysMatchingRequestCache.computeIfAbsent(
                    requestKey, this::keysMatchingRequestUncached);
        }

        //对Key进行
        private ImmutableSet<Key> keysMatchingRequestUncached(Key requestKey) {
            ImmutableSet.Builder<Key> keys = ImmutableSet.builder();
            //1.requestKey本身
            keys.add(requestKey);
            //2.如果是Set<Produced<T>>返回Set<T>，否则不处理；
            keyFactory.unwrapSetKey(requestKey, Produced.class).ifPresent(keys::add);
            //3.如果是Map<K, Producer<T>>返回Map<K, Producer<T>>，否则不处理；
            keyFactory.rewrapMapKey(requestKey, Producer.class, Provider.class).ifPresent(keys::add);
            //4.如果是Map<K, Provider<T>>返回Map<K, Producer<T>>，否则不处理；
            keyFactory.rewrapMapKey(requestKey, Provider.class, Producer.class).ifPresent(keys::add);
            //5.如果是 Map<K, V>} 或   Map<K, Produced<V>>改成Map<K, Provider<V>>} 和 {@code Map<K, Producer<V>>；
            keys.addAll(keyFactory.implicitFrameworkMapKeys(requestKey));
            return keys.build();
        }

        private ImmutableSet<ContributionBinding> createDelegateBindings(
                ImmutableSet<DelegateDeclaration> delegateDeclarations) {
            ImmutableSet.Builder<ContributionBinding> builder = ImmutableSet.builder();
            for (DelegateDeclaration delegateDeclaration : delegateDeclarations) {
                builder.add(createDelegateBinding(delegateDeclaration));
            }
            return builder.build();
        }

        /**
         * Creates one (and only one) delegate binding for a delegate declaration, based on the resolved
         * bindings of the right-hand-side of a {@link dagger.Binds} method. If there are duplicate
         * bindings for the dependency key, there should still be only one binding for the delegate key.
         * <p>
         * Binds修饰的方法DelegateDeclaration生成ContributionBinding对象
         */
        private ContributionBinding createDelegateBinding(DelegateDeclaration delegateDeclaration) {

            //Binds修饰的方法参数生成的key
            Key delegateKey = delegateDeclaration.delegateRequest().key();
            if (cycleStack.contains(delegateKey)) {
                return bindingFactory.unresolvedDelegateBinding(delegateDeclaration);
            }

            ResolvedBindings resolvedDelegate;
            try {
                cycleStack.push(delegateKey);
                //对新的依赖的key生成forContributionBindings类型ResolvedBindings对象
                resolvedDelegate = lookUpBindings(delegateKey);
            } finally {
                cycleStack.pop();
            }

            //表示component、非private、非static、abstract(接口除外)的返回类型是void或返回类型和参数类型一致的参数生成ResolvedBindings
            if (resolvedDelegate.contributionBindings().isEmpty()) {
                // This is guaranteed to result in a missing binding error, so it doesn't matter if the
                // binding is a Provision or Production, except if it is a @IntoMap method, in which
                // case the key will be of type Map<K, Provider<V>>, which will be "upgraded" into a
                // Map<K, Producer<V>> if it's requested in a ProductionComponent. This may result in a
                // strange error, that the RHS needs to be provided with an @Inject or @Provides
                // annotated method, but a user should be able to figure out if a @Produces annotation
                // is needed.
                // TODO(gak): revisit how we model missing delegates if/when we clean up how we model
                // binding declarations
                return bindingFactory.unresolvedDelegateBinding(delegateDeclaration);
            }
            // It doesn't matter which of these is selected, since they will later on produce a
            // duplicate binding error.
            ContributionBinding explicitDelegate =
                    resolvedDelegate.contributionBindings().iterator().next();
            return bindingFactory.delegateBinding(delegateDeclaration, explicitDelegate);
        }

        /**
         * Returns the component that should contain the framework field for {@code binding}.
         *
         * <p>If {@code binding} is either not bound in an ancestor component or depends transitively on
         * bindings in this component, returns this component.
         *
         * <p>Otherwise, resolves {@code request} in this component's parent in order to resolve any
         * multibinding contributions in the parent, and returns the parent-resolved {@link
         * ResolvedBindings#owningComponent(ContributionBinding)}.
         */
        private TypeElement getOwningComponent(Key requestKey, ContributionBinding binding) {
            if (isResolvedInParent(requestKey, binding)
                    && !new LocalDependencyChecker().dependsOnLocalBindings(binding)) {
                ResolvedBindings parentResolvedBindings =
                        parentResolver.get().resolvedContributionBindings.get(requestKey);
                return parentResolvedBindings.owningComponent(binding);
            } else {
                return componentDescriptor.typeElement();
            }
        }

        /**
         * Returns {@code true} if {@code binding} is owned by an ancestor. If so, {@linkplain #resolve
         * resolves} the {@link Key} in this component's parent. Don't resolve directly in the owning
         * component in case it depends on multibindings in any of its descendants.
         */
        private boolean isResolvedInParent(Key requestKey, ContributionBinding binding) {
            Optional<Resolver> owningResolver = getOwningResolver(binding);
            if (owningResolver.isPresent() && !owningResolver.get().equals(this)) {
                parentResolver.get().resolve(requestKey);
                return true;
            } else {
                return false;
            }
        }

        private Optional<Resolver> getOwningResolver(ContributionBinding binding) {
            // TODO(ronshapiro): extract the different pieces of this method into their own methods
            if ((binding.scope().isPresent() && binding.scope().get().isProductionScope())
                    || binding.bindingType().equals(BindingType.PRODUCTION)) {
                for (Resolver requestResolver : getResolverLineage()) {
                    // Resolve @Inject @ProductionScope bindings at the highest production component.
                    if (binding.kind().equals(INJECTION)
                            && requestResolver.componentDescriptor.isProduction()) {
                        return Optional.of(requestResolver);
                    }

                    // Resolve explicit @Produces and @ProductionScope bindings at the highest component that
                    // installs the binding.
                    if (requestResolver.containsExplicitBinding(binding)) {
                        return Optional.of(requestResolver);
                    }
                }
            }

            if (binding.scope().isPresent() && binding.scope().get().isReusable()) {
                for (Resolver requestResolver : getResolverLineage().reverse()) {
                    // If a @Reusable binding was resolved in an ancestor, use that component.
                    ResolvedBindings resolvedBindings =
                            requestResolver.resolvedContributionBindings.get(binding.key());
                    if (resolvedBindings != null
                            && resolvedBindings.contributionBindings().contains(binding)) {
                        return Optional.of(requestResolver);
                    }
                }
                // If a @Reusable binding was not resolved in any ancestor, resolve it here.
                return Optional.empty();
            }

            for (Resolver requestResolver : getResolverLineage().reverse()) {
                if (requestResolver.containsExplicitBinding(binding)) {
                    return Optional.of(requestResolver);
                }
            }

            // look for scope separately.  we do this for the case where @Singleton can appear twice
            // in the † compatibility mode
            Optional<Scope> bindingScope = binding.scope();
            if (bindingScope.isPresent()) {
                for (Resolver requestResolver : getResolverLineage().reverse()) {
                    if (requestResolver.componentDescriptor.scopes().contains(bindingScope.get())) {
                        return Optional.of(requestResolver);
                    }
                }
            }
            return Optional.empty();
        }

        private boolean containsExplicitBinding(ContributionBinding binding) {
            return explicitBindingsSet.contains(binding)
                    || resolverContainsDelegateDeclarationForBinding(binding)
                    || subcomponentDeclarations.containsKey(binding.key());
        }

        /**
         * Returns true if {@code binding} was installed in a module in this resolver's component.
         */
        private boolean resolverContainsDelegateDeclarationForBinding(ContributionBinding binding) {
            if (!binding.kind().equals(DELEGATE)) {
                return false;
            }

            // Map multibinding key values are wrapped with a framework type. This needs to be undone
            // to look it up in the delegate declarations map.
            // TODO(erichang): See if we can standardize the way map keys are used in these data
            // structures, either always wrapped or unwrapped to be consistent and less errorprone.
            Key bindingKey = binding.key();
            if (compilerOptions.strictMultibindingValidation()
                    && binding.contributionType().equals(ContributionType.MAP)) {
                bindingKey = keyFactory.unwrapMapValueType(bindingKey);
            }

            return delegateDeclarations.get(bindingKey).stream()
                    .anyMatch(
                            declaration ->
                                    declaration.contributingModule().equals(binding.contributingModule())
                                            && declaration.bindingElement().equals(binding.bindingElement()));
        }

        /**
         * Returns the resolver lineage from parent to child.
         */
        private ImmutableList<Resolver> getResolverLineage() {
            ImmutableList.Builder<Resolver> resolverList = ImmutableList.builder();
            for (Optional<Resolver> currentResolver = Optional.of(this);
                 currentResolver.isPresent();
                 currentResolver = currentResolver.get().parentResolver) {
                resolverList.add(currentResolver.get());
            }
            return resolverList.build().reverse();
        }

        /**
         * Returns the explicit {@link ContributionBinding}s that match the {@code key} from this
         * resolver.
         */
        private ImmutableSet<ContributionBinding> getLocalExplicitBindings(Key key) {
            return new ImmutableSet.Builder<ContributionBinding>()
                    //1.explicitBindings匹配key
                    .addAll(explicitBindings.get(key))
                    // @Binds @IntoMap declarations have key Map<K, V>, unlike @Provides @IntoMap or @Produces
                    // @IntoMap, which have Map<K, Provider/Producer<V>> keys. So unwrap the key's type's
                    // value type if it's a Map<K, Provider/Producer<V>> before looking in
                    // delegateDeclarations. createDelegateBindings() will create bindings with the properly
                    // wrapped key type.
                    //2.(1)key表示type类型（如果是Map<K, Provider<V>>,Map<K, Producer<V>>，Map<K, Produced<V>>返回Map<K, V>）生成新的key,否则沿用之前的key对象；
                    // （2）在delegateDeclarations（component关联的module关联的使用@Binds修饰的bindingMethod方法生成的对象，key的type是bindingMethod方法返回类型）中匹配（1）中的key对象
                    //（3）（2）中收集的delegateDeclaration对象生成ContributionBinding绑定对象
                    .addAll(
                            createDelegateBindings(delegateDeclarations.get(keyFactory.unwrapMapValueType(key))))
                    .build();
        }

        /**
         * Returns the explicit multibinding contributions that contribute to the map or set requested
         * by {@code key} from this resolver.
         */
        private ImmutableSet<ContributionBinding> getLocalExplicitMultibindings(Key key) {
            ImmutableSet.Builder<ContributionBinding> multibindings = ImmutableSet.builder();

            //1.explicitMultibindings表示使用@Provides或@Produces修饰的bindingMethod也是使用了@IntoMap，@IntoSet，@ElementsIntoSet修饰的bindingMethod
            // 生成的ContributionBinding对象(当前对象的key属性的type属性：要么是Set<返回类型>集合，要么是Map<K,Provider<方法返回类型>>),匹配keysMatchingRequest
            multibindings.addAll(explicitMultibindings.get(key));

            //如果key的type不是Map类型 || Map(而不是Map<K,V>) || Map<K,V>，V是Produced、Producer、Provider、Lazy、MembersInjector
            if (!MapType.isMap(key)
                    || MapType.from(key).isRawType()
                    || MapType.from(key).valuesAreFrameworkType()) {
                // @Binds @IntoMap declarations have key Map<K, V>, unlike @Provides @IntoMap or @Produces
                // @IntoMap, which have Map<K, Provider/Producer<V>> keys. So unwrap the key's type's
                // value type if it's a Map<K, Provider/Producer<V>> before looking in
                // delegateMultibindingDeclarations. createDelegateBindings() will create bindings with the
                // properly wrapped key type.

                //2. (1)keysMatchingRequest，根据type类型筛选：(① 不是Map类型) || (Map(而不是Map<K,V>)) || (Map<K,V>，V是Produced、Producer、Provider、Lazy、MembersInjector)
                // (2)delegateMultibindingDeclarations表示使用@Binds && @IntoSet、@IntoMap、@ElementsIntoSet中任意一个注解 的方法生成的DelegateDeclaration对象(key的type属性：要么是Set<方法返回类型>，要么是Map<K,方法返回类型>)
                // (3)在（1）中筛选出的keysMatchingRequest（如果是Map<K, Provider<V>>,Map<K, Producer<V>>，Map<K, Produced<V>>返回Map<K, V>;否则还是使用type）生成新的key匹配（2）；
                //（4）在（3）中筛选出的delegateDeclarations生成ContributionBinding对象；
                multibindings.addAll(
                        createDelegateBindings(
                                delegateMultibindingDeclarations.get(keyFactory.unwrapMapValueType(key))));
            }
            return multibindings.build();
        }

        /**
         * Returns the {@link OptionalBindingDeclaration}s that match the {@code key} from this and all
         * ancestor resolvers.
         */
        private ImmutableSet<OptionalBindingDeclaration> getOptionalBindingDeclarations(Key key) {
            Optional<Key> unwrapped = keyFactory.unwrapOptional(key);
            if (!unwrapped.isPresent()) {
                return ImmutableSet.of();
            }
            ImmutableSet.Builder<OptionalBindingDeclaration> declarations = ImmutableSet.builder();
            for (Resolver resolver : getResolverLineage()) {
                declarations.addAll(resolver.optionalBindingDeclarations.get(unwrapped.get()));
            }
            return declarations.build();
        }

        /**
         * Returns the {@link ResolvedBindings} for {@code key} that was resolved in this resolver or an
         * ancestor resolver. Only checks for {@link ContributionBinding}s as {@link
         * MembersInjectionBinding}s are not inherited.
         * <p>
         * （1）如果当前的resolvedContributionBindings存在，获取key的ResolvedBindings；
         * （2）如果（1）不存在，那么在父级Resolver查找，获取key的ResolvedBindings；
         * （3）如果（1），（2）都不存在，那么返回空。
         */
        private Optional<ResolvedBindings> getPreviouslyResolvedBindings(Key key) {
            Optional<ResolvedBindings> result =
                    Optional.ofNullable(resolvedContributionBindings.get(key));
            if (result.isPresent()) {
                return result;
            } else if (parentResolver.isPresent()) {
                return parentResolver.get().getPreviouslyResolvedBindings(key);
            } else {
                return Optional.empty();
            }
        }

        private void resolveMembersInjection(Key key) {

            //方法参数生成注册到injectBindingRegistry，并且生成ResolvedBindings对象
            ResolvedBindings bindings = lookUpMembersInjectionBinding(key);

            //解析当前component有且仅有一个参数 && 方法返回类型是void或者返回类型和参数类型一致的方法参数生成的MembersInjectionBinding对象
            resolveDependencies(bindings);
            resolvedMembersInjectionBindings.put(key, bindings);
        }

        void resolve(Key key) {
            // If we find a cycle, stop resolving. The original request will add it with all of the
            // other resolved deps.
            if (cycleStack.contains(key)) {
                return;
            }

            // If the binding was previously resolved in this (sub)component, don't resolve it again.
            if (resolvedContributionBindings.containsKey(key)) {
                return;
            }

            /*
             * If the binding was previously resolved in an ancestor component, then we may be able to
             * avoid resolving it here and just depend on the ancestor component resolution.
             *
             * 1. If it depends transitively on multibinding contributions or optional bindings with
             *    bindings from this subcomponent, then we have to resolve it in this subcomponent so
             *    that it sees the local bindings.
             *
             *
             * 2. If there are any explicit bindings in this component, they may conflict with those in
             *    the ancestor component, so resolve them here so that conflicts can be caught.
             *
             *表示 1.resolvedContributionBindings（或者父级的resolvedContributionBindings）属性找到key下面的ResolvedBindings；
             * && 2. componentMethod方法没有使用qualifier修饰的注解修饰 &&该参数(不存在参数则使用返回类型)不能是componentAll节点也不能是creator节点
             */
            if (getPreviouslyResolvedBindings(key).isPresent() && !Keys.isComponentOrCreator(key)) {
                /* Resolve in the parent in case there are multibinding contributions or conflicts in some
                 * component between this one and the previously-resolved one. */

                //如果这两个条件都满足，调用 父类Resolver 递归当前resolve方法 解析当前key对象；
                parentResolver.get().resolve(key);

                //如果该key没有被处理过
                if (!new LocalDependencyChecker().dependsOnLocalBindings(key)
                        && getLocalExplicitBindings(key).isEmpty()) {
                    /* Cache the inherited parent component's bindings in case resolving at the parent found
                     * bindings in some component between this one and the previously-resolved one. */
                    resolvedContributionBindings.put(key, getPreviouslyResolvedBindings(key).get());
                    return;
                }
            }

            //表示正在处理
            cycleStack.push(key);
            try {
                //key生成一个ResolvedBindings对象
                ResolvedBindings bindings = lookUpBindings(key);
                resolvedContributionBindings.put(key, bindings);
                resolveDependencies(bindings);
            } finally {
                //表示结束处理
                cycleStack.pop();
            }
        }

        /**
         * {@link #resolve(Key) Resolves} each of the dependencies of the bindings owned by this
         * component.
         */
        private void resolveDependencies(ResolvedBindings resolvedBindings) {
            //遍历成员注入
            for (Binding binding : resolvedBindings.bindingsOwnedBy(componentDescriptor)) {
                //收集该类（遍历所有非Object父类）的所用使用Inject、非private、非static修饰的节点所生成的InjectionSite对象里面的所有依赖
                //（节点如果是变量则该依赖就是该变量，如果是方法，那么依赖表示的是方法的所有参数）
                for (DependencyRequest dependency : binding.dependencies()) {
                    resolve(dependency.key());
                }
            }
        }

        /**
         * Returns all of the {@link ResolvedBindings} for {@link ContributionBinding}s from this and
         * all ancestor resolvers, indexed by {@link ResolvedBindings#key()}.
         */
        Map<Key, ResolvedBindings> getResolvedContributionBindings() {
            Map<Key, ResolvedBindings> bindings = new LinkedHashMap<>();
            parentResolver.ifPresent(parent -> bindings.putAll(parent.getResolvedContributionBindings()));
            bindings.putAll(resolvedContributionBindings);
            return bindings;
        }

        /**
         * Returns all of the {@link ResolvedBindings} for {@link MembersInjectionBinding} from this
         * resolvers, indexed by {@link ResolvedBindings#key()}.
         */
        ImmutableMap<Key, ResolvedBindings> getResolvedMembersInjectionBindings() {
            return ImmutableMap.copyOf(resolvedMembersInjectionBindings);
        }

        private final class LocalDependencyChecker {
            private final Set<Object> cycleChecker = new HashSet<>();

            /**
             * Returns {@code true} if any of the bindings resolved for {@code key} are multibindings with
             * contributions declared within this component's modules or optional bindings with present
             * values declared within this component's modules, or if any of its unscoped dependencies
             * depend on such bindings.
             *
             * <p>We don't care about scoped dependencies because they will never depend on bindings from
             * subcomponents.
             *
             * @throws IllegalArgumentException if {@link #getPreviouslyResolvedBindings(Key)} is empty
             */
            private boolean dependsOnLocalBindings(Key key) {
                // Don't recur infinitely if there are valid cycles in the dependency graph.
                // http://b/23032377
                if (!cycleChecker.add(key)) {
                    return false;
                }
                return reentrantComputeIfAbsent(
                        keyDependsOnLocalBindingsCache, key, this::dependsOnLocalBindingsUncached);
            }

            /**
             * Returns {@code true} if {@code binding} is unscoped (or has {@link Reusable @Reusable}
             * scope) and depends on multibindings with contributions declared within this component's
             * modules, or if any of its unscoped or {@link Reusable @Reusable} scoped dependencies depend
             * on such local multibindings.
             *
             * <p>We don't care about non-reusable scoped dependencies because they will never depend on
             * multibindings with contributions from subcomponents.
             */
            private boolean dependsOnLocalBindings(Binding binding) {
                if (!cycleChecker.add(binding)) {
                    return false;
                }
                return reentrantComputeIfAbsent(
                        bindingDependsOnLocalBindingsCache, binding, this::dependsOnLocalBindingsUncached);
            }

            private boolean dependsOnLocalBindingsUncached(Key key) {
                checkArgument(
                        getPreviouslyResolvedBindings(key).isPresent(),
                        "no previously resolved bindings in %s for %s",
                        Resolver.this,
                        key);
                ResolvedBindings previouslyResolvedBindings = getPreviouslyResolvedBindings(key).get();
                if (hasLocalMultibindingContributions(key)
                        || hasLocalOptionalBindingContribution(previouslyResolvedBindings)) {
                    return true;
                }

                for (Binding binding : previouslyResolvedBindings.bindings()) {
                    if (dependsOnLocalBindings(binding)) {
                        return true;
                    }
                }
                return false;
            }

            private boolean dependsOnLocalBindingsUncached(Binding binding) {
                if ((!binding.scope().isPresent() || binding.scope().get().isReusable())
                        // TODO(beder): Figure out what happens with production subcomponents.
                        && !binding.bindingType().equals(BindingType.PRODUCTION)) {
                    for (DependencyRequest dependency : binding.dependencies()) {
                        if (dependsOnLocalBindings(dependency.key())) {
                            return true;
                        }
                    }
                }
                return false;
            }

            /**
             * Returns {@code true} if there is at least one multibinding contribution declared within
             * this component's modules that matches the key.
             */
            private boolean hasLocalMultibindingContributions(Key requestKey) {
                return keysMatchingRequest(requestKey)
                        .stream()
                        .anyMatch(key -> !getLocalExplicitMultibindings(key).isEmpty());
            }

            /**
             * Returns {@code true} if there is a contribution in this component for an {@code
             * Optional<Foo>} key that has not been contributed in a parent.
             */
            private boolean hasLocalOptionalBindingContribution(ResolvedBindings resolvedBindings) {
                if (resolvedBindings
                        .contributionBindings()
                        .stream()
                        .map(ContributionBinding::kind)
                        .anyMatch(isEqual(OPTIONAL))) {
                    return !getLocalExplicitBindings(keyFactory.unwrapOptional(resolvedBindings.key()).get())
                            .isEmpty();
                } else {
                    // If a parent contributes a @Provides Optional<Foo> binding and a child has a
                    // @BindsOptionalOf Foo method, the two should conflict, even if there is no binding for
                    // Foo on its own
                    return !getOptionalBindingDeclarations(resolvedBindings.key()).isEmpty();
                }
            }
        }
    }

    /**
     * A multimap of those {@code declarations} that are multibinding contribution declarations,
     * indexed by the key of the set or map to which they contribute.
     * <p>
     * 如果使用了@IntoMap或@IntoSet或@ElementsIntoSet注解中的任何一个
     */
    static <T extends BindingDeclaration>
    ImmutableSetMultimap<Key, T> multibindingContributionsByMultibindingKey(
            Iterable<T> declarations) {
        ImmutableSetMultimap.Builder<Key, T> builder = ImmutableSetMultimap.builder();
        for (T declaration : declarations) {
            if (declaration.key().multibindingContributionIdentifier().isPresent()) {
                builder.put(
                        declaration
                                .key()
                                .toBuilder()
                                .multibindingContributionIdentifier(Optional.empty())
                                .build(),
                        declaration);
            }
        }
        return builder.build();
    }
}
