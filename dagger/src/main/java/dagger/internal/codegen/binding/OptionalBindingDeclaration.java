package dagger.internal.codegen.binding;

import javax.inject.Inject;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: OptionalBindingDeclaration
 * Author: 佛学徒
 * Date: 2021/10/22 11:14
 * Description:
 * History:
 */
class OptionalBindingDeclaration {

    static class Factory {
        private final KeyFactory keyFactory;

        @Inject
        Factory(KeyFactory keyFactory) {
            this.keyFactory = keyFactory;
        }
    }
}
