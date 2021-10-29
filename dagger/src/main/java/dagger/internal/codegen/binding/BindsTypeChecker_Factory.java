package dagger.internal.codegen.binding;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class BindsTypeChecker_Factory implements Factory<BindsTypeChecker> {
    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    public BindsTypeChecker_Factory(Provider<DaggerTypes> typesProvider,
                                    Provider<DaggerElements> elementsProvider) {
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
    }

    @Override
    public BindsTypeChecker get() {
        return newInstance(typesProvider.get(), elementsProvider.get());
    }

    public static BindsTypeChecker_Factory create(Provider<DaggerTypes> typesProvider,
                                                  Provider<DaggerElements> elementsProvider) {
        return new BindsTypeChecker_Factory(typesProvider, elementsProvider);
    }

    public static BindsTypeChecker newInstance(DaggerTypes types, DaggerElements elements) {
        return new BindsTypeChecker(types, elements);
    }
}
