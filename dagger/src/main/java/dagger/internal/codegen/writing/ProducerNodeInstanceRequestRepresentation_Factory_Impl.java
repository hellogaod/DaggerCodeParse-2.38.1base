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
public final class ProducerNodeInstanceRequestRepresentation_Factory_Impl implements ProducerNodeInstanceRequestRepresentation.Factory {
    private final ProducerNodeInstanceRequestRepresentation_Factory delegateFactory;

    ProducerNodeInstanceRequestRepresentation_Factory_Impl(
            ProducerNodeInstanceRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public ProducerNodeInstanceRequestRepresentation create(ContributionBinding binding,
                                                            FrameworkInstanceSupplier frameworkInstanceSupplier) {
        return delegateFactory.get(binding, frameworkInstanceSupplier);
    }

    public static Provider<ProducerNodeInstanceRequestRepresentation.Factory> create(
            ProducerNodeInstanceRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new ProducerNodeInstanceRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
