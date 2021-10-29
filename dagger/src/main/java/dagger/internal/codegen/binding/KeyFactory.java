package dagger.internal.codegen.binding;


import javax.inject.Inject;

import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.base.Preconditions.checkNotNull;

/** A factory for {@link Key}s. */
public final class KeyFactory {
    private final DaggerTypes types;
    private final DaggerElements elements;
    private final InjectionAnnotations injectionAnnotations;

    @Inject
    KeyFactory(
            DaggerTypes types,
            DaggerElements elements,
            InjectionAnnotations injectionAnnotations
    ) {
        this.types = checkNotNull(types);
        this.elements = checkNotNull(elements);
        this.injectionAnnotations = injectionAnnotations;
    }
}
