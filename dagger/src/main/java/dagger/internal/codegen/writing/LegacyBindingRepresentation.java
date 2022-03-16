package dagger.internal.codegen.writing;

import com.squareup.javapoet.CodeBlock;

import java.util.Optional;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.Binding;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.BindingType;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.FrameworkType;
import dagger.internal.codegen.binding.MembersInjectionBinding;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.BindingKind;
import dagger.spi.model.Key;
import dagger.spi.model.RequestKind;

import static com.google.common.base.Preconditions.checkArgument;
import static dagger.internal.codegen.binding.BindingRequest.bindingRequest;
import static dagger.internal.codegen.javapoet.TypeNames.DOUBLE_CHECK;
import static dagger.internal.codegen.javapoet.TypeNames.SINGLE_CHECK;
import static dagger.internal.codegen.writing.DelegateRequestRepresentation.isBindsScopeStrongerThanDependencyScope;
import static dagger.internal.codegen.writing.MemberSelect.staticFactoryCreation;
import static dagger.spi.model.BindingKind.DELEGATE;
import static dagger.spi.model.BindingKind.MULTIBOUND_MAP;
import static dagger.spi.model.BindingKind.MULTIBOUND_SET;


/**
 * A binding representation that wraps code generation methods that satisfy all kinds of request for
 * that binding.
 */
final class LegacyBindingRepresentation implements BindingRepresentation {

    private final BindingGraph graph;
    private final boolean isFastInit;
    private final Binding binding;
    private final ComponentImplementation componentImplementation;
    private final ComponentMethodRequestRepresentation.Factory
            componentMethodRequestRepresentationFactory;
    private final DelegateRequestRepresentation.Factory delegateRequestRepresentationFactory;
    private final DerivedFromFrameworkInstanceRequestRepresentation.Factory
            derivedFromFrameworkInstanceRequestRepresentationFactory;
    private final ImmediateFutureRequestRepresentation.Factory
            immediateFutureRequestRepresentationFactory;
    private final MembersInjectionRequestRepresentation.Factory
            membersInjectionRequestRepresentationFactory;
    private final PrivateMethodRequestRepresentation.Factory
            privateMethodRequestRepresentationFactory;
    private final AssistedPrivateMethodRequestRepresentation.Factory
            assistedPrivateMethodRequestRepresentationFactory;
    private final ProducerNodeInstanceRequestRepresentation.Factory
            producerNodeInstanceRequestRepresentationFactory;
    private final ProviderInstanceRequestRepresentation.Factory
            providerInstanceRequestRepresentationFactory;
    private final UnscopedDirectInstanceRequestRepresentationFactory
            unscopedDirectInstanceRequestRepresentationFactory;
    private final ProducerFromProviderCreationExpression.Factory
            producerFromProviderCreationExpressionFactory;
    private final UnscopedFrameworkInstanceCreationExpressionFactory
            unscopedFrameworkInstanceCreationExpressionFactory;
    private final SwitchingProviders switchingProviders;

    @AssistedInject
    LegacyBindingRepresentation(
            @Assisted boolean isFastInit,
            @Assisted Binding binding,
            @Assisted SwitchingProviders switchingProviders,
            BindingGraph graph,
            ComponentImplementation componentImplementation,
            ComponentMethodRequestRepresentation.Factory componentMethodRequestRepresentationFactory,
            DelegateRequestRepresentation.Factory delegateRequestRepresentationFactory,
            DerivedFromFrameworkInstanceRequestRepresentation.Factory
                    derivedFromFrameworkInstanceRequestRepresentationFactory,
            ImmediateFutureRequestRepresentation.Factory immediateFutureRequestRepresentationFactory,
            MembersInjectionRequestRepresentation.Factory membersInjectionRequestRepresentationFactory,
            PrivateMethodRequestRepresentation.Factory privateMethodRequestRepresentationFactory,
            AssistedPrivateMethodRequestRepresentation.Factory
                    assistedPrivateMethodRequestRepresentationFactory,
            ProducerNodeInstanceRequestRepresentation.Factory
                    producerNodeInstanceRequestRepresentationFactory,
            ProviderInstanceRequestRepresentation.Factory providerInstanceRequestRepresentationFactory,
            UnscopedDirectInstanceRequestRepresentationFactory
                    unscopedDirectInstanceRequestRepresentationFactory,
            ProducerFromProviderCreationExpression.Factory producerFromProviderCreationExpressionFactory,
            UnscopedFrameworkInstanceCreationExpressionFactory
                    unscopedFrameworkInstanceCreationExpressionFactory,
            DaggerTypes types) {
        this.isFastInit = isFastInit;
        this.binding = binding;
        this.switchingProviders = switchingProviders;
        this.graph = graph;
        this.componentImplementation = componentImplementation;
        this.componentMethodRequestRepresentationFactory = componentMethodRequestRepresentationFactory;
        this.delegateRequestRepresentationFactory = delegateRequestRepresentationFactory;
        this.derivedFromFrameworkInstanceRequestRepresentationFactory =
                derivedFromFrameworkInstanceRequestRepresentationFactory;
        this.immediateFutureRequestRepresentationFactory = immediateFutureRequestRepresentationFactory;
        this.membersInjectionRequestRepresentationFactory =
                membersInjectionRequestRepresentationFactory;
        this.privateMethodRequestRepresentationFactory = privateMethodRequestRepresentationFactory;
        this.producerNodeInstanceRequestRepresentationFactory =
                producerNodeInstanceRequestRepresentationFactory;
        this.providerInstanceRequestRepresentationFactory =
                providerInstanceRequestRepresentationFactory;
        this.unscopedDirectInstanceRequestRepresentationFactory =
                unscopedDirectInstanceRequestRepresentationFactory;
        this.producerFromProviderCreationExpressionFactory =
                producerFromProviderCreationExpressionFactory;
        this.unscopedFrameworkInstanceCreationExpressionFactory =
                unscopedFrameworkInstanceCreationExpressionFactory;
        this.assistedPrivateMethodRequestRepresentationFactory =
                assistedPrivateMethodRequestRepresentationFactory;
    }

