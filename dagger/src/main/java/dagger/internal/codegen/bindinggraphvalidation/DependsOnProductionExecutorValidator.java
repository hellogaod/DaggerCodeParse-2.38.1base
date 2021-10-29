package dagger.internal.codegen.bindinggraphvalidation;

import javax.inject.Inject;

import dagger.internal.codegen.binding.KeyFactory;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.spi.model.BindingGraphPlugin;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: DependsOnProductionExecutorValidator
 * Author: 佛学徒
 * Date: 2021/10/25 9:36
 * Description:
 * History:
 */
class DependsOnProductionExecutorValidator implements BindingGraphPlugin {

    private final CompilerOptions compilerOptions;
    private final KeyFactory keyFactory;

    @Inject
    DependsOnProductionExecutorValidator(
            CompilerOptions compilerOptions,
            KeyFactory keyFactory) {
        this.compilerOptions = compilerOptions;
        this.keyFactory = keyFactory;
    }
}
