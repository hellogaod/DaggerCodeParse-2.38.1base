package dagger.internal.codegen.base;


import com.google.auto.common.AnnotationMirrors;
import com.google.common.base.Equivalence;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Name;

/**
 * A utility class for working with {@link AnnotationMirror} instances, similar to {@link
 * AnnotationMirrors}.
 */
public final class MoreAnnotationMirrors {

    private MoreAnnotationMirrors() {
    }

    /**
     * Wraps an {@link Optional} of a type in an {@code Optional} of a {@link Equivalence.Wrapper} for
     * that type.
     */
    public static Optional<Equivalence.Wrapper<AnnotationMirror>> wrapOptionalInEquivalence(
            Optional<AnnotationMirror> optional) {
        return optional.map(AnnotationMirrors.equivalence()::wrap);
    }

    /**
     * Unwraps an {@link Optional} of a {@link Equivalence.Wrapper} into an {@code Optional} of the
     * underlying type.
     */
    public static Optional<AnnotationMirror> unwrapOptionalEquivalence(
            Optional<Equivalence.Wrapper<AnnotationMirror>> wrappedOptional) {
        return wrappedOptional.map(Equivalence.Wrapper::get);
    }

    //改变类型
    public static Name simpleName(AnnotationMirror annotationMirror) {
        return annotationMirror.getAnnotationType().asElement().getSimpleName();
    }

}
