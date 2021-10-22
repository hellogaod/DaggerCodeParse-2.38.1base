package dagger.internal.codegen;


import javax.inject.Inject;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XProcessingEnv;
import androidx.room.compiler.processing.XVariableElement;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;

/**
 * An annotation processor for {@link dagger.assisted.Assisted}-annotated types.
 *
 * <p>This processing step should run after {@link AssistedFactoryProcessingStep}.
 */
final class AssistedProcessingStep extends TypeCheckingProcessingStep<XVariableElement> {

    private final DaggerElements elements;
    private final XMessager messager;
    private final XProcessingEnv processingEnv;

    @Inject
    AssistedProcessingStep(
            DaggerElements elements,
            XMessager messager,
            XProcessingEnv processingEnv) {
        this.elements = elements;
        this.messager = messager;
        this.processingEnv = processingEnv;
    }
}
