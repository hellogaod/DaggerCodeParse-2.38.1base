package dagger.internal.codegen.binding;

import javax.inject.Inject;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: BindingDeclarationFormatter
 * Author: 佛学徒
 * Date: 2021/10/22 11:17
 * Description:
 * History:
 */
public class BindingDeclarationFormatter {
    private final MethodSignatureFormatter methodSignatureFormatter;

    @Inject
    BindingDeclarationFormatter(MethodSignatureFormatter methodSignatureFormatter) {
        this.methodSignatureFormatter = methodSignatureFormatter;
    }
}
