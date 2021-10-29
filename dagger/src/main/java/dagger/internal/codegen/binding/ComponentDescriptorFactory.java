package dagger.internal.codegen.binding;


import javax.inject.Inject;

import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * A factory for {@link ComponentDescriptor}s.
 */
public final class ComponentDescriptorFactory {
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final DependencyRequestFactory dependencyRequestFactory;
    private final ModuleDescriptor.Factory moduleDescriptorFactory;
    private final InjectionAnnotations injectionAnnotations;

    @Inject
    ComponentDescriptorFactory(
            DaggerElements elements,
            DaggerTypes types,
            DependencyRequestFactory dependencyRequestFactory,
            ModuleDescriptor.Factory moduleDescriptorFactory,
            InjectionAnnotations injectionAnnotations
    ) {
        this.elements = elements;
        this.types = types;
        this.dependencyRequestFactory = dependencyRequestFactory;
        this.moduleDescriptorFactory = moduleDescriptorFactory;
        this.injectionAnnotations = injectionAnnotations;
    }
}
