package dagger.internal.codegen.binding;


import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import dagger.Lazy;
import dagger.internal.codegen.base.MapType;
import dagger.internal.codegen.base.OptionalType;
import dagger.spi.model.DaggerElement;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.Key;
import dagger.spi.model.RequestKind;

import static com.google.auto.common.MoreTypes.isTypeOf;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.base.RequestKinds.extractKeyType;
import static dagger.internal.codegen.base.RequestKinds.frameworkClass;
import static dagger.internal.codegen.base.RequestKinds.getRequestKind;
import static dagger.internal.codegen.binding.ConfigurationAnnotations.getNullableType;
import static dagger.internal.codegen.langmodel.DaggerTypes.unwrapType;
import static dagger.spi.model.RequestKind.FUTURE;
import static dagger.spi.model.RequestKind.INSTANCE;
import static dagger.spi.model.RequestKind.MEMBERS_INJECTION;
import static dagger.spi.model.RequestKind.PRODUCER;
import static dagger.spi.model.RequestKind.PROVIDER;

/**
 * Factory for {@link DependencyRequest}s.
 *
 * <p>Any factory method may throw {@link TypeNotPresentException} if a type is not available, which
 * may mean that the type will be generated in a later round of processing.
 */
public final class DependencyRequestFactory {
    private final KeyFactory keyFactory;
    private final InjectionAnnotations injectionAnnotations;

    @Inject
    DependencyRequestFactory(
            KeyFactory keyFactory,
            InjectionAnnotations injectionAnnotations
    ) {
        this.keyFactory = keyFactory;
        this.injectionAnnotations = injectionAnnotations;
    }

    //方法上的参数生成依赖对象集合
    ImmutableSet<DependencyRequest> forRequiredResolvedVariables(
            List<? extends VariableElement> variables,
            List<? extends TypeMirror> resolvedTypes
    ) {

        checkState(resolvedTypes.size() == variables.size());

        ImmutableSet.Builder<DependencyRequest> builder = ImmutableSet.builder();
        for (int i = 0; i < variables.size(); i++) {
            builder.add(forRequiredResolvedVariable(variables.get(i), resolvedTypes.get(i)));
        }
        return builder.build();
    }

    /**
     * Creates synthetic dependency requests for each individual multibinding contribution in {@code
     * multibindingContributions}.
     */
    ImmutableSet<DependencyRequest> forMultibindingContributions(
            Key multibindingKey, Iterable<ContributionBinding> multibindingContributions) {

        ImmutableSet.Builder<DependencyRequest> requests = ImmutableSet.builder();
        for (ContributionBinding multibindingContribution : multibindingContributions) {
            requests.add(forMultibindingContribution(multibindingKey, multibindingContribution));
        }
        return requests.build();
    }

    /**
     * Creates a synthetic dependency request for one individual {@code multibindingContribution}.
     */
    private DependencyRequest forMultibindingContribution(
            Key multibindingKey, ContributionBinding multibindingContribution) {
        checkArgument(
                multibindingContribution.key().multibindingContributionIdentifier().isPresent(),
                "multibindingContribution's key must have a multibinding contribution identifier: %s",
                multibindingContribution);

        return DependencyRequest.builder()
                .kind(multibindingContributionRequestKind(multibindingKey, multibindingContribution))
                .key(multibindingContribution.key())
                .build();
    }

    /**
     * Returns a synthetic request for the present value of an optional binding generated from a
     * {@link dagger.BindsOptionalOf} declaration.
     */
    DependencyRequest forSyntheticPresentOptionalBinding(Key requestKey, RequestKind kind) {
        Optional<Key> key = keyFactory.unwrapOptional(requestKey);
        checkArgument(key.isPresent(), "not a request for optional: %s", requestKey);
        return DependencyRequest.builder()
                .kind(kind)
                .key(key.get())
                .isNullable(
                        allowsNull(getRequestKind(OptionalType.from(requestKey).valueType()), Optional.empty()))
                .build();
    }

    // TODO(b/28555349): support PROVIDER_OF_LAZY here too
    private static final ImmutableSet<RequestKind> WRAPPING_MAP_VALUE_FRAMEWORK_TYPES =
            ImmutableSet.of(PROVIDER, PRODUCER);

    private RequestKind multibindingContributionRequestKind(
            Key multibindingKey, ContributionBinding multibindingContribution) {
        switch (multibindingContribution.contributionType()) {
            case MAP:
                MapType mapType = MapType.from(multibindingKey);
                for (RequestKind kind : WRAPPING_MAP_VALUE_FRAMEWORK_TYPES) {
                    if (mapType.valuesAreTypeOf(frameworkClass(kind))) {
                        return kind;
                    }
                }
                // fall through
            case SET:
            case SET_VALUES:
                return INSTANCE;
            case UNIQUE:
                throw new IllegalArgumentException(
                        "multibindingContribution must be a multibinding: " + multibindingContribution);
        }
        throw new AssertionError(multibindingContribution.toString());
    }

