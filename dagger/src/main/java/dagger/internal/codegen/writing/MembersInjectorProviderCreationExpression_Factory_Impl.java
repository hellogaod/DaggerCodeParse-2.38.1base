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
public final class MembersInjectorProviderCreationExpression_Factory_Impl implements MembersInjectorProviderCreationExpression.Factory {
    private final MembersInjectorProviderCreationExpression_Factory delegateFactory;

    MembersInjectorProviderCreationExpression_Factory_Impl(
            MembersInjectorProviderCreationExpression_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public MembersInjectorProviderCreationExpression create(ProvisionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<MembersInjectorProviderCreationExpression.Factory> create(
            MembersInjectorProviderCreationExpression_Factory delegateFactory) {
        return InstanceFactory.create(new MembersInjectorProviderCreationExpression_Factory_Impl(delegateFactory));
    }
}
