package dagger.internal.codegen.validation;

import javax.inject.Inject;

import androidx.room.compiler.processing.XExecutableElement;
import androidx.room.compiler.processing.XMessager;

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
}
