package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
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
public final class PrivateMethodRequestRepresentation_Factory {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    public PrivateMethodRequestRepresentation_Factory(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<DaggerTypes> typesProvider, Provider<CompilerOptions> compilerOptionsProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
        this.typesProvider = typesProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    public PrivateMethodRequestRepresentation get(BindingRequest request, ContributionBinding binding,
                                                  RequestRepresentation wrappedRequestRepresentation) {
        return newInstance(request, binding, wrappedRequestRepresentation, componentImplementationProvider.get(), typesProvider.get(), compilerOptionsProvider.get());
    }

    public static PrivateMethodRequestRepresentation_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<DaggerTypes> typesProvider, Provider<CompilerOptions> compilerOptionsProvider) {
        return new PrivateMethodRequestRepresentation_Factory(componentImplementationProvider, typesProvider, compilerOptionsProvider);
    }

    public static PrivateMethodRequestRepresentation newInstance(BindingRequest request,
                                                                 ContributionBinding binding, Object wrappedRequestRepresentation,
                                                                 ComponentImplementation componentImplementation, DaggerTypes types,
                                                                 CompilerOptions compilerOptions) {
        return new PrivateMethodRequestRepresentation(request, binding, (RequestRepresentation) wrappedRequestRepresentation, componentImplementation, types, compilerOptions);
    }
}
