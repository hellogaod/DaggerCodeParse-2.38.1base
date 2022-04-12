package dagger.hilt.processor.internal.root;


import com.squareup.javapoet.AnnotationSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;

/** Generates an {@link dagger.hilt.internal.processedrootsentinel.ProcessedRootSentinel}. */
final class ProcessedRootSentinelGenerator {
    private final TypeElement processedRoot;
    private final ProcessingEnvironment processingEnv;

    ProcessedRootSentinelGenerator(TypeElement processedRoot, ProcessingEnvironment processingEnv) {
        this.processedRoot = processedRoot;
        this.processingEnv = processingEnv;
    }

    void generate() throws IOException {
        Processors.generateAggregatingClass(
                ClassNames.PROCESSED_ROOT_SENTINEL_PACKAGE,
                AnnotationSpec.builder(ClassNames.PROCESSED_ROOT_SENTINEL)
                        .addMember("roots", "$S", processedRoot.getQualifiedName())
                        .build(),
                processedRoot,
                getClass(),
                processingEnv);
    }
}