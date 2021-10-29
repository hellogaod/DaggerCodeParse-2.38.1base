package dagger.internal.codegen.javapoet;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

/** Common names and convenience methods for JavaPoet {@link TypeName} usage. */
public final class TypeNames {

    public static final ClassName BINDS = ClassName.get("dagger", "Binds");

    public static final ClassName MULTIBINDS = ClassName.get("dagger.multibindings", "Multibinds");

    public static final ClassName PROVIDES = ClassName.get("dagger", "Provides");

    public static final ClassName PRODUCES = ClassName.get("dagger.producers", "Produces");

    public static final ClassName BINDS_OPTIONAL_OF = ClassName.get("dagger", "BindsOptionalOf");
}
