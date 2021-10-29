package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
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
public final class ComponentInstanceRequestRepresentation_Factory_Impl implements ComponentInstanceRequestRepresentation.Factory {
    private final ComponentInstanceRequestRepresentation_Factory delegateFactory;

    ComponentInstanceRequestRepresentation_Factory_Impl(
            ComponentInstanceRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public ComponentInstanceRequestRepresentation create(ContributionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<ComponentInstanceRequestRepresentation.Factory> create(
            ComponentInstanceRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new ComponentInstanceRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
