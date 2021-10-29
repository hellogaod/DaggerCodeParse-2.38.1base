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
public final class ProducerFromProviderCreationExpression_Factory {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    public ProducerFromProviderCreationExpression_Factory(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
    }

    public ProducerFromProviderCreationExpression get(ContributionBinding binding) {
        return newInstance(binding, componentImplementationProvider.get(), componentRequestRepresentationsProvider.get());
    }

    public static ProducerFromProviderCreationExpression_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider) {
        return new ProducerFromProviderCreationExpression_Factory(componentImplementationProvider, componentRequestRepresentationsProvider);
    }

    public static ProducerFromProviderCreationExpression newInstance(ContributionBinding binding,
                                                                     ComponentImplementation componentImplementation,
                                                                     ComponentRequestRepresentations componentRequestRepresentations) {
        return new ProducerFromProviderCreationExpression(binding, componentImplementation, componentRequestRepresentations);
    }
}
