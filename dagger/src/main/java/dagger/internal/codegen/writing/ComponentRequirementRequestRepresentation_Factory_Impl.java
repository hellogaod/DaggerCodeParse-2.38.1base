package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
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
public final class ComponentRequirementRequestRepresentation_Factory_Impl implements ComponentRequirementRequestRepresentation.Factory {
    private final ComponentRequirementRequestRepresentation_Factory delegateFactory;

    ComponentRequirementRequestRepresentation_Factory_Impl(
            ComponentRequirementRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public ComponentRequirementRequestRepresentation create(ContributionBinding binding,
                                                            ComponentRequirement componentRequirement) {
        return delegateFactory.get(binding, componentRequirement);
    }

    public static Provider<ComponentRequirementRequestRepresentation.Factory> create(
            ComponentRequirementRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new ComponentRequirementRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
