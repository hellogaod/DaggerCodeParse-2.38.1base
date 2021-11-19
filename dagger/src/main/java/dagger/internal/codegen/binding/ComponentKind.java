package dagger.internal.codegen.binding;


import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.Optional;

import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.javapoet.TypeNames;

import static com.google.common.collect.Sets.immutableEnumSet;
import static dagger.internal.codegen.extension.DaggerStreams.stream;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.internal.codegen.extension.DaggerStreams.valuesOf;
import static dagger.internal.codegen.langmodel.DaggerElements.isAnnotationPresent;
import static java.util.EnumSet.allOf;

/**
 * Enumeration of the different kinds of components.
 */
public enum ComponentKind {
    /**
     * {@code @Component}
     */
    COMPONENT(TypeNames.COMPONENT, true, false),

    /**
     * {@code @Subcomponent}
     */
    SUBCOMPONENT(TypeNames.SUBCOMPONENT, false, false),

    /**
     * {@code @ProductionComponent}
     */
    PRODUCTION_COMPONENT(TypeNames.PRODUCTION_COMPONENT, true, true),

    /**
     * {@code @ProductionSubcomponent}
     */
    PRODUCTION_SUBCOMPONENT(TypeNames.PRODUCTION_SUBCOMPONENT, false, true),

    /**
     * Kind for a descriptor that was generated from a {@link dagger.Module} instead of a component
     * type in order to validate the module's bindings.
     */
    MODULE(TypeNames.MODULE, true, false),

    /**
     * Kind for a descriptor was generated from a {@link dagger.producers.ProducerModule} instead of a
     * component type in order to validate the module's bindings.
     */
    PRODUCER_MODULE(TypeNames.PRODUCER_MODULE, true, true),
    ;

    //当前枚举所有值筛选非Module注解，isRoot
    private static final ImmutableSet<ComponentKind> ROOT_COMPONENT_KINDS =
            valuesOf(ComponentKind.class)
                    .filter(kind -> !kind.isForModuleValidation())
                    .filter(kind -> kind.isRoot())
                    .collect(toImmutableSet());

    //当前枚举所有值筛选非Module注解，非isRoot
    private static final ImmutableSet<ComponentKind> SUBCOMPONENT_KINDS =
            valuesOf(ComponentKind.class)
                    .filter(kind -> !kind.isForModuleValidation())
                    .filter(kind -> !kind.isRoot())
                    .collect(toImmutableSet());

    /**
     * Returns the set of kinds for root components.
     */
    public static ImmutableSet<ComponentKind> rootComponentKinds() {
        return ROOT_COMPONENT_KINDS;
    }

    /**
     * Returns the set of kinds for subcomponents.
     */
    public static ImmutableSet<ComponentKind> subcomponentKinds() {
        return SUBCOMPONENT_KINDS;
    }

    /**
     * Returns the annotations for components of the given kinds.
     * <p>
     * ComponentKind筛选出注解ClassName类型集合
     */
    public static ImmutableSet<ClassName> annotationsFor(Iterable<ComponentKind> kinds) {
        return stream(kinds).map(ComponentKind::annotation).collect(toImmutableSet());
    }

    /**
     * Returns the set of component kinds the given {@code element} has annotations for.
     *
     * 筛选element节点使用了当前枚举的那些注解
     */
    public static ImmutableSet<ComponentKind> getComponentKinds(TypeElement element) {
        return valuesOf(ComponentKind.class)
                .filter(kind -> isAnnotationPresent(element, kind.annotation()))
                .collect(toImmutableSet());
    }


    /**
     * Returns the kind of an annotated element if it is annotated with one of the {@linkplain
     * #annotation() annotations}.
     *
     * 筛选element节点使用了当前枚举的那些注解,最多只能使用该枚举集合注解中的一个，否则报错。
     *
     * @throws IllegalArgumentException if the element is annotated with more than one of the
     *     annotations
     */
    public static Optional<ComponentKind> forAnnotatedElement(TypeElement element) {
        ImmutableSet<ComponentKind> kinds = getComponentKinds(element);
        if (kinds.size() > 1) {
            throw new IllegalArgumentException(
                    element + " cannot be annotated with more than one of " + annotationsFor(kinds));
        }
        return kinds.stream().findAny();
    }

    private final ClassName annotation;
    private final boolean isRoot;
    private final boolean production;

    ComponentKind(ClassName annotation, boolean isRoot, boolean production) {
        this.annotation = annotation;
        this.isRoot = isRoot;
        this.production = production;
    }

    /**
     * Returns the annotation that marks a component of this kind.
     */
    public ClassName annotation() {
        return annotation;
    }

    /**
     * Returns the kinds of modules that can be used with a component of this kind.
     * <p>
     * 当前Component是否使用了Producer，如果是则module类可以使用ProducerModule或Module注解；
     * 否则module类只能使用Module注解修饰
     */
    public ImmutableSet<ModuleKind> legalModuleKinds() {
        return isProducer()
                ? immutableEnumSet(allOf(ModuleKind.class))
                : immutableEnumSet(ModuleKind.MODULE);
    }

    /**
     * Returns the kinds of subcomponents a component of this kind can have.
     * <p>
     * subcompoennt使用注解判断：当前是Producer类型，那么只能使用ProductionSubcomponent；
     * 否则Subcomponent和ProductionSubcomponent注解都可用
     */
    public ImmutableSet<ComponentKind> legalSubcomponentKinds() {
        return isProducer()
                ? immutableEnumSet(PRODUCTION_SUBCOMPONENT)
                : immutableEnumSet(SUBCOMPONENT, PRODUCTION_SUBCOMPONENT);
    }

    /**
     * Returns {@code true} if the descriptor is for a root component (not a subcomponent) or is for
     * {@linkplain #isForModuleValidation() module-validation}.
     */
    public boolean isRoot() {
        return isRoot;
    }

    /**
     * Returns true if this is a production component.
     */
    public boolean isProducer() {
        return production;
    }

    /**
     * Returns {@code true} if the descriptor is for a module in order to validate its bindings.
     */
    public boolean isForModuleValidation() {
        switch (this) {
            case MODULE:
            case PRODUCER_MODULE:
                return true;
            default:
                // fall through
        }
        return false;
    }
}
