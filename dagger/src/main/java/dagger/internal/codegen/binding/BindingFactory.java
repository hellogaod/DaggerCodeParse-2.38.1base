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

        //???????????????????????????@Qualifier?????????????????????
        checkArgument(!injectionAnnotations.getQualifier(constructorElement).isPresent());

        ExecutableType constructorType = MoreTypes.asExecutable(constructorElement.asType());
        DeclaredType constructedType =
                MoreTypes.asDeclared(constructorElement.getEnclosingElement().asType());

        // If the class this is constructing has some type arguments, resolve everything.
        //1.?????????????????????????????????????????????????????????resolvedType??????;?????????????????????????????????resolved
        if (!constructedType.getTypeArguments().isEmpty() && resolvedType.isPresent()) {
            DeclaredType resolved = MoreTypes.asDeclared(resolvedType.get());

            //??????????????????????????????resolved???constructedType??????????????????
            // Validate that we're resolving from the correct type.
            checkState(
                    types.isSameType(types.erasure(resolved), types.erasure(constructedType)),
                    "erased expected type: %s, erased actual type: %s",
                    types.erasure(resolved),
                    types.erasure(constructedType));

            constructorType = MoreTypes.asExecutable(types.asMemberOf(resolved, constructorElement));
            constructedType = resolved;
        }

        //2.?????????????????????????????????????????????????????????????????????@Assisted??????????????????
        // Collect all dependency requests within the provision method.
        // Note: we filter out @Assisted parameters since these aren't considered dependency requests.
        ImmutableSet.Builder<DependencyRequest> provisionDependencies = ImmutableSet.builder();
        for (int i = 0; i < constructorElement.getParameters().size(); i++) {
            VariableElement parameter = constructorElement.getParameters().get(i);
            TypeMirror parameterType = constructorType.getParameterTypes().get(i);

            //?????????????????????@Assisted??????
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
                        //????????????Inject???AssistedInject????????????????????????????????????????????????????????????Object?????????????????????????????????Inject?????????????????????private???static????????????????????????InjectionSite??????
                        //1.???????????????????????????????????????InjectionSite??????
                        //2.???????????????????????????????????????????????????????????????????????????????????????????????????????????????InjectionSite??????
                        .injectionSites(injectionSiteFactory.getInjectionSites(constructedType))
                        .kind(
                                isAnnotationPresent(constructorElement, AssistedInject.class)
                                        ? ASSISTED_INJECTION
                                        : INJECTION)
                        .scope(uniqueScopeOf(constructorElement.getEnclosingElement()));

        //??????????????????????????????????????????????????????????????????List<T>,????????????????????????List
        TypeElement bindingTypeElement = MoreElements.asType(constructorElement.getEnclosingElement());
        if (hasNonDefaultTypeParameters(bindingTypeElement, key.type().java(), types)) {
            builder.unresolved(injectionBinding(constructorElement, Optional.empty()));
        }
        return builder.build();
    }

    //??????AssistedFactory?????????????????????
    public ProvisionBinding assistedFactoryBinding(
            TypeElement factory,
            Optional<TypeMirror> resolvedType
    ) {
        //??????factory??????????????? && resolvedType?????? ???????????????????????????resolvedType????????????????????????
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

        //????????????????????????????????????abstract??????static??????private ??????
        ExecutableElement factoryMethod =
                AssistedInjectionAnnotations.assistedFactoryMethod(factory, elements);

        ExecutableType factoryMethodType =
                MoreTypes.asExecutable(types.asMemberOf(factoryType, factoryMethod));

        return ProvisionBinding.builder()
                .contributionType(ContributionType.UNIQUE)
                .key(Key.builder(fromJava(factoryType)).build())
                .bindingElement(factory)
                //?????????????????????????????????????????????kind = PROVIDER
                .provisionDependencies(
                        ImmutableSet.of(
                                DependencyRequest.builder()
                                        .key(Key.builder(fromJava(factoryMethodType.getReturnType())).build())
                                        .kind(RequestKind.PROVIDER)
                                        .build()))
                //??????kind ASSISTED_FACTORY
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
            ExecutableElement providesMethod, //Provides???????????????
            TypeElement contributedBy//???????????????module???
    ) {
        //???????????????????????????Map???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????key?????????????????????
        return setMethodBindingProperties(
                ProvisionBinding.builder(),
                providesMethod,
                contributedBy,
                //????????????Provides???IntoSet???IntoMap???ElementsIntoSet???????????????????????????????????????
                //IntoSet???IntoMap???ElementsIntoSet?????????????????????multibindingContributionIdentifier??????
                //????????????????????????Qualifier??????????????????????????????key????????????????????????
                keyFactory.forProvidesMethod(providesMethod, contributedBy),
                this::providesMethodBinding)
                .kind(PROVISION) //PROVISION??????
                .scope(uniqueScopeOf(providesMethod))//??????????????????Scope???????????????????????????????????????Optional<Scope>
                .nullableType(getNullableType(providesMethod))//???????????????Nullable??????
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
                        .productionKind(ProductionBinding.ProductionKind.fromProducesMethod(producesMethod))//??????????????????????????????????????????@ElementsIntoSet???????????????productionKind??????
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

        //?????????????????????????????????????????????List???????????????????????????List<T>??????????????????????????????????????????
        if (!types.isSameType(methodType, method.asType())) {
            builder.unresolved(create.apply(method, MoreElements.asType(method.getEnclosingElement())));
        }

        boolean isKotlinObject =
                metadataUtil.isObjectClass(contributedBy)
                        || metadataUtil.isCompanionObjectClass(contributedBy);

        return builder
                .contributionType(ContributionType.fromBindingElement(method))//ContributionType??????
                .bindingElement(method)//?????????????????????
                .contributingModule(contributedBy)//????????????module???
                .isContributingModuleKotlinObject(isKotlinObject)//?????????KotlinObject??????
                .key(key)//?????????????????????????????????Key
                //?????????????????????????????????
                // ?????????????????????????????????????????????Provider??????
                // ??????????????????Key????????????qualifier?????????key???qualifier???type????????????????????????(??????Provider<T>??????T)???
                // ????????????????????????????????????????????????Nullable???????????????isnullable = true
                .dependencies(
                        dependencyRequestFactory.forRequiredResolvedVariables(
                                method.getParameters(), methodType.getParameterTypes())
                )
                //MapKey?????????????????????
                .wrappedMapKeyAnnotation(wrapOptionalInEquivalence(getMapKey(method)));
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#COMPONENT} binding for the component.
     * <p>
     * component???????????????ProvisionBinding????????????
     */
    public ProvisionBinding componentBinding(TypeElement componentDefinitionType) {
        checkNotNull(componentDefinitionType);
        return ProvisionBinding.builder()
                .contributionType(ContributionType.UNIQUE)
                .bindingElement(componentDefinitionType)
                .key(keyFactory.forType(componentDefinitionType.asType()))//component?????????type??????key??????
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
                .key(keyFactory.forType(dependency.type()))//???componentAnnotation#dependencies???dependency????????????type??????key
                .kind(COMPONENT_DEPENDENCY)
                .build();
    }

    /**
     * Returns a {@link dagger.spi.model.BindingKind#COMPONENT_PROVISION} or {@link
     * dagger.spi.model.BindingKind#COMPONENT_PRODUCTION} binding for a method on a component's
     * dependency.
     * <p>
     * ???component#dependencies?????????????????????????????????????????????void?????????????????????CongtributionBinding????????????
     *
     * @param componentDescriptor the component with the dependency, not the dependency that has the
     *                            method
     */
    public ContributionBinding componentDependencyMethodBinding(
            ComponentDescriptor componentDescriptor,
            ExecutableElement dependencyMethod
    ) {
        checkArgument(dependencyMethod.getKind().equals(METHOD));//??????
        checkArgument(dependencyMethod.getParameters().isEmpty());//??????

        ContributionBinding.Builder<?, ?> builder;
        if (componentDescriptor.isProduction()
                && isComponentProductionMethod(elements, dependencyMethod)) {
            builder =
                    ProductionBinding.builder()
                            .key(keyFactory.forProductionComponentMethod(dependencyMethod))//???????????????????????????key???type
                            .kind(COMPONENT_PRODUCTION)
                            .thrownTypes(dependencyMethod.getThrownTypes());
        } else {
            builder =
                    ProvisionBinding.builder()
                            .key(keyFactory.forComponentMethod(dependencyMethod))//???????????????????????????key???type
                            .nullableType(getNullableType(dependencyMethod))
                            .kind(COMPONENT_PROVISION)
                            .scope(uniqueScopeOf(dependencyMethod));
        }

        return builder
                .contributionType(ContributionType.UNIQUE)
                .bindingElement(dependencyMethod)//??????????????????????????????
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
                .key(requirement.key().get())//?????????????????????????????????RequestKind?????????????????????type?????????key
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
                        subcomponentCreatorMethod, asDeclared(component.asType()));//?????????????????????????????????type??????key

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
        //1.?????????Map<K,V>,??????V???Producer???Produced
        if (MapType.isMap(key)) {
            MapType mapType = MapType.from(key);
            if (mapType.valuesAreTypeOf(Producer.class) || mapType.valuesAreTypeOf(Produced.class)) {
                return true;
            }
        }
        //2.?????????Set<T>,??????T???Produced??????
        else if (SetType.isSet(key) && SetType.from(key).elementsAreTypeOf(Produced.class)) {
            return true;
        }
        //3.????????????????????????PRODUCTION
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

        //???????????????????????????resolvedType?????????????????????????????????resolvedType?????????????????????resolvedType??????
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

        //??????????????????????????????Object????????????????????????Inject??????private??????static???????????????????????????InjectionSite???????????????????????????
        //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        ImmutableSet<DependencyRequest> dependencies =
                injectionSites.stream()
                        .flatMap(injectionSite -> injectionSite.dependencies().stream())
                        .collect(toImmutableSet());

        //?????????????????????
        Key key = keyFactory.forMembersInjectedType(declaredType);

        TypeElement typeElement = MoreElements.asType(declaredType.asElement());

        return new AutoValue_MembersInjectionBinding(
                key,//?????????????????????
                dependencies,//??????????????????InjectionSite???????????????????????????
                typeElement,//???????????????
                //???????????????????????????????????????????????????List<T>,???????????????List???????????????true
                hasNonDefaultTypeParameters(typeElement, key.type().java(), types)
                        ? Optional.of(
                        membersInjectionBinding(asDeclared(typeElement.asType()), Optional.empty()))
                        : Optional.empty(),
                injectionSites//??????????????????InjectionSite??????
        );
    }
}
