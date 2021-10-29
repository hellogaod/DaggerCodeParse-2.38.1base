package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.langmodel.DaggerElements;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: AnnotationCreatorGenerator
 * Author: 佛学徒
 * Date: 2021/10/22 10:48
 * Description:
 * History:
 */
public class AnnotationCreatorGenerator extends SourceFileGenerator<TypeElement> {
    private static final ClassName AUTO_ANNOTATION =
            ClassName.get("com.google.auto.value", "AutoAnnotation");

    @Inject
    AnnotationCreatorGenerator(XFiler filer, DaggerElements elements, SourceVersion sourceVersion) {

    }
}
