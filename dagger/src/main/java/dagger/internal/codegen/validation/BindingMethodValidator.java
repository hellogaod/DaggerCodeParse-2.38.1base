package dagger.internal.codegen.validation;

import com.squareup.javapoet.ClassName;

import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: BindingMethodValidator
 * Author: 佛学徒
 * Date: 2021/10/22 10:58
 * Description:
 * History:
 */
abstract class BindingMethodValidator {

    private ClassName methodAnnotation;

    protected BindingMethodValidator(
            ClassName methodAnnotation) {
        this.methodAnnotation = methodAnnotation;
    }

    /**
     * The annotation that identifies binding methods validated by this object.
     */
    final ClassName methodAnnotation() {
        return methodAnnotation;
    }
}
