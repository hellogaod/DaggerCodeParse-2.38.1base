package dagger.internal.codegen;


import javax.inject.Inject;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XProcessingEnv;
import androidx.room.compiler.processing.XVariableElement;
import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;

/**
 * An annotation processor for {@link dagger.assisted.Assisted}-annotated types.
 *
 * <p>This processing step should run after {@link AssistedFactoryProcessingStep}.
 */
final class AssistedProcessingStep extends TypeCheckingProcessingStep<XVariableElement> {

    private final KotlinMetadataUtil kotlinMetadataUtil;
    private final InjectionAnnotations injectionAnnotations;
    private final DaggerElements elements;
    private final XMessager messager;
    private final XProcessingEnv processingEnv;

    @Inject
    AssistedProcessingStep(
            KotlinMetadataUtil kotlinMetadataUtil,
            InjectionAnnotations injectionAnnotations,
            DaggerElements elements,
            XMessager messager,
            XProcessingEnv processingEnv) {
        this.kotlinMetadataUtil = kotlinMetadataUtil;
        this.injectionAnnotations = injectionAnnotations;
        this.elements = elements;
        this.messager = messager;
        this.processingEnv = processingEnv;
    }
}
