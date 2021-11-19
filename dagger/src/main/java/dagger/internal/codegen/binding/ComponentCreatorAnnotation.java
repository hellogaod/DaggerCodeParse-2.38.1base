package dagger.internal.codegen.binding;


import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.base.ComponentAnnotation;
import dagger.internal.codegen.javapoet.TypeNames;

import static com.google.common.base.Ascii.toUpperCase;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.internal.codegen.extension.DaggerStreams.valuesOf;
import static dagger.internal.codegen.langmodel.DaggerElements.isAnnotationPresent;
import static java.util.stream.Collectors.mapping;

/**
 * Simple representation of a component creator annotation type.
 * <p>
 * (Production)(Sub)Component.Builder或(Production)(Sub)Component.Factory类型
 */
public enum ComponentCreatorAnnotation {

    COMPONENT_BUILDER(TypeNames.COMPONENT_BUILDER),
    COMPONENT_FACTORY(TypeNames.COMPONENT_FACTORY),
    SUBCOMPONENT_BUILDER(TypeNames.SUBCOMPONENT_BUILDER),
    SUBCOMPONENT_FACTORY(TypeNames.SUBCOMPONENT_FACTORY),
    PRODUCTION_COMPONENT_BUILDER(TypeNames.PRODUCTION_COMPONENT_BUILDER),
    PRODUCTION_COMPONENT_FACTORY(TypeNames.PRODUCTION_COMPONENT_FACTORY),
    PRODUCTION_SUBCOMPONENT_BUILDER(TypeNames.PRODUCTION_SUBCOMPONENT_BUILDER),
    PRODUCTION_SUBCOMPONENT_FACTORY(TypeNames.PRODUCTION_SUBCOMPONENT_FACTORY),
    ;

    private final ClassName annotation;
    private final ComponentCreatorKind creatorKind;
    private final ClassName componentAnnotation;

    ComponentCreatorAnnotation(ClassName annotation) {
        this.annotation = annotation;
        this.creatorKind = ComponentCreatorKind.valueOf(toUpperCase(annotation.simpleName()));
        this.componentAnnotation = annotation.enclosingClassName();
    }

    /**
     * The actual annotation type.
     */
    public ClassName annotation() {
        return annotation;
    }

    /**
     * The component annotation type that encloses this creator annotation type.
     */
    public final ClassName componentAnnotation() {
        return componentAnnotation;
    }

    /**
     * Returns {@code true} if the creator annotation is for a subcomponent.
     */
    public final boolean isSubcomponentCreatorAnnotation() {
        return componentAnnotation().simpleName().endsWith("Subcomponent");
    }

    /**
     * Returns {@code true} if the creator annotation is for a production component or subcomponent.
     */
    public final boolean isProductionCreatorAnnotation() {
        return componentAnnotation().simpleName().startsWith("Production");
    }

    /**
     * The creator kind the annotation is associated with.
     */
    // TODO(dpb): Remove ComponentCreatorKind.
    public ComponentCreatorKind creatorKind() {
        return creatorKind;
    }

    @Override
    public final String toString() {
        return annotation().canonicalName();
    }


    /**
     * Returns all component creator annotations.
     * <p>
     * 所有component creator 注解收集
     */
    public static ImmutableSet<ClassName> allCreatorAnnotations() {
        return stream().collect(toAnnotationClasses());
    }

    /**
     * Returns all root component creator annotations.
     * <p>
     * 非Sub修饰componentAnnotation的ComponentCreatorAnnotation注解集合，以ClassName呈现
     */
    public static ImmutableSet<ClassName> rootComponentCreatorAnnotations() {
        return stream()
                .filter(
                        componentCreatorAnnotation ->
                                !componentCreatorAnnotation.isSubcomponentCreatorAnnotation())
                .collect(toAnnotationClasses());
    }

    /**
     * Returns all subcomponent creator annotations.
     * <p>
     * 返回ComponentCreatorAnnotation枚举中componentAnnotation使用(Production)SubComponent注解的集合
     */
    public static ImmutableSet<ClassName> subcomponentCreatorAnnotations() {
        return stream()
                .filter(
                        componentCreatorAnnotation ->
                                componentCreatorAnnotation.isSubcomponentCreatorAnnotation())
                .collect(toAnnotationClasses());
    }

    /**
     * Returns all production component creator annotations.
     * <p>
     * 返回ComponentCreatorAnnotation枚举中componentAnnotation使用Production(Sub)Component注解的集合
     */
    public static ImmutableSet<ClassName> productionCreatorAnnotations() {
        return stream()
                .filter(
                        componentCreatorAnnotation ->
                                componentCreatorAnnotation.isProductionCreatorAnnotation())
                .collect(toAnnotationClasses());
    }

    /**
     * Returns the legal creator annotations for the given {@code componentAnnotation}.
     * <p>
     * ComponentAnnotation中的注解对应于ComponentCreatorAnnotation的componentAnnotation()注解，
     * 返回ComponentCreatorAnnotation的annotation（即Builder或Factory注解）以ClassName呈现
     */
    public static ImmutableSet<ClassName> creatorAnnotationsFor(
            ComponentAnnotation componentAnnotation) {
        return stream()
                .filter(
                        creatorAnnotation ->
                                creatorAnnotation
                                        .componentAnnotation()
                                        .simpleName()
                                        .equals(componentAnnotation.simpleName()))
                .collect(toAnnotationClasses());
    }

    /**
     * Returns all creator annotations present on the given {@code type}.
     * <p>
     * 收集type类使用了ComponentCreatorAnnotation枚举中注解集
     */
    public static ImmutableSet<ComponentCreatorAnnotation> getCreatorAnnotations(TypeElement type) {
        return stream()
                .filter(cca -> isAnnotationPresent(type, cca.annotation()))
                .collect(toImmutableSet());
    }

    private static Stream<ComponentCreatorAnnotation> stream() {
        return valuesOf(ComponentCreatorAnnotation.class);
    }

    private static Collector<ComponentCreatorAnnotation, ?, ImmutableSet<ClassName>>
    toAnnotationClasses() {
        return mapping(ComponentCreatorAnnotation::annotation, toImmutableSet());
    }
}
