package dagger.spi.model;


import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.devtools.ksp.symbol.KSDeclaration;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;

import static dagger.spi.model.CompilerEnvironment.JAVA;
import static dagger.spi.model.CompilerEnvironment.KSP;

/**
 * Wrapper type for an element.
 */
@AutoValue
public abstract class DaggerElement {

    public static DaggerElement fromJava(Element element) {
        return new AutoValue_DaggerElement(
                JAVA, Preconditions.checkNotNull(element), null);
    }

    public static DaggerElement fromKsp(KSDeclaration element) {
        return new AutoValue_DaggerElement(
                KSP, null, Preconditions.checkNotNull(element));
    }

    public Element java() {
        Preconditions.checkState(compiler() == JAVA);
        return javaInternal();
    }

    public KSDeclaration ksp() {
        Preconditions.checkState(compiler() == KSP);
        return kspInternal();
    }

    public abstract CompilerEnvironment compiler();

    @Nullable
    abstract Element javaInternal();

    @Nullable
    abstract KSDeclaration kspInternal();

    @Override
    public final String toString() {
        return (compiler() == JAVA ? java() : ksp()).toString();
    }
}
