package dagger.hilt.processor.internal.definecomponent;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.ComponentDescriptor;
import dagger.hilt.processor.internal.ProcessorErrors;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/**
 * A utility class for getting {@link DefineComponentMetadatas.DefineComponentMetadata} and {@link
 * DefineComponentBuilderMetadatas.DefineComponentBuilderMetadata}.
 */
public final class DefineComponents {

    public static DefineComponents create() {
        return new DefineComponents();
    }

    private final Map<Element, ComponentDescriptor> componentDescriptors = new HashMap<>();
    private final DefineComponentMetadatas componentMetadatas = DefineComponentMetadatas.create();
    private final DefineComponentBuilderMetadatas componentBuilderMetadatas =
            DefineComponentBuilderMetadatas.create(componentMetadatas);

    private DefineComponents() {}

    /** Returns the {@link ComponentDescriptor} for the given component element. */
    // TODO(b/144940889): This descriptor doesn't contain the "creator" or the "installInName".
    public ComponentDescriptor componentDescriptor(Element element) {
        if (!componentDescriptors.containsKey(element)) {
            componentDescriptors.put(element, uncachedComponentDescriptor(element));
        }
        return componentDescriptors.get(element);
    }

    private ComponentDescriptor uncachedComponentDescriptor(Element element) {
        DefineComponentMetadatas.DefineComponentMetadata metadata = componentMetadatas.get(element);
        ComponentDescriptor.Builder builder =
                ComponentDescriptor.builder()
                        .component(ClassName.get(metadata.component()))
                        .scopes(metadata.scopes().stream().map(ClassName::get).collect(toImmutableSet()));


        metadata.parentMetadata()
                .map(DefineComponentMetadatas.DefineComponentMetadata::component)
                .map(this::componentDescriptor)
                .ifPresent(builder::parent);

        return builder.build();
    }

    /** Returns the set of aggregated {@link ComponentDescriptor}s. */
    public ImmutableSet<ComponentDescriptor> getComponentDescriptors(
            ImmutableSet<DefineComponentClassesMetadata> aggregatedMetadatas) {
        ImmutableSet<DefineComponentMetadatas.DefineComponentMetadata> components =
                aggregatedMetadatas.stream()
                        .filter(DefineComponentClassesMetadata::isComponent)
                        .map(DefineComponentClassesMetadata::element)
                        .map(componentMetadatas::get)
                        .collect(toImmutableSet());

        ImmutableSet<DefineComponentBuilderMetadatas.DefineComponentBuilderMetadata> builders =
                aggregatedMetadatas.stream()
                        .filter(DefineComponentClassesMetadata::isComponentBuilder)
                        .map(DefineComponentClassesMetadata::element)
                        .map(componentBuilderMetadatas::get)
                        .collect(toImmutableSet());

        ListMultimap<DefineComponentMetadatas.DefineComponentMetadata, DefineComponentBuilderMetadatas.DefineComponentBuilderMetadata> builderMultimap =
                ArrayListMultimap.create();
        builders.forEach(builder -> builderMultimap.put(builder.componentMetadata(), builder));

        // Check that there are not multiple builders per component
        for (DefineComponentMetadatas.DefineComponentMetadata componentMetadata : builderMultimap.keySet()) {
            TypeElement component = componentMetadata.component();
            ProcessorErrors.checkState(
                    builderMultimap.get(componentMetadata).size() <= 1,
                    component,
                    "Multiple @%s declarations are not allowed for @%s type, %s. Found: %s",
                    ClassNames.DEFINE_COMPONENT_BUILDER,
                    ClassNames.DEFINE_COMPONENT,
                    component,
                    builderMultimap.get(componentMetadata).stream()
                            .map(DefineComponentBuilderMetadatas.DefineComponentBuilderMetadata::builder)
                            .map(TypeElement::toString)
                            .sorted()
                            .collect(toImmutableList()));
        }

        // Now that we know there is at most 1 builder per component, convert the Multimap to Map.
        Map<DefineComponentMetadatas.DefineComponentMetadata, DefineComponentBuilderMetadatas.DefineComponentBuilderMetadata> builderMap = new LinkedHashMap<>();
        builderMultimap.entries().forEach(e -> builderMap.put(e.getKey(), e.getValue()));

        return components.stream()
                .map(componentMetadata -> toComponentDescriptor(componentMetadata, builderMap))
                .collect(toImmutableSet());
    }

    private static ComponentDescriptor toComponentDescriptor(
            DefineComponentMetadatas.DefineComponentMetadata componentMetadata,
            Map<DefineComponentMetadatas.DefineComponentMetadata, DefineComponentBuilderMetadatas.DefineComponentBuilderMetadata> builderMap) {
        ComponentDescriptor.Builder builder =
                ComponentDescriptor.builder()
                        .component(ClassName.get(componentMetadata.component()))
                        .scopes(
                                componentMetadata.scopes().stream().map(ClassName::get).collect(toImmutableSet()));


        if (builderMap.containsKey(componentMetadata)) {
            builder.creator(ClassName.get(builderMap.get(componentMetadata).builder()));
        }

        componentMetadata
                .parentMetadata()
                .map(parent -> toComponentDescriptor(parent, builderMap))
                .ifPresent(builder::parent);

        return builder.build();
    }
}
