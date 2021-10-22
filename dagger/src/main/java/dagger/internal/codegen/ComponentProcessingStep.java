package dagger.internal.codegen;

import com.google.auto.common.BasicAnnotationProcessor;

import javax.inject.Inject;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;

/**
 * A {@link BasicAnnotationProcessor.ProcessingStep} that is responsible for dealing with a component or production component
 * as part of the {@link ComponentProcessor}.
 */
final class ComponentProcessingStep extends TypeCheckingProcessingStep<XTypeElement> {

    private final XMessager messager;

    @Inject
    ComponentProcessingStep(XMessager messager) {
        this.messager = messager;
    }
}
