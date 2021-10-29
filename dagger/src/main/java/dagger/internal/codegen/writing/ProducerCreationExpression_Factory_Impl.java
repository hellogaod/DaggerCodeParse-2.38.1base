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
public final class ProducerCreationExpression_Factory_Impl implements ProducerCreationExpression.Factory {
    private final ProducerCreationExpression_Factory delegateFactory;

    ProducerCreationExpression_Factory_Impl(ProducerCreationExpression_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public ProducerCreationExpression create(ContributionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<ProducerCreationExpression.Factory> create(
            ProducerCreationExpression_Factory delegateFactory) {
        return InstanceFactory.create(new ProducerCreationExpression_Factory_Impl(delegateFactory));
    }
}