    DependencyRequest forRequiredResolvedVariable(
            VariableElement variableElement, TypeMirror resolvedType) {

        checkNotNull(variableElement);
        checkNotNull(resolvedType);

        // Ban @Assisted parameters, they are not considered dependency requests.
        checkArgument(!AssistedInjectionAnnotations.isAssistedParameter(variableElement));

        //变量是否使用了Qualifier注解修饰的注解修饰
        Optional<AnnotationMirror> qualifier = injectionAnnotations.getQualifier(variableElement);

        return newDependencyRequest(variableElement, resolvedType, qualifier);
    }

    public DependencyRequest forComponentProvisionMethod(
            ExecutableElement provisionMethod, ExecutableType provisionMethodType) {
        checkNotNull(provisionMethod);
        checkNotNull(provisionMethodType);
        checkArgument(
                provisionMethod.getParameters().isEmpty(),
                "Component provision methods must be empty: %s",
                provisionMethod);
        Optional<AnnotationMirror> qualifier = injectionAnnotations.getQualifier(provisionMethod);

        return newDependencyRequest(provisionMethod, provisionMethodType.getReturnType(), qualifier);
    }

    public DependencyRequest forComponentProductionMethod(
            ExecutableElement productionMethod,
            ExecutableType productionMethodType
    ) {
        checkNotNull(productionMethod);
        checkNotNull(productionMethodType);
        checkArgument(
                productionMethod.getParameters().isEmpty(),
                "Component production methods must be empty: %s",
                productionMethod);

        TypeMirror type = productionMethodType.getReturnType();

        Optional<AnnotationMirror> qualifier = injectionAnnotations.getQualifier(productionMethod);
        // Only a component production method can be a request for a ListenableFuture, so we
        // special-case it here.
        //isTypeOf(ListenableFuture.class, type):type是不是ListenableFuture类型
        if (isTypeOf(ListenableFuture.class, type)) {
            return DependencyRequest.builder()
                    .kind(FUTURE)
                    .key(keyFactory.forQualifiedType(qualifier, unwrapType(type)))
                    .requestElement(DaggerElement.fromJava(productionMethod))
                    .build();
        } else {
            return newDependencyRequest(productionMethod, type, qualifier);
        }
    }

    DependencyRequest forComponentMembersInjectionMethod(
            ExecutableElement membersInjectionMethod,
            ExecutableType membersInjectionMethodType
    ) {
        checkNotNull(membersInjectionMethod);
        checkNotNull(membersInjectionMethodType);
        Optional<AnnotationMirror> qualifier =
                injectionAnnotations.getQualifier(membersInjectionMethod);
        checkArgument(!qualifier.isPresent());
        TypeMirror membersInjectedType = getOnlyElement(membersInjectionMethodType.getParameterTypes());
        return DependencyRequest.builder()
                .kind(MEMBERS_INJECTION)
                .key(keyFactory.forMembersInjectedType(membersInjectedType))//将方法参数作为Key
                .requestElement(DaggerElement.fromJava(membersInjectionMethod))
                .build();
    }

    //Key:Executor作为type，ProductionImplementation表示qualifier属性；kind：PROVIDER
    DependencyRequest forProductionImplementationExecutor() {
        return DependencyRequest.builder()
                .kind(PROVIDER)
                .key(keyFactory.forProductionImplementationExecutor())
                .build();
    }

    //key:ProductionComponentMonitor生成，kind：PROVIDER
    DependencyRequest forProductionComponentMonitor() {
        return DependencyRequest.builder()
                .kind(PROVIDER)
                .key(keyFactory.forProductionComponentMonitor())
                .build();
    }

    private DependencyRequest newDependencyRequest(
            Element requestElement,
            TypeMirror type,
            Optional<AnnotationMirror> qualifier
    ) {
        //根据type判断RequestKind请求类型
        RequestKind requestKind = getRequestKind(type);

        return DependencyRequest.builder()
                .kind(requestKind)
                //依赖的key的qualifier使用的是方法方法上的qualifier，type：如果使用了架构类型，剥离架构类型，例如Provider<T>,使用T作为type；如果没有使用架构类型，直接使用type
                .key(keyFactory.forQualifiedType(qualifier, extractKeyType(type)))
                .requestElement(DaggerElement.fromJava(requestElement))
                .isNullable(allowsNull(requestKind, getNullableType(requestElement)))//如果该参数使用了架构类型 || 参数节点上使用了Nullable注解，表示true
                .build();
    }

    /**
     * Returns {@code true} if a given request element allows null values. {@link
     * RequestKind#INSTANCE} requests must be annotated with {@code @Nullable} in order to allow null
     * values. All other request kinds implicitly allow null values because they are are wrapped
     * inside {@link Provider}, {@link Lazy}, etc.
     * <p>
     * 如果给定的节点允许为null，返回true
     */
    private boolean allowsNull(RequestKind kind, Optional<DeclaredType> nullableType) {
        return nullableType.isPresent() || !kind.equals(INSTANCE);
    }
}
