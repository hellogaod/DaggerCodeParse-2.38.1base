package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: InjectionOrProvisionProviderCreationExpression
 * Author: 佛学徒
 * Date: 2021/10/26 8:01
 * Description:
 * History:
 */
class InjectionOrProvisionProviderCreationExpression {

    private final ContributionBinding binding;
//    private final ShardImplementation shardImplementation;
    private final ComponentRequestRepresentations componentRequestRepresentations;

    @AssistedInject
    InjectionOrProvisionProviderCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations) {
        this.binding = checkNotNull(binding);
//        this.shardImplementation = componentImplementation.shardImplementation(binding);
        this.componentRequestRepresentations = componentRequestRepresentations;
    }

    @AssistedFactory
    static interface Factory {
        InjectionOrProvisionProviderCreationExpression create(ContributionBinding binding);
    }
}
