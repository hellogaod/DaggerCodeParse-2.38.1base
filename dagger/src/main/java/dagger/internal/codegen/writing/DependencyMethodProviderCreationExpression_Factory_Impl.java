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
public final class DependencyMethodProviderCreationExpression_Factory_Impl implements DependencyMethodProviderCreationExpression.Factory {
    private final DependencyMethodProviderCreationExpression_Factory delegateFactory;

    DependencyMethodProviderCreationExpression_Factory_Impl(
            DependencyMethodProviderCreationExpression_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public DependencyMethodProviderCreationExpression create(ContributionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<DependencyMethodProviderCreationExpression.Factory> create(
            DependencyMethodProviderCreationExpression_Factory delegateFactory) {
        return InstanceFactory.create(new DependencyMethodProviderCreationExpression_Factory_Impl(delegateFactory));
    }
}
