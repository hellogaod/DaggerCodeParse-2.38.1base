package dagger.internal.codegen;

import javax.inject.Inject;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import dagger.MapKey;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.internal.codegen.validation.MapKeyValidator;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;
import dagger.internal.codegen.writing.AnnotationCreatorGenerator;
import dagger.internal.codegen.writing.UnwrappedMapKeyGenerator;

/**
 * The annotation processor responsible for validating the mapKey annotation and auto-generate
 * implementation of annotations marked with {@link MapKey @MapKey} where necessary.
 */
final class MapKeyProcessingStep extends TypeCheckingProcessingStep<XTypeElement> {
    private final XMessager messager;
    private final DaggerTypes types;
    private final MapKeyValidator mapKeyValidator;
    private final AnnotationCreatorGenerator annotationCreatorGenerator;
    private final UnwrappedMapKeyGenerator unwrappedMapKeyGenerator;

    @Inject
    MapKeyProcessingStep(
            XMessager messager,
            DaggerTypes types,
            MapKeyValidator mapKeyValidator,
            AnnotationCreatorGenerator annotationCreatorGenerator,
            UnwrappedMapKeyGenerator unwrappedMapKeyGenerator
    ) {
        this.messager = messager;
        this.types = types;
        this.mapKeyValidator = mapKeyValidator;
        this.annotationCreatorGenerator = annotationCreatorGenerator;
        this.unwrappedMapKeyGenerator = unwrappedMapKeyGenerator;
    }
}
