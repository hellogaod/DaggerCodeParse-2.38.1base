package dagger.internal.codegen.writing;

import javax.inject.Inject;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: OptionalFactories
 * Author: 佛学徒
 * Date: 2021/10/26 8:03
 * Description:
 * History:
 */
class OptionalFactories {

    /** Keeps track of the fields, methods, and classes already added to the generated file. */
    @PerGeneratedFile
    static final class PerGeneratedFileCache {

        @Inject
        PerGeneratedFileCache() {}
    }


    private final PerGeneratedFileCache perGeneratedFileCache;
//    private final ShardImplementation rootComponentShard;

    @Inject
    OptionalFactories(
            PerGeneratedFileCache perGeneratedFileCache,
            ComponentImplementation componentImplementation) {
        this.perGeneratedFileCache = perGeneratedFileCache;
//        this.rootComponentShard =
//                componentImplementation.rootComponentImplementation().getComponentShard();
    }
}
