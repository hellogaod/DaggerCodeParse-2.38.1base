package dagger.hilt.processor.internal.root;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;

import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;

/** Generator for the {@code EarlySingletonComponentCreator}. */
final class EarlySingletonComponentCreatorGenerator {
    private static final ClassName EARLY_SINGLETON_COMPONENT_CREATOR =
            ClassName.get("dagger.hilt.android.internal.testing", "EarlySingletonComponentCreator");
    private static final ClassName EARLY_SINGLETON_COMPONENT_CREATOR_IMPL =
            ClassName.get(
                    "dagger.hilt.android.internal.testing", "EarlySingletonComponentCreatorImpl");
    private static final ClassName DEFAULT_COMPONENT_IMPL =
            ClassName.get(
                    "dagger.hilt.android.internal.testing.root", "DaggerDefault_HiltComponents_SingletonC");

    static void generate(ProcessingEnvironment env) throws IOException {
        TypeSpec.Builder builder =
                TypeSpec.classBuilder(EARLY_SINGLETON_COMPONENT_CREATOR_IMPL)
                        .superclass(EARLY_SINGLETON_COMPONENT_CREATOR)
                        .addMethod(
                                MethodSpec.methodBuilder("create")
                                        .returns(ClassName.OBJECT)
                                        .addStatement(
                                                "return $T.builder()\n"
                                                        + ".applicationContextModule(\n"
                                                        + "    new $T($T.getApplication($T.getApplicationContext())))\n"
                                                        + ".build()",
                                                DEFAULT_COMPONENT_IMPL,
                                                ClassNames.APPLICATION_CONTEXT_MODULE,
                                                ClassNames.CONTEXTS,
                                                ClassNames.APPLICATION_PROVIDER)
                                        .build());

        Processors.addGeneratedAnnotation(builder, env, ClassNames.ROOT_PROCESSOR.toString());

        JavaFile.builder(EARLY_SINGLETON_COMPONENT_CREATOR_IMPL.packageName(), builder.build())
                .build()
                .writeTo(env.getFiler());
    }

    private EarlySingletonComponentCreatorGenerator() {}
}

