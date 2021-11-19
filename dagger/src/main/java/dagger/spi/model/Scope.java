package dagger.spi.model;

import com.google.auto.value.AutoValue;
import com.squareup.javapoet.ClassName;

import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * A representation of a {@link javax.inject.Scope}.
 * <p>
 * 表示一个Scope注解，仅有scopeAnnotation字段表示Scope注解
 * <p>
 * 注意：Scope注解是用来修饰注解的，这里表示被Scope修饰的注解，有ProductionScope，Singleton，Reusable或本身Scope四类
 */
@AutoValue
public abstract class Scope {

    /**
     * Creates a {@link Scope} object from the {@link javax.inject.Scope}-annotated annotation type.
     */
    public static Scope scope(DaggerAnnotation scopeAnnotation) {
        checkArgument(isScope(scopeAnnotation));
        return new AutoValue_Scope(scopeAnnotation);
    }


    /**
     * Returns {@code true} if {@link #scopeAnnotation()} is a {@link javax.inject.Scope} annotation.
     */
    public static boolean isScope(DaggerAnnotation scopeAnnotation) {
        return isScope(scopeAnnotation.annotationTypeElement());
    }

    /**
     * Returns {@code true} if {@code scopeAnnotationType} is a {@link javax.inject.Scope} annotation.
     *
     * 是否使用了Scope注解修饰(Scope只能修饰注解)
     */
    public static boolean isScope(DaggerTypeElement scopeAnnotationType) {
        // TODO(bcorso): Replace Scope class reference with class name once auto-common is updated.
        return isAnnotationPresent(scopeAnnotationType.java(), javax.inject.Scope.class);
    }

    private static final ClassName PRODUCTION_SCOPE =
            ClassName.get("dagger.producers", "ProductionScope");
    private static final ClassName SINGLETON = ClassName.get("javax.inject", "Singleton");
    private static final ClassName REUSABLE = ClassName.get("dagger", "Reusable");
    private static final ClassName SCOPE = ClassName.get("javax.inject", "Scope");

    /**
     * The {@link DaggerAnnotation} that represents the scope annotation.
     */
    public abstract DaggerAnnotation scopeAnnotation();

    //注解ClassName类
    public final ClassName className() {
        return scopeAnnotation().className();
    }

    /**
     * Returns {@code true} if this scope is the {@link javax.inject.Singleton @Singleton} scope.
     */
    public final boolean isSingleton() {
        return isScope(SINGLETON);
    }

    /**
     * Returns {@code true} if this scope is the {@link dagger.Reusable @Reusable} scope.
     */
    public final boolean isReusable() {
        return isScope(REUSABLE);
    }

    /**
     * Returns {@code true} if this scope is the {@link
     * dagger.producers.ProductionScope @ProductionScope} scope.
     */
    public final boolean isProductionScope() {
        return isScope(PRODUCTION_SCOPE);
    }

    private boolean isScope(ClassName annotation) {
        return scopeAnnotation().className().equals(annotation);
    }

    /**
     * Returns a debug representation of the scope.
     */
    @Override
    public final String toString() {
        return scopeAnnotation().toString();
    }
}
