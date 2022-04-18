package dagger.hilt.processor.internal.root;


import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import dagger.hilt.android.internal.earlyentrypoint.AggregatedEarlyEntryPoint;
import dagger.hilt.android.internal.uninstallmodules.AggregatedUninstallModules;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;
import dagger.hilt.processor.internal.aggregateddeps.AggregatedDeps;
import dagger.hilt.processor.internal.root.ir.ComponentTreeDepsIr;

import static com.google.common.base.Preconditions.checkArgument;
import static dagger.hilt.processor.internal.AggregatedElements.unwrapProxies;
import static dagger.hilt.processor.internal.AnnotationValues.getTypeElements;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/**
 * Represents the values stored in an {@link
 * dagger.hilt.internal.componenttreedeps.ComponentTreeDeps}.
 *
 * <p>This class is used in both writing ({@link ComponentTreeDepsGenerator}) and reading ({@link
 * ComponentTreeDepsProcessor}) of the {@code @ComponentTreeDeps} annotation.
 */
@AutoValue
abstract class ComponentTreeDepsMetadata {
    /**
     * Returns the name of the element annotated with {@link
     * dagger.hilt.internal.componenttreedeps.ComponentTreeDeps}.
     */
    abstract ClassName name();

    /** Returns the {@link dagger.hilt.internal.aggregatedroot.AggregatedRoot} deps. */
    abstract ImmutableSet<TypeElement> aggregatedRootDeps();

    /** Returns the {@link dagger.hilt.internal.definecomponent.DefineComponentClasses} deps. */
    abstract ImmutableSet<TypeElement> defineComponentDeps();

    /** Returns the {@link dagger.hilt.internal.aliasof.AliasOfPropagatedData} deps. */
    abstract ImmutableSet<TypeElement> aliasOfDeps();

    /** Returns the {@link  AggregatedDeps} deps. */
    abstract ImmutableSet<TypeElement> aggregatedDeps();

    /** Returns the {@link  AggregatedUninstallModules} deps. */
    abstract ImmutableSet<TypeElement> aggregatedUninstallModulesDeps();

    /** Returns the {@link  AggregatedEarlyEntryPoint} deps. */
    abstract ImmutableSet<TypeElement> aggregatedEarlyEntryPointDeps();

    static ComponentTreeDepsMetadata from(TypeElement element, Elements elements) {

        checkArgument(Processors.hasAnnotation(element, ClassNames.COMPONENT_TREE_DEPS));

        AnnotationMirror annotationMirror =
                Processors.getAnnotationMirror(element, ClassNames.COMPONENT_TREE_DEPS);

        ImmutableMap<String, AnnotationValue> values =
                Processors.getAnnotationValues(elements, annotationMirror);

        return create(
                ClassName.get(element),
                unwrapProxies(getTypeElements(values.get("rootDeps")), elements),
                unwrapProxies(getTypeElements(values.get("defineComponentDeps")), elements),
                unwrapProxies(getTypeElements(values.get("aliasOfDeps")), elements),
                unwrapProxies(getTypeElements(values.get("aggregatedDeps")), elements),
                unwrapProxies(getTypeElements(values.get("uninstallModulesDeps")), elements),
                unwrapProxies(getTypeElements(values.get("earlyEntryPointDeps")), elements));
    }

    static ComponentTreeDepsMetadata from(ComponentTreeDepsIr ir, Elements elements) {
        return create(
                ir.getName(),
                ir.getRootDeps().stream()
                        .map(it -> elements.getTypeElement(it.canonicalName()))
                        .collect(toImmutableSet()),
                ir.getDefineComponentDeps().stream()
                        .map(it -> elements.getTypeElement(it.canonicalName()))
                        .collect(toImmutableSet()),
                ir.getAliasOfDeps().stream()
                        .map(it -> elements.getTypeElement(it.canonicalName()))
                        .collect(toImmutableSet()),
                ir.getAggregatedDeps().stream()
                        .map(it -> elements.getTypeElement(it.canonicalName()))
                        .collect(toImmutableSet()),
                ir.getUninstallModulesDeps().stream()
                        .map(it -> elements.getTypeElement(it.canonicalName()))
                        .collect(toImmutableSet()),
                ir.getEarlyEntryPointDeps().stream()
                        .map(it -> elements.getTypeElement(it.canonicalName()))
                        .collect(toImmutableSet()));
    }

    static ComponentTreeDepsMetadata create(
            ClassName name,
            ImmutableSet<TypeElement> aggregatedRootDeps,
            ImmutableSet<TypeElement> defineComponentDeps,
            ImmutableSet<TypeElement> aliasOfDeps,
            ImmutableSet<TypeElement> aggregatedDeps,
            ImmutableSet<TypeElement> aggregatedUninstallModulesDeps,
            ImmutableSet<TypeElement> aggregatedEarlyEntryPointDeps) {
        return new AutoValue_ComponentTreeDepsMetadata(
                name,
                aggregatedRootDeps,
                defineComponentDeps,
                aliasOfDeps,
                aggregatedDeps,
                aggregatedUninstallModulesDeps,
                aggregatedEarlyEntryPointDeps);
    }
}

