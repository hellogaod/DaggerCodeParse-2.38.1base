package dagger.hilt.processor.internal.aliasof;


import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import dagger.hilt.processor.internal.AggregatedElements;
import dagger.hilt.processor.internal.AnnotationValues;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;
import dagger.hilt.processor.internal.root.ir.AliasOfPropagatedDataIr;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/**
 * A class that represents the values stored in an {@link
 * dagger.hilt.internal.aliasof.AliasOfPropagatedData} annotation.
 */
@AutoValue
public abstract class AliasOfPropagatedDataMetadata {

    /** Returns the aggregating element */
    public abstract TypeElement aggregatingElement();

    abstract TypeElement defineComponentScopeElement();

    abstract TypeElement aliasElement();

    /** Returns metadata for all aggregated elements in the aggregating package. */
    public static ImmutableSet<AliasOfPropagatedDataMetadata> from(Elements elements) {
        return from(
                AggregatedElements.from(
                        ClassNames.ALIAS_OF_PROPAGATED_DATA_PACKAGE,
                        ClassNames.ALIAS_OF_PROPAGATED_DATA,
                        elements),
                elements);
    }

    /** Returns metadata for each aggregated element. */
    public static ImmutableSet<AliasOfPropagatedDataMetadata> from(
            ImmutableSet<TypeElement> aggregatedElements, Elements elements) {
        return aggregatedElements.stream()
                .map(aggregatedElement -> create(aggregatedElement, elements))
                .collect(toImmutableSet());
    }

    public static AliasOfPropagatedDataIr toIr(AliasOfPropagatedDataMetadata metadata) {
        return new AliasOfPropagatedDataIr(
                ClassName.get(metadata.aggregatingElement()),
                ClassName.get(metadata.defineComponentScopeElement()),
                ClassName.get(metadata.aliasElement()));
    }

    private static AliasOfPropagatedDataMetadata create(TypeElement element, Elements elements) {
        AnnotationMirror annotationMirror =
                Processors.getAnnotationMirror(element, ClassNames.ALIAS_OF_PROPAGATED_DATA);

        ImmutableMap<String, AnnotationValue> values =
                Processors.getAnnotationValues(elements, annotationMirror);

        return new AutoValue_AliasOfPropagatedDataMetadata(
                element,
                AnnotationValues.getTypeElement(values.get("defineComponentScope")),
                AnnotationValues.getTypeElement(values.get("alias")));
    }
}
