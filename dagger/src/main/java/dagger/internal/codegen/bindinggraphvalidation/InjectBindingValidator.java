package dagger.internal.codegen.bindinggraphvalidation;

import javax.inject.Inject;

import dagger.internal.codegen.validation.InjectValidator;
import dagger.spi.model.BindingGraphPlugin;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: InjectBindingValidator
 * Author: 佛学徒
 * Date: 2021/10/25 9:37
 * Description:
 * History:
 */
class InjectBindingValidator implements BindingGraphPlugin {

    private final InjectValidator injectValidator;

    @Inject
    InjectBindingValidator(InjectValidator injectValidator) {
        this.injectValidator = injectValidator.whenGeneratingCode();
    }
}
