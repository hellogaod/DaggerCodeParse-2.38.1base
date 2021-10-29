package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.Binding;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.langmodel.DaggerTypes;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class LegacyBindingRepresentation_Factory {
    private final Provider<BindingGraph> graphProvider;

    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<ComponentMethodRequestRepresentation.Factory> componentMethodRequestRepresentationFactoryProvider;

    private final Provider<DelegateRequestRepresentation.Factory> delegateRequestRepresentationFactoryProvider;

    private final Provider<DerivedFromFrameworkInstanceRequestRepresentation.Factory> derivedFromFrameworkInstanceRequestRepresentationFactoryProvider;

    private final Provider<ImmediateFutureRequestRepresentation.Factory> immediateFutureRequestRepresentationFactoryProvider;

    private final Provider<MembersInjectionRequestRepresentation.Factory> membersInjectionRequestRepresentationFactoryProvider;

    private final Provider<PrivateMethodRequestRepresentation.Factory> privateMethodRequestRepresentationFactoryProvider;

    private final Provider<AssistedPrivateMethodRequestRepresentation.Factory> assistedPrivateMethodRequestRepresentationFactoryProvider;

    private final Provider<ProducerNodeInstanceRequestRepresentation.Factory> producerNodeInstanceRequestRepresentationFactoryProvider;

    private final Provider<ProviderInstanceRequestRepresentation.Factory> providerInstanceRequestRepresentationFactoryProvider;

    private final Provider<UnscopedDirectInstanceRequestRepresentationFactory> unscopedDirectInstanceRequestRepresentationFactoryProvider;

    private final Provider<ProducerFromProviderCreationExpression.Factory> producerFromProviderCreationExpressionFactoryProvider;

    private final Provider<UnscopedFrameworkInstanceCreationExpressionFactory> unscopedFrameworkInstanceCreationExpressionFactoryProvider;

    private final Provider<DaggerTypes> typesProvider;

    public LegacyBindingRepresentation_Factory(Provider<BindingGraph> graphProvider,
                                               Provider<ComponentImplementation> componentImplementationProvider,
                                               Provider<ComponentMethodRequestRepresentation.Factory> componentMethodRequestRepresentationFactoryProvider,
                                               Provider<DelegateRequestRepresentation.Factory> delegateRequestRepresentationFactoryProvider,
                                               Provider<DerivedFromFrameworkInstanceRequestRepresentation.Factory> derivedFromFrameworkInstanceRequestRepresentationFactoryProvider,
                                               Provider<ImmediateFutureRequestRepresentation.Factory> immediateFutureRequestRepresentationFactoryProvider,
                                               Provider<MembersInjectionRequestRepresentation.Factory> membersInjectionRequestRepresentationFactoryProvider,
                                               Provider<PrivateMethodRequestRepresentation.Factory> privateMethodRequestRepresentationFactoryProvider,
                                               Provider<AssistedPrivateMethodRequestRepresentation.Factory> assistedPrivateMethodRequestRepresentationFactoryProvider,
                                               Provider<ProducerNodeInstanceRequestRepresentation.Factory> producerNodeInstanceRequestRepresentationFactoryProvider,
                                               Provider<ProviderInstanceRequestRepresentation.Factory> providerInstanceRequestRepresentationFactoryProvider,
                                               Provider<UnscopedDirectInstanceRequestRepresentationFactory> unscopedDirectInstanceRequestRepresentationFactoryProvider,
                                               Provider<ProducerFromProviderCreationExpression.Factory> producerFromProviderCreationExpressionFactoryProvider,
                                               Provider<UnscopedFrameworkInstanceCreationExpressionFactory> unscopedFrameworkInstanceCreationExpressionFactoryProvider,
                                               Provider<DaggerTypes> typesProvider) {
        this.graphProvider = graphProvider;
        this.componentImplementationProvider = componentImplementationProvider;
        this.componentMethodRequestRepresentationFactoryProvider = componentMethodRequestRepresentationFactoryProvider;
        this.delegateRequestRepresentationFactoryProvider = delegateRequestRepresentationFactoryProvider;
        this.derivedFromFrameworkInstanceRequestRepresentationFactoryProvider = derivedFromFrameworkInstanceRequestRepresentationFactoryProvider;
        this.immediateFutureRequestRepresentationFactoryProvider = immediateFutureRequestRepresentationFactoryProvider;
        this.membersInjectionRequestRepresentationFactoryProvider = membersInjectionRequestRepresentationFactoryProvider;
        this.privateMethodRequestRepresentationFactoryProvider = privateMethodRequestRepresentationFactoryProvider;
        this.assistedPrivateMethodRequestRepresentationFactoryProvider = assistedPrivateMethodRequestRepresentationFactoryProvider;
        this.producerNodeInstanceRequestRepresentationFactoryProvider = producerNodeInstanceRequestRepresentationFactoryProvider;
        this.providerInstanceRequestRepresentationFactoryProvider = providerInstanceRequestRepresentationFactoryProvider;
        this.unscopedDirectInstanceRequestRepresentationFactoryProvider = unscopedDirectInstanceRequestRepresentationFactoryProvider;
        this.producerFromProviderCreationExpressionFactoryProvider = producerFromProviderCreationExpressionFactoryProvider;
        this.unscopedFrameworkInstanceCreationExpressionFactoryProvider = unscopedFrameworkInstanceCreationExpressionFactoryProvider;
        this.typesProvider = typesProvider;
    }

    public LegacyBindingRepresentation get(boolean isFastInit, Binding binding,
                                           SwitchingProviders switchingProviders) {
        return newInstance(isFastInit, binding, switchingProviders, graphProvider.get(), componentImplementationProvider.get(), componentMethodRequestRepresentationFactoryProvider.get(), delegateRequestRepresentationFactoryProvider.get(), derivedFromFrameworkInstanceRequestRepresentationFactoryProvider.get(), immediateFutureRequestRepresentationFactoryProvider.get(), membersInjectionRequestRepresentationFactoryProvider.get(), privateMethodRequestRepresentationFactoryProvider.get(), assistedPrivateMethodRequestRepresentationFactoryProvider.get(), producerNodeInstanceRequestRepresentationFactoryProvider.get(), providerInstanceRequestRepresentationFactoryProvider.get(), unscopedDirectInstanceRequestRepresentationFactoryProvider.get(), producerFromProviderCreationExpressionFactoryProvider.get(), unscopedFrameworkInstanceCreationExpressionFactoryProvider.get(), typesProvider.get());
    }

    public static LegacyBindingRepresentation_Factory create(Provider<BindingGraph> graphProvider,
                                                             Provider<ComponentImplementation> componentImplementationProvider,
                                                             Provider<ComponentMethodRequestRepresentation.Factory> componentMethodRequestRepresentationFactoryProvider,
                                                             Provider<DelegateRequestRepresentation.Factory> delegateRequestRepresentationFactoryProvider,
                                                             Provider<DerivedFromFrameworkInstanceRequestRepresentation.Factory> derivedFromFrameworkInstanceRequestRepresentationFactoryProvider,
                                                             Provider<ImmediateFutureRequestRepresentation.Factory> immediateFutureRequestRepresentationFactoryProvider,
                                                             Provider<MembersInjectionRequestRepresentation.Factory> membersInjectionRequestRepresentationFactoryProvider,
                                                             Provider<PrivateMethodRequestRepresentation.Factory> privateMethodRequestRepresentationFactoryProvider,
                                                             Provider<AssistedPrivateMethodRequestRepresentation.Factory> assistedPrivateMethodRequestRepresentationFactoryProvider,
                                                             Provider<ProducerNodeInstanceRequestRepresentation.Factory> producerNodeInstanceRequestRepresentationFactoryProvider,
                                                             Provider<ProviderInstanceRequestRepresentation.Factory> providerInstanceRequestRepresentationFactoryProvider,
                                                             Provider<UnscopedDirectInstanceRequestRepresentationFactory> unscopedDirectInstanceRequestRepresentationFactoryProvider,
                                                             Provider<ProducerFromProviderCreationExpression.Factory> producerFromProviderCreationExpressionFactoryProvider,
                                                             Provider<UnscopedFrameworkInstanceCreationExpressionFactory> unscopedFrameworkInstanceCreationExpressionFactoryProvider,
                                                             Provider<DaggerTypes> typesProvider) {
        return new LegacyBindingRepresentation_Factory(graphProvider, componentImplementationProvider, componentMethodRequestRepresentationFactoryProvider, delegateRequestRepresentationFactoryProvider, derivedFromFrameworkInstanceRequestRepresentationFactoryProvider, immediateFutureRequestRepresentationFactoryProvider, membersInjectionRequestRepresentationFactoryProvider, privateMethodRequestRepresentationFactoryProvider, assistedPrivateMethodRequestRepresentationFactoryProvider, producerNodeInstanceRequestRepresentationFactoryProvider, providerInstanceRequestRepresentationFactoryProvider, unscopedDirectInstanceRequestRepresentationFactoryProvider, producerFromProviderCreationExpressionFactoryProvider, unscopedFrameworkInstanceCreationExpressionFactoryProvider, typesProvider);
    }

    public static LegacyBindingRepresentation newInstance(boolean isFastInit, Binding binding,
                                                          Object switchingProviders, BindingGraph graph,
                                                          ComponentImplementation componentImplementation,
                                                          Object componentMethodRequestRepresentationFactory,
                                                          Object delegateRequestRepresentationFactory,
                                                          Object derivedFromFrameworkInstanceRequestRepresentationFactory,
                                                          Object immediateFutureRequestRepresentationFactory,
                                                          Object membersInjectionRequestRepresentationFactory,
                                                          Object privateMethodRequestRepresentationFactory,
                                                          Object assistedPrivateMethodRequestRepresentationFactory,
                                                          Object producerNodeInstanceRequestRepresentationFactory,
                                                          Object providerInstanceRequestRepresentationFactory,
                                                          Object unscopedDirectInstanceRequestRepresentationFactory,
                                                          Object producerFromProviderCreationExpressionFactory,
                                                          Object unscopedFrameworkInstanceCreationExpressionFactory, DaggerTypes types) {
        return new LegacyBindingRepresentation(isFastInit, binding, (SwitchingProviders) switchingProviders, graph, componentImplementation, (ComponentMethodRequestRepresentation.Factory) componentMethodRequestRepresentationFactory, (DelegateRequestRepresentation.Factory) delegateRequestRepresentationFactory, (DerivedFromFrameworkInstanceRequestRepresentation.Factory) derivedFromFrameworkInstanceRequestRepresentationFactory, (ImmediateFutureRequestRepresentation.Factory) immediateFutureRequestRepresentationFactory, (MembersInjectionRequestRepresentation.Factory) membersInjectionRequestRepresentationFactory, (PrivateMethodRequestRepresentation.Factory) privateMethodRequestRepresentationFactory, (AssistedPrivateMethodRequestRepresentation.Factory) assistedPrivateMethodRequestRepresentationFactory, (ProducerNodeInstanceRequestRepresentation.Factory) producerNodeInstanceRequestRepresentationFactory, (ProviderInstanceRequestRepresentation.Factory) providerInstanceRequestRepresentationFactory, (UnscopedDirectInstanceRequestRepresentationFactory) unscopedDirectInstanceRequestRepresentationFactory, (ProducerFromProviderCreationExpression.Factory) producerFromProviderCreationExpressionFactory, (UnscopedFrameworkInstanceCreationExpressionFactory) unscopedFrameworkInstanceCreationExpressionFactory, types);
    }
}
