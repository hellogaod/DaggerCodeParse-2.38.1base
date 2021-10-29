package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.FrameworkType;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: DerivedFromFrameworkInstanceRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 10:48
 * Description:
 * History:
 */
class DerivedFromFrameworkInstanceRequestRepresentation {

    private final BindingRequest bindingRequest;
//    private final BindingRequest frameworkRequest;
    private final FrameworkType frameworkType;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerTypes types;

    @AssistedInject
    DerivedFromFrameworkInstanceRequestRepresentation(
            @Assisted BindingRequest bindingRequest,
            @Assisted FrameworkType frameworkType,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types) {
        this.bindingRequest = checkNotNull(bindingRequest);
        this.frameworkType = checkNotNull(frameworkType);
//        this.frameworkRequest = bindingRequest(bindingRequest.key(), frameworkType);
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.types = types;
    }

    @AssistedFactory
    static interface Factory {
        DerivedFromFrameworkInstanceRequestRepresentation create(
                BindingRequest request, FrameworkType frameworkType);
    }

}
