package dagger.hilt.android.processor.internal.androidentrypoint;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import dagger.hilt.android.processor.internal.AndroidClassNames;
import dagger.hilt.processor.internal.ComponentNames;
import dagger.hilt.processor.internal.Processors;

/**
 * Generates an Hilt Application for an @AndroidEntryPoint app class.
 */
public final class ApplicationGenerator {
    private final ProcessingEnvironment env;
    private final AndroidEntryPointMetadata metadata;
    private final ClassName wrapperClassName;
    private final ComponentNames componentNames;

    public ApplicationGenerator(ProcessingEnvironment env, AndroidEntryPointMetadata metadata) {
        this.env = env;
        this.metadata = metadata;
        //@HiltAndroidApp修饰的$APP生成的Hilt_$APP
        this.wrapperClassName = metadata.generatedClassName();

        this.componentNames = ComponentNames.withoutRenaming();
    }

    //A generated base class to be extended by the @HiltAndroidApp annotated class. If using the Gradle plugin, this is swapped as the base class via bytecode transformation.
    // @Generated("ApplicationGenerator")
    //public abstract class Hilt_Application extends Application implements GeneratedComponentManagerHolder{
    //   ...
    // }
    public void generate() throws IOException {

        TypeSpec.Builder typeSpecBuilder =
                TypeSpec.classBuilder(wrapperClassName.simpleName())
                        .addOriginatingElement(metadata.element())
                        //1. Hilt_$APP继承Application；
                        .superclass(metadata.baseClassName())
                        //2. 如果是kotlin并且是public修饰，那么使用public abstract修饰；否则使用abstract修饰
                        .addModifiers(metadata.generatedClassModifiers())
                        //3. ApplicationComponentManager变量
                        .addField(componentManagerField())
                        //4.生成componentManager对象
                        .addMethod(componentManagerMethod());

        Generators.addGeneratedBaseClassJavadoc(typeSpecBuilder, AndroidClassNames.HILT_ANDROID_APP);
        Processors.addGeneratedAnnotation(typeSpecBuilder, env, getClass());

        //baseElement上的泛型添加到当前新生成的类中
        metadata.baseElement().getTypeParameters().stream()
                .map(TypeVariableName::get)
                .forEachOrdered(typeSpecBuilder::addTypeVariable);

        //TargetApi注解拷贝
        Generators.copyLintAnnotations(metadata.element(), typeSpecBuilder);

        Generators.addComponentOverride(metadata, typeSpecBuilder);

        //继承onCreate方法，并且实现自己的代码
        typeSpecBuilder.addMethod(onCreateMethod());

        JavaFile.builder(metadata.elementClassName().packageName(), typeSpecBuilder.build())
                .build()
                .writeTo(env.getFiler());
    }

    // private final ApplicationComponentManager componentManager =
    //     new ApplicationComponentManager(...);
    private FieldSpec componentManagerField() {
        ParameterSpec managerParam = metadata.componentManagerParam();
        return FieldSpec.builder(managerParam.type, managerParam.name)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T($L)", AndroidClassNames.APPLICATION_COMPONENT_MANAGER, creatorType())
                .build();
    }

    // @Override
    // public final ApplicationComponentManager componentManager() {
    //   return componentManager;
    // }
    private MethodSpec componentManagerMethod() {
        return MethodSpec.methodBuilder("componentManager")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(metadata.componentManagerParam().type)
                .addStatement("return $N", metadata.componentManagerParam())
                .build();
    }

    // new ComponentSupplier() {
    //   @Override
    //   public Object get() {
    //     return Dagger_com_aregyan_github_Application_HiltComponents_SingletonComponent.builder()
    //         .applicationContextModule(new ApplicationContextModule(Hilt_Application.this))
    //         .build();
    //   }
    // }
    private TypeSpec creatorType() {
        //$APP_HiltComponents的内部类SingletonComponent
        ClassName component =
                componentNames.generatedComponent(
                        metadata.elementClassName(), AndroidClassNames.SINGLETON_COMPONENT);

        return TypeSpec.anonymousClassBuilder("")
                //1.new ComponentSupplier接口
                .addSuperinterface(AndroidClassNames.COMPONENT_SUPPLIER)
                .addMethod(
                        //2.添加get方法，该方法继承ComponentSupplier接口
                        MethodSpec.methodBuilder("get")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(TypeName.OBJECT)
                                .addStatement(
                                        "return $T.builder()\n"
                                                + ".applicationContextModule(new $T($T.this))\n"
                                                + ".build()",
                                        Processors.prepend(Processors.getEnclosedClassName(component), "Dagger"),
                                        AndroidClassNames.APPLICATION_CONTEXT_MODULE,
                                        wrapperClassName)
                                .build())
                .build();
    }

    // @CallSuper
    // @Override
    // public void onCreate() {
    //   // This is a known unsafe cast but should be fine if the only use is
    //   // $APP extends Hilt_$APP
    //   generatedComponent().inject(($APP) this);
    //   super.onCreate();
    // }
    private MethodSpec onCreateMethod() {
        return MethodSpec.methodBuilder("onCreate")
                .addAnnotation(AndroidClassNames.CALL_SUPER)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addCode(injectCodeBlock())
                .addStatement("super.onCreate()")
                .build();
    }

    //   // This is a known unsafe cast but should be fine if the only use is
    //   // $APP extends Hilt_$APP
    //   generatedComponent().inject$APP(($APP) this);
    private CodeBlock injectCodeBlock() {
        return CodeBlock.builder()
                .add("// This is a known unsafe cast, but is safe in the only correct use case:\n")
                .add("// $T extends $T\n", metadata.elementClassName(), metadata.generatedClassName())
                .addStatement(
                        "(($T) generatedComponent()).$L($L)",
                        metadata.injectorClassName(),
                        metadata.injectMethodName(),
                        Generators.unsafeCastThisTo(metadata.elementClassName()))
                .build();
    }
}
