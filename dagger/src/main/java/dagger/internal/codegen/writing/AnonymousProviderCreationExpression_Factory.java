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
public final class AnonymousProviderCreationExpression_Factory {
    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    private final Provider<ComponentImplementation> componentImplementationProvider;

    public AnonymousProviderCreationExpression_Factory(
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<ComponentImplementation> componentImplementationProvider) {
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
        this.componentImplementationProvider = componentImplementationProvider;
    }

    public AnonymousProviderCreationExpression get(ContributionBinding binding) {
        return newInstance(binding, componentRequestRepresentationsProvider.get(), componentImplementationProvider.get());
    }

    public static AnonymousProviderCreationExpression_Factory create(
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<ComponentImplementation> componentImplementationProvider) {
        return new AnonymousProviderCreationExpression_Factory(componentRequestRepresentationsProvider, componentImplementationProvider);
    }

    public static AnonymousProviderCreationExpression newInstance(ContributionBinding binding,
                                                                  ComponentRequestRepresentations componentRequestRepresentations,
                                                                  ComponentImplementation componentImplementation) {
        return new AnonymousProviderCreationExpression(binding, componentRequestRepresentations, componentImplementation);
    }
}
