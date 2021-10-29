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
public final class ProducerFromProviderCreationExpression_Factory_Impl implements ProducerFromProviderCreationExpression.Factory {
    private final ProducerFromProviderCreationExpression_Factory delegateFactory;

    ProducerFromProviderCreationExpression_Factory_Impl(
            ProducerFromProviderCreationExpression_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public ProducerFromProviderCreationExpression create(ContributionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<ProducerFromProviderCreationExpression.Factory> create(
            ProducerFromProviderCreationExpression_Factory delegateFactory) {
        return InstanceFactory.create(new ProducerFromProviderCreationExpression_Factory_Impl(delegateFactory));
    }
}
