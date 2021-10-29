package dagger.internal.codegen.binding;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class OptionalBindingDeclaration_Factory_Factory implements Factory<OptionalBindingDeclaration.Factory> {
    private final Provider<KeyFactory> keyFactoryProvider;

    public OptionalBindingDeclaration_Factory_Factory(Provider<KeyFactory> keyFactoryProvider) {
        this.keyFactoryProvider = keyFactoryProvider;
    }

    @Override
    public OptionalBindingDeclaration.Factory get() {
        return newInstance(keyFactoryProvider.get());
    }

    public static OptionalBindingDeclaration_Factory_Factory create(
            Provider<KeyFactory> keyFactoryProvider) {
        return new OptionalBindingDeclaration_Factory_Factory(keyFactoryProvider);
    }

    public static OptionalBindingDeclaration.Factory newInstance(KeyFactory keyFactory) {
        return new OptionalBindingDeclaration.Factory(keyFactory);
    }
}
