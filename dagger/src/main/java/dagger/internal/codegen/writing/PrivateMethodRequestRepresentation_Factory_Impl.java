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
public final class PrivateMethodRequestRepresentation_Factory_Impl implements PrivateMethodRequestRepresentation.Factory {
    private final PrivateMethodRequestRepresentation_Factory delegateFactory;

    PrivateMethodRequestRepresentation_Factory_Impl(
            PrivateMethodRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public PrivateMethodRequestRepresentation create(BindingRequest request,
                                                     ContributionBinding binding, RequestRepresentation wrappedRequestRepresentation) {
        return delegateFactory.get(request, binding, wrappedRequestRepresentation);
    }

    public static Provider<PrivateMethodRequestRepresentation.Factory> create(
            PrivateMethodRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new PrivateMethodRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
