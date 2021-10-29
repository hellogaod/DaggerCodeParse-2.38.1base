package dagger.internal.codegen.bindinggraphvalidation;

import javax.inject.Inject;

import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.spi.model.BindingGraphPlugin;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: NullableBindingValidator
 * Author: 佛学徒
 * Date: 2021/10/25 9:41
 * Description:
 * History:
 */
class NullableBindingValidator implements BindingGraphPlugin {

    private final CompilerOptions compilerOptions;

    @Inject
    NullableBindingValidator(CompilerOptions compilerOptions) {
        this.compilerOptions = compilerOptions;
    }
}
