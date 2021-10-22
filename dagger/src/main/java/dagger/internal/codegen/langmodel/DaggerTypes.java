package dagger.internal.codegen.langmodel;


import javax.lang.model.util.Types;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extension of {@link Types} that adds Dagger-specific methods.
 */
public final class DaggerTypes {
    private final Types types;
    private final DaggerElements elements;

    public DaggerTypes(Types types, DaggerElements elements) {
        this.types = checkNotNull(types);
        this.elements = checkNotNull(elements);
    }
}