    @Override
    public RequestRepresentation getRequestRepresentation(BindingRequest request) {
        switch (binding.bindingType()) {
            case MEMBERS_INJECTION://如果是MembersInjectionBinding对象
                checkArgument(request.isRequestKind(RequestKind.MEMBERS_INJECTION));
                return membersInjectionRequestRepresentationFactory.create(
                        (MembersInjectionBinding) binding);

            case PROVISION://如果是ProvisionBinding对象
                return provisionRequestRepresentation((ContributionBinding) binding, request);

            case PRODUCTION://如果是ProductionBinding对象
                return productionRequestRepresentation((ContributionBinding) binding, request);
        }
        throw new AssertionError(binding);
    }

    /**
     * Returns a binding expression that uses a {@link javax.inject.Provider} for provision bindings
     * or a {@link dagger.producers.Producer} for production bindings.
     */
    private RequestRepresentation frameworkInstanceRequestRepresentation(
            ContributionBinding binding) {
        FrameworkFieldInitializer.FrameworkInstanceCreationExpression frameworkInstanceCreationExpression =
                unscopedFrameworkInstanceCreationExpressionFactory.create(binding);

        if (isFastInit
                // Some creation expressions can opt out of using switching providers.
                && frameworkInstanceCreationExpression.useSwitchingProvider()
                // Production types are not yet supported with switching providers.
                && binding.bindingType() != BindingType.PRODUCTION) {
            // First try to get the instance expression via getRequestRepresentation(). However, if that
            // expression is a DerivedFromFrameworkInstanceRequestRepresentation (e.g. fooProvider.get()),
            // then we can't use it to create an instance within the SwitchingProvider since that would
            // cause a cycle. In such cases, we try to use the unscopedDirectInstanceRequestRepresentation
            // directly, or else fall back to default mode.
            BindingRequest instanceRequest = bindingRequest(binding.key(), RequestKind.INSTANCE);
            RequestRepresentation instanceExpression = getRequestRepresentation(instanceRequest);
            if (!(instanceExpression instanceof DerivedFromFrameworkInstanceRequestRepresentation)) {
                frameworkInstanceCreationExpression =
                        switchingProviders.newFrameworkInstanceCreationExpression(binding, instanceExpression);
            } else {
                Optional<RequestRepresentation> unscopedInstanceExpression =
                        unscopedDirectInstanceRequestRepresentationFactory.create(binding);
                if (unscopedInstanceExpression.isPresent()) {
                    frameworkInstanceCreationExpression =
                            switchingProviders.newFrameworkInstanceCreationExpression(
                                    binding,
                                    unscopedInstanceExpression.get().requiresMethodEncapsulation()
                                            ? privateMethodRequestRepresentationFactory.create(
                                            instanceRequest, binding, unscopedInstanceExpression.get())
                                            : unscopedInstanceExpression.get());
                }
            }
        }

        // TODO(bcorso): Consider merging the static factory creation logic into CreationExpressions?
        Optional<MemberSelect> staticMethod =
                useStaticFactoryCreation() ? staticFactoryCreation(binding) : Optional.empty();
        FrameworkInstanceSupplier frameworkInstanceSupplier =
                staticMethod.isPresent()
                        ? staticMethod::get
                        : new FrameworkFieldInitializer(
                        componentImplementation,
                        binding,
                        binding.scope().isPresent()
                                ? scope(frameworkInstanceCreationExpression)
                                : frameworkInstanceCreationExpression);

        switch (binding.bindingType()) {
            case PROVISION:
                return providerInstanceRequestRepresentationFactory.create(
                        binding, frameworkInstanceSupplier);
            case PRODUCTION:
                return producerNodeInstanceRequestRepresentationFactory.create(
                        binding, frameworkInstanceSupplier);
            default:
                throw new AssertionError("invalid binding type: " + binding.bindingType());
        }
    }

