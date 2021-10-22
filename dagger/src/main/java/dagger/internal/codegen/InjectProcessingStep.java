package dagger.internal.codegen;


import javax.inject.Inject;

import androidx.room.compiler.processing.XElement;
import dagger.internal.codegen.binding.InjectBindingRegistry;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;

/**
 * An annotation processor for generating Dagger implementation code based on the {@link Inject}
 * annotation.
 */
// TODO(gak): add some error handling for bad source files
final class InjectProcessingStep extends TypeCheckingProcessingStep<XElement> {

    @Inject
    InjectProcessingStep(InjectBindingRegistry injectBindingRegistry) {

    }
}
