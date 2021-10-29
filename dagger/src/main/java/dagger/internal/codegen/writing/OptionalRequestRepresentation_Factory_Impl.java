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
public final class OptionalRequestRepresentation_Factory_Impl implements OptionalRequestRepresentation.Factory {
    private final OptionalRequestRepresentation_Factory delegateFactory;

    OptionalRequestRepresentation_Factory_Impl(
            OptionalRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public OptionalRequestRepresentation create(ProvisionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<OptionalRequestRepresentation.Factory> create(
            OptionalRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new OptionalRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
