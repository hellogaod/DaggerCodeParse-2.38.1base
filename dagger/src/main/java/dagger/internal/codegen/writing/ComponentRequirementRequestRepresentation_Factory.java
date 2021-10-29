package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.ComponentRequirement;
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
public final class ComponentRequirementRequestRepresentation_Factory {
    private final Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider;

    public ComponentRequirementRequestRepresentation_Factory(
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider) {
        this.componentRequirementExpressionsProvider = componentRequirementExpressionsProvider;
    }

    public ComponentRequirementRequestRepresentation get(ContributionBinding binding,
                                                         ComponentRequirement componentRequirement) {
        return newInstance(binding, componentRequirement, componentRequirementExpressionsProvider.get());
    }

    public static ComponentRequirementRequestRepresentation_Factory create(
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider) {
        return new ComponentRequirementRequestRepresentation_Factory(componentRequirementExpressionsProvider);
    }

    public static ComponentRequirementRequestRepresentation newInstance(ContributionBinding binding,
                                                                        ComponentRequirement componentRequirement,
                                                                        ComponentRequirementExpressions componentRequirementExpressions) {
        return new ComponentRequirementRequestRepresentation(binding, componentRequirement, componentRequirementExpressions);
    }
}
