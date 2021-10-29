package dagger.internal.codegen.binding;

import javax.inject.Inject;

import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: DelegateDeclaration
 * Author: 佛学徒
 * Date: 2021/10/22 11:12
 * Description:
 * History:
 */
public abstract class DelegateDeclaration {
    /**
     * A {@link DelegateDeclaration} factory.
     */
    public static final class Factory {
        private final DaggerTypes types;
        private final KeyFactory keyFactory;
        private final DependencyRequestFactory dependencyRequestFactory;

        @Inject
        Factory(
                DaggerTypes types,
                KeyFactory keyFactory,
                DependencyRequestFactory dependencyRequestFactory
        ) {
            this.types = types;
            this.keyFactory = keyFactory;
            this.dependencyRequestFactory = dependencyRequestFactory;
        }
    }


}
