package dagger.internal.codegen.base;


import com.google.auto.common.AnnotationMirrors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Name;

/**
 * A utility class for working with {@link AnnotationMirror} instances, similar to {@link
 * AnnotationMirrors}.
 */
public final class MoreAnnotationMirrors {

    private MoreAnnotationMirrors() {}

    //改变类型
    public static Name simpleName(AnnotationMirror annotationMirror) {
        return annotationMirror.getAnnotationType().asElement().getSimpleName();
    }

}
