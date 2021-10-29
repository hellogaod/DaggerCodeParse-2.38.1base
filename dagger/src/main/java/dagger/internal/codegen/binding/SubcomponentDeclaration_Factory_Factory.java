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
public final class SubcomponentDeclaration_Factory_Factory implements Factory<SubcomponentDeclaration.Factory> {
    private final Provider<KeyFactory> keyFactoryProvider;

    public SubcomponentDeclaration_Factory_Factory(Provider<KeyFactory> keyFactoryProvider) {
        this.keyFactoryProvider = keyFactoryProvider;
    }

    @Override
    public SubcomponentDeclaration.Factory get() {
        return newInstance(keyFactoryProvider.get());
    }

    public static SubcomponentDeclaration_Factory_Factory create(
            Provider<KeyFactory> keyFactoryProvider) {
        return new SubcomponentDeclaration_Factory_Factory(keyFactoryProvider);
    }

    public static SubcomponentDeclaration.Factory newInstance(KeyFactory keyFactory) {
        return new SubcomponentDeclaration.Factory(keyFactory);
    }
}
