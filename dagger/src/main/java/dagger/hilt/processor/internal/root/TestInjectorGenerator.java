package dagger.hilt.processor.internal.root;


import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;

/** Generates an entry point for a test. */
public final class TestInjectorGenerator {
    private final ProcessingEnvironment env;
    private final TestRootMetadata metadata;

    TestInjectorGenerator(ProcessingEnvironment env, TestRootMetadata metadata) {
        this.env = env;
        this.metadata = metadata;
    }

    // @OriginatingElement(topLevelClass = FooTest.class)
    // @GeneratedEntryPoint
    // @InstallIn(SingletonComponent.class)
    // @Generated("TestInjectorGenerator")
    // public interface FooTest_GeneratedInjector {
    //   public void injectTest(FooTest fooTest);
    // }
    public void generate() throws IOException {

        TypeSpec.Builder builder =
                TypeSpec.interfaceBuilder(metadata.testInjectorName())
                        .addOriginatingElement(metadata.testElement())
                        .addAnnotation(Processors.getOriginatingElementAnnotation(metadata.testElement()))
                        .addAnnotation(ClassNames.GENERATED_ENTRY_POINT)
                        .addAnnotation(
                                AnnotationSpec.builder(ClassNames.INSTALL_IN)
                                        .addMember("value", "$T.class", installInComponent(metadata.testElement()))
                                        .build())
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(
                                MethodSpec.methodBuilder("injectTest")
                                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                        .addParameter(
                                                metadata.testName(),
                                                Processors.upperToLowerCamel(metadata.testName().simpleName()))
                                        .build());

        Processors.addGeneratedAnnotation(builder, env, getClass());

        JavaFile.builder(metadata.testInjectorName().packageName(), builder.build())
                .build()
                .writeTo(env.getFiler());
    }

    private static ClassName installInComponent(TypeElement testElement) {
        return ClassNames.SINGLETON_COMPONENT;
    }
}
