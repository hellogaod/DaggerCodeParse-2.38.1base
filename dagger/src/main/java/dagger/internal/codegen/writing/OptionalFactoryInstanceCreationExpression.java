package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: OptionalFactoryInstanceCreationExpression
 * Author: 佛学徒
 * Date: 2021/10/26 8:03
 * Description:
 * History:
 */
class OptionalFactoryInstanceCreationExpression {
    private final OptionalFactories optionalFactories;
    private final ContributionBinding binding;
    private final ComponentImplementation componentImplementation;
    private final ComponentRequestRepresentations componentRequestRepresentations;

    @AssistedInject
    OptionalFactoryInstanceCreationExpression(
            @Assisted ContributionBinding binding,
            OptionalFactories optionalFactories,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations) {
        this.optionalFactories = optionalFactories;
        this.binding = binding;
        this.componentImplementation = componentImplementation;
        this.componentRequestRepresentations = componentRequestRepresentations;
    }


    @AssistedFactory
    static interface Factory {
        OptionalFactoryInstanceCreationExpression create(ContributionBinding binding);
    }
}
