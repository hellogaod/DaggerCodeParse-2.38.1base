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
public final class SetRequestRepresentation_Factory_Impl implements SetRequestRepresentation.Factory {
    private final SetRequestRepresentation_Factory delegateFactory;

    SetRequestRepresentation_Factory_Impl(SetRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public SetRequestRepresentation create(ProvisionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<SetRequestRepresentation.Factory> create(
            SetRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new SetRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
