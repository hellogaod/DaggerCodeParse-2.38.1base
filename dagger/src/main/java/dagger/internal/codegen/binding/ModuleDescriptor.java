package dagger.internal.codegen.binding;


import com.google.auto.value.AutoValue;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.base.ClearableCache;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;

/**
 * Contains metadata that describes a module.
 */
@AutoValue
public abstract class ModuleDescriptor {

    /**
     * A {@link ModuleDescriptor} factory.
     */
    @Singleton
    public static final class Factory {
        private final DaggerElements elements;
        private final KotlinMetadataUtil metadataUtil;
        private final BindingFactory bindingFactory;
        private final MultibindingDeclaration.Factory multibindingDeclarationFactory;
        private final DelegateDeclaration.Factory bindingDelegateDeclarationFactory;
        private final SubcomponentDeclaration.Factory subcomponentDeclarationFactory;
        private final OptionalBindingDeclaration.Factory optionalBindingDeclarationFactory;
        private final Map<TypeElement, ModuleDescriptor> cache = new HashMap<>();

        @Inject
        Factory(
                DaggerElements elements,
                KotlinMetadataUtil metadataUtil,
                BindingFactory bindingFactory,
                MultibindingDeclaration.Factory multibindingDeclarationFactory,
                DelegateDeclaration.Factory bindingDelegateDeclarationFactory,
                SubcomponentDeclaration.Factory subcomponentDeclarationFactory,
                OptionalBindingDeclaration.Factory optionalBindingDeclarationFactory) {
            this.elements = elements;
            this.metadataUtil = metadataUtil;
            this.bindingFactory = bindingFactory;
            this.multibindingDeclarationFactory = multibindingDeclarationFactory;
            this.bindingDelegateDeclarationFactory = bindingDelegateDeclarationFactory;
            this.subcomponentDeclarationFactory = subcomponentDeclarationFactory;
            this.optionalBindingDeclarationFactory = optionalBindingDeclarationFactory;
        }
    }
}
