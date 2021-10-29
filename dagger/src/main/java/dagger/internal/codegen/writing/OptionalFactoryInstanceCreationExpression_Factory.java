package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.ContributionBinding;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class OptionalFactoryInstanceCreationExpression_Factory {
    private final Provider<OptionalFactories> optionalFactoriesProvider;

    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    public OptionalFactoryInstanceCreationExpression_Factory(
            Provider<OptionalFactories> optionalFactoriesProvider,
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider) {
        this.optionalFactoriesProvider = optionalFactoriesProvider;
        this.componentImplementationProvider = componentImplementationProvider;
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
    }

    public OptionalFactoryInstanceCreationExpression get(ContributionBinding binding) {
        return newInstance(binding, optionalFactoriesProvider.get(), componentImplementationProvider.get(), componentRequestRepresentationsProvider.get());
    }

    public static OptionalFactoryInstanceCreationExpression_Factory create(
            Provider<OptionalFactories> optionalFactoriesProvider,
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider) {
        return new OptionalFactoryInstanceCreationExpression_Factory(optionalFactoriesProvider, componentImplementationProvider, componentRequestRepresentationsProvider);
    }

    public static OptionalFactoryInstanceCreationExpression newInstance(ContributionBinding binding,
                                                                        Object optionalFactories, ComponentImplementation componentImplementation,
                                                                        ComponentRequestRepresentations componentRequestRepresentations) {
        return new OptionalFactoryInstanceCreationExpression(binding, (OptionalFactories) optionalFactories, componentImplementation, componentRequestRepresentations);
    }
}
