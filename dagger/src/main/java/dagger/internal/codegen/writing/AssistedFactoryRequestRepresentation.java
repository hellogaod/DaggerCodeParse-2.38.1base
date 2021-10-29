package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: AssistedFactoryRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 11:15
 * Description:
 * History:
 */
class AssistedFactoryRequestRepresentation {

    private final ProvisionBinding binding;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerElements elements;
    private final DaggerTypes types;

    @AssistedInject
    AssistedFactoryRequestRepresentation(
            @Assisted ProvisionBinding binding,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types,
            DaggerElements elements) {
//        super(binding);
        this.binding = checkNotNull(binding);
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.elements = elements;
        this.types = types;
    }

    @AssistedFactory
    static interface Factory {
        AssistedFactoryRequestRepresentation create(ProvisionBinding binding);
    }
}
