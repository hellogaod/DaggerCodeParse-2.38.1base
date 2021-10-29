package dagger.internal.codegen.writing;


import java.util.Optional;

import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.BindingGraph;
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
public final class ComponentRequirementExpressions_Factory implements Factory<ComponentRequirementExpressions> {
    private final Provider<Optional<ComponentRequirementExpressions>> parentProvider;

    private final Provider<BindingGraph> graphProvider;

    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<ModuleProxies> moduleProxiesProvider;

    public ComponentRequirementExpressions_Factory(
            Provider<Optional<ComponentRequirementExpressions>> parentProvider,
            Provider<BindingGraph> graphProvider,
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<DaggerElements> elementsProvider, Provider<ModuleProxies> moduleProxiesProvider) {
        this.parentProvider = parentProvider;
        this.graphProvider = graphProvider;
        this.componentImplementationProvider = componentImplementationProvider;
        this.elementsProvider = elementsProvider;
        this.moduleProxiesProvider = moduleProxiesProvider;
    }

    @Override
    public ComponentRequirementExpressions get() {
        return newInstance(parentProvider.get(), graphProvider.get(), componentImplementationProvider.get(), elementsProvider.get(), moduleProxiesProvider.get());
    }

    public static ComponentRequirementExpressions_Factory create(
            Provider<Optional<ComponentRequirementExpressions>> parentProvider,
            Provider<BindingGraph> graphProvider,
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<DaggerElements> elementsProvider, Provider<ModuleProxies> moduleProxiesProvider) {
        return new ComponentRequirementExpressions_Factory(parentProvider, graphProvider, componentImplementationProvider, elementsProvider, moduleProxiesProvider);
    }

    public static ComponentRequirementExpressions newInstance(
            Optional<ComponentRequirementExpressions> parent, BindingGraph graph,
            ComponentImplementation componentImplementation, DaggerElements elements,
            ModuleProxies moduleProxies) {
        return new ComponentRequirementExpressions(parent, graph, componentImplementation, elements, moduleProxies);
    }
}
