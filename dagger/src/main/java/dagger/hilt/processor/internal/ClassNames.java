package dagger.hilt.processor.internal;


import com.squareup.javapoet.ClassName;

import static com.squareup.javapoet.ClassName.get;

/**
 * Holder for commonly used class names.
 */
public final class ClassNames {

    public static final ClassName ALIAS_OF = get("dagger.hilt.migration", "AliasOf");

    public static final ClassName DEFINE_COMPONENT_BUILDER =
            get("dagger.hilt", "DefineComponent", "Builder");

    public static final ClassName DEFINE_COMPONENT_NO_PARENT =
            get("dagger.hilt.internal.definecomponent", "DefineComponentNoParent");

    public static final ClassName INSTALL_IN =
            get("dagger.hilt", "InstallIn");
    public static final ClassName INJECT =
            get("javax.inject", "Inject");
    public static final ClassName ASSISTED_INJECT = get("dagger.assisted", "AssistedInject");
    public static final ClassName SCOPE =
            get("javax.inject", "Scope");
    public static final ClassName CONTEXT = get("android.content", "Context");
    public static final ClassName SINGLETON_COMPONENT =
            get("dagger.hilt.components", "SingletonComponent");

    public static final ClassName ALIAS_OF_PROPAGATED_DATA =
            get("dagger.hilt.internal.aliasof", "AliasOfPropagatedData");
    public static final String ALIAS_OF_PROPAGATED_DATA_PACKAGE =
            "dagger.hilt.processor.internal.aliasof.codegen";

    public static final String DEFINE_COMPONENT_CLASSES_PACKAGE =
            "dagger.hilt.processor.internal.definecomponent.codegen";

    public static final ClassName DEFINE_COMPONENT_CLASSES =
            get("dagger.hilt.internal.definecomponent", "DefineComponentClasses");

    public static final ClassName DISABLE_INSTALL_IN_CHECK =
            get("dagger.hilt.migration", "DisableInstallInCheck");

    public static final ClassName DEFINE_COMPONENT = get("dagger.hilt", "DefineComponent");

    public static final ClassName TEST_APPLICATION_COMPONENT_MANAGER =
            get("dagger.hilt.android.internal.testing", "TestApplicationComponentManager");
    public static final ClassName TEST_APPLICATION_COMPONENT_MANAGER_HOLDER =
            get("dagger.hilt.android.internal.testing", "TestApplicationComponentManagerHolder");

    public static final ClassName CONTEXTS = get("dagger.hilt.android.internal", "Contexts");

    public static final ClassName PROVIDES =
            get("dagger", "Provides");
    public static final ClassName MODULE = get("dagger", "Module");

    public static final ClassName GENERATED_COMPONENT_MANAGER =
            get("dagger.hilt.internal", "GeneratedComponentManager");

    public static final ClassName BINDS =
            get("dagger", "Binds");

    public static final ClassName CONTRIBUTES_ANDROID_INJECTOR =
            get("dagger.android", "ContributesAndroidInjector");

    public static final ClassName INTO_MAP = get("dagger.multibindings", "IntoMap");
    public static final ClassName INTO_SET = get("dagger.multibindings", "IntoSet");
    public static final ClassName STRING_KEY = get("dagger.multibindings", "StringKey");

    public static final ClassName PRECONDITIONS = get("dagger.hilt.internal", "Preconditions");
    public static final ClassName OBJECT = get("java.lang", "Object");
    public static final ClassName ORIGINATING_ELEMENT =
            get("dagger.hilt.codegen", "OriginatingElement");
    public static final ClassName GENERATED_ENTRY_POINT =
            get("dagger.hilt.internal", "GeneratedEntryPoint");

    public static final ClassName UNSAFE_CASTS = get("dagger.hilt.internal", "UnsafeCasts");

    // Kotlin-specific class names
    public static final ClassName KOTLIN_METADATA = get("kotlin", "Metadata");

    public static final ClassName GENERATED_COMPONENT_MANAGER_HOLDER =
            get("dagger.hilt.internal", "GeneratedComponentManagerHolder");

    public static final ClassName ANDROID_BIND_VALUE =
            get("dagger.hilt.android.testing", "BindValue");
    public static final ClassName ANDROID_BIND_VALUE_INTO_SET =
            get("dagger.hilt.android.testing", "BindValueIntoSet");
    public static final ClassName ANDROID_BIND_ELEMENTS_INTO_SET =
            get("dagger.hilt.android.testing", "BindElementsIntoSet");
    public static final ClassName ANDROID_BIND_VALUE_INTO_MAP =
            get("dagger.hilt.android.testing", "BindValueIntoMap");
    public static final ClassName HILT_ANDROID_TEST =
            get("dagger.hilt.android.testing", "HiltAndroidTest");

    public static final ClassName APPLICATION_CONTEXT =
            get("dagger.hilt.android.qualifiers", "ApplicationContext");

    public static final ClassName CUSTOM_TEST_APPLICATION =
            get("dagger.hilt.android.testing", "CustomTestApplication");

    public static final ClassName HILT_ANDROID_APP =
            get("dagger.hilt.android", "HiltAndroidApp");

    public static final ClassName ENTRY_POINT =
            get("dagger.hilt", "EntryPoint");
    public static final ClassName EARLY_ENTRY_POINT = get("dagger.hilt.android", "EarlyEntryPoint");
    public static final ClassName COMPONENT_ENTRY_POINT =
            get("dagger.hilt.internal", "ComponentEntryPoint");

    public static final ClassName TEST_INSTALL_IN = get("dagger.hilt.testing", "TestInstallIn");

    public static final ClassName APPLICATION = get("android.app", "Application");
    public static final ClassName APPLICATION_CONTEXT_MODULE =
            get("dagger.hilt.android.internal.modules", "ApplicationContextModule");

    public static final ClassName BINDS_OPTIONAL_OF =
            get("dagger", "BindsOptionalOf");
    public static final ClassName MULTIBINDS =
            get("dagger.multibindings", "Multibinds");

    private ClassNames() {
    }
}
