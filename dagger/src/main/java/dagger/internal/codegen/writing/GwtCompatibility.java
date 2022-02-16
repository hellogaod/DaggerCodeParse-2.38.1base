package dagger.internal.codegen.writing;

import com.squareup.javapoet.AnnotationSpec;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;

import dagger.internal.codegen.binding.Binding;

import static com.google.common.base.Preconditions.checkArgument;

final class GwtCompatibility {


    /**
     * Returns a {@code @GwtIncompatible} annotation that is applied to {@code binding}'s {@link
     * Binding#bindingElement()} or any enclosing type.
     */
    static Optional<AnnotationSpec> gwtIncompatibleAnnotation(Binding binding) {
        checkArgument(binding.bindingElement().isPresent());

        Element element = binding.bindingElement().get();
        while (element != null) {
            Optional<AnnotationSpec> gwtIncompatible =
                    element
                            .getAnnotationMirrors()
                            .stream()
                            .filter(annotation -> isGwtIncompatible(annotation))
                            .map(AnnotationSpec::get)
                            .findFirst();
            if (gwtIncompatible.isPresent()) {
                return gwtIncompatible;
            }
            element = element.getEnclosingElement();
        }
        return Optional.empty();

    }

    private static boolean isGwtIncompatible(AnnotationMirror annotation) {
        Name simpleName = annotation.getAnnotationType().asElement().getSimpleName();
        return simpleName.contentEquals("GwtIncompatible");
    }
}
