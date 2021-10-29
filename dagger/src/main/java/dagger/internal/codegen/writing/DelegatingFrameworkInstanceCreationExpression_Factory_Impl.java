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
public final class DelegatingFrameworkInstanceCreationExpression_Factory_Impl implements DelegatingFrameworkInstanceCreationExpression.Factory {
    private final DelegatingFrameworkInstanceCreationExpression_Factory delegateFactory;

    DelegatingFrameworkInstanceCreationExpression_Factory_Impl(
            DelegatingFrameworkInstanceCreationExpression_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public DelegatingFrameworkInstanceCreationExpression create(ContributionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<DelegatingFrameworkInstanceCreationExpression.Factory> create(
            DelegatingFrameworkInstanceCreationExpression_Factory delegateFactory) {
        return InstanceFactory.create(new DelegatingFrameworkInstanceCreationExpression_Factory_Impl(delegateFactory));
    }
}
