package dagger.internal.codegen.binding;


import javax.inject.Inject;

import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/** A factory for {@link Binding} objects. */
public final class BindingFactory {
    private final DaggerTypes types;
    private final KeyFactory keyFactory;
    private final DependencyRequestFactory dependencyRequestFactory;
    private final InjectionSiteFactory injectionSiteFactory;
    private final DaggerElements elements;
    private final InjectionAnnotations injectionAnnotations;
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    BindingFactory(
            DaggerTypes types,
            DaggerElements elements,
            KeyFactory keyFactory,
            DependencyRequestFactory dependencyRequestFactory,
            InjectionSiteFactory injectionSiteFactory,
            InjectionAnnotations injectionAnnotations,
            KotlinMetadataUtil metadataUtil) {
        this.types = types;
        this.elements = elements;
        this.keyFactory = keyFactory;
        this.dependencyRequestFactory = dependencyRequestFactory;
        this.injectionSiteFactory = injectionSiteFactory;
        this.injectionAnnotations = injectionAnnotations;
        this.metadataUtil = metadataUtil;
    }
}
