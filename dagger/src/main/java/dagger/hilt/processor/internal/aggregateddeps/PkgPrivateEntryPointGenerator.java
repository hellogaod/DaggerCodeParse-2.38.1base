package dagger.hilt.processor.internal.aggregateddeps;


import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import dagger.hilt.processor.internal.Processors;

/**
 * Generates a public Dagger entrypoint that includes a user's pkg-private entrypoint. This allows a
 * user's entrypoint to use pkg-private visibility to hide from external packages.
 */
final class PkgPrivateEntryPointGenerator {
    private final ProcessingEnvironment env;
    private final PkgPrivateMetadata metadata;

    PkgPrivateEntryPointGenerator(ProcessingEnvironment env, PkgPrivateMetadata metadata) {
        this.env = env;
        this.metadata = metadata;
    }

    // This method creates the following generated code for an EntryPoint in pkg.MyEntryPoint that is
    // package
    // private
    //
    // package pkg; //same package
    //
    // import dagger.hilt.InstallIn;
    // import dagger.hilt.EntryPoint;;
    // import javax.annotation.Generated;
    //
    // @Generated("PkgPrivateEntryPointGenerator")
    // @InstallIn(ActivityComponent.class)
    // @OriginatingElement(topLevelClass = MyEntryPoint.class)
    // @EntryPoint
    // public interface HiltWrapper_MyEntryPoint implements MyEntryPoint  {
    // }
    void generate() throws IOException {

        TypeSpec.Builder entryPointInterfaceBuilder =
                TypeSpec.interfaceBuilder(metadata.generatedClassName().simpleName())
                        .addOriginatingElement(metadata.getTypeElement())
                        .addAnnotation(Processors.getOriginatingElementAnnotation(metadata.getTypeElement()))
                        .addModifiers(Modifier.PUBLIC)
                        .addSuperinterface(metadata.baseClassName())
                        .addAnnotation(metadata.getAnnotation());

        Processors.addGeneratedAnnotation(entryPointInterfaceBuilder, env, getClass());

        if (metadata.getOptionalInstallInAnnotationMirror().isPresent()) {
            entryPointInterfaceBuilder.addAnnotation(
                    AnnotationSpec.get(metadata.getOptionalInstallInAnnotationMirror().get()));
        }

        JavaFile.builder(
                metadata.generatedClassName().packageName(), entryPointInterfaceBuilder.build())
                .build()
                .writeTo(env.getFiler());
    }
}
