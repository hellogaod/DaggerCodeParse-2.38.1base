package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.Preconditions;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ComponentRequirement;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.javapoet.Expression;

/**
 * A binding expression for component provision methods.
 *
 *component节点不是production的 componentAnnotation#dependencies里面的类的无参返回类型不是void的方法 生成的ProvisionBinding
 */
final class ComponentProvisionRequestRepresentation extends SimpleInvocationRequestRepresentation {
    private final ProvisionBinding binding;
    private final BindingGraph bindingGraph;
    private final ComponentRequirementExpressions componentRequirementExpressions;
    private final CompilerOptions compilerOptions;

    @AssistedInject
    ComponentProvisionRequestRepresentation(
            @Assisted ProvisionBinding binding,
            BindingGraph bindingGraph,
            ComponentRequirementExpressions componentRequirementExpressions,
            CompilerOptions compilerOptions) {
        super(binding);
        this.binding = binding;
        this.bindingGraph = bindingGraph;
        this.componentRequirementExpressions = componentRequirementExpressions;
        this.compilerOptions = compilerOptions;
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {
        CodeBlock invocation =
                CodeBlock.of(
                        "$L.$L()",
                        componentRequirementExpressions.getExpression(componentRequirement(), requestingClass),
                        binding.bindingElement().get().getSimpleName());
        return Expression.create(
                binding.contributedPrimitiveType().orElse(binding.key().type().java()),
                maybeCheckForNull(binding, compilerOptions, invocation));
    }

    private ComponentRequirement componentRequirement() {
        return bindingGraph
                .componentDescriptor()
                .getDependencyThatDefinesMethod(binding.bindingElement().get());
    }

    static CodeBlock maybeCheckForNull(
            ProvisionBinding binding, CompilerOptions compilerOptions, CodeBlock invocation) {
        return binding.shouldCheckForNull(compilerOptions)
                ? CodeBlock.of("$T.checkNotNullFromComponent($L)", Preconditions.class, invocation)
                : invocation;
    }

    @AssistedFactory
    static interface Factory {
        ComponentProvisionRequestRepresentation create(ProvisionBinding binding);
    }
}
