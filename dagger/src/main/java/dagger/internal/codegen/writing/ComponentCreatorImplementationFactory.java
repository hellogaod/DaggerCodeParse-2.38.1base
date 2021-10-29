package dagger.internal.codegen.writing;

import javax.inject.Inject;

import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ComponentCreatorImplementationFactory
 * Author: 佛学徒
 * Date: 2021/10/27 8:24
 * Description:
 * History:
 */
class ComponentCreatorImplementationFactory {


    private final ComponentImplementation componentImplementation;
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final KotlinMetadataUtil metadataUtil;
    private final ModuleProxies moduleProxies;

    @Inject
    ComponentCreatorImplementationFactory(
            ComponentImplementation componentImplementation,
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil metadataUtil,
            ModuleProxies moduleProxies) {
        this.componentImplementation = componentImplementation;
        this.elements = elements;
        this.types = types;
        this.metadataUtil = metadataUtil;
        this.moduleProxies = moduleProxies;
    }
}
