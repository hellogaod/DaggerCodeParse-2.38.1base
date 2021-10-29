package dagger.internal.codegen.writing;


import java.util.Optional;

import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.BindingGraph;
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
public final class ComponentRequestRepresentations_Factory implements Factory<ComponentRequestRepresentations> {
    private final Provider<Optional<ComponentRequestRepresentations>> parentProvider;

    private final Provider<BindingGraph> graphProvider;

    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider;

    private final Provider<LegacyBindingRepresentation.Factory> legacyBindingRepresentationFactoryProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    public ComponentRequestRepresentations_Factory(
            Provider<Optional<ComponentRequestRepresentations>> parentProvider,
            Provider<BindingGraph> graphProvider,
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider,
            Provider<LegacyBindingRepresentation.Factory> legacyBindingRepresentationFactoryProvider,
            Provider<DaggerTypes> typesProvider, Provider<CompilerOptions> compilerOptionsProvider) {
        this.parentProvider = parentProvider;
        this.graphProvider = graphProvider;
        this.componentImplementationProvider = componentImplementationProvider;
        this.componentRequirementExpressionsProvider = componentRequirementExpressionsProvider;
        this.legacyBindingRepresentationFactoryProvider = legacyBindingRepresentationFactoryProvider;
        this.typesProvider = typesProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    @Override
    public ComponentRequestRepresentations get() {
        return newInstance(parentProvider.get(), graphProvider.get(), componentImplementationProvider.get(), componentRequirementExpressionsProvider.get(), legacyBindingRepresentationFactoryProvider.get(), typesProvider.get(), compilerOptionsProvider.get());
    }

    public static ComponentRequestRepresentations_Factory create(
            Provider<Optional<ComponentRequestRepresentations>> parentProvider,
            Provider<BindingGraph> graphProvider,
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider,
            Provider<LegacyBindingRepresentation.Factory> legacyBindingRepresentationFactoryProvider,
            Provider<DaggerTypes> typesProvider, Provider<CompilerOptions> compilerOptionsProvider) {
        return new ComponentRequestRepresentations_Factory(parentProvider, graphProvider, componentImplementationProvider, componentRequirementExpressionsProvider, legacyBindingRepresentationFactoryProvider, typesProvider, compilerOptionsProvider);
    }

    public static ComponentRequestRepresentations newInstance(
            Optional<ComponentRequestRepresentations> parent, BindingGraph graph,
            ComponentImplementation componentImplementation,
            ComponentRequirementExpressions componentRequirementExpressions,
            Object legacyBindingRepresentationFactory, DaggerTypes types,
            CompilerOptions compilerOptions) {
        return new ComponentRequestRepresentations(parent, graph, componentImplementation, componentRequirementExpressions, (LegacyBindingRepresentation.Factory) legacyBindingRepresentationFactory, types, compilerOptions);
    }
}
