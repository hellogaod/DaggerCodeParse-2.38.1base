package dagger.internal.codegen.writing;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import dagger.internal.codegen.base.SourceFileGenerator;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * A source file generator that only writes the relevant code necessary for Bazel to create a
 * correct header (ABI) jar.
 * <p>
 * 一个源文件生成器，只编写 Bazel 所需的相关代码，以创建正确的标头 (ABI) jar。
 */
public final class HjarSourceFileGenerator<T> extends SourceFileGenerator<T> {
    private final SourceFileGenerator<T> delegate;

    private HjarSourceFileGenerator(SourceFileGenerator<T> delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    public static <T> SourceFileGenerator<T> wrap(SourceFileGenerator<T> delegate) {
        return new HjarSourceFileGenerator<>(delegate);
    }

    @Override
    public Element originatingElement(T input) {
        return delegate.originatingElement(input);
    }

    @Override
    public ImmutableList<TypeSpec.Builder> topLevelTypes(T input) {
        return delegate.topLevelTypes(input).stream()
                .map(completeType -> skeletonType(completeType.build()))
                .collect(toImmutableList());
    }

    //对completeType类筛选然后生成skeleton类:如果是普通方法，筛选出非privte修饰的方法。其他的都复制过来
    private TypeSpec.Builder skeletonType(TypeSpec completeType) {

        TypeSpec.Builder skeleton =
                classBuilder(completeType.name)
                        .addSuperinterfaces(completeType.superinterfaces)
                        .addTypeVariables(completeType.typeVariables)
                        .addModifiers(completeType.modifiers.toArray(new Modifier[0]))
                        .addAnnotations(completeType.annotations);

        if (!completeType.superclass.equals(ClassName.OBJECT)) {
            skeleton.superclass(completeType.superclass);
        }

        //筛选：completeType类的方法不是private修饰或者是构造函数 添加到skeleton类
        completeType.methodSpecs.stream()
                .filter(method -> !method.modifiers.contains(PRIVATE) || method.isConstructor())
                .map(this::skeletonMethod)
                .forEach(skeleton::addMethod);

        completeType.fieldSpecs.stream()
                .filter(field -> !field.modifiers.contains(PRIVATE))
                .map(this::skeletonField)
                .forEach(skeleton::addField);

        completeType.typeSpecs.stream()
                .map(type -> skeletonType(type).build())
                .forEach(skeleton::addType);

        return skeleton;
    }

    private MethodSpec skeletonMethod(MethodSpec completeMethod) {
        MethodSpec.Builder skeleton =
                completeMethod.isConstructor()
                        ? constructorBuilder()
                        : methodBuilder(completeMethod.name).returns(completeMethod.returnType);

        if (completeMethod.isConstructor()) {
            // Code in Turbine must (for technical reasons in javac) have a valid super() call for
            // constructors, otherwise javac will bark, and Turbine has no way to avoid this. So we retain
            // constructor method bodies if they do exist
            skeleton.addCode(completeMethod.code);
        }

        return skeleton
                .addModifiers(completeMethod.modifiers)
                .addTypeVariables(completeMethod.typeVariables)
                .addParameters(completeMethod.parameters)
                .addExceptions(completeMethod.exceptions)
                .varargs(completeMethod.varargs)
                .addAnnotations(completeMethod.annotations)
                .build();
    }

    private FieldSpec skeletonField(FieldSpec completeField) {
        return FieldSpec.builder(
                completeField.type,
                completeField.name,
                completeField.modifiers.toArray(new Modifier[0]))
                .addAnnotations(completeField.annotations)
                .build();
    }
}
