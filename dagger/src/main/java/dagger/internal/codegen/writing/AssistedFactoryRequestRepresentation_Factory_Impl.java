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
public final class AssistedFactoryRequestRepresentation_Factory_Impl implements AssistedFactoryRequestRepresentation.Factory {
    private final AssistedFactoryRequestRepresentation_Factory delegateFactory;

    AssistedFactoryRequestRepresentation_Factory_Impl(
            AssistedFactoryRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public AssistedFactoryRequestRepresentation create(ProvisionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<AssistedFactoryRequestRepresentation.Factory> create(
            AssistedFactoryRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new AssistedFactoryRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
