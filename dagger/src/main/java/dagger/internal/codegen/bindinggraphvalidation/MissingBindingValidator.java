package dagger.internal.codegen.bindinggraphvalidation;

import javax.inject.Inject;

import dagger.internal.codegen.binding.DependencyRequestFormatter;
import dagger.internal.codegen.binding.InjectBindingRegistry;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.internal.codegen.validation.DiagnosticMessageGenerator;
import dagger.spi.model.BindingGraphPlugin;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: MissingBindingValidator
 * Author: 佛学徒
 * Date: 2021/10/25 9:41
 * Description:
 * History:
 */
class MissingBindingValidator implements BindingGraphPlugin {

    private final DaggerTypes types;
    private final InjectBindingRegistry injectBindingRegistry;
    private final DependencyRequestFormatter dependencyRequestFormatter;
    private final DiagnosticMessageGenerator.Factory diagnosticMessageGeneratorFactory;

    @Inject
    MissingBindingValidator(
            DaggerTypes types,
            InjectBindingRegistry injectBindingRegistry,
            DependencyRequestFormatter dependencyRequestFormatter,
            DiagnosticMessageGenerator.Factory diagnosticMessageGeneratorFactory) {
        this.types = types;
        this.injectBindingRegistry = injectBindingRegistry;
        this.dependencyRequestFormatter = dependencyRequestFormatter;
        this.diagnosticMessageGeneratorFactory = diagnosticMessageGeneratorFactory;
    }
}
