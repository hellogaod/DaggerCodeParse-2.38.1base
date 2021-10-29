package dagger.internal.codegen.validation;


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
public final class ComponentHierarchyValidator_Factory implements Factory<ComponentHierarchyValidator> {
    private final Provider<CompilerOptions> compilerOptionsProvider;

    public ComponentHierarchyValidator_Factory(Provider<CompilerOptions> compilerOptionsProvider) {
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    @Override
    public ComponentHierarchyValidator get() {
        return newInstance(compilerOptionsProvider.get());
    }

    public static ComponentHierarchyValidator_Factory create(
            Provider<CompilerOptions> compilerOptionsProvider) {
        return new ComponentHierarchyValidator_Factory(compilerOptionsProvider);
    }

    public static ComponentHierarchyValidator newInstance(CompilerOptions compilerOptions) {
        return new ComponentHierarchyValidator(compilerOptions);
    }
}
