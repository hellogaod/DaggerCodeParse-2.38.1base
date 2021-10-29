package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.Key;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ProducerNodeInstanceRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 11:10
 * Description:
 * History:
 */
class ProducerNodeInstanceRequestRepresentation {

//    private final ShardImplementation shardImplementation;
//    private final Key key;
//    private final ProducerEntryPointView producerEntryPointView;

    @AssistedInject
    ProducerNodeInstanceRequestRepresentation(
            @Assisted ContributionBinding binding,
            @Assisted FrameworkInstanceSupplier frameworkInstanceSupplier,
            DaggerTypes types,
            DaggerElements elements,
            ComponentImplementation componentImplementation) {
//        super(binding, frameworkInstanceSupplier, types, elements);
//        this.shardImplementation = componentImplementation.shardImplementation(binding);
//        this.key = binding.key();
//        this.producerEntryPointView = new ProducerEntryPointView(shardImplementation, types);
    }

    @AssistedFactory
    static interface Factory {
        ProducerNodeInstanceRequestRepresentation create(
                ContributionBinding binding,
                FrameworkInstanceSupplier frameworkInstanceSupplier
        );
    }
}
