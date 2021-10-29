package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class UnscopedFrameworkInstanceCreationExpressionFactory_Factory implements Factory<UnscopedFrameworkInstanceCreationExpressionFactory> {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider;

    private final Provider<AnonymousProviderCreationExpression.Factory> anonymousProviderCreationExpressionFactoryProvider;

    private final Provider<DelegatingFrameworkInstanceCreationExpression.Factory> delegatingFrameworkInstanceCreationExpressionFactoryProvider;

    private final Provider<DependencyMethodProducerCreationExpression.Factory> dependencyMethodProducerCreationExpressionFactoryProvider;

    private final Provider<DependencyMethodProviderCreationExpression.Factory> dependencyMethodProviderCreationExpressionFactoryProvider;

    private final Provider<InjectionOrProvisionProviderCreationExpression.Factory> injectionOrProvisionProviderCreationExpressionFactoryProvider;

    private final Provider<MapFactoryCreationExpression.Factory> mapFactoryCreationExpressionFactoryProvider;

    private final Provider<MembersInjectorProviderCreationExpression.Factory> membersInjectorProviderCreationExpressionFactoryProvider;

    private final Provider<OptionalFactoryInstanceCreationExpression.Factory> optionalFactoryInstanceCreationExpressionFactoryProvider;

    private final Provider<ProducerCreationExpression.Factory> producerCreationExpressionFactoryProvider;

    private final Provider<SetFactoryCreationExpression.Factory> setFactoryCreationExpressionFactoryProvider;

    public UnscopedFrameworkInstanceCreationExpressionFactory_Factory(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider,
            Provider<AnonymousProviderCreationExpression.Factory> anonymousProviderCreationExpressionFactoryProvider,
            Provider<DelegatingFrameworkInstanceCreationExpression.Factory> delegatingFrameworkInstanceCreationExpressionFactoryProvider,
            Provider<DependencyMethodProducerCreationExpression.Factory> dependencyMethodProducerCreationExpressionFactoryProvider,
            Provider<DependencyMethodProviderCreationExpression.Factory> dependencyMethodProviderCreationExpressionFactoryProvider,
            Provider<InjectionOrProvisionProviderCreationExpression.Factory> injectionOrProvisionProviderCreationExpressionFactoryProvider,
            Provider<MapFactoryCreationExpression.Factory> mapFactoryCreationExpressionFactoryProvider,
            Provider<MembersInjectorProviderCreationExpression.Factory> membersInjectorProviderCreationExpressionFactoryProvider,
            Provider<OptionalFactoryInstanceCreationExpression.Factory> optionalFactoryInstanceCreationExpressionFactoryProvider,
            Provider<ProducerCreationExpression.Factory> producerCreationExpressionFactoryProvider,
            Provider<SetFactoryCreationExpression.Factory> setFactoryCreationExpressionFactoryProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
        this.componentRequirementExpressionsProvider = componentRequirementExpressionsProvider;
        this.anonymousProviderCreationExpressionFactoryProvider = anonymousProviderCreationExpressionFactoryProvider;
        this.delegatingFrameworkInstanceCreationExpressionFactoryProvider = delegatingFrameworkInstanceCreationExpressionFactoryProvider;
        this.dependencyMethodProducerCreationExpressionFactoryProvider = dependencyMethodProducerCreationExpressionFactoryProvider;
        this.dependencyMethodProviderCreationExpressionFactoryProvider = dependencyMethodProviderCreationExpressionFactoryProvider;
        this.injectionOrProvisionProviderCreationExpressionFactoryProvider = injectionOrProvisionProviderCreationExpressionFactoryProvider;
        this.mapFactoryCreationExpressionFactoryProvider = mapFactoryCreationExpressionFactoryProvider;
        this.membersInjectorProviderCreationExpressionFactoryProvider = membersInjectorProviderCreationExpressionFactoryProvider;
        this.optionalFactoryInstanceCreationExpressionFactoryProvider = optionalFactoryInstanceCreationExpressionFactoryProvider;
        this.producerCreationExpressionFactoryProvider = producerCreationExpressionFactoryProvider;
        this.setFactoryCreationExpressionFactoryProvider = setFactoryCreationExpressionFactoryProvider;
    }

    @Override
    public UnscopedFrameworkInstanceCreationExpressionFactory get() {
        return newInstance(componentImplementationProvider.get(), componentRequirementExpressionsProvider.get(), anonymousProviderCreationExpressionFactoryProvider.get(), delegatingFrameworkInstanceCreationExpressionFactoryProvider.get(), dependencyMethodProducerCreationExpressionFactoryProvider.get(), dependencyMethodProviderCreationExpressionFactoryProvider.get(), injectionOrProvisionProviderCreationExpressionFactoryProvider.get(), mapFactoryCreationExpressionFactoryProvider.get(), membersInjectorProviderCreationExpressionFactoryProvider.get(), optionalFactoryInstanceCreationExpressionFactoryProvider.get(), producerCreationExpressionFactoryProvider.get(), setFactoryCreationExpressionFactoryProvider.get());
    }

    public static UnscopedFrameworkInstanceCreationExpressionFactory_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider,
            Provider<AnonymousProviderCreationExpression.Factory> anonymousProviderCreationExpressionFactoryProvider,
            Provider<DelegatingFrameworkInstanceCreationExpression.Factory> delegatingFrameworkInstanceCreationExpressionFactoryProvider,
            Provider<DependencyMethodProducerCreationExpression.Factory> dependencyMethodProducerCreationExpressionFactoryProvider,
            Provider<DependencyMethodProviderCreationExpression.Factory> dependencyMethodProviderCreationExpressionFactoryProvider,
            Provider<InjectionOrProvisionProviderCreationExpression.Factory> injectionOrProvisionProviderCreationExpressionFactoryProvider,
            Provider<MapFactoryCreationExpression.Factory> mapFactoryCreationExpressionFactoryProvider,
            Provider<MembersInjectorProviderCreationExpression.Factory> membersInjectorProviderCreationExpressionFactoryProvider,
            Provider<OptionalFactoryInstanceCreationExpression.Factory> optionalFactoryInstanceCreationExpressionFactoryProvider,
            Provider<ProducerCreationExpression.Factory> producerCreationExpressionFactoryProvider,
            Provider<SetFactoryCreationExpression.Factory> setFactoryCreationExpressionFactoryProvider) {
        return new UnscopedFrameworkInstanceCreationExpressionFactory_Factory(componentImplementationProvider, componentRequirementExpressionsProvider, anonymousProviderCreationExpressionFactoryProvider, delegatingFrameworkInstanceCreationExpressionFactoryProvider, dependencyMethodProducerCreationExpressionFactoryProvider, dependencyMethodProviderCreationExpressionFactoryProvider, injectionOrProvisionProviderCreationExpressionFactoryProvider, mapFactoryCreationExpressionFactoryProvider, membersInjectorProviderCreationExpressionFactoryProvider, optionalFactoryInstanceCreationExpressionFactoryProvider, producerCreationExpressionFactoryProvider, setFactoryCreationExpressionFactoryProvider);
    }

    public static UnscopedFrameworkInstanceCreationExpressionFactory newInstance(
            ComponentImplementation componentImplementation,
            ComponentRequirementExpressions componentRequirementExpressions,
            Object anonymousProviderCreationExpressionFactory,
            Object delegatingFrameworkInstanceCreationExpressionFactory,
            Object dependencyMethodProducerCreationExpressionFactory,
            Object dependencyMethodProviderCreationExpressionFactory,
            Object injectionOrProvisionProviderCreationExpressionFactory,
            Object mapFactoryCreationExpressionFactory,
            Object membersInjectorProviderCreationExpressionFactory,
            Object optionalFactoryInstanceCreationExpressionFactory,
            Object producerCreationExpressionFactory, Object setFactoryCreationExpressionFactory) {
        return new UnscopedFrameworkInstanceCreationExpressionFactory(componentImplementation, componentRequirementExpressions, (AnonymousProviderCreationExpression.Factory) anonymousProviderCreationExpressionFactory, (DelegatingFrameworkInstanceCreationExpression.Factory) delegatingFrameworkInstanceCreationExpressionFactory, (DependencyMethodProducerCreationExpression.Factory) dependencyMethodProducerCreationExpressionFactory, (DependencyMethodProviderCreationExpression.Factory) dependencyMethodProviderCreationExpressionFactory, (InjectionOrProvisionProviderCreationExpression.Factory) injectionOrProvisionProviderCreationExpressionFactory, (MapFactoryCreationExpression.Factory) mapFactoryCreationExpressionFactory, (MembersInjectorProviderCreationExpression.Factory) membersInjectorProviderCreationExpressionFactory, (OptionalFactoryInstanceCreationExpression.Factory) optionalFactoryInstanceCreationExpressionFactory, (ProducerCreationExpression.Factory) producerCreationExpressionFactory, (SetFactoryCreationExpression.Factory) setFactoryCreationExpressionFactory);
    }
}
