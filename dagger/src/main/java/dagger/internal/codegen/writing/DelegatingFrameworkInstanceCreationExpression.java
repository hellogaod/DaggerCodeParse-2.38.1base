package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: DelegatingFrameworkInstanceCreationExpression
 * Author: 佛学徒
 * Date: 2021/10/26 7:59
 * Description:
 * History:
 */
class DelegatingFrameworkInstanceCreationExpression {

    private final ContributionBinding binding;
    private final ComponentImplementation componentImplementation;
    private final ComponentRequestRepresentations componentRequestRepresentations;

    @AssistedInject
    DelegatingFrameworkInstanceCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations,
            CompilerOptions compilerOptions) {
        this.binding = checkNotNull(binding);
        this.componentImplementation = componentImplementation;
        this.componentRequestRepresentations = componentRequestRepresentations;
    }


    @AssistedFactory
    static interface Factory {
        DelegatingFrameworkInstanceCreationExpression create(ContributionBinding binding);
    }
}
