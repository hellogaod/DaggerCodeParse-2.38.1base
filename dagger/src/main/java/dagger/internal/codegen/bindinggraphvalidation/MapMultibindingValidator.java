package dagger.internal.codegen.bindinggraphvalidation;

import javax.inject.Inject;

import dagger.internal.codegen.binding.BindingDeclarationFormatter;
import dagger.internal.codegen.binding.KeyFactory;
import dagger.spi.model.BindingGraphPlugin;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: MapMultibindingValidator
 * Author: 佛学徒
 * Date: 2021/10/25 9:40
 * Description:
 * History:
 */
class MapMultibindingValidator implements BindingGraphPlugin {

    private final BindingDeclarationFormatter bindingDeclarationFormatter;
    private final KeyFactory keyFactory;

    @Inject
    MapMultibindingValidator(
            BindingDeclarationFormatter bindingDeclarationFormatter,
            KeyFactory keyFactory
    ) {
        this.bindingDeclarationFormatter = bindingDeclarationFormatter;
        this.keyFactory = keyFactory;
    }
}
