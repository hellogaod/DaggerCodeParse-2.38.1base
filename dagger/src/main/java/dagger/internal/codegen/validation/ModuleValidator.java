package dagger.internal.codegen.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.binding.BindingGraphFactory;
import dagger.internal.codegen.binding.ComponentDescriptorFactory;
import dagger.internal.codegen.binding.MethodSignatureFormatter;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ModuleValidator
 * Author: 佛学徒
 * Date: 2021/10/22 11:08
 * Description:
 * History:
 */
@Singleton
public final class ModuleValidator {

    private final DaggerTypes types;
    private final DaggerElements elements;
    private final AnyBindingMethodValidator anyBindingMethodValidator;
    private final MethodSignatureFormatter methodSignatureFormatter;
    private final ComponentDescriptorFactory componentDescriptorFactory;
    private final BindingGraphFactory bindingGraphFactory;
    private final BindingGraphValidator bindingGraphValidator;
    private final KotlinMetadataUtil metadataUtil;
    private final Map<TypeElement, ValidationReport> cache = new HashMap<>();
    private final Set<TypeElement> knownModules = new HashSet<>();

    @Inject
    ModuleValidator(
            DaggerTypes types,
            DaggerElements elements,
            AnyBindingMethodValidator anyBindingMethodValidator,
            MethodSignatureFormatter methodSignatureFormatter,
            ComponentDescriptorFactory componentDescriptorFactory,
            BindingGraphFactory bindingGraphFactory,
            BindingGraphValidator bindingGraphValidator,
            KotlinMetadataUtil metadataUtil) {
        this.types = types;
        this.elements = elements;
        this.anyBindingMethodValidator = anyBindingMethodValidator;
        this.methodSignatureFormatter = methodSignatureFormatter;
        this.componentDescriptorFactory = componentDescriptorFactory;
        this.bindingGraphFactory = bindingGraphFactory;
        this.bindingGraphValidator = bindingGraphValidator;
        this.metadataUtil = metadataUtil;
    }

}
