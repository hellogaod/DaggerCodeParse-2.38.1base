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
public final class SetFactoryCreationExpression_Factory_Impl implements SetFactoryCreationExpression.Factory {
    private final SetFactoryCreationExpression_Factory delegateFactory;

    SetFactoryCreationExpression_Factory_Impl(SetFactoryCreationExpression_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public SetFactoryCreationExpression create(ContributionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<SetFactoryCreationExpression.Factory> create(
            SetFactoryCreationExpression_Factory delegateFactory) {
        return InstanceFactory.create(new SetFactoryCreationExpression_Factory_Impl(delegateFactory));
    }
}
