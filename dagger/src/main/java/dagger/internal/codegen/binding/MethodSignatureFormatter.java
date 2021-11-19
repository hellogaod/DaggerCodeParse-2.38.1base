package dagger.internal.codegen.binding;


import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.base.Formatter;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.base.Preconditions.checkState;
import static dagger.internal.codegen.base.DiagnosticFormatting.stripCommonTypePrefixes;


/**
 * Formats the signature of an {@link ExecutableElement} suitable for use in error messages.
 * <p>
 * 本方法的目的是针对方法 格式化需要展示的错误信息
 */
public final class MethodSignatureFormatter extends Formatter<ExecutableElement> {
    private final DaggerTypes types;
    private final InjectionAnnotations injectionAnnotations;

    @Inject
    public MethodSignatureFormatter(
            DaggerTypes types,
            InjectionAnnotations injectionAnnotations
    ) {
        this.types = types;
        this.injectionAnnotations = injectionAnnotations;
    }


    /**
     * A formatter that uses the type where the method is declared for the annotations and name of the
     * method, but the method's resolved type as a member of {@code declaredType} for the key.
     */
    public Formatter<ExecutableElement> typedFormatter(DeclaredType declaredType) {
        return new Formatter<ExecutableElement>() {
            @Override
            public String format(ExecutableElement method) {
                return MethodSignatureFormatter.this.format(
                        method,
                        MoreTypes.asExecutable(types.asMemberOf(declaredType, method)),
                        MoreElements.asType(method.getEnclosingElement()));
            }
        };
    }


    @Override
    public String format(ExecutableElement method) {
        return format(method, Optional.empty());
    }


    /**
     * Formats an ExecutableElement as if it were contained within the container, if the container is
     * present.
     *
     * container和method父级的关系，container有可能是method父级，但是也有可能container包含method父级，也就是说method所在类可能是一个内部类
     */
    public String format(ExecutableElement method, Optional<DeclaredType> container) {
        TypeElement type = MoreElements.asType(method.getEnclosingElement());
        ExecutableType executableType = MoreTypes.asExecutable(method.asType());

        if (container.isPresent()) {
            executableType = MoreTypes.asExecutable(types.asMemberOf(container.get(), method));
            type = MoreElements.asType(container.get().asElement());
        }

        return format(method, executableType, type);
    }


    private String format(
            ExecutableElement method,
            ExecutableType methodType,
            TypeElement declaringType
    ) {

        StringBuilder builder = new StringBuilder();
        // TODO(user): AnnotationMirror formatter.
        //收集method方法上所有的注解
        List<? extends AnnotationMirror> annotations = method.getAnnotationMirrors();

        //注解不为空，将注解存储于builder中
        if (!annotations.isEmpty()) {
            Iterator<? extends AnnotationMirror> annotationIterator = annotations.iterator();
            for (int i = 0; annotationIterator.hasNext(); i++) {
                if (i > 0) {
                    builder.append(' ');
                }
                builder.append(formatAnnotation(annotationIterator.next()));
            }
            builder.append(' ');
        }
        //如果方法是包含<init>,builder拼接当前方法所在父类
        if (method.getSimpleName().contentEquals("<init>")) {
            builder.append(declaringType.getQualifiedName());
        } else {

            //拼接方法返回类型 方法所在类.方法名
            builder
                    .append(nameOfType(methodType.getReturnType()))
                    .append(' ')
                    .append(declaringType.getQualifiedName())
                    .append('.')
                    .append(method.getSimpleName());
        }
        //拼接方法参数
        builder.append('(');
        checkState(method.getParameters().size() == methodType.getParameterTypes().size());
        Iterator<? extends VariableElement> parameters = method.getParameters().iterator();
        Iterator<? extends TypeMirror> parameterTypes = methodType.getParameterTypes().iterator();
        for (int i = 0; parameters.hasNext(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            appendParameter(builder, parameters.next(), parameterTypes.next());
        }
        builder.append(')');

        return builder.toString();
    }

    //builder存储1.参数上使用Qualifier注解的注解String类型；2.type类型string类型
    private void appendParameter(StringBuilder builder, VariableElement parameter, TypeMirror type) {
        injectionAnnotations
                .getQualifier(parameter)
                .ifPresent(
                        qualifier -> {
                            builder.append(formatAnnotation(qualifier)).append(' ');
                        });
        builder.append(nameOfType(type));
    }

    //类型转string类型
    private static String nameOfType(TypeMirror type) {
        return stripCommonTypePrefixes(type.toString());
    }

    //注解转string类型
    private static String formatAnnotation(AnnotationMirror annotation) {
        return stripCommonTypePrefixes(annotation.toString());
    }
}
