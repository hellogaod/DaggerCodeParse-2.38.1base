package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: SetRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 11:23
 * Description:
 * History:
 */
class SetRequestRepresentation {

    private final ProvisionBinding binding;
    private final BindingGraph graph;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerTypes types;
    private final DaggerElements elements;

    @AssistedInject
    SetRequestRepresentation(
            @Assisted ProvisionBinding binding,
            BindingGraph graph,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types,
            DaggerElements elements) {
//        super(binding);
        this.binding = binding;
        this.graph = graph;
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.types = types;
        this.elements = elements;
    }


    @AssistedFactory
    static interface Factory {
        SetRequestRepresentation create(ProvisionBinding binding);
    }
}
