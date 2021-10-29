package dagger.internal.codegen.validation;


import javax.inject.Inject;

import androidx.room.compiler.processing.XElement;
import androidx.room.compiler.processing.XMessager;

/**
 * Processing step that validates that the {@code BindsInstance} annotation is applied to the
 * correct elements.
 */
public final class BindsInstanceProcessingStep extends TypeCheckingProcessingStep<XElement> {

    private final BindsInstanceMethodValidator methodValidator;
    private final BindsInstanceParameterValidator parameterValidator;
    private final XMessager messager;

    @Inject
    BindsInstanceProcessingStep(
            BindsInstanceMethodValidator methodValidator,
            BindsInstanceParameterValidator parameterValidator,
            XMessager messager
    ) {
        this.methodValidator = methodValidator;
        this.parameterValidator = parameterValidator;
        this.messager = messager;
    }
}
