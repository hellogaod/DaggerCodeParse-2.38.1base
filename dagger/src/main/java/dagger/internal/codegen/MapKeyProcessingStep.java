package dagger.internal.codegen;

import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

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

import static dagger.internal.codegen.binding.MapKeys.getUnwrappedMapKeyType;
import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;

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

        if (mapKeyReport.isClean()) {
            MapKey mapkey = mapKeyAnnotationType.getAnnotation(MapKey.class);

            //如果mapKey#unwrapValue = false
            if (!mapkey.unwrapValue()) {
                annotationCreatorGenerator.generate(mapKeyAnnotationType, messager);
            }
            //如果获取使用MapKey注解修饰的注解里面唯一的方法返回类型还是一个注解
            else if (unwrappedValueKind(mapKeyAnnotationType).equals(ANNOTATION_TYPE)) {
                unwrappedMapKeyGenerator.generate(mapKeyAnnotationType, messager);
            }
        }
    }

    //获取使用MapKey注解修饰的注解里面唯一的方法返回类型
    private ElementKind unwrappedValueKind(TypeElement mapKeyAnnotationType) {
        DeclaredType unwrappedMapKeyType =
                getUnwrappedMapKeyType(MoreTypes.asDeclared(mapKeyAnnotationType.asType()), types);
        return unwrappedMapKeyType.asElement().getKind();
    }
}
