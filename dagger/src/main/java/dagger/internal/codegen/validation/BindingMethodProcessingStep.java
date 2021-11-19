package dagger.internal.codegen.validation;


import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;

import androidx.room.compiler.processing.XExecutableElement;
import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.compat.XConverters;

import static com.google.common.base.Preconditions.checkArgument;

/** A step that validates all binding methods that were not validated while processing modules. */
public final class BindingMethodProcessingStep
        extends TypeCheckingProcessingStep<XExecutableElement> {

    private final XMessager messager;
    private final AnyBindingMethodValidator anyBindingMethodValidator;

    @Inject
    BindingMethodProcessingStep(
            XMessager messager,
            AnyBindingMethodValidator anyBindingMethodValidator
    ) {
        this.messager = messager;
        this.anyBindingMethodValidator = anyBindingMethodValidator;
    }

    @Override
    public ImmutableSet<ClassName> annotationClassNames() {
        return anyBindingMethodValidator.methodAnnotations();
    }

    @Override
    protected void process(XExecutableElement xElement, ImmutableSet<ClassName> annotations) {
        ExecutableElement method = XConverters.toJavac(xElement);
        checkArgument(
                anyBindingMethodValidator.isBindingMethod(method),
                "%s is not annotated with any of %s",
                method,
                annotations());

        if (!anyBindingMethodValidator.wasAlreadyValidated(method)) {
            anyBindingMethodValidator.validate(method).printMessagesTo(messager);
        }
    }
}
