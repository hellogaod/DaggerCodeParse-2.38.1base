package dagger.internal.codegen;

import javax.inject.Inject;

import androidx.room.compiler.processing.XExecutableElement;
import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XProcessingEnv;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;

/**
 * An annotation processor for {@link dagger.assisted.AssistedInject}-annotated elements.
 */
final class AssistedInjectProcessingStep extends TypeCheckingProcessingStep<XExecutableElement> {

    private final DaggerTypes types;
    private final XMessager messager;
    private final XProcessingEnv processingEnv;

    @Inject
    AssistedInjectProcessingStep(
            DaggerTypes types,
            XMessager messager,
            XProcessingEnv processingEnv
    ) {
        this.types = types;
        this.messager = messager;
        this.processingEnv = processingEnv;
    }
}
