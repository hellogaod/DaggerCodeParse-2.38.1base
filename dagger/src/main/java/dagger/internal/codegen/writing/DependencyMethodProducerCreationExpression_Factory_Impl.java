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
public final class DependencyMethodProducerCreationExpression_Factory_Impl implements DependencyMethodProducerCreationExpression.Factory {
    private final DependencyMethodProducerCreationExpression_Factory delegateFactory;

    DependencyMethodProducerCreationExpression_Factory_Impl(
            DependencyMethodProducerCreationExpression_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public DependencyMethodProducerCreationExpression create(ContributionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<DependencyMethodProducerCreationExpression.Factory> create(
            DependencyMethodProducerCreationExpression_Factory delegateFactory) {
        return InstanceFactory.create(new DependencyMethodProducerCreationExpression_Factory_Impl(delegateFactory));
    }
}
