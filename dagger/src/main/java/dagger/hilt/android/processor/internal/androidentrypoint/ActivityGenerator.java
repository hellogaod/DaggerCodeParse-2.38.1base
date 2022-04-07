package dagger.hilt.android.processor.internal.androidentrypoint;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import dagger.hilt.android.processor.internal.AndroidClassNames;
import dagger.hilt.processor.internal.Processors;

/**
 * Generates an Hilt Activity class for the @AndroidEntryPoint annotated class.
 */
public final class ActivityGenerator {
    private final ProcessingEnvironment env;
    private final AndroidEntryPointMetadata metadata;
    private final ClassName generatedClassName;

    public ActivityGenerator(ProcessingEnvironment env, AndroidEntryPointMetadata metadata) {
        this.env = env;
        this.metadata = metadata;

        generatedClassName = metadata.generatedClassName();
    }

    //A generated base class to be extended by the @AndroidEntryPoint annotated class. If using  the Gradle plugin, this is swapped as the base class via bytecode transformation.
    // @Generated("ActivityGenerator")
    // abstract class Hilt_$CLASS extends $BASE implements GeneratedComponentManagerHolder {
    //   ...
    // }
    public void generate() throws IOException {
        TypeSpec.Builder builder =
                TypeSpec.classBuilder(generatedClassName.simpleName())
                        .addOriginatingElement(metadata.element())
                        .superclass(metadata.baseClassName())
                        .addModifiers(metadata.generatedClassModifiers());

        Generators.addGeneratedBaseClassJavadoc(builder, AndroidClassNames.ANDROID_ENTRY_POINT);
        Processors.addGeneratedAnnotation(builder, env, getClass());

        //将baseElement中非private并且存在参数的构造函数拷贝，并且调用_initHiltInternal()方法
        Generators.copyConstructors(
                metadata.baseElement(),
                CodeBlock.builder().addStatement("_initHiltInternal()").build(),
                builder);

        //实现_initHiltInternal()方法
        builder.addMethod(init());

        //baseElement节点的泛型全部拷贝到当前新生成的类中
        metadata.baseElement().getTypeParameters().stream()
                .map(TypeVariableName::get)
                .forEachOrdered(builder::addTypeVariable);

        Generators.addComponentOverride(metadata, builder);
        //如果存在@TargetApi注解，将其拷贝
        Generators.copyLintAnnotations(metadata.element(), builder);

        Generators.addInjectionMethods(metadata, builder);

        if (Processors.isAssignableFrom(metadata.baseElement(), AndroidClassNames.COMPONENT_ACTIVITY)
                && !metadata.overridesAndroidEntryPointClass()) {
            builder.addMethod(getDefaultViewModelProviderFactory());
        }

        JavaFile.builder(generatedClassName.packageName(), builder.build())
                .build()
                .writeTo(env.getFiler());
    }

    // private void _initHiltInternal() {
    //   addOnContextAvailableListener(new OnContextAvailableListener() {
    //     @Override
    //     public void onContextAvailable(Context context) {
    //       inject();
    //     }
    //   });
    // }
    private MethodSpec init() {
        return MethodSpec.methodBuilder("_initHiltInternal")
                .addModifiers(Modifier.PRIVATE)
                .addStatement(
                        "addOnContextAvailableListener($L)",
                        TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(AndroidClassNames.ON_CONTEXT_AVAILABLE_LISTENER)
                                .addMethod(
                                        MethodSpec.methodBuilder("onContextAvailable")
                                                .addAnnotation(Override.class)
                                                .addModifiers(Modifier.PUBLIC)
                                                .addParameter(AndroidClassNames.CONTEXT, "context")
                                                .addStatement("inject()")
                                                .build())
                                .build())
                .build();
    }

    // @Override
    // public ViewModelProvider.Factory getDefaultViewModelProviderFactory() {
    //   return DefaultViewModelFactories.getActivityFactory(
    //       this, super.getDefaultViewModelProviderFactory());
    // }
    private MethodSpec getDefaultViewModelProviderFactory() {
        return MethodSpec.methodBuilder("getDefaultViewModelProviderFactory")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(AndroidClassNames.VIEW_MODEL_PROVIDER_FACTORY)
                .addStatement(
                        "return $T.getActivityFactory(this, super.getDefaultViewModelProviderFactory())",
                        AndroidClassNames.DEFAULT_VIEW_MODEL_FACTORIES)
                .build();
    }
}
