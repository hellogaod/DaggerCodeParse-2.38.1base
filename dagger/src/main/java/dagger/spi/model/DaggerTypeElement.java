package dagger.spi.model;


import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.devtools.ksp.symbol.KSClassDeclaration;
import com.squareup.javapoet.ClassName;

import javax.annotation.Nullable;
import javax.lang.model.element.TypeElement;

import static dagger.spi.model.CompilerEnvironment.JAVA;
import static dagger.spi.model.CompilerEnvironment.KSP;

/**
 * Wrapper type for a type element.
 * <p>
 * Dagger节点：分为java和kotlin，如果是java使用TypeElement表示类，如果是Kotlin使用KSClassDeclaration表示一个类
 */
@AutoValue
public abstract class DaggerTypeElement {

    public static DaggerTypeElement fromJava(TypeElement element) {
        return new AutoValue_DaggerTypeElement(JAVA, Preconditions.checkNotNull(element), null);
    }

    public static DaggerTypeElement fromKsp(KSClassDeclaration element) {
        return new AutoValue_DaggerTypeElement(KSP, null, Preconditions.checkNotNull(element));
    }

    public TypeElement java() {
        Preconditions.checkState(compiler() == JAVA);
        return javaInternal();
    }

    public KSClassDeclaration ksp() {
        Preconditions.checkState(compiler() == KSP);
        return kspInternal();
    }

    public ClassName className() {
        if (compiler() == KSP) {
            // TODO(bcorso): Add support for KSP. Consider using xprocessing types internally since that
            // already has support for KSP class names?
            throw new UnsupportedOperationException("Method className() is not yet supported in KSP.");
        } else {
            return ClassName.get(java());
        }
    }

    public abstract CompilerEnvironment compiler();

    @Nullable
    abstract TypeElement javaInternal();

    @Nullable
    abstract KSClassDeclaration kspInternal();

    @Override
    public final String toString() {
        return (compiler() == JAVA ? java() : ksp()).toString();
    }
}
