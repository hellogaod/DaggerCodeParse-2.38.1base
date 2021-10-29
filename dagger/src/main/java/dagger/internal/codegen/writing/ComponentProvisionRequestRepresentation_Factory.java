package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ProvisionBinding;
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
public final class ComponentProvisionRequestRepresentation_Factory {
    private final Provider<BindingGraph> bindingGraphProvider;

    private final Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    public ComponentProvisionRequestRepresentation_Factory(
            Provider<BindingGraph> bindingGraphProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        this.bindingGraphProvider = bindingGraphProvider;
        this.componentRequirementExpressionsProvider = componentRequirementExpressionsProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    public ComponentProvisionRequestRepresentation get(ProvisionBinding binding) {
        return newInstance(binding, bindingGraphProvider.get(), componentRequirementExpressionsProvider.get(), compilerOptionsProvider.get());
    }

    public static ComponentProvisionRequestRepresentation_Factory create(
            Provider<BindingGraph> bindingGraphProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        return new ComponentProvisionRequestRepresentation_Factory(bindingGraphProvider, componentRequirementExpressionsProvider, compilerOptionsProvider);
    }

    public static ComponentProvisionRequestRepresentation newInstance(ProvisionBinding binding,
                                                                      BindingGraph bindingGraph, ComponentRequirementExpressions componentRequirementExpressions,
                                                                      CompilerOptions compilerOptions) {
        return new ComponentProvisionRequestRepresentation(binding, bindingGraph, componentRequirementExpressions, compilerOptions);
    }
}
