package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ComponentRequirement;
import dagger.internal.codegen.binding.ContributionBinding;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ComponentRequirementRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 11:20
 * Description:
 * History:
 */
class ComponentRequirementRequestRepresentation {
    private final ComponentRequirement componentRequirement;
    private final ComponentRequirementExpressions componentRequirementExpressions;

    @AssistedInject
    ComponentRequirementRequestRepresentation(
            @Assisted ContributionBinding binding,
            @Assisted ComponentRequirement componentRequirement,
            ComponentRequirementExpressions componentRequirementExpressions) {
//        super(binding);
        this.componentRequirement = checkNotNull(componentRequirement);
        this.componentRequirementExpressions = componentRequirementExpressions;
    }


    @AssistedFactory
    static interface Factory {
        ComponentRequirementRequestRepresentation create(
                ContributionBinding binding, ComponentRequirement componentRequirement);
    }
}
