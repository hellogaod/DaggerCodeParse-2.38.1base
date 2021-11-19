package dagger.spi.model;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import com.google.devtools.ksp.symbol.KSAnnotation;
import com.squareup.javapoet.ClassName;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;

import static dagger.spi.model.CompilerEnvironment.JAVA;
import static dagger.spi.model.CompilerEnvironment.KSP;

/**
 * Wrapper type for an annotation.
 * <p>
 * Dagger注解，分为java和kotlin两种，java使用 Equivalence.Wrapper<AnnotationMirror>描述注解，kotlin使用KSAnnotation描述注解
 */
@AutoValue
public abstract class DaggerAnnotation {

    public static DaggerAnnotation fromJava(AnnotationMirror annotationMirror) {
        return new AutoValue_DaggerAnnotation(
                JAVA,
                AnnotationMirrors.equivalence().wrap(Preconditions.checkNotNull(annotationMirror)),
                null);
    }

    public static DaggerAnnotation fromKsp(KSAnnotation ksAnnotation) {
        return new AutoValue_DaggerAnnotation(
                KSP,
                null,
                Preconditions.checkNotNull(ksAnnotation));
    }

    public DaggerTypeElement annotationTypeElement() {
        //对注解类型转换成对应的TypeElement类型并且使用DaggerTypeElement呈现
        return DaggerTypeElement.fromJava(
                MoreTypes.asTypeElement(annotationMirror().get().getAnnotationType()));
    }

    public ClassName className() {
        return annotationTypeElement().className();
    }


    public AnnotationMirror java() {
        Preconditions.checkState(compiler() == JAVA);
        return annotationMirror().get();
    }

    public KSAnnotation ksp() {
        Preconditions.checkState(compiler() == KSP);
        return kspInternal();
    }

    public abstract CompilerEnvironment compiler();


    @Nullable
    abstract Equivalence.Wrapper<AnnotationMirror> annotationMirror();

    @Nullable
    abstract KSAnnotation kspInternal();

    @Override
    public final String toString() {
        return (compiler() == JAVA ? java() : ksp()).toString();
    }
}
