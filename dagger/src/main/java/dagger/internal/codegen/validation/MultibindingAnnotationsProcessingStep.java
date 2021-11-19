package dagger.internal.codegen.validation;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import javax.inject.Inject;

import androidx.room.compiler.processing.XExecutableElement;
import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.internal.codegen.javapoet.TypeNames;

import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Processing step that verifies that {@link dagger.multibindings.IntoSet}, {@link
 * dagger.multibindings.ElementsIntoSet} and {@link dagger.multibindings.IntoMap} are not present on
 * non-binding methods.
 */
public final class MultibindingAnnotationsProcessingStep
        extends TypeCheckingProcessingStep<XExecutableElement> {

    private final AnyBindingMethodValidator anyBindingMethodValidator;
    private final XMessager messager;

    @Inject
    MultibindingAnnotationsProcessingStep(
            AnyBindingMethodValidator anyBindingMethodValidator,
            XMessager messager
    ) {
        this.anyBindingMethodValidator = anyBindingMethodValidator;
        this.messager = messager;
    }

    @Override
    public ImmutableSet<ClassName> annotationClassNames() {
        return ImmutableSet.of(TypeNames.INTO_SET, TypeNames.ELEMENTS_INTO_SET, TypeNames.INTO_MAP);
    }

    @Override
    protected void process(XExecutableElement method, ImmutableSet<ClassName> annotations) {

        if (!anyBindingMethodValidator.isBindingMethod(XConverters.toJavac(method))) {
            annotations.forEach(
                    annotation ->
                            messager.printMessage(
                                    ERROR,
                                    "Multibinding annotations may only be on @Provides, @Produces, or @Binds methods",
                                    method,
                                    method.getAnnotation(annotation)));
        }
    }
}
