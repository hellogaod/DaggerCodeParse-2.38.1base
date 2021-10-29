package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.langmodel.DaggerElements;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: MapFactoryCreationExpression
 * Author: 佛学徒
 * Date: 2021/10/26 8:02
 * Description:
 * History:
 */
class MapFactoryCreationExpression {


    private final ComponentImplementation componentImplementation;
    private final BindingGraph graph;
    private final ContributionBinding binding;
    private final DaggerElements elements;

    @AssistedInject
    MapFactoryCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations,
            BindingGraph graph,
            DaggerElements elements) {
//        super(binding, componentImplementation, componentRequestRepresentations);
        this.binding = checkNotNull(binding);
        this.componentImplementation = componentImplementation;
        this.graph = graph;
        this.elements = elements;
    }

    @AssistedFactory
    static interface Factory {
        MapFactoryCreationExpression create(ContributionBinding binding);
    }
}
