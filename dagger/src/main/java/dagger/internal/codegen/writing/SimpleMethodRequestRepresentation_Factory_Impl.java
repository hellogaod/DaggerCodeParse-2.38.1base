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
public final class SimpleMethodRequestRepresentation_Factory_Impl implements SimpleMethodRequestRepresentation.Factory {
    private final SimpleMethodRequestRepresentation_Factory delegateFactory;

    SimpleMethodRequestRepresentation_Factory_Impl(
            SimpleMethodRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public SimpleMethodRequestRepresentation create(ProvisionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<SimpleMethodRequestRepresentation.Factory> create(
            SimpleMethodRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new SimpleMethodRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
