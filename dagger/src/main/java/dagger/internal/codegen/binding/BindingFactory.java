package dagger.internal.codegen.binding;


import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;

import java.util.Optional;
import java.util.function.BiFunction;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import dagger.Module;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.base.ContributionType;
import dagger.internal.codegen.base.MapType;
import dagger.internal.codegen.base.SetType;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.producers.Produced;
import dagger.producers.Producer;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.Key;
import dagger.spi.model.RequestKind;

import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.auto.common.MoreTypes.asDeclared;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.base.MoreAnnotationMirrors.wrapOptionalInEquivalence;
import static dagger.internal.codegen.base.Scopes.uniqueScopeOf;
import static dagger.internal.codegen.binding.Binding.hasNonDefaultTypeParameters;
import static dagger.internal.codegen.binding.ComponentDescriptor.isComponentProductionMethod;
import static dagger.internal.codegen.binding.ConfigurationAnnotations.getNullableType;
import static dagger.internal.codegen.binding.ContributionBinding.bindingKindForMultibindingKey;
import static dagger.internal.codegen.binding.MapKeys.getMapKey;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.spi.model.BindingKind.ASSISTED_FACTORY;
import static dagger.spi.model.BindingKind.ASSISTED_INJECTION;
import static dagger.spi.model.BindingKind.BOUND_INSTANCE;
import static dagger.spi.model.BindingKind.COMPONENT;
import static dagger.spi.model.BindingKind.COMPONENT_DEPENDENCY;
import static dagger.spi.model.BindingKind.COMPONENT_PRODUCTION;
import static dagger.spi.model.BindingKind.COMPONENT_PROVISION;
import static dagger.spi.model.BindingKind.DELEGATE;
import static dagger.spi.model.BindingKind.INJECTION;
import static dagger.spi.model.BindingKind.MEMBERS_INJECTOR;
import static dagger.spi.model.BindingKind.OPTIONAL;
import static dagger.spi.model.BindingKind.PRODUCTION;
import static dagger.spi.model.BindingKind.PROVISION;
import static dagger.spi.model.BindingKind.SUBCOMPONENT_CREATOR;
import static dagger.spi.model.DaggerType.fromJava;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.ElementKind.METHOD;

/**
 * A factory for {@link Binding} objects.
 */
public final class BindingFactory {
    private final DaggerTypes types;
    private final KeyFactory keyFactory;
    private final DependencyRequestFactory dependencyRequestFactory;
    private final InjectionSiteFactory injectionSiteFactory;
    private final DaggerElements elements;
    private final InjectionAnnotations injectionAnnotations;
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    BindingFactory(
            DaggerTypes types,
            DaggerElements elements,
            KeyFactory keyFactory,
            DependencyRequestFactory dependencyRequestFactory,
            InjectionSiteFactory injectionSiteFactory,
            InjectionAnnotations injectionAnnotations,
            KotlinMetadataUtil metadataUtil) {
        this.types = types;
        this.elements = elements;
        this.keyFactory = keyFactory;
        this.dependencyRequestFactory = dependencyRequestFactory;
        this.injectionSiteFactory = injectionSiteFactory;
        this.injectionAnnotations = injectionAnnotations;
        this.metadataUtil = metadataUtil;
    }

