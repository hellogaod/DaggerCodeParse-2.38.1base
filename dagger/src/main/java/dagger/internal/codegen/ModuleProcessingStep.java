package dagger.internal.codegen;


import com.google.auto.common.BasicAnnotationProcessor;

import javax.inject.Inject;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;

/**
 * A {@link BasicAnnotationProcessor.ProcessingStep} that validates module classes and generates factories for binding
 * methods.
 */
final class ModuleProcessingStep extends TypeCheckingProcessingStep<XTypeElement> {

    private final XMessager messager;
    private final SourceFileGenerator<ProvisionBinding> factoryGenerator;

    @Inject
    ModuleProcessingStep(
            XMessager messager,
            SourceFileGenerator<ProvisionBinding> factoryGenerator) {
        this.messager = messager;
        this.factoryGenerator = factoryGenerator;
    }

}
