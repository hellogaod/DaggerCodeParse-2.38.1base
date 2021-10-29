package dagger.internal.codegen;

import com.google.auto.common.BasicAnnotationProcessor;

import javax.inject.Inject;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.BindingGraphFactory;
import dagger.internal.codegen.binding.ComponentDescriptorFactory;
import dagger.internal.codegen.validation.BindingGraphValidator;
import dagger.internal.codegen.validation.ComponentCreatorValidator;
import dagger.internal.codegen.validation.ComponentDescriptorValidator;
import dagger.internal.codegen.validation.ComponentValidator;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;

/**
 * A {@link BasicAnnotationProcessor.ProcessingStep} that is responsible for dealing with a component or production component
 * as part of the {@link ComponentProcessor}.
 */
final class ComponentProcessingStep extends TypeCheckingProcessingStep<XTypeElement> {

    private final XMessager messager;
    private final ComponentValidator componentValidator;
    private final ComponentCreatorValidator creatorValidator;
    private final ComponentDescriptorValidator componentDescriptorValidator;
    private final ComponentDescriptorFactory componentDescriptorFactory;
    private final BindingGraphFactory bindingGraphFactory;
    private final SourceFileGenerator<BindingGraph> componentGenerator;
    private final BindingGraphValidator bindingGraphValidator;

    @Inject
    ComponentProcessingStep(
            XMessager messager,
            ComponentValidator componentValidator,
            ComponentCreatorValidator creatorValidator,
            ComponentDescriptorValidator componentDescriptorValidator,
            ComponentDescriptorFactory componentDescriptorFactory,
            BindingGraphFactory bindingGraphFactory,
            SourceFileGenerator<BindingGraph> componentGenerator,
            BindingGraphValidator bindingGraphValidator) {
        this.messager = messager;
        this.componentValidator = componentValidator;
        this.creatorValidator = creatorValidator;
        this.componentDescriptorValidator = componentDescriptorValidator;
        this.componentDescriptorFactory = componentDescriptorFactory;
        this.bindingGraphFactory = bindingGraphFactory;
        this.componentGenerator = componentGenerator;
        this.bindingGraphValidator = bindingGraphValidator;
    }

}
