package dagger.internal.codegen.binding;


import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;

import dagger.internal.codegen.langmodel.DaggerTypes;

/** Formats the signature of an {@link ExecutableElement} suitable for use in error messages. */
public final class MethodSignatureFormatter  {
    private final DaggerTypes types;
    private final InjectionAnnotations injectionAnnotations;

    @Inject
    public MethodSignatureFormatter(
            DaggerTypes types,
            InjectionAnnotations injectionAnnotations
    ) {
        this.types = types;
        this.injectionAnnotations = injectionAnnotations;
    }
}
