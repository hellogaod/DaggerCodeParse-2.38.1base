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
public final class AnonymousProviderCreationExpression_Factory_Impl implements AnonymousProviderCreationExpression.Factory {
    private final AnonymousProviderCreationExpression_Factory delegateFactory;

    AnonymousProviderCreationExpression_Factory_Impl(
            AnonymousProviderCreationExpression_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public AnonymousProviderCreationExpression create(ContributionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<AnonymousProviderCreationExpression.Factory> create(
            AnonymousProviderCreationExpression_Factory delegateFactory) {
        return InstanceFactory.create(new AnonymousProviderCreationExpression_Factory_Impl(delegateFactory));
    }
}
