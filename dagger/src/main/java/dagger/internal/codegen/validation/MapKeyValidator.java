package dagger.internal.codegen.validation;


import javax.inject.Inject;

import dagger.MapKey;
import dagger.internal.codegen.langmodel.DaggerElements;

/**
 * A validator for {@link MapKey} annotations.
 */
// TODO(dpb,gak): Should unwrapped MapKeys be required to have their single member be named "value"?
public final class MapKeyValidator {
    private final DaggerElements elements;

    @Inject
    MapKeyValidator(DaggerElements elements) {
        this.elements = elements;
    }
}
