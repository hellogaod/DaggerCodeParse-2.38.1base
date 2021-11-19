package dagger.internal.codegen.binding;


import com.google.common.collect.ImmutableSet;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import dagger.MapKey;

import static com.google.auto.common.AnnotationMirrors.getAnnotatedAnnotations;

/**
 * Methods for extracting {@link MapKey} annotations and key code blocks from binding elements.
 */
public final class MapKeys {

    /**
     * Returns all of the {@link MapKey} annotations that annotate {@code bindingElement}.
     * <p>
     * 返回bindingElement节点上的被MapKey修饰的注解集
     */
    public static ImmutableSet<? extends AnnotationMirror> getMapKeys(Element bindingElement) {
        return getAnnotatedAnnotations(bindingElement, MapKey.class);
    }
}
