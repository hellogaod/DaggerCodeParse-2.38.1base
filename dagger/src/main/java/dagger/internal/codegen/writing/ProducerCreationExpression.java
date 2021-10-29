package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ProducerCreationExpression
 * Author: 佛学徒
 * Date: 2021/10/26 8:04
 * Description:
 * History:
 */
class ProducerCreationExpression {


//    private final ShardImplementation shardImplementation;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final ContributionBinding binding;

    @AssistedInject
    ProducerCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations) {
        this.binding = checkNotNull(binding);
//        this.shardImplementation = componentImplementation.shardImplementation(binding);
        this.componentRequestRepresentations = checkNotNull(componentRequestRepresentations);
    }

    @AssistedFactory
    static interface Factory {
        ProducerCreationExpression create(ContributionBinding binding);
    }
}
