package dagger.internal.codegen.binding;


import com.google.auto.common.AnnotationMirrors;
import com.google.common.base.Equivalence;

import javax.inject.Inject;
import javax.lang.model.element.AnnotationMirror;

import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;

/** Utilities relating to annotations defined in the {@code javax.inject} package. */
public final class InjectionAnnotations {

    private final DaggerElements elements;
    private final KotlinMetadataUtil kotlinMetadataUtil;

    @Inject
    InjectionAnnotations(
            DaggerElements elements,
            KotlinMetadataUtil kotlinMetadataUtil
    ) {
        this.elements = elements;
        this.kotlinMetadataUtil = kotlinMetadataUtil;
    }
}
