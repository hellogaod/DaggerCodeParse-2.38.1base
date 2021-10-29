package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.RequestKind;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: DelegateRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 10:14
 * Description:
 * History:
 */
class DelegateRequestRepresentation {

    private final ContributionBinding binding;
    private final RequestKind requestKind;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerTypes types;
//    private final BindsTypeChecker bindsTypeChecker;

    @AssistedInject
    DelegateRequestRepresentation(
            @Assisted ContributionBinding binding,
            @Assisted RequestKind requestKind,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types,
            DaggerElements elements) {
        this.binding = checkNotNull(binding);
        this.requestKind = checkNotNull(requestKind);
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.types = types;
//        this.bindsTypeChecker = new BindsTypeChecker(types, elements);
    }


    @AssistedFactory
    static interface Factory {
        DelegateRequestRepresentation create(
                ContributionBinding binding,
                RequestKind requestKind
        );
    }
}
