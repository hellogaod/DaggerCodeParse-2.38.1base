package dagger.internal.codegen.writing;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.CodeBlock;

import javax.inject.Provider;
import javax.lang.model.type.TypeMirror;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.base.MapType;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.producers.Produced;
import dagger.producers.Producer;
import dagger.spi.model.DependencyRequest;

import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.MapKeys.getMapKeyExpression;
import static dagger.internal.codegen.binding.SourceFiles.mapFactoryClassName;

/**
 * A factory creation expression for a multibound map.
 * 如果key及其变异匹配上
 * （1）@Provides或@Produces或@Binds修饰的bindingMethod，该bindingMethod还是
 * 用了@IntoMap或@IntoSet或@ElementsIntoSet、
 * （2）@Multibinds修饰的bindingMethod方法。
 * 该key的type是Map<K,V>，那么生成的Binding对象的BindingKind属性；
 */
final class MapFactoryCreationExpression extends MultibindingFactoryCreationExpression {

    private final ComponentImplementation componentImplementation;
    private final BindingGraph graph;
    private final ContributionBinding binding;
    private final DaggerElements elements;

    @AssistedInject
    MapFactoryCreationExpression(
            @Assisted ContributionBinding binding,
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations componentRequestRepresentations,
            BindingGraph graph,
            DaggerElements elements) {
        super(binding, componentImplementation, componentRequestRepresentations);
        this.binding = checkNotNull(binding);
        this.componentImplementation = componentImplementation;
        this.graph = graph;
        this.elements = elements;
    }

    @Override
    public CodeBlock creationExpression() {
        CodeBlock.Builder builder = CodeBlock.builder().add("$T.", mapFactoryClassName(binding));
        if (!useRawType()) {
            MapType mapType = MapType.from(binding.key().type().java());
            // TODO(ronshapiro): either inline this into mapFactoryClassName, or add a
            // mapType.unwrappedValueType() method that doesn't require a framework type
            TypeMirror valueType = mapType.valueType();
            for (Class<?> frameworkClass :
                    ImmutableSet.of(Provider.class, Producer.class, Produced.class)) {
                if (mapType.valuesAreTypeOf(frameworkClass)) {
                    valueType = mapType.unwrappedValueType(frameworkClass);
                    break;
                }
            }
            builder.add("<$T, $T>", mapType.keyType(), valueType);
        }

        builder.add("builder($L)", binding.dependencies().size());

        for (DependencyRequest dependency : binding.dependencies()) {
            ContributionBinding contributionBinding = graph.contributionBinding(dependency.key());
            builder.add(
                    ".put($L, $L)",
                    getMapKeyExpression(contributionBinding, componentImplementation.name(), elements),
                    multibindingDependencyExpression(dependency));
        }
        builder.add(".build()");

        return builder.build();
    }

    @AssistedFactory
    static interface Factory {
        MapFactoryCreationExpression create(ContributionBinding binding);
    }
}
