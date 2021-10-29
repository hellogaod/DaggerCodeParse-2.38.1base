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
public final class OptionalFactoryInstanceCreationExpression_Factory_Impl implements OptionalFactoryInstanceCreationExpression.Factory {
    private final OptionalFactoryInstanceCreationExpression_Factory delegateFactory;

    OptionalFactoryInstanceCreationExpression_Factory_Impl(
            OptionalFactoryInstanceCreationExpression_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public OptionalFactoryInstanceCreationExpression create(ContributionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<OptionalFactoryInstanceCreationExpression.Factory> create(
            OptionalFactoryInstanceCreationExpression_Factory delegateFactory) {
        return InstanceFactory.create(new OptionalFactoryInstanceCreationExpression_Factory_Impl(delegateFactory));
    }
}
