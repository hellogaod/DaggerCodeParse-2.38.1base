package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ContributionBinding;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class SetFactoryCreationExpression_Factory {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    private final Provider<BindingGraph> graphProvider;

    public SetFactoryCreationExpression_Factory(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<BindingGraph> graphProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
        this.graphProvider = graphProvider;
    }

    public SetFactoryCreationExpression get(ContributionBinding binding) {
        return newInstance(binding, componentImplementationProvider.get(), componentRequestRepresentationsProvider.get(), graphProvider.get());
    }

    public static SetFactoryCreationExpression_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<BindingGraph> graphProvider) {
        return new SetFactoryCreationExpression_Factory(componentImplementationProvider, componentRequestRepresentationsProvider, graphProvider);
    }

    public static SetFactoryCreationExpression newInstance(ContributionBinding binding,
                                                           ComponentImplementation componentImplementation,
                                                           ComponentRequestRepresentations componentRequestRepresentations, BindingGraph graph) {
        return new SetFactoryCreationExpression(binding, componentImplementation, componentRequestRepresentations, graph);
    }
}
