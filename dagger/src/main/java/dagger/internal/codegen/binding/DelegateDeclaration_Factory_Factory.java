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
public final class DelegateDeclaration_Factory_Factory implements Factory<DelegateDeclaration.Factory> {
    private final Provider<DaggerTypes> typesProvider;

    private final Provider<KeyFactory> keyFactoryProvider;

    private final Provider<DependencyRequestFactory> dependencyRequestFactoryProvider;

    public DelegateDeclaration_Factory_Factory(Provider<DaggerTypes> typesProvider,
                                               Provider<KeyFactory> keyFactoryProvider,
                                               Provider<DependencyRequestFactory> dependencyRequestFactoryProvider) {
        this.typesProvider = typesProvider;
        this.keyFactoryProvider = keyFactoryProvider;
        this.dependencyRequestFactoryProvider = dependencyRequestFactoryProvider;
    }

    @Override
    public DelegateDeclaration.Factory get() {
        return newInstance(typesProvider.get(), keyFactoryProvider.get(), dependencyRequestFactoryProvider.get());
    }

    public static DelegateDeclaration_Factory_Factory create(Provider<DaggerTypes> typesProvider,
                                                             Provider<KeyFactory> keyFactoryProvider,
                                                             Provider<DependencyRequestFactory> dependencyRequestFactoryProvider) {
        return new DelegateDeclaration_Factory_Factory(typesProvider, keyFactoryProvider, dependencyRequestFactoryProvider);
    }

    public static DelegateDeclaration.Factory newInstance(DaggerTypes types, KeyFactory keyFactory,
                                                          DependencyRequestFactory dependencyRequestFactory) {
        return new DelegateDeclaration.Factory(types, keyFactory, dependencyRequestFactory);
    }
}
