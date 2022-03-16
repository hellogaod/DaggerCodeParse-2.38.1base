package dagger.internal.codegen.writing;

import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.SimpleTypeVisitor6;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.langmodel.DaggerElements;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static dagger.internal.codegen.binding.AnnotationExpression.createMethodName;
import static dagger.internal.codegen.binding.AnnotationExpression.getAnnotationCreatorClassName;
import static dagger.internal.codegen.javapoet.CodeBlocks.makeParametersCodeBlock;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * Generates classes that create annotation instances for an annotation type. The generated class
 * will have a private empty constructor, a static method that creates the annotation type itself,
 * and a static method that creates each annotation type that is nested in the top-level annotation
 * type.
 *
 * <p>So for an example annotation:
 *
 * <pre>
 *   {@literal @interface} Foo {
 *     String s();
 *     int i();
 *     Bar bar(); // an annotation defined elsewhere
 *   }
 * </pre>
 * <p>
 * the generated class will look like:
 *
 * <pre>
 *   public final class FooCreator {
 *     private FooCreator() {}
 *
 *     public static Foo createFoo(String s, int i, Bar bar) { … }
 *     public static Bar createBar(…) { … }
 *   }
 * </pre>
 * <p>
 * 注解类生成普通类，案例如上
 */
public class AnnotationCreatorGenerator extends SourceFileGenerator<TypeElement> {
    private static final ClassName AUTO_ANNOTATION =
            ClassName.get("com.google.auto.value", "AutoAnnotation");

    @Inject
    AnnotationCreatorGenerator(XFiler filer, DaggerElements elements, SourceVersion sourceVersion) {
        super(filer, elements, sourceVersion);
    }

    @Override
    public ImmutableList<TypeSpec.Builder> topLevelTypes(TypeElement annotationType) {
        //生成类名： 节点信息 + "Creator"
        ClassName generatedTypeName = getAnnotationCreatorClassName(annotationType);

        //这里final和private的无参构造函数的意思是保证当前类不可更改性
        TypeSpec.Builder annotationCreatorBuilder =
                classBuilder(generatedTypeName)
                        .addModifiers(PUBLIC, FINAL)
                        .addMethod(constructorBuilder().addModifiers(PRIVATE).build());//生成private无参构造函数

        //当前注解类，并且遍历当前注解类所有方法返回类型，如果发现还是注解类收集（深入递归查找，直到找不到为止）
        for (TypeElement annotationElement : annotationsToCreate(annotationType)) {
            annotationCreatorBuilder.addMethod(buildCreateMethod(generatedTypeName, annotationElement));
        }

        return ImmutableList.of(annotationCreatorBuilder);
    }

    @Override
    public Element originatingElement(TypeElement annotationType) {
        return annotationType;
    }

    private MethodSpec buildCreateMethod(ClassName generatedTypeName, TypeElement annotationElement) {

        //方法名："create" + 当前节点名
        String createMethodName = createMethodName(annotationElement);

        //当前方法名1.方法使用AutoAnnotation注释，2.返回类型是当前节点类型；3.public statc修饰
        MethodSpec.Builder createMethod =
                methodBuilder(createMethodName)
                        .addAnnotation(AUTO_ANNOTATION)
                        .addModifiers(PUBLIC, STATIC)
                        .returns(TypeName.get(annotationElement.asType()));

        //方法参数是当前注解类中的方法返回类型，有多少个方法就有多少个参数
        ImmutableList.Builder<CodeBlock> parameters = ImmutableList.builder();
        for (ExecutableElement annotationMember : methodsIn(annotationElement.getEnclosedElements())) {
            String parameterName = annotationMember.getSimpleName().toString();
            TypeName parameterType = TypeName.get(annotationMember.getReturnType());
            createMethod.addParameter(parameterType, parameterName);
            parameters.add(CodeBlock.of("$L", parameterName));
        }

        //方法里面的代码 return new AutoAnnotation_"注解名"_"当前方法名"(逗号隔开的参数);
        ClassName autoAnnotationClass =
                generatedTypeName.peerClass(
                        "AutoAnnotation_" + generatedTypeName.simpleName() + "_" + createMethodName);
        createMethod.addStatement(
                "return new $T($L)", autoAnnotationClass, makeParametersCodeBlock(parameters.build()));
        return createMethod.build();
    }

    /**
     * Returns the annotation types for which {@code @AutoAnnotation static Foo createFoo(…)} methods
     * should be written.
     */
    protected Set<TypeElement> annotationsToCreate(TypeElement annotationElement) {
        return nestedAnnotationElements(annotationElement, new LinkedHashSet<>());
    }

    @CanIgnoreReturnValue
    private static Set<TypeElement> nestedAnnotationElements(
            TypeElement annotationElement,
            Set<TypeElement> annotationElements
    ) {

        if (annotationElements.add(annotationElement)) {
            //遍历当前注解类的所有方法，对方法返回类型判断，如果方法返回类型还是注解类，那么收集到annotationElements集合中
            for (ExecutableElement method : methodsIn(annotationElement.getEnclosedElements())) {
                TRAVERSE_NESTED_ANNOTATIONS.visit(method.getReturnType(), annotationElements);
            }
        }
        return annotationElements;
    }

    private static final SimpleTypeVisitor6<Void, Set<TypeElement>> TRAVERSE_NESTED_ANNOTATIONS =
            new SimpleTypeVisitor6<Void, Set<TypeElement>>() {
                @Override
                public Void visitDeclared(DeclaredType t, Set<TypeElement> p) {
                    TypeElement typeElement = MoreTypes.asTypeElement(t);
                    if (typeElement.getKind() == ElementKind.ANNOTATION_TYPE) {
                        nestedAnnotationElements(typeElement, p);
                    }
                    return null;
                }
            };
}
