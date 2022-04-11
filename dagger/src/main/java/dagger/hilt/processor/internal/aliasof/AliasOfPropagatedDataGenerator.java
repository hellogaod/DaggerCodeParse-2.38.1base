package dagger.hilt.processor.internal.aliasof;


import com.squareup.javapoet.AnnotationSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;

/**
 * Generates resource files for {@link dagger.hilt.migration.AliasOf}.
 */
final class AliasOfPropagatedDataGenerator {

    private final ProcessingEnvironment processingEnv;
    private final TypeElement aliasScope;
    private final TypeElement defineComponentScope;

    AliasOfPropagatedDataGenerator(
            ProcessingEnvironment processingEnv,
            TypeElement aliasScope,
            TypeElement defineComponentScope) {
        this.processingEnv = processingEnv;
        this.aliasScope = aliasScope;
        this.defineComponentScope = defineComponentScope;
    }

    void generate() throws IOException {
        Processors.generateAggregatingClass(
                ClassNames.ALIAS_OF_PROPAGATED_DATA_PACKAGE,
                AnnotationSpec.builder(ClassNames.ALIAS_OF_PROPAGATED_DATA)
                        .addMember("defineComponentScope", "$T.class", defineComponentScope)
                        .addMember("alias", "$T.class", aliasScope)
                        .build(),
                aliasScope,
                getClass(),
                processingEnv);
    }
}
