package dagger.hilt.android.processor.internal.androidentrypoint;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.ElementFilter;

import dagger.hilt.android.processor.internal.AndroidClassNames;
import dagger.hilt.processor.internal.Processors;

/** Generates an Hilt Service class for the @AndroidEntryPoint annotated class. */
public final class ServiceGenerator {
    private final ProcessingEnvironment env;
    private final AndroidEntryPointMetadata metadata;
    private final ClassName generatedClassName;

    public ServiceGenerator(ProcessingEnvironment env, AndroidEntryPointMetadata metadata) {
        this.env = env;
        this.metadata = metadata;

        generatedClassName = metadata.generatedClassName();
    }

    // @Generated("ServiceGenerator")
    // abstract class Hilt_$CLASS extends $BASE {
    //   ...
    // }
    public void generate() throws IOException {
        TypeSpec.Builder builder =
                TypeSpec.classBuilder(generatedClassName.simpleName())
                        .addOriginatingElement(metadata.element())
                        .superclass(metadata.baseClassName())
                        .addModifiers(metadata.generatedClassModifiers())
                        .addMethods(baseClassConstructors())
                        .addMethod(onCreateMethod());

        Generators.addGeneratedBaseClassJavadoc(builder, AndroidClassNames.ANDROID_ENTRY_POINT);
        Processors.addGeneratedAnnotation(builder, env, getClass());
        Generators.copyLintAnnotations(metadata.element(), builder);

        metadata.baseElement().getTypeParameters().stream()
                .map(TypeVariableName::get)
                .forEachOrdered(builder::addTypeVariable);

        Generators.addInjectionMethods(metadata, builder);

        Generators.addComponentOverride(metadata, builder);

        JavaFile.builder(generatedClassName.packageName(), builder.build())
                .build().writeTo(env.getFiler());
    }

    private List<MethodSpec> baseClassConstructors() {
        return ElementFilter.constructorsIn(metadata.baseElement().getEnclosedElements())
                .stream()
                .map((constructor) -> {
                    List<ParameterSpec> params =
                            constructor.getParameters()
                                    .stream()
                                    .map(p -> ParameterSpec.builder(TypeName.get(p.asType()), p.toString()).build())
                                    .collect(Collectors.toList());

                    return MethodSpec.constructorBuilder()
                            .addParameters(params)
                            .addStatement(
                                    "super($L)",
                                    params.stream().map(p -> p.name).collect(Collectors.joining(",")))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // @CallSuper
    // @Override
    // protected void onCreate() {
    //   inject();
    //   super.onCreate();
    // }
    private MethodSpec onCreateMethod() throws IOException {
        return MethodSpec.methodBuilder("onCreate")
                .addAnnotation(AndroidClassNames.CALL_SUPER)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("inject()")
                .addStatement("super.onCreate()")
                .build();
    }
}
