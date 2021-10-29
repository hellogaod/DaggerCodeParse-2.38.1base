package dagger.internal.codegen.binding;

import javax.inject.Inject;

import dagger.internal.codegen.langmodel.DaggerTypes;

public final class DependencyRequestFormatter  {

    private final DaggerTypes types;

    @Inject
    DependencyRequestFormatter(DaggerTypes types) {
        this.types = types;
    }
}
