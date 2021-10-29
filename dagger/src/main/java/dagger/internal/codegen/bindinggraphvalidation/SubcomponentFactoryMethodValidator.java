package dagger.internal.codegen.bindinggraphvalidation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.BindingGraphPlugin;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: SubcomponentFactoryMethodValidator
 * Author: 佛学徒
 * Date: 2021/10/25 9:43
 * Description:
 * History:
 */
class SubcomponentFactoryMethodValidator implements BindingGraphPlugin {


    private final DaggerTypes types;
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    SubcomponentFactoryMethodValidator(DaggerTypes types, KotlinMetadataUtil metadataUtil) {
        this.types = types;
        this.metadataUtil = metadataUtil;
    }
}
