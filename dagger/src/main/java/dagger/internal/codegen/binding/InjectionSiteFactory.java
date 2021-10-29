package dagger.internal.codegen.binding;


import javax.inject.Inject;

import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/** A factory for {@link Binding} objects. */
final class InjectionSiteFactory {

    private final DaggerTypes types;
    private final DaggerElements elements;
    private final DependencyRequestFactory dependencyRequestFactory;

    @Inject
    InjectionSiteFactory(
            DaggerTypes types,
            DaggerElements elements,
            DependencyRequestFactory dependencyRequestFactory
    ) {
        this.types = types;
        this.elements = elements;
        this.dependencyRequestFactory = dependencyRequestFactory;
    }
}
