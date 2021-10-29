package dagger.internal.codegen.writing;

import javax.lang.model.SourceVersion;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: OptionalRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 11:23
 * Description:
 * History:
 */
class OptionalRequestRepresentation {

    private final ProvisionBinding binding;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerTypes types;
    private final SourceVersion sourceVersion;

    @AssistedInject
    OptionalRequestRepresentation(
            @Assisted ProvisionBinding binding,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types,
            SourceVersion sourceVersion) {
//        super(binding);
        this.binding = binding;
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.types = types;
        this.sourceVersion = sourceVersion;
    }

    @AssistedFactory
    static interface Factory {
        OptionalRequestRepresentation create(ProvisionBinding binding);
    }
}
