package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: DependencyMethodProviderCreationExpression
 * Author: 佛学徒
 * Date: 2021/10/26 8:00
 * Description:
 * History:
 */
class DependencyMethodProviderCreationExpression {


//    private final ShardImplementation shardImplementation;
    private final ComponentRequirementExpressions componentRequirementExpressions;
    private final CompilerOptions compilerOptions;
    private final BindingGraph graph;
    private final ContributionBinding binding;

    @AssistedInject
    DependencyMethodProviderCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequirementExpressions componentRequirementExpressions,
            CompilerOptions compilerOptions,
            BindingGraph graph) {
        this.binding = checkNotNull(binding);
//        this.shardImplementation = componentImplementation.shardImplementation(binding);
        this.componentRequirementExpressions = componentRequirementExpressions;
        this.compilerOptions = compilerOptions;
        this.graph = graph;
    }

    @AssistedFactory
    static interface Factory {
        DependencyMethodProviderCreationExpression create(ContributionBinding binding);
    }
}
