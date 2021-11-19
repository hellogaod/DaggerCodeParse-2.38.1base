package dagger.spi.model;


import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import com.google.devtools.ksp.symbol.KSType;

import javax.annotation.Nullable;
import javax.lang.model.type.TypeMirror;

import static dagger.spi.model.CompilerEnvironment.JAVA;
import static dagger.spi.model.CompilerEnvironment.KSP;

/**
 * Wrapper type for a type.
 * <p>
 * Dagger下的type表示，有两个，一个是java类型，一个是kotlin类型
 */
@AutoValue
public abstract class DaggerType {

    public static DaggerType fromJava(TypeMirror typeMirror) {
        return new AutoValue_DaggerType(
                JAVA,
                MoreTypes.equivalence().wrap(Preconditions.checkNotNull(typeMirror)),
                null);
    }

    public static DaggerType fromKsp(KSType ksType) {
        return new AutoValue_DaggerType(
                KSP,
                null,
                Preconditions.checkNotNull(ksType));
    }

    public TypeMirror java() {
        Preconditions.checkState(compiler() == JAVA);
        return typeMirror().get();
    }

    public KSType ksp() {
        Preconditions.checkState(compiler() == KSP);
        return kspInternal();
    }

    public abstract CompilerEnvironment compiler();

    @Nullable
    abstract Equivalence.Wrapper<TypeMirror> typeMirror();

    @Nullable
    abstract KSType kspInternal();

    @Override
    public final String toString() {
        return (compiler() == JAVA ? java() : ksp()).toString();
    }
}
