package dagger.hilt.processor.internal.aggregateddeps;


import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import dagger.hilt.processor.internal.Processors;

/**
 * Generates a public Dagger module that includes a user's pkg-private module. This allows a user's
 * module to use pkg-private visibility to hide from external packages, but still allows Hilt to
 * install the module when the component is created in another package.
 */
final class PkgPrivateModuleGenerator {
    private final ProcessingEnvironment env;
    private final PkgPrivateMetadata metadata;

    PkgPrivateModuleGenerator(ProcessingEnvironment env, PkgPrivateMetadata metadata) {
        this.env = env;
        this.metadata = metadata;
    }

    // This method creates the following generated code for a pkg-private module, pkg.MyModule:
    //
    // package pkg; //same as module
    //
    // import dagger.Module;
    // import dagger.hilt.InstallIn;
    // import javax.annotation.Generated;
    //
    // @Generated("PkgPrivateModuleGenerator")
    // @InstallIn(ActivityComponent.class)
    // @OriginatingElement(topLevelClass = MyModule.class)
    // @Module(includes = MyModule.class)
    // public final class HiltWrapper_MyModule {}
    void generate() throws IOException {
        TypeSpec.Builder builder =
                TypeSpec.classBuilder(metadata.generatedClassName().simpleName())
                        .addOriginatingElement(metadata.getTypeElement())
                        .addAnnotation(Processors.getOriginatingElementAnnotation(metadata.getTypeElement()))
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        // generated @InstallIn is exactly the same as the module being processed
                        .addAnnotation(
                                AnnotationSpec.get(metadata.getOptionalInstallInAnnotationMirror().get()))
                        .addAnnotation(
                                AnnotationSpec.builder(metadata.getAnnotation())
                                        .addMember("includes", "$T.class", metadata.getTypeElement())
                                        .build());

        Processors.addGeneratedAnnotation(builder, env, getClass());

        JavaFile.builder(metadata.generatedClassName().packageName(), builder.build())
                .build()
                .writeTo(env.getFiler());
    }
}

