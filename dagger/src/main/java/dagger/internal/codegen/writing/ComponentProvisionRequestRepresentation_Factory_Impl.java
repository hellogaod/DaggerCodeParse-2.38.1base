package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
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
public final class ComponentProvisionRequestRepresentation_Factory_Impl implements ComponentProvisionRequestRepresentation.Factory {
    private final ComponentProvisionRequestRepresentation_Factory delegateFactory;

    ComponentProvisionRequestRepresentation_Factory_Impl(
            ComponentProvisionRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public ComponentProvisionRequestRepresentation create(ProvisionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<ComponentProvisionRequestRepresentation.Factory> create(
            ComponentProvisionRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new ComponentProvisionRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
