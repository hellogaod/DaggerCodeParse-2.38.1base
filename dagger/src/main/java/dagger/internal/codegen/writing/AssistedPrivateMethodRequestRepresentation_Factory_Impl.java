package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import dagger.internal.codegen.binding.BindingRequest;
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
public final class AssistedPrivateMethodRequestRepresentation_Factory_Impl implements AssistedPrivateMethodRequestRepresentation.Factory {
    private final AssistedPrivateMethodRequestRepresentation_Factory delegateFactory;

    AssistedPrivateMethodRequestRepresentation_Factory_Impl(
            AssistedPrivateMethodRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public AssistedPrivateMethodRequestRepresentation create(BindingRequest request,
                                                             ContributionBinding binding, RequestRepresentation wrappedRequestRepresentation) {
        return delegateFactory.get(request, binding, wrappedRequestRepresentation);
    }

    public static Provider<AssistedPrivateMethodRequestRepresentation.Factory> create(
            AssistedPrivateMethodRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new AssistedPrivateMethodRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
