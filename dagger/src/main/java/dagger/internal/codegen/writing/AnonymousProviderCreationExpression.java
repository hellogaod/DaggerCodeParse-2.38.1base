package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: AnonymousProviderCreationExpression
 * Author: 佛学徒
 * Date: 2021/10/26 7:58
 * Description:
 * History:
 */
class AnonymousProviderCreationExpression {
    private final ContributionBinding binding;
    private final ComponentRequestRepresentations componentRequestRepresentations;
//    private final ClassName requestingClass;

    @AssistedInject
    AnonymousProviderCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentRequestRepresentations componentRequestRepresentations,
            ComponentImplementation componentImplementation) {
        this.binding = checkNotNull(binding);
        this.componentRequestRepresentations = componentRequestRepresentations;
//        this.requestingClass = componentImplementation.name();
    }


    @AssistedFactory
    static interface Factory {
        AnonymousProviderCreationExpression create(ContributionBinding binding);
    }
}
