package dagger.hilt.android.processor.internal.androidentrypoint;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;

/**
 * Generates an entry point that allows for injection of the given activity
 */
public final class InjectorEntryPointGenerator {
    private final ProcessingEnvironment env;
    private final AndroidEntryPointMetadata metadata;

    public InjectorEntryPointGenerator(
            ProcessingEnvironment env, AndroidEntryPointMetadata metadata) {
        this.env = env;
        this.metadata = metadata;
    }

    // @Generated("InjectorEntryPointGenerator")
    // @InstallIn(ActivityComponent.class)
    // @GeneratedEntryPoint
    // @OriginatingElement(topLevelClass = FooActivity.class)
    // public interface FooActivity_GeneratedInjector {
    //   void injectFooActivity(FooActivity fooActivity);
    // }
    public void generate() throws IOException {
        //1. 生成的类名是@HiltAndroidApp或@AndroidEntryPoint注解修饰的节点拼接：$CLASS_GeneratedInjector
        ClassName name = metadata.injectorClassName();

        //2.生成一个接口：
        TypeSpec.Builder builder =
                TypeSpec.interfaceBuilder(name.simpleName())
                        .addOriginatingElement(metadata.element())
                        //(1)添加@OriginatingElement(topLevelClass = @HiltAndroidApp或@AndroidEntryPoint注解修饰的节点类型.class)注解
                        .addAnnotation(Processors.getOriginatingElementAnnotation(metadata.element()))
                        //（2）添加@GeneratedEntryPoint注解
                        .addAnnotation(ClassNames.GENERATED_ENTRY_POINT)
                        //（3）添加@InstallIn(T.class)注释，T根据type.component属性；
                        .addAnnotation(metadata.injectorInstallInAnnotation())
                        .addModifiers(Modifier.PUBLIC)
                        //(4)添加public abstract修饰的方法，方法名：e.g. （@HiltAndroidApp或@AndroidEntryPoint注解修饰的节点）Foo.Bar.Baz -> injectFoo_Bar_Baz
                        .addMethod(
                                MethodSpec.methodBuilder(metadata.injectMethodName())
                                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                                        //方法参数：（@HiltAndroidApp或@AndroidEntryPoint注解修饰的节点）"SomeString" => "someString"
                                        .addParameter(
                                                metadata.elementClassName(),
                                                Processors.upperToLowerCamel(metadata.elementClassName().simpleName()))
                                        .build());

        //当前接口添加@Generated注解
        Processors.addGeneratedAnnotation(builder, env, getClass());

        //如果当前@HiltAndroidApp或@AndroidEntryPoint注解修饰的节点使用了@TargetApi注解修饰，把当前@TargetApi注解拷贝到新生成的接口中
        Generators.copyLintAnnotations(metadata.element(), builder);

        JavaFile.builder(name.packageName(), builder.build()).build().writeTo(env.getFiler());
    }
}
