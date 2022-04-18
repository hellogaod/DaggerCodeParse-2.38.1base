package dagger.hilt.processor.internal.root;


import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import dagger.hilt.processor.internal.AggregatedElements;
import dagger.hilt.processor.internal.AnnotationValues;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;
import dagger.hilt.processor.internal.root.ir.ProcessedRootSentinelIr;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/**
 * Represents the values stored in an {@link
 * dagger.hilt.internal.processedrootsentinel.ProcessedRootSentinel}.
 */
@AutoValue
abstract class ProcessedRootSentinelMetadata {

    /**
     * Returns the aggregating element
     */
    public abstract TypeElement aggregatingElement();

    /**
     * Returns the processed root elements.
     */
    abstract ImmutableSet<TypeElement> rootElements();

    static ImmutableSet<ProcessedRootSentinelMetadata> from(Elements elements) {
        //dagger.hilt.internal.processedrootsentinel.codegen包下使用@ProcessedRootSentinel注解修饰的节点
        return AggregatedElements.from(
                ClassNames.PROCESSED_ROOT_SENTINEL_PACKAGE,
                ClassNames.PROCESSED_ROOT_SENTINEL,
                elements)
                .stream()
                .map(aggregatedElement -> create(aggregatedElement, elements))
                .collect(toImmutableSet());
    }

    static ProcessedRootSentinelIr toIr(ProcessedRootSentinelMetadata metadata) {
        return new ProcessedRootSentinelIr(
                ClassName.get(metadata.aggregatingElement()),
                metadata.rootElements().stream().map(ClassName::get).collect(Collectors.toList())
        );
    }

    private static ProcessedRootSentinelMetadata create(TypeElement element, Elements elements) {
        AnnotationMirror annotationMirror =
                Processors.getAnnotationMirror(element, ClassNames.PROCESSED_ROOT_SENTINEL);

        ImmutableMap<String, AnnotationValue> values =
                Processors.getAnnotationValues(elements, annotationMirror);

        //aggregatingElement:dagger.hilt.internal.processedrootsentinel.codegen包下使用@ProcessedRootSentinel注解修饰的节点
        //rootElements:@ProcessedRootSentinel注解的roots值
        return new AutoValue_ProcessedRootSentinelMetadata(
                element,
                AnnotationValues.getStrings(values.get("roots")).stream()
                        .map(elements::getTypeElement)
                        .collect(toImmutableSet()));
    }
}
