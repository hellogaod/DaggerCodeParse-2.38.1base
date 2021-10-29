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
public final class ComponentInstanceRequestRepresentation_Factory {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    public ComponentInstanceRequestRepresentation_Factory(
            Provider<ComponentImplementation> componentImplementationProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
    }

    public ComponentInstanceRequestRepresentation get(ContributionBinding binding) {
        return newInstance(binding, componentImplementationProvider.get());
    }

    public static ComponentInstanceRequestRepresentation_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider) {
        return new ComponentInstanceRequestRepresentation_Factory(componentImplementationProvider);
    }

    public static ComponentInstanceRequestRepresentation newInstance(ContributionBinding binding,
                                                                     ComponentImplementation componentImplementation) {
        return new ComponentInstanceRequestRepresentation(binding, componentImplementation);
    }
}
