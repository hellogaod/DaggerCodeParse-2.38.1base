package dagger.internal.codegen.bindinggraphvalidation;

import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class NullableBindingValidator_Factory implements Factory<NullableBindingValidator> {
    private final Provider<CompilerOptions> compilerOptionsProvider;

    public NullableBindingValidator_Factory(Provider<CompilerOptions> compilerOptionsProvider) {
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    @Override
    public NullableBindingValidator get() {
        return newInstance(compilerOptionsProvider.get());
    }

    public static NullableBindingValidator_Factory create(
            Provider<CompilerOptions> compilerOptionsProvider) {
        return new NullableBindingValidator_Factory(compilerOptionsProvider);
    }

    public static NullableBindingValidator newInstance(CompilerOptions compilerOptions) {
        return new NullableBindingValidator(compilerOptions);
    }
}
