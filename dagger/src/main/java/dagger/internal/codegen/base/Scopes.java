package dagger.internal.codegen.base;

import com.google.auto.common.AnnotationMirrors;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.Optional;

import javax.inject.Singleton;
import javax.lang.model.element.Element;

import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.producers.ProductionScope;
import dagger.spi.model.DaggerAnnotation;
import dagger.spi.model.Scope;

import dagger.internal.codegen.base.DiagnosticFormatting;

import static dagger.internal.codegen.extension.DaggerCollectors.toOptional;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/**
 * Common names and convenience methods for {@link Scope}s.
 */
public final class Scopes {

    /**
     * Returns a representation for {@link ProductionScope @ProductionScope} scope.
     * <p>
     * ProductionScope注解生成Scope对象
     */
    public static Scope productionScope(DaggerElements elements) {
        return scope(elements, TypeNames.PRODUCTION_SCOPE);
    }

    /**
     * Returns a representation for {@link Singleton @Singleton} scope.
     * <p>
     * Singleton对象生成Scope对象
     */
    public static Scope singletonScope(DaggerElements elements) {
        return scope(elements, TypeNames.SINGLETON);
    }

    /**
     * Creates a {@link Scope} object from the {@link javax.inject.Scope}-annotated annotation type.
     * <p>
     * 针对使用Scope注解修饰的注解生成Scope对象
     */
    private static Scope scope(DaggerElements elements, ClassName scopeAnnotationClassName) {
        return Scope.scope(
                DaggerAnnotation.fromJava(
                        SimpleAnnotationMirror.of(
                                elements.getTypeElement(scopeAnnotationClassName.canonicalName()))));
    }

    /**
     * Returns at most one associated scoped annotation from the source code element, throwing an
     * exception if there are more than one.
     * <p>
     * 有且仅有一个值，并且返回Optional类型格式
     */
    public static Optional<Scope> uniqueScopeOf(Element element) {
        return scopesOf(element).stream().collect(toOptional());
    }

    /**
     * Returns the readable source representation (name with @ prefix) of the scope's annotation type.
     *
     * <p>It's readable source because it has had common package prefixes removed, e.g.
     * {@code @javax.inject.Singleton} is returned as {@code @Singleton}.
     * <p>
     * 如果是 {@code @javax.inject.Singleton} is returned as {@code @Singleton}.
     */
    public static String getReadableSource(Scope scope) {
        return DiagnosticFormatting.stripCommonTypePrefixes(scope.toString());
    }

    /**
     * Returns all of the associated scopes for a source code element.
     * <p>
     * element节点上 所有使用Scope注解修饰的注解 转换成Scope对象
     */
    public static ImmutableSet<Scope> scopesOf(Element element) {
        // TODO(bcorso): Replace Scope class reference with class name once auto-common is updated.
        //AnnotationMirrors.getAnnotatedAnnotations()：element节点上 所有使用Scope注解修饰的注解
        return AnnotationMirrors.getAnnotatedAnnotations(element, javax.inject.Scope.class)
                .stream()
                .map(DaggerAnnotation::fromJava)
                .map(Scope::scope)
                .collect(toImmutableSet());
    }
}
