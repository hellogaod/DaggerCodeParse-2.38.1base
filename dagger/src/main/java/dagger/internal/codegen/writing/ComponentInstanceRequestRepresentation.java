package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ComponentInstanceRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 11:15
 * Description:
 * History:
 */
class ComponentInstanceRequestRepresentation {

    private final ComponentImplementation componentImplementation;
    private final ContributionBinding binding;

    @AssistedInject
    ComponentInstanceRequestRepresentation(
            @Assisted ContributionBinding binding, ComponentImplementation componentImplementation) {

        this.componentImplementation = componentImplementation;
        this.binding = binding;
    }

    @AssistedFactory
    static interface Factory {
        ComponentInstanceRequestRepresentation create(ContributionBinding binding);
    }
}
