package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.langmodel.DaggerElements;
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
public final class SetRequestRepresentation_Factory {
    private final Provider<BindingGraph> graphProvider;

    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    public SetRequestRepresentation_Factory(Provider<BindingGraph> graphProvider,
                                            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
                                            Provider<DaggerTypes> typesProvider, Provider<DaggerElements> elementsProvider) {
        this.graphProvider = graphProvider;
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
    }

    public SetRequestRepresentation get(ProvisionBinding binding) {
        return newInstance(binding, graphProvider.get(), componentRequestRepresentationsProvider.get(), typesProvider.get(), elementsProvider.get());
    }

    public static SetRequestRepresentation_Factory create(Provider<BindingGraph> graphProvider,
                                                          Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
                                                          Provider<DaggerTypes> typesProvider, Provider<DaggerElements> elementsProvider) {
        return new SetRequestRepresentation_Factory(graphProvider, componentRequestRepresentationsProvider, typesProvider, elementsProvider);
    }

    public static SetRequestRepresentation newInstance(ProvisionBinding binding, BindingGraph graph,
                                                       ComponentRequestRepresentations componentRequestRepresentations, DaggerTypes types,
                                                       DaggerElements elements) {
        return new SetRequestRepresentation(binding, graph, componentRequestRepresentations, types, elements);
    }
}
