package dagger.internal.codegen.binding;

import javax.inject.Inject;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: SubcomponentDeclaration
 * Author: 佛学徒
 * Date: 2021/10/22 11:13
 * Description:
 * History:
 */
public class SubcomponentDeclaration {


    /** A {@link SubcomponentDeclaration} factory. */
    public static class Factory {
        private final KeyFactory keyFactory;

        @Inject
        Factory(KeyFactory keyFactory) {
            this.keyFactory = keyFactory;
        }

    }
}
