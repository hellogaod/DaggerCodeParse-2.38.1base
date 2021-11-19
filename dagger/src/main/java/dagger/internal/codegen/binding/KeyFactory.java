package dagger.internal.codegen.binding;


import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.DaggerAnnotation;
import dagger.spi.model.Key;

import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.spi.model.DaggerType.fromJava;

/**
 * A factory for {@link Key}s.
 */
public final class KeyFactory {
    private final DaggerTypes types;
    private final DaggerElements elements;
    private final InjectionAnnotations injectionAnnotations;

    @Inject
    KeyFactory(
            DaggerTypes types,
            DaggerElements elements,
            InjectionAnnotations injectionAnnotations
    ) {
        this.types = checkNotNull(types);
        this.elements = checkNotNull(elements);
        this.injectionAnnotations = injectionAnnotations;
    }

    //如果是原始类型，那么包装成PrimitiveType类型，例如int包装成Integer
    private TypeMirror boxPrimitives(TypeMirror type) {
        return type.getKind().isPrimitive() ? types.boxedClass((PrimitiveType) type).asType() : type;
    }

    //生成Key对象
    Key forQualifiedType(Optional<AnnotationMirror> qualifier, TypeMirror type) {
        return Key.builder(fromJava(boxPrimitives(type)))
                .qualifier(qualifier.map(DaggerAnnotation::fromJava))
                .build();
    }
}
