package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.spi.model.RequestKind;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class DelegateRequestRepresentation_Factory_Impl implements DelegateRequestRepresentation.Factory {
    private final DelegateRequestRepresentation_Factory delegateFactory;

    DelegateRequestRepresentation_Factory_Impl(
            DelegateRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public DelegateRequestRepresentation create(ContributionBinding binding,
                                                RequestKind requestKind) {
        return delegateFactory.get(binding, requestKind);
    }

    public static Provider<DelegateRequestRepresentation.Factory> create(
            DelegateRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new DelegateRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
