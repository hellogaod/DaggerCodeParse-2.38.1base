package dagger.internal.codegen.validation;


import javax.inject.Inject;

import androidx.room.compiler.processing.XExecutableElement;
import androidx.room.compiler.processing.XMessager;

/** A step that validates all binding methods that were not validated while processing modules. */
public final class BindingMethodProcessingStep
        extends TypeCheckingProcessingStep<XExecutableElement> {


    private final XMessager messager;

    @Inject
    BindingMethodProcessingStep(
            XMessager messager
    ) {
        this.messager = messager;
    }
}
