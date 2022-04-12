package dagger.hilt.processor.internal.generatesrootinput;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;

/** Generates resource files for {@link GeneratesRootInputs}. */
final class GeneratesRootInputPropagatedDataGenerator {
    private final ProcessingEnvironment processingEnv;
    private final Element element;

    GeneratesRootInputPropagatedDataGenerator(ProcessingEnvironment processingEnv, Element element) {
        this.processingEnv = processingEnv;
        this.element = element;
    }

    void generate() throws IOException {
        TypeSpec.Builder generator =
                TypeSpec.classBuilder(Processors.getFullEnclosedName(element))
                        .addOriginatingElement(element)
                        .addAnnotation(
                                AnnotationSpec.builder(ClassNames.GENERATES_ROOT_INPUT_PROPAGATED_DATA)
                                        .addMember("value", "$T.class", element)
                                        .build())
                        .addJavadoc(
                                "Generated class to"
                                        + "get the list of annotations that generate input for root.\n");

        Processors.addGeneratedAnnotation(generator, processingEnv, getClass());

        JavaFile.builder(GeneratesRootInputs.AGGREGATING_PACKAGE, generator.build())
                .build()
                .writeTo(processingEnv.getFiler());
    }
}
