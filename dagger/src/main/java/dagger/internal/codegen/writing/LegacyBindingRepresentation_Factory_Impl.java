package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import dagger.internal.codegen.binding.Binding;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class LegacyBindingRepresentation_Factory_Impl implements LegacyBindingRepresentation.Factory {
    private final LegacyBindingRepresentation_Factory delegateFactory;

    LegacyBindingRepresentation_Factory_Impl(LegacyBindingRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public LegacyBindingRepresentation create(boolean isFastInit, Binding binding,
                                              SwitchingProviders switchingProviders) {
        return delegateFactory.get(isFastInit, binding, switchingProviders);
    }

    public static Provider<LegacyBindingRepresentation.Factory> create(
            LegacyBindingRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new LegacyBindingRepresentation_Factory_Impl(delegateFactory));
    }
}
