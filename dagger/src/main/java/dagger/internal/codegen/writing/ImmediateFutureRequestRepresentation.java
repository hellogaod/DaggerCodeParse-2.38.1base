package dagger.internal.codegen.writing;

import javax.lang.model.SourceVersion;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.Key;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ImmediateFutureRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 10:50
 * Description:
 * History:
 */
class ImmediateFutureRequestRepresentation {

    private final Key key;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerTypes types;
    private final SourceVersion sourceVersion;

    @AssistedInject
    ImmediateFutureRequestRepresentation(
            @Assisted Key key,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types,
            SourceVersion sourceVersion) {
        this.key = key;
        this.componentRequestRepresentations = checkNotNull(componentRequestRepresentations);
        this.types = checkNotNull(types);
        this.sourceVersion = checkNotNull(sourceVersion);
    }

    @AssistedFactory
    static interface Factory {
        ImmediateFutureRequestRepresentation create(Key key);
    }
}
