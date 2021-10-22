package dagger.internal.codegen;

import javax.inject.Inject;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import dagger.MapKey;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;

/**
 * The annotation processor responsible for validating the mapKey annotation and auto-generate
 * implementation of annotations marked with {@link MapKey @MapKey} where necessary.
 */
final class MapKeyProcessingStep extends TypeCheckingProcessingStep<XTypeElement> {
    private final XMessager messager;
    private final DaggerTypes types;

    @Inject
    MapKeyProcessingStep(
            XMessager messager,
            DaggerTypes types
    ) {
        this.messager = messager;
        this.types = types;
    }
}
