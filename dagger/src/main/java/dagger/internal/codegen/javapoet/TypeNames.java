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
    public static final ClassName DEPENDENCY_METHOD_PRODUCER =
            ClassName.get("dagger.producers.internal", "DependencyMethodProducer");

    // Dagger Internal classnames
    public static final ClassName DOUBLE_CHECK = ClassName.get("dagger.internal", "DoubleCheck");
    public static final ClassName FACTORY = ClassName.get("dagger.internal", "Factory");
    public static final ClassName INJECTED_FIELD_SIGNATURE =
            ClassName.get("dagger.internal", "InjectedFieldSignature");
    public static final ClassName INSTANCE_FACTORY =
            ClassName.get("dagger.internal", "InstanceFactory");
    public static final ClassName MAP_FACTORY = ClassName.get("dagger.internal", "MapFactory");
    public static final ClassName MEMBERS_INJECTOR = ClassName.get("dagger", "MembersInjector");
    public static final ClassName PROVIDER = ClassName.get("javax.inject", "Provider");
    public static final ClassName PROVIDER_OF_LAZY =
            ClassName.get("dagger.internal", "ProviderOfLazy");
    public static final ClassName SET_FACTORY = ClassName.get("dagger.internal", "SetFactory");
    public static final ClassName SINGLE_CHECK = ClassName.get("dagger.internal", "SingleCheck");
    public static final ClassName LAZY = ClassName.get("dagger", "Lazy");
    public static final ClassName MAP_OF_PRODUCED_PRODUCER =
            ClassName.get("dagger.producers.internal", "MapOfProducedProducer");
    public static final ClassName MAP_OF_PRODUCER_PRODUCER =
            ClassName.get("dagger.producers.internal", "MapOfProducerProducer");
    public static final ClassName MAP_PRODUCER =
            ClassName.get("dagger.producers.internal", "MapProducer");
    public static final ClassName MAP_PROVIDER_FACTORY =
            ClassName.get("dagger.internal", "MapProviderFactory");
    public static final ClassName MEMBERS_INJECTORS =
            ClassName.get("dagger.internal", "MembersInjectors");

    // Dagger Producers classnames
    public static final ClassName ABSTRACT_PRODUCER =
            ClassName.get("dagger.producers.internal", "AbstractProducer");
    public static final ClassName CANCELLATION_POLICY =
            ClassName.get("dagger.producers", "CancellationPolicy");
    public static final ClassName CANCELLATION_LISTENER =
            ClassName.get("dagger.producers.internal", "CancellationListener");

    public static final ClassName PRODUCED = ClassName.get("dagger.producers", "Produced");

    public static final ClassName PRODUCER = ClassName.get("dagger.producers", "Producer");
    public static final ClassName PRODUCERS = ClassName.get("dagger.producers.internal", "Producers");
    public static final ClassName PRODUCTION_COMPONENT =
            ClassName.get("dagger.producers", "ProductionComponent");
    public static final ClassName PRODUCTION_COMPONENT_BUILDER =
            PRODUCTION_COMPONENT.nestedClass("Builder");
    public static final ClassName PRODUCTION_COMPONENT_FACTORY =
            PRODUCTION_COMPONENT.nestedClass("Factory");
    public static final ClassName SET_OF_PRODUCED_PRODUCER =
            ClassName.get("dagger.producers.internal", "SetOfProducedProducer");
    public static final ClassName SET_PRODUCER =
            ClassName.get("dagger.producers.internal", "SetProducer");

    public static final ClassName PRODUCTION_SUBCOMPONENT =
            ClassName.get("dagger.producers", "ProductionSubcomponent");

    public static final ClassName PRODUCTION_SUBCOMPONENT_BUILDER =
            PRODUCTION_SUBCOMPONENT.nestedClass("Builder");
    public static final ClassName PRODUCTION_SUBCOMPONENT_FACTORY =
            PRODUCTION_SUBCOMPONENT.nestedClass("Factory");
    public static final ClassName PRODUCTION_COMPONENT_MONITOR_FACTORY =
            ClassName.get("dagger.producers.monitoring", "ProductionComponentMonitor", "Factory");

    public static final ClassName PRODUCTION_SCOPE =
            ClassName.get("dagger.producers", "ProductionScope");

    public static final ClassName PRODUCER_TOKEN =
            ClassName.get("dagger.producers.monitoring", "ProducerToken");

    // Other classnames
    public static final ClassName SINGLETON = ClassName.get("javax.inject", "Singleton");
    public static final ClassName INJECT = ClassName.get("javax.inject", "Inject");
    public static final ClassName SET = ClassName.get("java.util", "Set");
    public static final ClassName LISTENABLE_FUTURE =
            ClassName.get("com.google.common.util.concurrent", "ListenableFuture");
    public static final ClassName FUTURES =
            ClassName.get("com.google.common.util.concurrent", "Futures");
    public static final ClassName LIST = ClassName.get("java.util", "List");
    /**
     * {@link TypeName#VOID} is lowercase-v {@code void} whereas this represents the class, {@link
     * Void}.
     */
    public static final ClassName VOID_CLASS = ClassName.get(Void.class);

    public static ParameterizedTypeName factoryOf(TypeName factoryType) {
        return ParameterizedTypeName.get(FACTORY, factoryType);
    }

    public static ParameterizedTypeName abstractProducerOf(TypeName typeName) {
        return ParameterizedTypeName.get(ABSTRACT_PRODUCER, typeName);
    }

    public static ParameterizedTypeName lazyOf(TypeName typeName) {
        return ParameterizedTypeName.get(LAZY, typeName);
    }

    public static ParameterizedTypeName listOf(TypeName typeName) {
        return ParameterizedTypeName.get(LIST, typeName);
    }

    public static ParameterizedTypeName listenableFutureOf(TypeName typeName) {
        return ParameterizedTypeName.get(LISTENABLE_FUTURE, typeName);
    }

    public static ParameterizedTypeName membersInjectorOf(TypeName membersInjectorType) {
        return ParameterizedTypeName.get(MEMBERS_INJECTOR, membersInjectorType);
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

    public static ParameterizedTypeName setOf(TypeName elementType) {
        return ParameterizedTypeName.get(SET, elementType);
    }

    /**
     * Returns the {@link TypeName} for the raw type of the given type name. If the argument isn't a
     * parameterized type, it returns the argument unchanged.
     */
    public static TypeName rawTypeName(TypeName typeName) {
        return (typeName instanceof ParameterizedTypeName)
                ? ((ParameterizedTypeName) typeName).rawType//如果类型带有嵌套,例如List<T>返回List
                : typeName;
    }

    public static ParameterizedTypeName dependencyMethodProducerOf(TypeName typeName) {
        return ParameterizedTypeName.get(DEPENDENCY_METHOD_PRODUCER, typeName);
    }

    private TypeNames() {
    }
}
