package dagger.internal.codegen.bindinggraphvalidation;

import javax.inject.Inject;

import dagger.internal.codegen.binding.BindingDeclarationFormatter;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.spi.model.BindingGraphPlugin;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: DuplicateBindingsValidator
 * Author: 佛学徒
 * Date: 2021/10/25 9:36
 * Description:
 * History:
 */
class DuplicateBindingsValidator implements BindingGraphPlugin {

    private final BindingDeclarationFormatter bindingDeclarationFormatter;
    private final CompilerOptions compilerOptions;

    @Inject
    DuplicateBindingsValidator(
            BindingDeclarationFormatter bindingDeclarationFormatter,
            CompilerOptions compilerOptions) {
        this.bindingDeclarationFormatter = bindingDeclarationFormatter;
        this.compilerOptions = compilerOptions;
    }

}
