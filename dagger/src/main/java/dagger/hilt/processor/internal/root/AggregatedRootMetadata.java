package dagger.hilt.processor.internal.root;


import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.AggregatedElements;
import dagger.hilt.processor.internal.AnnotationValues;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;
import dagger.hilt.processor.internal.root.ir.AggregatedRootIr;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/**
 * Represents the values stored in an {@link dagger.hilt.internal.aggregatedroot.AggregatedRoot}.
 */
@AutoValue
abstract class AggregatedRootMetadata {

    /**
     * Returns the aggregating element
     */
    public abstract TypeElement aggregatingElement();

    /**
     * Returns the element that was annotated with the root annotation.
     */
    abstract TypeElement rootElement();

    /**
     * Returns the originating root element. In most cases this will be the same as
     * {@link #rootElement()}.
     */
    abstract TypeElement originatingRootElement();

    /**
     * Returns the root annotation as an element.
     */
    abstract TypeElement rootAnnotation();

    /**
     * Returns whether this root can use a shared component.
     */
    abstract boolean allowsSharingComponent();

    @Memoized
    RootType rootType() {
        return RootType.of(rootElement());
    }

    static ImmutableSet<AggregatedRootMetadata> from(ProcessingEnvironment env) {
        return from(
                //dagger.hilt.internal.aggregatedroot.codegen包下使用@AggregatedRoot修饰的节点
                AggregatedElements.from(
                        ClassNames.AGGREGATED_ROOT_PACKAGE, ClassNames.AGGREGATED_ROOT, env.getElementUtils()),
                env);
    }

    /**
     * Returns metadata for each aggregated element.
     */
    public static ImmutableSet<AggregatedRootMetadata> from(
            ImmutableSet<TypeElement> aggregatedElements, ProcessingEnvironment env) {
        return aggregatedElements.stream()
                .map(aggregatedElement -> create(aggregatedElement, env))
                .collect(toImmutableSet());
    }

    public static AggregatedRootIr toIr(AggregatedRootMetadata metadata) {
        return new AggregatedRootIr(
                ClassName.get(metadata.aggregatingElement()),
                ClassName.get(metadata.rootElement()),
                ClassName.get(metadata.originatingRootElement()),
                ClassName.get(metadata.rootAnnotation()),
                metadata.allowsSharingComponent());
    }

    private static AggregatedRootMetadata create(TypeElement element, ProcessingEnvironment env) {
        AnnotationMirror annotationMirror =
                Processors.getAnnotationMirror(element, ClassNames.AGGREGATED_ROOT);

        ImmutableMap<String, AnnotationValue> values =
                Processors.getAnnotationValues(env.getElementUtils(), annotationMirror);

        TypeElement rootElement =
                env.getElementUtils().getTypeElement(AnnotationValues.getString(values.get("root")));
        boolean allowSharingComponent = true;
        //dagger.hilt.internal.aggregatedroot.codegen包下使用@AggregatedRoot修饰的节点,
        // 以及@AggregatedRoot#root中的节点、@AggregatedRoot#originatingRoot中的节点、@AggregatedRoot#rootAnnotation中的节点，
        return new AutoValue_AggregatedRootMetadata(
                element,
                rootElement,
                env.getElementUtils()
                        .getTypeElement(AnnotationValues.getString(values.get("originatingRoot"))),
                AnnotationValues.getTypeElement(values.get("rootAnnotation")),
                allowSharingComponent);
    }
}
