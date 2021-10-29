package dagger.internal.codegen.bindinggraphvalidation;

import javax.inject.Inject;

import dagger.internal.codegen.binding.DependencyRequestFormatter;
import dagger.spi.model.BindingGraphPlugin;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: DependencyCycleValidator
 * Author: 佛学徒
 * Date: 2021/10/25 9:35
 * Description:
 * History:
 */
class DependencyCycleValidator implements BindingGraphPlugin {


    private final DependencyRequestFormatter dependencyRequestFormatter;

    @Inject
    DependencyCycleValidator(DependencyRequestFormatter dependencyRequestFormatter) {
        this.dependencyRequestFormatter = dependencyRequestFormatter;
    }
}
