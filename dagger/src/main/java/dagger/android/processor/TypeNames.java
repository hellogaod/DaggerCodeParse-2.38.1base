package dagger.android.processor;


// TODO(bcorso): Dedupe with dagger/internal/codegen/javapoet/TypeNames.java?

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

/** Common names and methods for JavaPoet {@link TypeName} and {@link ClassName} usage. */
public final class TypeNames {

    // Core Dagger classnames
    public static final ClassName BINDS = ClassName.get("dagger", "Binds");
    public static final ClassName CLASS_KEY = ClassName.get("dagger.multibindings", "ClassKey");
    public static final ClassName INTO_MAP = ClassName.get("dagger.multibindings", "IntoMap");
    public static final ClassName MAP_KEY = ClassName.get("dagger", "MapKey");
    public static final ClassName MODULE = ClassName.get("dagger", "Module");
    public static final ClassName SUBCOMPONENT = ClassName.get("dagger", "Subcomponent");
    public static final ClassName SUBCOMPONENT_FACTORY = SUBCOMPONENT.nestedClass("Factory");

    // Dagger.android classnames
    public static final ClassName ANDROID_INJECTION_KEY =
            ClassName.get("dagger.android", "AndroidInjectionKey");
    public static final ClassName ANDROID_INJECTOR =
            ClassName.get("dagger.android", "AndroidInjector");
    public static final ClassName DISPATCHING_ANDROID_INJECTOR =
            ClassName.get("dagger.android", "DispatchingAndroidInjector");
    public static final ClassName ANDROID_INJECTOR_FACTORY = ANDROID_INJECTOR.nestedClass("Factory");
    public static final ClassName CONTRIBUTES_ANDROID_INJECTOR =
            ClassName.get("dagger.android", "ContributesAndroidInjector");

    // Other classnames
    public static final ClassName PROVIDER = ClassName.get("javax.inject", "Provider");
    public static final ClassName QUALIFIER = ClassName.get("javax.inject", "Qualifier");
    public static final ClassName SCOPE = ClassName.get("javax.inject", "Scope");

    private TypeNames() {}
}
