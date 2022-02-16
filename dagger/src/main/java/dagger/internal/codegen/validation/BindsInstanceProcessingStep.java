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
            case PARAMETER:
                //1. 对@BindsInstance修饰的方法参数校验：
                // - （1）当前使用@BindsInstance修饰的方法参数：
                //  - ①该参数不能使用FrameworkType架构类型：Provider<T>,Lazy<T>,MembersInjector<T>,Produced<T>,Producer<T>；
                //  - ②该参数在没有使用Qualifier修饰的注解修饰情况下，参数类型的构造函数不能使用AssistedInject修饰并且参数类型不能使用AssistedFactory注解修饰；
                //  - ③参数类型只能是原始类型或数组或接口或类或变量类型；
                // - （2）参数节点最多只能使用一个Qualifier修饰的注解修饰；
                // - （3）参数节点不能使用Scope修饰的注解修饰；
                // - （4）参数所在方法必须是abstract修饰的抽象方法或接口中的非default方法；
                // - （5）@BindsInstance修饰的参数所在方法的返回类型，只能是一个类或接口（可以是泛型），不能是void、数组又或者原始类型。
                parameterValidator.validate(MoreElements.asVariable(element)).printMessagesTo(messager);
                break;
            case METHOD:
                //2. 对@BindsInstance修饰的方法进行校验：
                // - （1）当前使用@BindsInstance修饰的方法校验：
                //  - 注：@BindsInstance修饰的方法有且仅有一个参数
                //  - ① 唯一参数的类型不能使用FrameworkType架构类型：Provider<T>,Lazy<T>,MembersInjector<T>,Produced<T>,Producer<T>；
                //  - ② 唯一参数的类型的构造函数不能使用AssistedInject修饰并且该参数节点不能使用AssistedFactory注解修饰；
                //  - ③ 唯一的参数类型只能是原始类型或数组或接口或类或变量类型；
                // - （2）@BindsInstance修饰的方法上最多只允许被一个Qualifier修饰的注解修饰；
                // - （3）@BindsInstance修饰的方法不能使用Scope修饰的注解修饰；
                // - （4）@BindsInstance修饰的方法必须使用abstract修饰或接口非default修饰的普通方法；
                // - （5）@BindsInstance修饰的方法所在父节点不允许是module节点，也不允许是componentAll节点，只能在creator节点中使用；
                methodValidator.validate(MoreElements.asExecutable(element)).printMessagesTo(messager);
                break;
            default:
                throw new AssertionError(element);
        }
    }
}