    /**
     * Returns an {@link dagger.spi.model.BindingKind#INJECTION} binding.
     *
     * @param constructorElement the {@code @Inject}-annotated constructor
     * @param resolvedType       the parameterized type if the constructor is for a generic class and the
     *                           binding should be for the parameterized type
     */
    // TODO(dpb): See if we can just pass the parameterized type and not also the constructor.
    public ProvisionBinding injectionBinding(
            ExecutableElement constructorElement,
            Optional<TypeMirror> resolvedType
    ) {
        checkArgument(constructorElement.getKind().equals(CONSTRUCTOR));
        checkArgument(
                isAnnotationPresent(constructorElement, Inject.class)
                        || isAnnotationPresent(constructorElement, AssistedInject.class));

        //该构造函数不能使用@Qualifier修饰的注解修饰
        checkArgument(!injectionAnnotations.getQualifier(constructorElement).isPresent());

        ExecutableType constructorType = MoreTypes.asExecutable(constructorElement.asType());
        DeclaredType constructedType =
                MoreTypes.asDeclared(constructorElement.getEnclosingElement().asType());

        // If the class this is constructing has some type arguments, resolve everything.
        //1.如果当前构造函数所在类使用了泛型，并且resolvedType存在;构造类型和所在类都关联resolved
        if (!constructedType.getTypeArguments().isEmpty() && resolvedType.isPresent()) {
            DeclaredType resolved = MoreTypes.asDeclared(resolvedType.get());

            //泛型进过类型擦除后，resolved和constructedType是同一个类型
            // Validate that we're resolving from the correct type.
            checkState(
                    types.isSameType(types.erasure(resolved), types.erasure(constructedType)),
                    "erased expected type: %s, erased actual type: %s",
                    types.erasure(resolved),
                    types.erasure(constructedType));

            constructorType = MoreTypes.asExecutable(types.asMemberOf(resolved, constructorElement));
            constructedType = resolved;
        }

        //2.收集依赖：构造函数的参数生成依赖对象（没有使用@Assisted注解的参数）
        // Collect all dependency requests within the provision method.
        // Note: we filter out @Assisted parameters since these aren't considered dependency requests.
        ImmutableSet.Builder<DependencyRequest> provisionDependencies = ImmutableSet.builder();
        for (int i = 0; i < constructorElement.getParameters().size(); i++) {
            VariableElement parameter = constructorElement.getParameters().get(i);
            TypeMirror parameterType = constructorType.getParameterTypes().get(i);

            //该参数没有使用@Assisted注解
            if (!AssistedInjectionAnnotations.isAssistedParameter(parameter)) {
                provisionDependencies.add(
                        dependencyRequestFactory.forRequiredResolvedVariable(parameter, parameterType));
            }
        }

        Key key = keyFactory.forInjectConstructorWithResolvedType(constructedType);

        ProvisionBinding.Builder builder =
                ProvisionBinding.builder()
                        .contributionType(ContributionType.UNIQUE)
                        .bindingElement(constructorElement)
                        .key(key)
                        .provisionDependencies(provisionDependencies.build())
                        //当前使用Inject或AssistedInject修饰的构造函数所在父类（包括遍历父类的非Object超类），里面的所有使用Inject注解修饰并且非private非static修饰的节点生成的InjectionSite集合
                        //1.如果是变量则使用变量类型的InjectionSite对象
                        //2.如果是方法，①排除覆写的方法；②有方法和方法参数（依赖）生成一个方法类型的InjectionSite对象
                        .injectionSites(injectionSiteFactory.getInjectionSites(constructedType))
                        .kind(
                                isAnnotationPresent(constructorElement, AssistedInject.class)
                                        ? ASSISTED_INJECTION
                                        : INJECTION)
                        .scope(uniqueScopeOf(constructorElement.getEnclosingElement()));

        //使用了泛型，并且类型和类型不匹配，例如类型是List<T>,但是节点使用的是List
        TypeElement bindingTypeElement = MoreElements.asType(constructorElement.getEnclosingElement());
        if (hasNonDefaultTypeParameters(bindingTypeElement, key.type().java(), types)) {
            builder.unresolved(injectionBinding(constructorElement, Optional.empty()));
        }
        return builder.build();
    }

