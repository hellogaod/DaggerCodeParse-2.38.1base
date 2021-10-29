package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.langmodel.DaggerElements;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class MapFactoryCreationExpression_Factory {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    private final Provider<BindingGraph> graphProvider;

    private final Provider<DaggerElements> elementsProvider;

    public MapFactoryCreationExpression_Factory(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<BindingGraph> graphProvider, Provider<DaggerElements> elementsProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
        this.graphProvider = graphProvider;
        this.elementsProvider = elementsProvider;
    }

    public MapFactoryCreationExpression get(ContributionBinding binding) {
        return newInstance(binding, componentImplementationProvider.get(), componentRequestRepresentationsProvider.get(), graphProvider.get(), elementsProvider.get());
    }

    public static MapFactoryCreationExpression_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<BindingGraph> graphProvider, Provider<DaggerElements> elementsProvider) {
        return new MapFactoryCreationExpression_Factory(componentImplementationProvider, componentRequestRepresentationsProvider, graphProvider, elementsProvider);
    }

    public static MapFactoryCreationExpression newInstance(ContributionBinding binding,
                                                           ComponentImplementation componentImplementation,
                                                           ComponentRequestRepresentations componentRequestRepresentations, BindingGraph graph,
                                                           DaggerElements elements) {
        return new MapFactoryCreationExpression(binding, componentImplementation, componentRequestRepresentations, graph, elements);
    }
}
