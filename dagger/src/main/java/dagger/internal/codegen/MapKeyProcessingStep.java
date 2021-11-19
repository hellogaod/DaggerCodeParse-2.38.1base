package dagger.internal.codegen;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.MapKey;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.internal.codegen.validation.MapKeyValidator;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;
import dagger.internal.codegen.validation.ValidationReport;
import dagger.internal.codegen.writing.AnnotationCreatorGenerator;
import dagger.internal.codegen.writing.UnwrappedMapKeyGenerator;

/**
 * The annotation processor responsible for validating the mapKey annotation and auto-generate
 * implementation of annotations marked with {@link MapKey @MapKey} where necessary.
 * <p>
 * 使用了MapKey注解的节点，该节点还是一个注解，因为MapKey只能用于修饰注解
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

    @Override
    protected ImmutableSet<ClassName> annotationClassNames() {
        return ImmutableSet.of(TypeNames.MAP_KEY);
    }

    @Override
    protected void process(XTypeElement xElement, ImmutableSet<ClassName> annotations) {

        TypeElement mapKeyAnnotationType = XConverters.toJavac(xElement);
        ValidationReport mapKeyReport = mapKeyValidator.validate(mapKeyAnnotationType);
        mapKeyReport.printMessagesTo(messager);

//        if (mapKeyReport.isClean()) {
//
//        }
    }

}
