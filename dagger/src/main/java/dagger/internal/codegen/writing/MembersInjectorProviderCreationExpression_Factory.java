package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.ProvisionBinding;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class MembersInjectorProviderCreationExpression_Factory {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    public MembersInjectorProviderCreationExpression_Factory(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
    }

    public MembersInjectorProviderCreationExpression get(ProvisionBinding binding) {
        return newInstance(binding, componentImplementationProvider.get(), componentRequestRepresentationsProvider.get());
    }

    public static MembersInjectorProviderCreationExpression_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider) {
        return new MembersInjectorProviderCreationExpression_Factory(componentImplementationProvider, componentRequestRepresentationsProvider);
    }

    public static MembersInjectorProviderCreationExpression newInstance(ProvisionBinding binding,
                                                                        ComponentImplementation componentImplementation,
                                                                        ComponentRequestRepresentations componentRequestRepresentations) {
        return new MembersInjectorProviderCreationExpression(binding, componentImplementation, componentRequestRepresentations);
    }
}
