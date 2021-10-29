package dagger.internal.codegen;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import dagger.internal.codegen.binding.BindingFactory;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import androidx.room.compiler.processing.XProcessingEnv;

import dagger.internal.codegen.validation.TypeCheckingProcessingStep;

/**
 * An annotation processor for {@link dagger.assisted.AssistedFactory}-annotated types.
 */
final class AssistedFactoryProcessingStep extends TypeCheckingProcessingStep<XTypeElement> {

    private final XProcessingEnv processingEnv;
    private final XMessager messager;
    private final XFiler filer;
    private final SourceVersion sourceVersion;
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final BindingFactory bindingFactory;

    @Inject
    AssistedFactoryProcessingStep(
            XProcessingEnv processingEnv,
            XMessager messager,
            XFiler filer,
            SourceVersion sourceVersion,
            DaggerElements elements,
            DaggerTypes types,
            BindingFactory bindingFactory
    ) {
        this.processingEnv = processingEnv;
        this.messager = messager;
        this.filer = filer;
        this.sourceVersion = sourceVersion;
        this.elements = elements;
        this.types = types;
        this.bindingFactory = bindingFactory;
    }
}
