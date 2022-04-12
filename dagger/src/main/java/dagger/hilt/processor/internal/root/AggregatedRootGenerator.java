package dagger.hilt.processor.internal.root;


import com.squareup.javapoet.AnnotationSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;

/** Generates an {@link dagger.hilt.internal.aggregatedroot.AggregatedRoot}. */
final class AggregatedRootGenerator {
    private final TypeElement rootElement;
    private final TypeElement originatingRootElement;
    private final TypeElement rootAnnotation;
    private final ProcessingEnvironment processingEnv;

    AggregatedRootGenerator(
            TypeElement rootElement,
            TypeElement originatingRootElement,
            TypeElement rootAnnotation,
            ProcessingEnvironment processingEnv) {
        this.rootElement = rootElement;
        this.originatingRootElement = originatingRootElement;
        this.rootAnnotation = rootAnnotation;
        this.processingEnv = processingEnv;
    }

    void generate() throws IOException {
        Processors.generateAggregatingClass(
                ClassNames.AGGREGATED_ROOT_PACKAGE,
                AnnotationSpec.builder(ClassNames.AGGREGATED_ROOT)
                        .addMember("root", "$S", rootElement.getQualifiedName())
                        .addMember("originatingRoot", "$S", originatingRootElement.getQualifiedName())
                        .addMember("rootAnnotation", "$T.class", rootAnnotation)
                        .build(),
                rootElement,
                getClass(),
                processingEnv);
    }
}
