package dagger.internal.codegen.bindinggraphvalidation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.KeyFactory;
import dagger.internal.codegen.compileroption.CompilerOptions;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class DependsOnProductionExecutorValidator_Factory implements Factory<DependsOnProductionExecutorValidator> {
    private final Provider<CompilerOptions> compilerOptionsProvider;

    private final Provider<KeyFactory> keyFactoryProvider;

    public DependsOnProductionExecutorValidator_Factory(
            Provider<CompilerOptions> compilerOptionsProvider, Provider<KeyFactory> keyFactoryProvider) {
        this.compilerOptionsProvider = compilerOptionsProvider;
        this.keyFactoryProvider = keyFactoryProvider;
    }

    @Override
    public DependsOnProductionExecutorValidator get() {
        return newInstance(compilerOptionsProvider.get(), keyFactoryProvider.get());
    }

    public static DependsOnProductionExecutorValidator_Factory create(
            Provider<CompilerOptions> compilerOptionsProvider, Provider<KeyFactory> keyFactoryProvider) {
        return new DependsOnProductionExecutorValidator_Factory(compilerOptionsProvider, keyFactoryProvider);
    }

    public static DependsOnProductionExecutorValidator newInstance(CompilerOptions compilerOptions,
                                                                   KeyFactory keyFactory) {
        return new DependsOnProductionExecutorValidator(compilerOptions, keyFactory);
    }
}
