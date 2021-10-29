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
public final class ProviderInstanceRequestRepresentation_Factory_Impl implements ProviderInstanceRequestRepresentation.Factory {
    private final ProviderInstanceRequestRepresentation_Factory delegateFactory;

    ProviderInstanceRequestRepresentation_Factory_Impl(
            ProviderInstanceRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public ProviderInstanceRequestRepresentation create(ContributionBinding binding,
                                                        FrameworkInstanceSupplier frameworkInstanceSupplier) {
        return delegateFactory.get(binding, frameworkInstanceSupplier);
    }

    public static Provider<ProviderInstanceRequestRepresentation.Factory> create(
            ProviderInstanceRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new ProviderInstanceRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
