package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ComponentProvisionRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 11:18
 * Description:
 * History:
 */
class ComponentProvisionRequestRepresentation {

    private final ProvisionBinding binding;
    private final BindingGraph bindingGraph;
    private final ComponentRequirementExpressions componentRequirementExpressions;
    private final CompilerOptions compilerOptions;

    @AssistedInject
    ComponentProvisionRequestRepresentation(
            @Assisted ProvisionBinding binding,
            BindingGraph bindingGraph,
            ComponentRequirementExpressions componentRequirementExpressions,
            CompilerOptions compilerOptions) {
//        super(binding);
        this.binding = binding;
        this.bindingGraph = bindingGraph;
        this.componentRequirementExpressions = componentRequirementExpressions;
        this.compilerOptions = compilerOptions;
    }

    @AssistedFactory
    static interface Factory {
        ComponentProvisionRequestRepresentation create(ProvisionBinding binding);
    }
}
