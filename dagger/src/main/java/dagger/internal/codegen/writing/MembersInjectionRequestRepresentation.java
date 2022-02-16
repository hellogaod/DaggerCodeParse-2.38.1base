package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.element.ExecutableElement;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.MembersInjectionBinding;
import dagger.internal.codegen.javapoet.Expression;

import static com.google.common.collect.Iterables.getOnlyElement;
import static javax.lang.model.type.TypeKind.VOID;

/**
 * A binding expression for members injection component methods. See {@link
 * MembersInjectionMethods}.
 */
final class MembersInjectionRequestRepresentation extends RequestRepresentation {

    private final MembersInjectionBinding binding;
    private final MembersInjectionMethods membersInjectionMethods;

    @AssistedInject
    MembersInjectionRequestRepresentation(
            @Assisted MembersInjectionBinding binding,
            MembersInjectionMethods membersInjectionMethods
    ) {
        this.binding = binding;
        this.membersInjectionMethods = membersInjectionMethods;
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {
        throw new UnsupportedOperationException(binding.toString());
    }

    // TODO(ronshapiro): This class doesn't need to be a RequestRepresentation, as
    // getDependencyExpression() should never be called for members injection methods. It's probably
    // better suited as a method on MembersInjectionMethods
    // e.g.
    //（1）返回重写的void inject(ComponentProcessor processor)方法中的代码片段:  injectComponentProcessor(processor);
    // ①加入的代码片段，以及②该代码片段执行的代码，以及③该执行的代码需要的其他一坨代码
    @Override
    protected CodeBlock getComponentMethodImplementation(
            ComponentDescriptor.ComponentMethodDescriptor componentMethod, ComponentImplementation component) {
        ExecutableElement methodElement = componentMethod.methodElement();
        ParameterSpec parameter = ParameterSpec.get(getOnlyElement(methodElement.getParameters()));

        if (binding.injectionSites().isEmpty()) {
            return methodElement.getReturnType().getKind().equals(VOID)
                    ? CodeBlock.of("")
                    : CodeBlock.of("return $N;", parameter);
        } else {
            ClassName requestingClass = component.name();
            return methodElement.getReturnType().getKind().equals(VOID)
                    //返回的代码片段是：injectComponentProcessor(processor)
                    ? CodeBlock.of("$L;", membersInjectionInvocation(parameter, requestingClass).codeBlock())
                    : CodeBlock.of(
                    "return $L;", membersInjectionInvocation(parameter, requestingClass).codeBlock());
        }
    }

    private Expression membersInjectionInvocation(ParameterSpec target, ClassName requestingClass) {
        //e.g.
        //binding表示 ComponentProcessor类使用Inject修饰的变量生成的MembersInjectionBinding对象
        //binding.key()： ComponentProcessor类作为type类型生成的key；
        //CodeBlock.of("$N", target)：void inject(ComponentProcessor processor)方法参数作为代码块
        //requestingClass：新生成的Component类
        return membersInjectionMethods.getInjectExpression(
                binding.key(), CodeBlock.of("$N", target), requestingClass);
    }

    @AssistedFactory
    static interface Factory {
        MembersInjectionRequestRepresentation create(MembersInjectionBinding binding);
    }
}
