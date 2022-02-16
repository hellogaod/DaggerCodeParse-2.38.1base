package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ComponentRequirement;
import dagger.internal.codegen.binding.ContributionBinding;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static dagger.internal.codegen.javapoet.TypeNames.dependencyMethodProducerOf;
import static dagger.internal.codegen.javapoet.TypeNames.listenableFutureOf;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * A {@link dagger.producers.Producer} creation expression for a production method on a production
 * component's {@linkplain dagger.producers.ProductionComponent#dependencies()} dependency} that
 * returns a {@link com.google.common.util.concurrent.ListenableFuture}.
 */
// TODO(dpb): Resolve with DependencyMethodProviderCreationExpression.
final class DependencyMethodProducerCreationExpression
        implements FrameworkFieldInitializer.FrameworkInstanceCreationExpression {
    private final ContributionBinding binding;
    private final ComponentImplementation componentImplementation;
    private final ComponentRequirementExpressions componentRequirementExpressions;
    private final BindingGraph graph;

    @AssistedInject
    DependencyMethodProducerCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequirementExpressions componentRequirementExpressions,
            BindingGraph graph) {
        this.binding = checkNotNull(binding);
        this.componentImplementation = componentImplementation;
        this.componentRequirementExpressions = componentRequirementExpressions;
        this.graph = graph;
    }

    @Override
    public CodeBlock creationExpression() {
        ComponentRequirement dependency =
                graph.componentDescriptor().getDependencyThatDefinesMethod(binding.bindingElement().get());
        FieldSpec dependencyField =
                FieldSpec.builder(
                        ClassName.get(dependency.typeElement()), dependency.variableName(), PRIVATE, FINAL)
                        .initializer(
                                componentRequirementExpressions.getExpressionDuringInitialization(
                                        dependency,
                                        // This isn't a real class name, but we want the requesting class for the
                                        // expression to *not* be the same class as the component implementation,
                                        // because it isn't... it's an anonymous inner class.
                                        // TODO(cgdecker): If we didn't use an anonymous inner class here but instead
                                        // generated a named nested class as with
                                        // DependencyMethodProviderCreationExpression, we wouldn't need to deal with
                                        // this and might be able to avoid potentially creating an extra field in the
                                        // component?
                                        componentImplementation.name().nestedClass("Anonymous")))
                        .build();
        // TODO(b/70395982): Explore using a private static type instead of an anonymous class.
        TypeName keyType = TypeName.get(binding.key().type().java());
        return CodeBlock.of(
                "$L",
                anonymousClassBuilder("")
                        .superclass(dependencyMethodProducerOf(keyType))
                        .addField(dependencyField)
                        .addMethod(
                                methodBuilder("callDependencyMethod")
                                        .addAnnotation(Override.class)
                                        .addModifiers(PUBLIC)
                                        .returns(listenableFutureOf(keyType))
                                        .addStatement(
                                                "return $N.$L()",
                                                dependencyField,
                                                binding.bindingElement().get().getSimpleName())
                                        .build())
                        .build());
    }

    @AssistedFactory
    static interface Factory {
        DependencyMethodProducerCreationExpression create(ContributionBinding binding);
    }
}
