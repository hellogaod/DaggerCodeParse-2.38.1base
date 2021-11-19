package dagger.android.processor;


// TODO(bcorso): Dedupe with dagger/internal/codegen/langmodel/DaggerElements.java?
// TODO(bcorso): Contribute upstream to auto common?

import com.google.auto.common.MoreElements;
import com.squareup.javapoet.ClassName;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/** Similar to auto common, but uses {@link ClassName} rather than {@link Class}. */
final class MoreDaggerElements {
    /**
     * Returns {@code true} iff the given element has an {@link AnnotationMirror} whose {@linkplain
     * AnnotationMirror#getAnnotationType() annotation type} has the same canonical name as that of
     * {@code annotationClass}. This method is a safer alternative to calling {@link
     * Element#getAnnotation} and checking for {@code null} as it avoids any interaction with
     * annotation proxies.
     */
    public static boolean isAnnotationPresent(Element element, ClassName annotationName) {
        return getAnnotationMirror(element, annotationName).isPresent();
    }

    /**
     * Returns an {@link AnnotationMirror} for the annotation of type {@code annotationClass} on
     * {@code element}, or {@link Optional#empty()} if no such annotation exists. This method is a
     * safer alternative to calling {@link Element#getAnnotation} as it avoids any interaction with
     * annotation proxies.
     */
    public static Optional<AnnotationMirror> getAnnotationMirror(
            Element element, ClassName annotationName) {
        String annotationClassName = annotationName.canonicalName();
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            TypeElement annotationTypeElement =
                    MoreElements.asType(annotationMirror.getAnnotationType().asElement());
            if (annotationTypeElement.getQualifiedName().contentEquals(annotationClassName)) {
                return Optional.of(annotationMirror);
            }
        }
        return Optional.empty();
    }

    private MoreDaggerElements() {}
}
