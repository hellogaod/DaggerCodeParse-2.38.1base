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
public final class SubcomponentCreatorRequestRepresentation_Factory {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    public SubcomponentCreatorRequestRepresentation_Factory(
            Provider<ComponentImplementation> componentImplementationProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
    }

    public SubcomponentCreatorRequestRepresentation get(ContributionBinding binding) {
        return newInstance(binding, componentImplementationProvider.get());
    }

    public static SubcomponentCreatorRequestRepresentation_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider) {
        return new SubcomponentCreatorRequestRepresentation_Factory(componentImplementationProvider);
    }

    public static SubcomponentCreatorRequestRepresentation newInstance(ContributionBinding binding,
                                                                       ComponentImplementation componentImplementation) {
        return new SubcomponentCreatorRequestRepresentation(binding, componentImplementation);
    }
}