    //使用AssistedFactory注解修饰的节点
    public ProvisionBinding assistedFactoryBinding(
            TypeElement factory,
            Optional<TypeMirror> resolvedType
    ) {
        //如果factory使用了泛型 && resolvedType存在 ，那么类型擦除后和resolvedType一定是同一个类型
        // If the class this is constructing has some type arguments, resolve everything.
        DeclaredType factoryType = MoreTypes.asDeclared(factory.asType());
        if (!factoryType.getTypeArguments().isEmpty() && resolvedType.isPresent()) {
            DeclaredType resolved = MoreTypes.asDeclared(resolvedType.get());
            // Validate that we're resolving from the correct type by checking that the erasure of the
            // resolvedType is the same as the erasure of the factoryType.
            checkState(
                    types.isSameType(types.erasure(resolved), types.erasure(factoryType)),
                    "erased expected type: %s, erased actual type: %s",
                    types.erasure(resolved),
                    types.erasure(factoryType));
            factoryType = resolved;
        }

        //返回该类上有且仅有的一个abstract、非static、非private 方法
        ExecutableElement factoryMethod =
                AssistedInjectionAnnotations.assistedFactoryMethod(factory, elements);

        ExecutableType factoryMethodType =
                MoreTypes.asExecutable(types.asMemberOf(factoryType, factoryMethod));

        return ProvisionBinding.builder()
                .contributionType(ContributionType.UNIQUE)
                .key(Key.builder(fromJava(factoryType)).build())
                .bindingElement(factory)
                //把方法的返回类型作为依赖，依赖kind = PROVIDER
                .provisionDependencies(
                        ImmutableSet.of(
                                DependencyRequest.builder()
                                        .key(Key.builder(fromJava(factoryMethodType.getReturnType())).build())
                                        .kind(RequestKind.PROVIDER)
                                        .build()))
                //绑定kind ASSISTED_FACTORY
                .kind(ASSISTED_FACTORY)
                .build();
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#PROVISION} binding for a
     * {@code @Provides}-annotated method.
     *
     * @param contributedBy the installed module that declares or inherits the method
     */
    public ProvisionBinding providesMethodBinding(
            ExecutableElement providesMethod, //Provides修饰的方法
            TypeElement contributedBy//方法所在的module类
    ) {
        //方法如果返回类型是Map，会根据是否存在架构类型，加入框架包裹；参数如果原先就使用了架构类型，则去掉架构类型外层的包裹生成key作为依赖的属性
        return setMethodBindingProperties(
                ProvisionBinding.builder(),
                providesMethod,
                contributedBy,
                //方法的（Provides，IntoSet，IntoMap，ElementsIntoSet）注解决定方法的返回类型，
                //IntoSet，IntoMap，ElementsIntoSet如果使用，又有multibindingContributionIdentifier属性
                //方法上是否使用了Qualifier注解修饰的注解修饰，key还需要携带该信息
                keyFactory.forProvidesMethod(providesMethod, contributedBy),
                this::providesMethodBinding)
                .kind(PROVISION) //PROVISION类型
                .scope(uniqueScopeOf(providesMethod))//方法上使用了Scope注解修饰的注解修饰，转换成Optional<Scope>
                .nullableType(getNullableType(providesMethod))//是否使用了Nullable注解
                .build();
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#PRODUCTION} binding for a
     * {@code @Produces}-annotated method.
     *
     * @param contributedBy the installed module that declares or inherits the method
     */
    public ProductionBinding producesMethodBinding(
            ExecutableElement producesMethod, TypeElement contributedBy) {
        // TODO(beder): Add nullability checking with Java 8.
        ProductionBinding.Builder builder =
                setMethodBindingProperties(
                        ProductionBinding.builder(),
                        producesMethod,
                        contributedBy,
                        keyFactory.forProducesMethod(producesMethod, contributedBy),
                        this::producesMethodBinding)
                        .kind(PRODUCTION)
                        .productionKind(ProductionBinding.ProductionKind.fromProducesMethod(producesMethod))//根据返回类型和方法上是否使用@ElementsIntoSet修饰决定该productionKind类型
                        .thrownTypes(producesMethod.getThrownTypes())
                        .executorRequest(dependencyRequestFactory.forProductionImplementationExecutor())
                        .monitorRequest(dependencyRequestFactory.forProductionComponentMonitor());
        return builder.build();
    }

    private <C extends ContributionBinding, B extends ContributionBinding.Builder<C, B>>
    B setMethodBindingProperties(
            B builder,
            ExecutableElement method,
            TypeElement contributedBy,
            Key key,
            BiFunction<ExecutableElement, TypeElement, C> create) {

        checkArgument(method.getKind().equals(METHOD));

        ExecutableType methodType =
                MoreTypes.asExecutable(
                        types.asMemberOf(MoreTypes.asDeclared(contributedBy.asType()), method));

        //如果当前绑定存在类似于节点使用List，但是其节点类型是List<T>的情况，那么会出现未解析绑定
        if (!types.isSameType(methodType, method.asType())) {
            builder.unresolved(create.apply(method, MoreElements.asType(method.getEnclosingElement())));
        }

        boolean isKotlinObject =
                metadataUtil.isObjectClass(contributedBy)
                        || metadataUtil.isCompanionObjectClass(contributedBy);

        return builder
                .contributionType(ContributionType.fromBindingElement(method))//ContributionType类型
                .bindingElement(method)//当前绑定的方法
                .contributingModule(contributedBy)//方法所在module类
                .isContributingModuleKotlinObject(isKotlinObject)//是否是KotlinObject对象
                .key(key)//被处理的返回类型生成的Key
                //方法参数生成依赖集合，
                // ①依赖参数使用的架构类型（例如Provider）；
                // ②依赖参数的Key根据方法qualifier决定该key的qualifier，type表示去掉架构类型(例如Provider<T>使用T)；
                // ③使用了架构类型或者参数节点上游Nullable注解，表示isnullable = true
                .dependencies(
                        dependencyRequestFactory.forRequiredResolvedVariables(
                                method.getParameters(), methodType.getParameterTypes())
                )
                //MapKey注解修饰的注解
                .wrappedMapKeyAnnotation(wrapOptionalInEquivalence(getMapKey(method)));
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#COMPONENT} binding for the component.
     * <p>
     * component类生成一个ProvisionBinding绑定对象
     */
    public ProvisionBinding componentBinding(TypeElement componentDefinitionType) {
        checkNotNull(componentDefinitionType);
        return ProvisionBinding.builder()
                .contributionType(ContributionType.UNIQUE)
                .bindingElement(componentDefinitionType)
                .key(keyFactory.forType(componentDefinitionType.asType()))//component类作为type生成key对象
                .kind(COMPONENT)
                .build();
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#COMPONENT_DEPENDENCY} binding for a component's
     * dependency.
     */
    public ProvisionBinding componentDependencyBinding(ComponentRequirement dependency) {
        checkNotNull(dependency);
        return ProvisionBinding.builder()
                .contributionType(ContributionType.UNIQUE)
                .bindingElement(dependency.typeElement())
                .key(keyFactory.forType(dependency.type()))//将componentAnnotation#dependencies的dependency类型作为type生成key
                .kind(COMPONENT_DEPENDENCY)
                .build();
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#COMPONENT_PROVISION} or {@link
     * dagger.spi.model.BindingKind#COMPONENT_PRODUCTION} binding for a method on a component's
     * dependency.
     * <p>
     * 对component#dependencies里面的类中的无参，返回类型不是void的方法生成一个CongtributionBinding绑定对象
     *
     * @param componentDescriptor the component with the dependency, not the dependency that has the
     *                            method
     */
    public ContributionBinding componentDependencyMethodBinding(
            ComponentDescriptor componentDescriptor,
            ExecutableElement dependencyMethod
    ) {
        checkArgument(dependencyMethod.getKind().equals(METHOD));//方法
        checkArgument(dependencyMethod.getParameters().isEmpty());//无参

        ContributionBinding.Builder<?, ?> builder;
        if (componentDescriptor.isProduction()
                && isComponentProductionMethod(elements, dependencyMethod)) {
            builder =
                    ProductionBinding.builder()
                            .key(keyFactory.forProductionComponentMethod(dependencyMethod))//方法的返回类型作为key的type
                            .kind(COMPONENT_PRODUCTION)
                            .thrownTypes(dependencyMethod.getThrownTypes());
        } else {
            builder =
                    ProvisionBinding.builder()
                            .key(keyFactory.forComponentMethod(dependencyMethod))//方法的返回类型作为key的type
                            .nullableType(getNullableType(dependencyMethod))
                            .kind(COMPONENT_PROVISION)
                            .scope(uniqueScopeOf(dependencyMethod));
        }

        return builder
                .contributionType(ContributionType.UNIQUE)
                .bindingElement(dependencyMethod)//绑定节点是该方法节点
                .build();
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#BOUND_INSTANCE} binding for a
     * {@code @BindsInstance}-annotated builder setter method or factory method parameter.
     */
    ProvisionBinding boundInstanceBinding(ComponentRequirement requirement, Element element) {

        checkArgument(element instanceof VariableElement || element instanceof ExecutableElement);

        VariableElement parameterElement =
                element instanceof VariableElement
                        ? MoreElements.asVariable(element)
                        : getOnlyElement(MoreElements.asExecutable(element).getParameters());

        return ProvisionBinding.builder()
                .contributionType(ContributionType.UNIQUE)
                .bindingElement(element)
                .key(requirement.key().get())//方法上的参数类型（根据RequestKind剥离外壳）作为type生成的key
                .nullableType(getNullableType(parameterElement))
                .kind(BOUND_INSTANCE)
                .build();
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#SUBCOMPONENT_CREATOR} binding declared by a
     * component method that returns a subcomponent builder. Use {{@link
     * #subcomponentCreatorBinding(ImmutableSet)}} for bindings declared using {@link
     * Module#subcomponents()}.
     *
     * @param component the component that declares or inherits the method
     */
    ProvisionBinding subcomponentCreatorBinding(
            ExecutableElement subcomponentCreatorMethod, TypeElement component) {
        checkArgument(subcomponentCreatorMethod.getKind().equals(METHOD));
        checkArgument(subcomponentCreatorMethod.getParameters().isEmpty());

        Key key =
                keyFactory.forSubcomponentCreatorMethod(
                        subcomponentCreatorMethod, asDeclared(component.asType()));//当前方法的返回类型作为type生成key

        return ProvisionBinding.builder()
                .contributionType(ContributionType.UNIQUE)
                .bindingElement(subcomponentCreatorMethod)
                .key(key)
                .kind(SUBCOMPONENT_CREATOR)
                .build();
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#DELEGATE} binding used when there is no binding
     * that satisfies the {@code @Binds} declaration.
     */
    public ContributionBinding unresolvedDelegateBinding(DelegateDeclaration delegateDeclaration) {
        return buildDelegateBinding(
                ProvisionBinding.builder().scope(uniqueScopeOf(delegateDeclaration.bindingElement().get())),
                delegateDeclaration,
                Provider.class);
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#DELEGATE} binding.
     *
     * @param delegateDeclaration the {@code @Binds}-annotated declaration
     * @param actualBinding       the binding that satisfies the {@code @Binds} declaration
     */
    ContributionBinding delegateBinding(
            DelegateDeclaration delegateDeclaration, ContributionBinding actualBinding) {
        switch (actualBinding.bindingType()) {
            case PRODUCTION:
                return buildDelegateBinding(
                        ProductionBinding.builder().nullableType(actualBinding.nullableType()),
                        delegateDeclaration,
                        Producer.class);

            case PROVISION:
                return buildDelegateBinding(
                        ProvisionBinding.builder()
                                .scope(uniqueScopeOf(delegateDeclaration.bindingElement().get()))
                                .nullableType(actualBinding.nullableType()),
                        delegateDeclaration,
                        Provider.class);

            case MEMBERS_INJECTION: // fall-through to throw
        }
        throw new AssertionError("bindingType: " + actualBinding);
    }

    private ContributionBinding buildDelegateBinding(
            ContributionBinding.Builder<?, ?> builder,
            DelegateDeclaration delegateDeclaration,
            Class<?> frameworkType) {
        boolean isKotlinObject =
                metadataUtil.isObjectClass(delegateDeclaration.contributingModule().get())
                        || metadataUtil.isCompanionObjectClass(delegateDeclaration.contributingModule().get());
        return builder
                .contributionType(delegateDeclaration.contributionType())
                .bindingElement(delegateDeclaration.bindingElement().get())
                .contributingModule(delegateDeclaration.contributingModule().get())
                .isContributingModuleKotlinObject(isKotlinObject)
                .key(keyFactory.forDelegateBinding(delegateDeclaration, frameworkType))
                .dependencies(delegateDeclaration.delegateRequest())
                .wrappedMapKeyAnnotation(delegateDeclaration.wrappedMapKey())
                .kind(DELEGATE)
                .build();
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#SUBCOMPONENT_CREATOR} binding declared using
     * {@link Module#subcomponents()}.
     */
    ProvisionBinding subcomponentCreatorBinding(
            ImmutableSet<SubcomponentDeclaration> subcomponentDeclarations) {
        SubcomponentDeclaration subcomponentDeclaration = subcomponentDeclarations.iterator().next();
        return ProvisionBinding.builder()
                .contributionType(ContributionType.UNIQUE)
                .key(subcomponentDeclaration.key())
                .kind(SUBCOMPONENT_CREATOR)
                .build();
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#MULTIBOUND_MAP} or {@link
     * dagger.spi.model.BindingKind#MULTIBOUND_SET} binding given a set of multibinding contribution
     * bindings.
     *
     * @param key a key that may be satisfied by a multibinding
     */
    public ContributionBinding syntheticMultibinding(
            Key key, Iterable<ContributionBinding> multibindingContributions) {

        ContributionBinding.Builder<?, ?> builder =
                multibindingRequiresProduction(key, multibindingContributions)
                        ? ProductionBinding.builder()
                        : ProvisionBinding.builder();
        return builder
                .contributionType(ContributionType.UNIQUE)
                .key(key)
                .dependencies(
                        dependencyRequestFactory.forMultibindingContributions(key, multibindingContributions))
                .kind(bindingKindForMultibindingKey(key))
                .build();
    }

    private boolean multibindingRequiresProduction(
            Key key, Iterable<ContributionBinding> multibindingContributions) {
        //1.如果是Map<K,V>,那么V是Producer或Produced
        if (MapType.isMap(key)) {
            MapType mapType = MapType.from(key);
            if (mapType.valuesAreTypeOf(Producer.class) || mapType.valuesAreTypeOf(Produced.class)) {
                return true;
            }
        }
        //2.如果是Set<T>,那么T是Produced类型
        else if (SetType.isSet(key) && SetType.from(key).elementsAreTypeOf(Produced.class)) {
            return true;
        }
        //3.又或者绑定类型是PRODUCTION
        return Iterables.any(
                multibindingContributions, binding -> binding.bindingType().equals(BindingType.PRODUCTION));
    }

    /**
     * Returns an {@link dagger.spi.model.BindingKind#OPTIONAL} binding for {@code key}.
     *
     * @param requestKind           the kind of request for the optional binding
     * @param underlyingKeyBindings the possibly empty set of bindings that exist in the component for
     *                              the underlying (non-optional) key
     */
    ContributionBinding syntheticOptionalBinding(
            Key key,
            RequestKind requestKind,
            ImmutableCollection<? extends Binding> underlyingKeyBindings) {
        if (underlyingKeyBindings.isEmpty()) {
            return ProvisionBinding.builder()
                    .contributionType(ContributionType.UNIQUE)
                    .key(key)
                    .kind(OPTIONAL)
                    .build();
        }

        boolean requiresProduction =
                underlyingKeyBindings.stream()
                        .anyMatch(binding -> binding.bindingType() == BindingType.PRODUCTION)
                        || requestKind.equals(RequestKind.PRODUCER) // handles producerFromProvider cases
                        || requestKind.equals(RequestKind.PRODUCED); // handles producerFromProvider cases

        return (requiresProduction ? ProductionBinding.builder() : ProvisionBinding.builder())
                .contributionType(ContributionType.UNIQUE)
                .key(key)
                .kind(OPTIONAL)
                .dependencies(dependencyRequestFactory.forSyntheticPresentOptionalBinding(key, requestKind))
                .build();
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#MEMBERS_INJECTOR} binding.
     */
    public ProvisionBinding membersInjectorBinding(
            Key key, MembersInjectionBinding membersInjectionBinding) {
        return ProvisionBinding.builder()
                .key(key)
                .contributionType(ContributionType.UNIQUE)
                .kind(MEMBERS_INJECTOR)
                .bindingElement(MoreTypes.asTypeElement(membersInjectionBinding.key().type().java()))
                .provisionDependencies(membersInjectionBinding.dependencies())
                .injectionSites(membersInjectionBinding.injectionSites())
                .build();
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#MEMBERS_INJECTION} binding.
     *
     * @param resolvedType if {@code declaredType} is a generic class and {@code resolvedType} is a
     *                     parameterization of that type, the returned binding will be for the resolved type
     */
    // TODO(dpb): See if we can just pass one nongeneric/parameterized type.
    public MembersInjectionBinding membersInjectionBinding(
            DeclaredType declaredType,
            Optional<TypeMirror> resolvedType
    ) {

        //如果类有泛型，并且resolvedType存在，该类类型擦除后和resolvedType相同，那么使用resolvedType表示
        // If the class this is injecting has some type arguments, resolve everything.
        if (!declaredType.getTypeArguments().isEmpty() && resolvedType.isPresent()) {
            DeclaredType resolved = asDeclared(resolvedType.get());
            // Validate that we're resolving from the correct type.
            checkState(
                    types.isSameType(types.erasure(resolved), types.erasure(declaredType)),
                    "erased expected type: %s, erased actual type: %s",
                    types.erasure(resolved),
                    types.erasure(declaredType));
            declaredType = resolved;
        }

        ImmutableSortedSet<MembersInjectionBinding.InjectionSite> injectionSites =
                injectionSiteFactory.getInjectionSites(declaredType);

        //收集该类（遍历所有非Object父类）的所用使用Inject、非private、非static修饰的节点所生成的InjectionSite对象里面的所有依赖
        //（节点如果是变量则该依赖就是该变量，如果是方法，那么依赖表示的是方法的所有参数）
        ImmutableSet<DependencyRequest> dependencies =
                injectionSites.stream()
                        .flatMap(injectionSite -> injectionSite.dependencies().stream())
                        .collect(toImmutableSet());

        //针对当前类生成
        Key key = keyFactory.forMembersInjectedType(declaredType);

        TypeElement typeElement = MoreElements.asType(declaredType.asElement());

        return new AutoValue_MembersInjectionBinding(
                key,//针对当前类生成
                dependencies,//当前类关联的InjectionSite集合里面所有的依赖
                typeElement,//当前类节点
                //如果类型和节点类型不一致，即类型是List<T>,但是节点是List，那么返回true
                hasNonDefaultTypeParameters(typeElement, key.type().java(), types)
                        ? Optional.of(
                        membersInjectionBinding(asDeclared(typeElement.asType()), Optional.empty()))
                        : Optional.empty(),
                injectionSites//当前类关联的InjectionSite集合
        );
    }
}
