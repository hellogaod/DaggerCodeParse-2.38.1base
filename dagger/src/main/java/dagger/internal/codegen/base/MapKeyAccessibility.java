package dagger.internal.codegen.base;


import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;

import dagger.internal.codegen.langmodel.Accessibility;

import static dagger.internal.codegen.langmodel.Accessibility.isTypeAccessibleFrom;

/** Utility class for checking the visibility of an annotation.  */
public final class MapKeyAccessibility extends SimpleAnnotationValueVisitor8<Boolean, Void> {
    private final Predicate<TypeMirror> accessibilityChecker;

    private MapKeyAccessibility(Predicate<TypeMirror> accessibilityChecker) {
        this.accessibilityChecker = accessibilityChecker;
    }

    @Override
    public Boolean visitAnnotation(AnnotationMirror annotation, Void aVoid) {
        // The annotation type is not checked, as the generated code will refer to the @AutoAnnotation
        // generated type which is always public
        return visitValues(annotation.getElementValues().values());
    }

    @Override
    public Boolean visitArray(List<? extends AnnotationValue> values, Void aVoid) {
        return visitValues(values);
    }

    private boolean visitValues(Collection<? extends AnnotationValue> values) {
        return values.stream().allMatch(value -> value.accept(this, null));
    }

    @Override
    public Boolean visitEnumConstant(VariableElement enumConstant, Void aVoid) {
        return accessibilityChecker.test(enumConstant.getEnclosingElement().asType());
    }

    @Override
    public Boolean visitType(TypeMirror type, Void aVoid) {
        return accessibilityChecker.test(type);
    }

    @Override
    protected Boolean defaultAction(Object o, Void aVoid) {
        return true;
    }

    public static boolean isMapKeyAccessibleFrom(
            AnnotationMirror annotation, String accessingPackage) {
        return new MapKeyAccessibility(type -> isTypeAccessibleFrom(type, accessingPackage))
                .visitAnnotation(annotation, null);
    }

    public static boolean isMapKeyPubliclyAccessible(AnnotationMirror annotation) {
        return new MapKeyAccessibility(Accessibility::isTypePubliclyAccessible)
                .visitAnnotation(annotation, null);
    }
}
