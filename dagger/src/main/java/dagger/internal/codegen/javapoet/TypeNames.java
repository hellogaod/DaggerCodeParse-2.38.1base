package dagger.internal.codegen.javapoet;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

/**
 * Common names and convenience methods for JavaPoet {@link TypeName} usage.
 */
public final class TypeNames {

    // Dagger Core classnames
    public static final ClassName ASSISTED = ClassName.get("dagger.assisted", "Assisted");
    public static final ClassName ASSISTED_FACTORY =
            ClassName.get("dagger.assisted", "AssistedFactory");
    public static final ClassName ASSISTED_INJECT =
            ClassName.get("dagger.assisted", "AssistedInject");
    public static final ClassName BINDS_INSTANCE = ClassName.get("dagger", "BindsInstance");
    public static final ClassName COMPONENT = ClassName.get("dagger", "Component");
    public static final ClassName COMPONENT_BUILDER = COMPONENT.nestedClass("Builder");
    public static final ClassName COMPONENT_FACTORY = COMPONENT.nestedClass("Factory");
    public static final ClassName ELEMENTS_INTO_SET =
            ClassName.get("dagger.multibindings", "ElementsIntoSet");
    public static final ClassName INTO_MAP = ClassName.get("dagger.multibindings", "IntoMap");
    public static final ClassName INTO_SET = ClassName.get("dagger.multibindings", "IntoSet");

    public static final ClassName MAP_KEY = ClassName.get("dagger", "MapKey");

    public static final ClassName BINDS = ClassName.get("dagger", "Binds");
    public static final ClassName MODULE = ClassName.get("dagger", "Module");
    public static final ClassName MULTIBINDS = ClassName.get("dagger.multibindings", "Multibinds");

    public static final ClassName PROVIDES = ClassName.get("dagger", "Provides");
    public static final ClassName REUSABLE = ClassName.get("dagger", "Reusable");
    public static final ClassName PRODUCES = ClassName.get("dagger.producers", "Produces");
    public static final ClassName PRODUCER_MODULE =
            ClassName.get("dagger.producers", "ProducerModule");

    public static final ClassName BINDS_OPTIONAL_OF = ClassName.get("dagger", "BindsOptionalOf");

    public static final ClassName SUBCOMPONENT = ClassName.get("dagger", "Subcomponent");
    public static final ClassName SUBCOMPONENT_BUILDER = SUBCOMPONENT.nestedClass("Builder");
    public static final ClassName SUBCOMPONENT_FACTORY = SUBCOMPONENT.nestedClass("Factory");

    // Dagger Internal classnames
    public static final ClassName INJECTED_FIELD_SIGNATURE =
            ClassName.get("dagger.internal", "InjectedFieldSignature");

    public static final ClassName PROVIDER = ClassName.get("javax.inject", "Provider");

    public static final ClassName LAZY = ClassName.get("dagger", "Lazy");

    // Dagger Producers classnames
    public static final ClassName CANCELLATION_POLICY =
            ClassName.get("dagger.producers", "CancellationPolicy");

    public static final ClassName PRODUCED = ClassName.get("dagger.producers", "Produced");

    public static final ClassName PRODUCER = ClassName.get("dagger.producers", "Producer");

    public static final ClassName PRODUCTION_COMPONENT =
            ClassName.get("dagger.producers", "ProductionComponent");
    public static final ClassName PRODUCTION_COMPONENT_BUILDER =
            PRODUCTION_COMPONENT.nestedClass("Builder");
    public static final ClassName PRODUCTION_COMPONENT_FACTORY =
            PRODUCTION_COMPONENT.nestedClass("Factory");

    public static final ClassName PRODUCTION_SUBCOMPONENT =
            ClassName.get("dagger.producers", "ProductionSubcomponent");

    public static final ClassName PRODUCTION_SUBCOMPONENT_BUILDER =
            PRODUCTION_SUBCOMPONENT.nestedClass("Builder");
    public static final ClassName PRODUCTION_SUBCOMPONENT_FACTORY =
            PRODUCTION_SUBCOMPONENT.nestedClass("Factory");
    public static final ClassName PRODUCTION_SCOPE =
            ClassName.get("dagger.producers", "ProductionScope");


    // Other classnames
    public static final ClassName SINGLETON = ClassName.get("javax.inject", "Singleton");
    public static final ClassName INJECT = ClassName.get("javax.inject", "Inject");

    public static final ClassName LISTENABLE_FUTURE =
            ClassName.get("com.google.common.util.concurrent", "ListenableFuture");

    public static ParameterizedTypeName lazyOf(TypeName typeName) {
        return ParameterizedTypeName.get(LAZY, typeName);
    }

    public static ParameterizedTypeName listenableFutureOf(TypeName typeName) {
        return ParameterizedTypeName.get(LISTENABLE_FUTURE, typeName);
    }

    public static ParameterizedTypeName producedOf(TypeName typeName) {
        return ParameterizedTypeName.get(PRODUCED, typeName);
    }

    public static ParameterizedTypeName producerOf(TypeName typeName) {
        return ParameterizedTypeName.get(PRODUCER, typeName);
    }

    public static ParameterizedTypeName providerOf(TypeName typeName) {
        //ParameterizedTypeName.get():返回Provider<typeName>
        return ParameterizedTypeName.get(PROVIDER, typeName);
    }


    private TypeNames() {
    }
}
