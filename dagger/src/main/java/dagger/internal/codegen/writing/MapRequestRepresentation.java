package dagger.internal.codegen.writing;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import java.util.Collections;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.MapBuilder;
import dagger.internal.codegen.base.MapType;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.BindingKind;
import dagger.spi.model.DependencyRequest;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.binding.BindingRequest.bindingRequest;
import static dagger.internal.codegen.binding.MapKeys.getMapKeyExpression;
import static dagger.internal.codegen.javapoet.CodeBlocks.toParametersCodeBlock;
import static dagger.internal.codegen.langmodel.Accessibility.isTypeAccessibleFrom;
import static dagger.spi.model.BindingKind.MULTIBOUND_MAP;
import static javax.lang.model.util.ElementFilter.methodsIn;

/** A {@link RequestRepresentation} for multibound maps. */
final class MapRequestRepresentation extends SimpleInvocationRequestRepresentation {
    /** Maximum number of key-value pairs that can be passed to ImmutableMap.of(K, V, K, V, ...). */
    private static final int MAX_IMMUTABLE_MAP_OF_KEY_VALUE_PAIRS = 5;

    private final ProvisionBinding binding;
    private final ImmutableMap<DependencyRequest, ContributionBinding> dependencies;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerTypes types;
    private final DaggerElements elements;

    @AssistedInject
    MapRequestRepresentation(
            @Assisted ProvisionBinding binding,
            BindingGraph graph,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types,
            DaggerElements elements) {
        super(binding);
        this.binding = binding;
        BindingKind bindingKind = this.binding.kind();
        checkArgument(bindingKind.equals(MULTIBOUND_MAP), bindingKind);
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.types = types;
        this.elements = elements;
        this.dependencies =
                Maps.toMap(binding.dependencies(), dep -> graph.contributionBinding(dep.key()));
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {
        // TODO(ronshapiro): We should also make an ImmutableMap version of MapFactory
        boolean isImmutableMapAvailable = isImmutableMapAvailable();
        // TODO(ronshapiro, gak): Use Maps.immutableEnumMap() if it's available?
        //当前项目存在ImmutableMap && 绑定对象的依赖少于等于5个
        if (isImmutableMapAvailable && dependencies.size() <= MAX_IMMUTABLE_MAP_OF_KEY_VALUE_PAIRS) {
            return Expression.create(
                    immutableMapType(),
                    CodeBlock.builder()
                            .add("$T.", ImmutableMap.class)
                            .add(maybeTypeParameters(requestingClass))
                            .add(
                                    "of($L)",
                                    dependencies
                                            .keySet()
                                            .stream()
                                            .map(dependency -> keyAndValueExpression(dependency, requestingClass))
                                            .collect(toParametersCodeBlock()))
                            .build());
        }

        switch (dependencies.size()) {
            case 0:
                return collectionsStaticFactoryInvocation(requestingClass, CodeBlock.of("emptyMap()"));
            case 1:
                return collectionsStaticFactoryInvocation(
                        requestingClass,
                        CodeBlock.of(
                                "singletonMap($L)",
                                keyAndValueExpression(getOnlyElement(dependencies.keySet()), requestingClass)));
            default:
                CodeBlock.Builder instantiation = CodeBlock.builder();
                instantiation
                        .add("$T.", isImmutableMapAvailable ? ImmutableMap.class : MapBuilder.class)
                        .add(maybeTypeParameters(requestingClass));
                if (isImmutableMapBuilderWithExpectedSizeAvailable()) {
                    instantiation.add("builderWithExpectedSize($L)", dependencies.size());
                } else if (isImmutableMapAvailable) {
                    instantiation.add("builder()");
                } else {
                    instantiation.add("newMapBuilder($L)", dependencies.size());
                }
                for (DependencyRequest dependency : dependencies.keySet()) {
                    instantiation.add(".put($L)", keyAndValueExpression(dependency, requestingClass));
                }
                return Expression.create(
                        isImmutableMapAvailable ? immutableMapType() : binding.key().type().java(),
                        instantiation.add(".build()").build());
        }
    }

    //Map<K,V>换成ImmutableMap<K,V>
    private DeclaredType immutableMapType() {
        MapType mapType = MapType.from(binding.key());
        return types.getDeclaredType(
                elements.getTypeElement(ImmutableMap.class), mapType.keyType(), mapType.valueType());
    }

    private CodeBlock keyAndValueExpression(DependencyRequest dependency, ClassName requestingClass) {
        return CodeBlock.of(
                "$L, $L",
                getMapKeyExpression(dependencies.get(dependency), requestingClass, elements),
                componentRequestRepresentations
                        .getDependencyExpression(bindingRequest(dependency), requestingClass)
                        .codeBlock());
    }

    private Expression collectionsStaticFactoryInvocation(
            ClassName requestingClass, CodeBlock methodInvocation) {
        return Expression.create(
                binding.key().type().java(),
                CodeBlock.builder()
                        .add("$T.", Collections.class)
                        .add(maybeTypeParameters(requestingClass))
                        .add(methodInvocation)
                        .build());
    }

    //根据访问权限决定使用哪种类型代码
    private CodeBlock maybeTypeParameters(ClassName requestingClass) {
        TypeMirror bindingKeyType = binding.key().type().java();
        MapType mapType = MapType.from(binding.key());
        return isTypeAccessibleFrom(bindingKeyType, requestingClass.packageName())
                ? CodeBlock.of("<$T, $T>", mapType.keyType(), mapType.valueType())
                : CodeBlock.of("");
    }

    //ImmutableMap.builderWithExpectedSize方法
    private boolean isImmutableMapBuilderWithExpectedSizeAvailable() {
        if (isImmutableMapAvailable()) {
            return methodsIn(elements.getTypeElement(ImmutableMap.class).getEnclosedElements())
                    .stream()
                    .anyMatch(method -> method.getSimpleName().contentEquals("builderWithExpectedSize"));
        }
        return false;
    }

    //当前项目存在ImmutableMap
    private boolean isImmutableMapAvailable() {
        return elements.getTypeElement(ImmutableMap.class) != null;
    }

    @AssistedFactory
    static interface Factory {
        MapRequestRepresentation create(ProvisionBinding binding);
    }
}
