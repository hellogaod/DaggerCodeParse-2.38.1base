package dagger.internal.codegen.writing;

import com.squareup.javapoet.CodeBlock;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.base.ContributionType;
import dagger.internal.codegen.base.SetType;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.BindingType;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.producers.Produced;
import dagger.spi.model.DependencyRequest;

import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.SourceFiles.setFactoryClassName;

/** A factory creation expression for a multibound set. */
final class SetFactoryCreationExpression extends MultibindingFactoryCreationExpression {
    private final BindingGraph graph;
    private final ContributionBinding binding;

    @AssistedInject
    SetFactoryCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations,
            BindingGraph graph) {
        super(binding, componentImplementation, componentRequestRepresentations);
        this.binding = checkNotNull(binding);
        this.graph = graph;
    }

    @Override
    public CodeBlock creationExpression() {
        CodeBlock.Builder builder = CodeBlock.builder().add("$T.", setFactoryClassName(binding));
        if (!useRawType()) {
            SetType setType = SetType.from(binding.key());
            builder.add(
                    "<$T>",
                    setType.elementsAreTypeOf(Produced.class)
                            ? setType.unwrappedElementType(Produced.class)
                            : setType.elementType());
        }

        int individualProviders = 0;
        int setProviders = 0;
        CodeBlock.Builder builderMethodCalls = CodeBlock.builder();
        String methodNameSuffix =
                binding.bindingType().equals(BindingType.PROVISION) ? "Provider" : "Producer";

        for (DependencyRequest dependency : binding.dependencies()) {
            ContributionType contributionType =
                    graph.contributionBinding(dependency.key()).contributionType();
            String methodNamePrefix;
            switch (contributionType) {
                case SET:
                    individualProviders++;
                    methodNamePrefix = "add";
                    break;
                case SET_VALUES:
                    setProviders++;
                    methodNamePrefix = "addCollection";
                    break;
                default:
                    throw new AssertionError(dependency + " is not a set multibinding");
            }

            builderMethodCalls.add(
                    ".$N$N($L)",
                    methodNamePrefix,
                    methodNameSuffix,
                    multibindingDependencyExpression(dependency));
        }
        builder.add("builder($L, $L)", individualProviders, setProviders);
        builder.add(builderMethodCalls.build());

        return builder.add(".build()").build();
    }

    @AssistedFactory
    static interface Factory {
        SetFactoryCreationExpression create(ContributionBinding binding);
    }
}
