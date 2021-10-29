package dagger.internal.codegen.writing;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.codegen.langmodel.DaggerElements;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: UnwrappedMapKeyGenerator
 * Author: 佛学徒
 * Date: 2021/10/22 10:49
 * Description:
 * History:
 */
public final class UnwrappedMapKeyGenerator extends AnnotationCreatorGenerator {

    @Inject
    UnwrappedMapKeyGenerator(XFiler filer, DaggerElements elements, SourceVersion sourceVersion) {
        super(filer, elements, sourceVersion);
    }
}
