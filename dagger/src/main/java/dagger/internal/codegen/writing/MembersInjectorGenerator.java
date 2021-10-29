package dagger.internal.codegen.writing;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.MembersInjectionBinding;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Generates {@link MembersInjector} implementations from {@link MembersInjectionBinding} instances.
 */
public final class MembersInjectorGenerator extends SourceFileGenerator<MembersInjectionBinding> {
    private final DaggerTypes types;
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    MembersInjectorGenerator(
            XFiler filer,
            DaggerElements elements,
            DaggerTypes types,
            SourceVersion sourceVersion,
            KotlinMetadataUtil metadataUtil
    ) {
        this.types = types;
        this.metadataUtil = metadataUtil;
    }
}
