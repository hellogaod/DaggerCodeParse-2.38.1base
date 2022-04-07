package dagger.hilt.processor.internal;


import com.squareup.javapoet.ClassName;

import static com.squareup.javapoet.ClassName.get;

/** Holder for commonly used class names. */
public final class ClassNames {

    public static final ClassName INSTALL_IN =
            get("dagger.hilt", "InstallIn");

    public static final ClassName CONTEXTS = get("dagger.hilt.android.internal", "Contexts");

    public static final ClassName GENERATED_COMPONENT_MANAGER =
            get("dagger.hilt.internal", "GeneratedComponentManager");

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

    private ClassNames() {}
}
