package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ContributionBinding;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: SetFactoryCreationExpression
 * Author: 佛学徒
 * Date: 2021/10/26 8:04
 * Description:
 * History:
 */
class SetFactoryCreationExpression {

    private final BindingGraph graph;
    private final ContributionBinding binding;

    @AssistedInject
    SetFactoryCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations,
            BindingGraph graph) {
//        super(binding, componentImplementation, componentRequestRepresentations);
        this.binding = checkNotNull(binding);
        this.graph = graph;
    }

    @AssistedFactory
    static interface Factory {
        SetFactoryCreationExpression create(ContributionBinding binding);
    }
}
