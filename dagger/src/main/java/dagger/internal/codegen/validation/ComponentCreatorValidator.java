package dagger.internal.codegen.validation;


import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.base.ClearableCache;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Validates types annotated with component creator annotations.
 */
@Singleton
public final class ComponentCreatorValidator implements ClearableCache {

    private final DaggerElements elements;
    private final DaggerTypes types;
    private final Map<TypeElement, ValidationReport> reports = new HashMap<>();
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    ComponentCreatorValidator(
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil metadataUtil
    ) {
        this.elements = elements;
        this.types = types;
        this.metadataUtil = metadataUtil;
    }

    @Override
    public void clearCache() {

    }
}
