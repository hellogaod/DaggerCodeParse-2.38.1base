package dagger.internal.codegen.validation;


import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import javax.inject.Inject;
import javax.lang.model.element.Element;

import androidx.room.compiler.processing.XElement;
import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.internal.codegen.javapoet.TypeNames;

/**
 * Processing step that validates that the {@code BindsInstance} annotation is applied to the
 * correct elements.
 */
public final class BindsInstanceProcessingStep extends TypeCheckingProcessingStep<XElement> {

    private final BindsInstanceMethodValidator methodValidator;
    private final BindsInstanceParameterValidator parameterValidator;
    private final XMessager messager;

    @Inject
    BindsInstanceProcessingStep(
            BindsInstanceMethodValidator methodValidator,
            BindsInstanceParameterValidator parameterValidator,
            XMessager messager
    ) {
        this.methodValidator = methodValidator;
        this.parameterValidator = parameterValidator;
        this.messager = messager;
    }

    @Override
    public ImmutableSet<ClassName> annotationClassNames() {
        return ImmutableSet.of(TypeNames.BINDS_INSTANCE);
    }

    @Override
    protected void process(XElement xElement, ImmutableSet<ClassName> annotations) {
        Element element = XConverters.toJavac(xElement);
        switch (element.getKind()) {
            case PARAMETER://1.@BindsInstance注解修饰参数，对该参数进行校验
                parameterValidator.validate(MoreElements.asVariable(element)).printMessagesTo(messager);
                break;
            case METHOD://2.@BindsInstance注解修饰方法，对该方法进行校验
                methodValidator.validate(MoreElements.asExecutable(element)).printMessagesTo(messager);
                break;
            default://3.@BindsInstance注解只能修饰方法参数和方法，其他的一律报错
                throw new AssertionError(element);
        }
    }
}
