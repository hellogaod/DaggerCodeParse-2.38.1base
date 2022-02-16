package dagger.spi.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.devtools.ksp.symbol.KSFunctionDeclaration;

import javax.annotation.Nullable;
import javax.lang.model.element.ExecutableElement;

import static dagger.spi.model.CompilerEnvironment.JAVA;
import static dagger.spi.model.CompilerEnvironment.KSP;

/**
 * Wrapper type for an executable element.
 */
@AutoValue
public abstract class DaggerExecutableElement {
    public static DaggerExecutableElement fromJava(ExecutableElement element) {
        return new AutoValue_DaggerExecutableElement(JAVA, Preconditions.checkNotNull(element), null);
    }

    public static DaggerExecutableElement fromKsp(KSFunctionDeclaration element) {
        return new AutoValue_DaggerExecutableElement(KSP, null, Preconditions.checkNotNull(element));
    }

    public ExecutableElement java() {
        Preconditions.checkState(compiler() == JAVA);
        return javaInternal();
    }

    public KSFunctionDeclaration ksp() {
        Preconditions.checkState(compiler() == KSP);
        return kspInternal();
    }

    public abstract CompilerEnvironment compiler();

    @Nullable
    abstract ExecutableElement javaInternal();

    @Nullable
    abstract KSFunctionDeclaration kspInternal();

    @Override
    public final String toString() {
        return (compiler() == JAVA ? java() : ksp()).toString();
    }
}
