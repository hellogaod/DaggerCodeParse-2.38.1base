package dagger.internal.codegen.binding;

import javax.inject.Inject;

import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: MultibindingDeclaration
 * Author: 佛学徒
 * Date: 2021/10/22 11:11
 * Description:
 * History:
 */
public abstract class MultibindingDeclaration {

    /**
     * A factory for {@link MultibindingDeclaration}s.
     */
    public static final class Factory {
        private final DaggerTypes types;
        private final KeyFactory keyFactory;

        @Inject
        Factory(
                DaggerTypes types,
                KeyFactory keyFactory
        ) {
            this.types = types;
            this.keyFactory = keyFactory;
        }
    }
}
