package dagger.internal.codegen.bindinggraphvalidation;

import javax.inject.Inject;

import dagger.internal.codegen.binding.MethodSignatureFormatter;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.spi.model.BindingGraphPlugin;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: IncompatiblyScopedBindingsValidator
 * Author: 佛学徒
 * Date: 2021/10/25 9:37
 * Description:
 * History:
 */
class IncompatiblyScopedBindingsValidator implements BindingGraphPlugin {

    private final MethodSignatureFormatter methodSignatureFormatter;
    private final CompilerOptions compilerOptions;

    @Inject
    IncompatiblyScopedBindingsValidator(
            MethodSignatureFormatter methodSignatureFormatter,
            CompilerOptions compilerOptions) {
        this.methodSignatureFormatter = methodSignatureFormatter;
        this.compilerOptions = compilerOptions;
    }
}