    private FrameworkFieldInitializer.FrameworkInstanceCreationExpression scope(FrameworkFieldInitializer.FrameworkInstanceCreationExpression unscoped) {
        return () ->
                CodeBlock.of(
                        "$T.provider($L)",
                        binding.scope().get().isReusable() ? SINGLE_CHECK : DOUBLE_CHECK,
                        unscoped.creationExpression());
    }

    /**
     * Returns a binding expression for a provision binding.
     */
    private RequestRepresentation provisionRequestRepresentation(
            ContributionBinding binding, BindingRequest request) {
        Key key = request.key();
        switch (request.requestKind()) {
            case INSTANCE:
                return instanceRequestRepresentation(binding);

            case PROVIDER:
                return providerRequestRepresentation(binding);

            case LAZY:
            case PRODUCED:
            case PROVIDER_OF_LAZY:
                return derivedFromFrameworkInstanceRequestRepresentationFactory.create(
                        request, FrameworkType.PROVIDER);

            case PRODUCER:
                return producerFromProviderRequestRepresentation(binding);

            case FUTURE:
                return immediateFutureRequestRepresentationFactory.create(key);

            case MEMBERS_INJECTION:
                throw new IllegalArgumentException();
        }

        throw new AssertionError();
    }

    /**
     * Returns a binding expression for a production binding.
     */
    private RequestRepresentation productionRequestRepresentation(
            ContributionBinding binding, BindingRequest request) {
        return request.frameworkType().isPresent()
                ? frameworkInstanceRequestRepresentation(binding)
                : derivedFromFrameworkInstanceRequestRepresentationFactory.create(
                request, FrameworkType.PRODUCER_NODE);
    }

    /**
     * Returns a binding expression for {@link RequestKind#PROVIDER} requests.
     *
     * <p>{@code @Binds} bindings that don't {@linkplain #needsCaching(ContributionBinding) need to be
     * cached} can use a {@link DelegateRequestRepresentation}.
     *
     * <p>Otherwise, return a {@link FrameworkInstanceRequestRepresentation}.
     */
    private RequestRepresentation providerRequestRepresentation(ContributionBinding binding) {
        if (binding.kind().equals(DELEGATE) && !needsCaching(binding)) {
            return delegateRequestRepresentationFactory.create(binding, RequestKind.PROVIDER);
        }
        return frameworkInstanceRequestRepresentation(binding);
    }

    /**
     * Returns a binding expression that uses a {@link dagger.producers.Producer} field for a
     * provision binding.
     */
    private FrameworkInstanceRequestRepresentation producerFromProviderRequestRepresentation(
            ContributionBinding binding) {
        checkArgument(binding.bindingType().equals(BindingType.PROVISION));
        return producerNodeInstanceRequestRepresentationFactory.create(
                binding,
                new FrameworkFieldInitializer(
                        componentImplementation,
                        binding,
                        producerFromProviderCreationExpressionFactory.create(binding)));
    }

    /**
     * Returns a binding expression for {@link RequestKind#INSTANCE} requests.
     */
    private RequestRepresentation instanceRequestRepresentation(ContributionBinding binding) {

        Optional<RequestRepresentation> maybeDirectInstanceExpression =
                unscopedDirectInstanceRequestRepresentationFactory.create(binding);
        if (maybeDirectInstanceExpression.isPresent()) {
            RequestRepresentation directInstanceExpression = maybeDirectInstanceExpression.get();
            if (binding.kind() == BindingKind.ASSISTED_INJECTION) {
                BindingRequest request = bindingRequest(binding.key(), RequestKind.INSTANCE);
                return assistedPrivateMethodRequestRepresentationFactory.create(
                        request, binding, directInstanceExpression);
            }

            boolean isDefaultModeAssistedFactory =
                    binding.kind() == BindingKind.ASSISTED_FACTORY && !isFastInit;

            // If this is the case where we don't need to use Provider#get() because there's no caching
            // and it isn't a default mode assisted factory, we can try to use the direct expression,
            // possibly wrapped in a method if necessary (e.g. if it has dependencies).
            // Note: We choose not to use a direct expression for assisted factories in default mode
            // because they technically act more similar to a Provider than an instance, so we cache them
            // using a field in the component similar to Provider requests. This should also be the case
            // in FastInit, but it hasn't been implemented yet.
            if (!needsCaching(binding) && !isDefaultModeAssistedFactory) {
                return directInstanceExpression.requiresMethodEncapsulation()
                        ? wrapInMethod(binding, RequestKind.INSTANCE, directInstanceExpression)
                        : directInstanceExpression;
            }
        }
        return derivedFromFrameworkInstanceRequestRepresentationFactory.create(
                bindingRequest(binding.key(), RequestKind.INSTANCE), FrameworkType.PROVIDER);
    }

