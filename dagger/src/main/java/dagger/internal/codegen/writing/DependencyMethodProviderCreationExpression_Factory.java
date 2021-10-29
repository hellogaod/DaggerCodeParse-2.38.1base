package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.BindingGraph;
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
public final class DependencyMethodProviderCreationExpression_Factory {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    private final Provider<BindingGraph> graphProvider;

    public DependencyMethodProviderCreationExpression_Factory(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider,
            Provider<CompilerOptions> compilerOptionsProvider, Provider<BindingGraph> graphProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
        this.componentRequirementExpressionsProvider = componentRequirementExpressionsProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
        this.graphProvider = graphProvider;
    }

    public DependencyMethodProviderCreationExpression get(ContributionBinding binding) {
        return newInstance(binding, componentImplementationProvider.get(), componentRequirementExpressionsProvider.get(), compilerOptionsProvider.get(), graphProvider.get());
    }

    public static DependencyMethodProviderCreationExpression_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider,
            Provider<CompilerOptions> compilerOptionsProvider, Provider<BindingGraph> graphProvider) {
        return new DependencyMethodProviderCreationExpression_Factory(componentImplementationProvider, componentRequirementExpressionsProvider, compilerOptionsProvider, graphProvider);
    }

    public static DependencyMethodProviderCreationExpression newInstance(ContributionBinding binding,
                                                                         ComponentImplementation componentImplementation,
                                                                         ComponentRequirementExpressions componentRequirementExpressions,
                                                                         CompilerOptions compilerOptions, BindingGraph graph) {
        return new DependencyMethodProviderCreationExpression(binding, componentImplementation, componentRequirementExpressions, compilerOptions, graph);
    }
}
