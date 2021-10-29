package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ProviderInstanceRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 11:13
 * Description:
 * History:
 */
class ProviderInstanceRequestRepresentation {


    @AssistedInject
    ProviderInstanceRequestRepresentation(
            @Assisted ContributionBinding binding,
            @Assisted FrameworkInstanceSupplier frameworkInstanceSupplier,
            DaggerTypes types,
            DaggerElements elements) {
//        super(binding, frameworkInstanceSupplier, types, elements);
    }

    @AssistedFactory
    static interface Factory {
        ProviderInstanceRequestRepresentation create(
                ContributionBinding binding,
                FrameworkInstanceSupplier frameworkInstanceSupplier
        );
    }
}
