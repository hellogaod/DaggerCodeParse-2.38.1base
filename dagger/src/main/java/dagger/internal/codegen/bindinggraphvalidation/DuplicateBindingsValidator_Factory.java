package dagger.internal.codegen.bindinggraphvalidation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.BindingDeclarationFormatter;
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
public final class DuplicateBindingsValidator_Factory implements Factory<DuplicateBindingsValidator> {
    private final Provider<BindingDeclarationFormatter> bindingDeclarationFormatterProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    public DuplicateBindingsValidator_Factory(
            Provider<BindingDeclarationFormatter> bindingDeclarationFormatterProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        this.bindingDeclarationFormatterProvider = bindingDeclarationFormatterProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    @Override
    public DuplicateBindingsValidator get() {
        return newInstance(bindingDeclarationFormatterProvider.get(), compilerOptionsProvider.get());
    }

    public static DuplicateBindingsValidator_Factory create(
            Provider<BindingDeclarationFormatter> bindingDeclarationFormatterProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        return new DuplicateBindingsValidator_Factory(bindingDeclarationFormatterProvider, compilerOptionsProvider);
    }

    public static DuplicateBindingsValidator newInstance(
            BindingDeclarationFormatter bindingDeclarationFormatter, CompilerOptions compilerOptions) {
        return new DuplicateBindingsValidator(bindingDeclarationFormatter, compilerOptions);
    }
}
