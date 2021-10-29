package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ProducerFromProviderCreationExpression
 * Author: 佛学徒
 * Date: 2021/10/25 11:26
 * Description:
 * History:
 */
class ProducerFromProviderCreationExpression {

    private final ContributionBinding binding;
    private final ComponentImplementation componentImplementation;
    private final ComponentRequestRepresentations componentRequestRepresentations;

    @AssistedInject
    ProducerFromProviderCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations) {
        this.binding = checkNotNull(binding);
        this.componentImplementation = componentImplementation;
        this.componentRequestRepresentations = componentRequestRepresentations;
    }

    @AssistedFactory
    static interface Factory {
        ProducerFromProviderCreationExpression create(ContributionBinding binding);
    }
}

