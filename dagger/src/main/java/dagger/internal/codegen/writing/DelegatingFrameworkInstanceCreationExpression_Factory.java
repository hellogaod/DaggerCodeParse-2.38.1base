package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.ContributionBinding;
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
public final class DelegatingFrameworkInstanceCreationExpression_Factory {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    public DelegatingFrameworkInstanceCreationExpression_Factory(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    public DelegatingFrameworkInstanceCreationExpression get(ContributionBinding binding) {
        return newInstance(binding, componentImplementationProvider.get(), componentRequestRepresentationsProvider.get(), compilerOptionsProvider.get());
    }

    public static DelegatingFrameworkInstanceCreationExpression_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        return new DelegatingFrameworkInstanceCreationExpression_Factory(componentImplementationProvider, componentRequestRepresentationsProvider, compilerOptionsProvider);
    }

    public static DelegatingFrameworkInstanceCreationExpression newInstance(
            ContributionBinding binding, ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations,
            CompilerOptions compilerOptions) {
        return new DelegatingFrameworkInstanceCreationExpression(binding, componentImplementation, componentRequestRepresentations, compilerOptions);
    }
}
