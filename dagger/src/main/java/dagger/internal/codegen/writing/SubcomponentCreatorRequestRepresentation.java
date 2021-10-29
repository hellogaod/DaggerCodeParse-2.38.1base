package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: SubcomponentCreatorRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 11:25
 * Description:
 * History:
 */
class SubcomponentCreatorRequestRepresentation {

//    private final ShardImplementation shardImplementation;
    private final ContributionBinding binding;

    @AssistedInject
    SubcomponentCreatorRequestRepresentation(
            @Assisted ContributionBinding binding, ComponentImplementation componentImplementation) {
//        super(binding);
        this.binding = binding;
//        this.shardImplementation = componentImplementation.shardImplementation(binding);
    }

    @AssistedFactory
    static interface Factory {
        SubcomponentCreatorRequestRepresentation create(ContributionBinding binding);
    }
}
