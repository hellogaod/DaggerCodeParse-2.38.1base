package dagger.internal.codegen.binding;

import javax.inject.Inject;

import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: BindsTypeChecker
 * Author: 佛学徒
 * Date: 2021/10/26 8:55
 * Description:
 * History:
 */
public final class BindsTypeChecker {
    private final DaggerTypes types;
    private final DaggerElements elements;

    // TODO(bcorso): Make this pkg-private. Used by DelegateRequestRepresentation.
    @Inject
    public BindsTypeChecker(
            DaggerTypes types,
            DaggerElements elements
    ) {
        this.types = types;
        this.elements = elements;
    }
}
