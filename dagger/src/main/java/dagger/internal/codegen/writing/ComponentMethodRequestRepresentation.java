package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ComponentMethodRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 10:10
 * Description:
 * History:
 */
class ComponentMethodRequestRepresentation {

    private final RequestRepresentation wrappedRequestRepresentation;
    private final ComponentImplementation componentImplementation;
    private final ComponentDescriptor.ComponentMethodDescriptor componentMethod;
    private final DaggerTypes types;

    @AssistedInject
    ComponentMethodRequestRepresentation(
            @Assisted RequestRepresentation wrappedRequestRepresentation,
            @Assisted ComponentDescriptor.ComponentMethodDescriptor componentMethod,
            ComponentImplementation componentImplementation,
            DaggerTypes types) {

        this.wrappedRequestRepresentation = checkNotNull(wrappedRequestRepresentation);
        this.componentMethod = checkNotNull(componentMethod);
        this.componentImplementation = componentImplementation;
        this.types = types;
    }


    @AssistedFactory
    static interface Factory {
        ComponentMethodRequestRepresentation create(
                RequestRepresentation wrappedRequestRepresentation,
                ComponentDescriptor.ComponentMethodDescriptor componentMethod);
    }
}
