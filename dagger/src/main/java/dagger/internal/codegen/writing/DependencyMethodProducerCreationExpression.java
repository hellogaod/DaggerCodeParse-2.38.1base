package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ContributionBinding;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: DependencyMethodProducerCreationExpression
 * Author: 佛学徒
 * Date: 2021/10/26 8:00
 * Description:
 * History:
 */
class DependencyMethodProducerCreationExpression {
    private final ContributionBinding binding;
    private final ComponentImplementation componentImplementation;
    private final ComponentRequirementExpressions componentRequirementExpressions;
    private final BindingGraph graph;

    @AssistedInject
    DependencyMethodProducerCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequirementExpressions componentRequirementExpressions,
            BindingGraph graph) {
        this.binding = checkNotNull(binding);
        this.componentImplementation = componentImplementation;
        this.componentRequirementExpressions = componentRequirementExpressions;
        this.graph = graph;
    }

    @AssistedFactory
    static interface Factory {
        DependencyMethodProducerCreationExpression create(ContributionBinding binding);
    }
}
