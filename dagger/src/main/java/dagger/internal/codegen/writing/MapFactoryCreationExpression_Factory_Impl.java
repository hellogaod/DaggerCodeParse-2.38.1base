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
public final class MapFactoryCreationExpression_Factory_Impl implements MapFactoryCreationExpression.Factory {
    private final MapFactoryCreationExpression_Factory delegateFactory;

    MapFactoryCreationExpression_Factory_Impl(MapFactoryCreationExpression_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public MapFactoryCreationExpression create(ContributionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<MapFactoryCreationExpression.Factory> create(
            MapFactoryCreationExpression_Factory delegateFactory) {
        return InstanceFactory.create(new MapFactoryCreationExpression_Factory_Impl(delegateFactory));
    }
}
