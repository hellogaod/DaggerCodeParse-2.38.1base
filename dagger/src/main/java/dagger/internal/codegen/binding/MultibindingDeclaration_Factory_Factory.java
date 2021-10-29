package dagger.internal.codegen.binding;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MultibindingDeclaration_Factory_Factory implements Factory<MultibindingDeclaration.Factory> {
    private final Provider<DaggerTypes> typesProvider;

    private final Provider<KeyFactory> keyFactoryProvider;

    public MultibindingDeclaration_Factory_Factory(Provider<DaggerTypes> typesProvider,
                                                   Provider<KeyFactory> keyFactoryProvider) {
        this.typesProvider = typesProvider;
        this.keyFactoryProvider = keyFactoryProvider;
    }

    @Override
    public MultibindingDeclaration.Factory get() {
        return newInstance(typesProvider.get(), keyFactoryProvider.get());
    }

    public static MultibindingDeclaration_Factory_Factory create(Provider<DaggerTypes> typesProvider,
                                                                 Provider<KeyFactory> keyFactoryProvider) {
        return new MultibindingDeclaration_Factory_Factory(typesProvider, keyFactoryProvider);
    }

    public static MultibindingDeclaration.Factory newInstance(DaggerTypes types,
                                                              KeyFactory keyFactory) {
        return new MultibindingDeclaration.Factory(types, keyFactory);
    }
}
