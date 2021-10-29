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
public final class InjectionOrProvisionProviderCreationExpression_Factory_Impl implements InjectionOrProvisionProviderCreationExpression.Factory {
    private final InjectionOrProvisionProviderCreationExpression_Factory delegateFactory;

    InjectionOrProvisionProviderCreationExpression_Factory_Impl(
            InjectionOrProvisionProviderCreationExpression_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public InjectionOrProvisionProviderCreationExpression create(ContributionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<InjectionOrProvisionProviderCreationExpression.Factory> create(
            InjectionOrProvisionProviderCreationExpression_Factory delegateFactory) {
        return InstanceFactory.create(new InjectionOrProvisionProviderCreationExpression_Factory_Impl(delegateFactory));
    }
}
