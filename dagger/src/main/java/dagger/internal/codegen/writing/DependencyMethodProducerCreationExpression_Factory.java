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
public final class DependencyMethodProducerCreationExpression_Factory {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider;

    private final Provider<BindingGraph> graphProvider;

    public DependencyMethodProducerCreationExpression_Factory(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider,
            Provider<BindingGraph> graphProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
        this.componentRequirementExpressionsProvider = componentRequirementExpressionsProvider;
        this.graphProvider = graphProvider;
    }

    public DependencyMethodProducerCreationExpression get(ContributionBinding binding) {
        return newInstance(binding, componentImplementationProvider.get(), componentRequirementExpressionsProvider.get(), graphProvider.get());
    }

    public static DependencyMethodProducerCreationExpression_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider,
            Provider<BindingGraph> graphProvider) {
        return new DependencyMethodProducerCreationExpression_Factory(componentImplementationProvider, componentRequirementExpressionsProvider, graphProvider);
    }

    public static DependencyMethodProducerCreationExpression newInstance(ContributionBinding binding,
                                                                         ComponentImplementation componentImplementation,
                                                                         ComponentRequirementExpressions componentRequirementExpressions, BindingGraph graph) {
        return new DependencyMethodProducerCreationExpression(binding, componentImplementation, componentRequirementExpressions, graph);
    }
}
