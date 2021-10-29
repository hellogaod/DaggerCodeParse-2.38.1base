package dagger.internal.codegen.writing;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ModuleProxies
 * Author: 佛学徒
 * Date: 2021/10/22 11:22
 * Description:
 * History:
 */
public final class ModuleProxies {

    private final DaggerElements elements;
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    public ModuleProxies(DaggerElements elements, KotlinMetadataUtil metadataUtil) {
        this.elements = elements;
        this.metadataUtil = metadataUtil;
    }
    /** Generates a {@code public static} proxy method for constructing module instances. */
    // TODO(dpb): See if this can become a SourceFileGenerator<ModuleDescriptor> instead. Doing so may
    // cause ModuleProcessingStep to defer elements multiple times.
    public static final class ModuleConstructorProxyGenerator
            extends SourceFileGenerator<TypeElement> {

        private final ModuleProxies moduleProxies;
        private final KotlinMetadataUtil metadataUtil;

        @Inject
        ModuleConstructorProxyGenerator(
                XFiler filer,
                DaggerElements elements,
                SourceVersion sourceVersion,
                ModuleProxies moduleProxies,
                KotlinMetadataUtil metadataUtil) {
            this.moduleProxies = moduleProxies;
            this.metadataUtil = metadataUtil;
        }
    }
}
