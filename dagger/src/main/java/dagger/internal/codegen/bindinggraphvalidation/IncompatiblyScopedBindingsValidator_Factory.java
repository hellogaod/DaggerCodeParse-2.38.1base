package dagger.internal.codegen.bindinggraphvalidation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.MethodSignatureFormatter;
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
public final class IncompatiblyScopedBindingsValidator_Factory implements Factory<IncompatiblyScopedBindingsValidator> {
    private final Provider<MethodSignatureFormatter> methodSignatureFormatterProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    public IncompatiblyScopedBindingsValidator_Factory(
            Provider<MethodSignatureFormatter> methodSignatureFormatterProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        this.methodSignatureFormatterProvider = methodSignatureFormatterProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    @Override
    public IncompatiblyScopedBindingsValidator get() {
        return newInstance(methodSignatureFormatterProvider.get(), compilerOptionsProvider.get());
    }

    public static IncompatiblyScopedBindingsValidator_Factory create(
            Provider<MethodSignatureFormatter> methodSignatureFormatterProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        return new IncompatiblyScopedBindingsValidator_Factory(methodSignatureFormatterProvider, compilerOptionsProvider);
    }

    public static IncompatiblyScopedBindingsValidator newInstance(
            MethodSignatureFormatter methodSignatureFormatter, CompilerOptions compilerOptions) {
        return new IncompatiblyScopedBindingsValidator(methodSignatureFormatter, compilerOptions);
    }
}