    /**
     * Returns {@code true} if the binding should use the static factory creation strategy.
     *
     * <p>In default mode, we always use the static factory creation strategy. In fastInit mode, we
     * prefer to use a SwitchingProvider instead of static factories in order to reduce class loading;
     * however, we allow static factories that can reused across multiple bindings, e.g. {@code
     * MapFactory} or {@code SetFactory}.
     * <p>
     * 是否使用static方法创建  isFastInit在没有使用bazel时始终为false
     */
    private boolean useStaticFactoryCreation() {
        return !isFastInit
                || binding.kind().equals(MULTIBOUND_MAP)
                || binding.kind().equals(MULTIBOUND_SET);
    }

    /**
     * Returns a binding expression that uses a given one as the body of a method that users call. If
     * a component provision method matches it, it will be the method implemented. If it does not
     * match a component provision method and the binding is modifiable, then a new public modifiable
     * binding method will be written. If the binding doesn't match a component method and is not
     * modifiable, then a new private method will be written.
     */
    RequestRepresentation wrapInMethod(
            ContributionBinding binding,
            RequestKind requestKind,
            RequestRepresentation bindingExpression) {
        // If we've already wrapped the expression, then use the delegate.
        if (bindingExpression instanceof MethodRequestRepresentation) {
            return bindingExpression;
        }


        BindingRequest request = bindingRequest(binding.key(), requestKind);
        Optional<ComponentDescriptor.ComponentMethodDescriptor> matchingComponentMethod =
                graph.componentDescriptor().firstMatchingComponentMethod(request);

        ComponentImplementation.ShardImplementation shardImplementation = componentImplementation.shardImplementation(binding);

        // Consider the case of a request from a component method like:
        //
        //   DaggerMyComponent extends MyComponent {
        //     @Overrides
        //     Foo getFoo() {
        //       <FOO_BINDING_REQUEST>
        //     }
        //   }
        //
        // Normally, in this case we would return a ComponentMethodRequestRepresentation rather than a
        // PrivateMethodRequestRepresentation so that #getFoo() can inline the implementation rather
        // than
        // create an unnecessary private method and return that. However, with sharding we don't want to
        // inline the implementation because that would defeat some of the class pool savings if those
        // fields had to communicate across shards. Thus, when a key belongs to a separate shard use a
        // PrivateMethodRequestRepresentation and put the private method in the shard.
        if (matchingComponentMethod.isPresent() && shardImplementation.isComponentShard()) {
            ComponentDescriptor.ComponentMethodDescriptor componentMethod = matchingComponentMethod.get();
            return componentMethodRequestRepresentationFactory.create(bindingExpression, componentMethod);
        } else {
            return privateMethodRequestRepresentationFactory.create(request, binding, bindingExpression);
        }
    }

    /**
     * Returns {@code true} if the component needs to make sure the provided value is cached.
     *
     * <p>The component needs to cache the value for scoped bindings except for {@code @Binds}
     * bindings whose scope is no stronger than their delegate's.
     * <p>
     * needsCaching(binding):当前bindingMethod方法没有使用scope修饰的注解修饰 || @Binds修饰的bindingMethod使用了scope注解并且比它依赖的key匹配到的绑定对象
     * 更加强壮（e.g.当前bindingMethod使用了Reusable修饰 并且bindingMethod的依赖的key匹配到的Binding对象使用了非Reusable的scope注解）
     *
     * 其实意思就是当前绑定对象是否使用了scope注解。
     */
    private boolean needsCaching(ContributionBinding binding) {
        if (!binding.scope().isPresent()) {
            return false;
        }
        if (binding.kind().equals(DELEGATE)) {
            return isBindsScopeStrongerThanDependencyScope(binding, graph);
        }
        return true;
    }

    @AssistedFactory
    static interface Factory {
        LegacyBindingRepresentation create(
                boolean isFastInit,
                Binding binding,
                SwitchingProviders switchingProviders
        );
    }
}
