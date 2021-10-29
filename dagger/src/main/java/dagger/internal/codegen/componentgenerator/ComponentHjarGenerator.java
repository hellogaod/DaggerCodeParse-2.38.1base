package dagger.internal.codegen.componentgenerator;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

final class ComponentHjarGenerator extends SourceFileGenerator<ComponentDescriptor> {
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    ComponentHjarGenerator(
            XFiler filer,
            DaggerElements elements,
            DaggerTypes types,
            SourceVersion sourceVersion,
            KotlinMetadataUtil metadataUtil) {

        this.elements = elements;
        this.types = types;
        this.metadataUtil = metadataUtil;
    }

}
