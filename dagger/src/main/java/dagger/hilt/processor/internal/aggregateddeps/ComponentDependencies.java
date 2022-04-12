package dagger.hilt.processor.internal.aggregateddeps;


import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.ComponentDescriptor;
import dagger.hilt.processor.internal.earlyentrypoint.AggregatedEarlyEntryPointMetadata;
import dagger.hilt.processor.internal.uninstallmodules.AggregatedUninstallModulesMetadata;

import static com.google.common.base.Preconditions.checkState;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/** Represents information needed to create a component (i.e. modules, entry points, etc) */
@AutoValue
public abstract class ComponentDependencies {
    private static Builder builder() {
        return new AutoValue_ComponentDependencies.Builder();
    }

    /** Returns the modules for a component, without any filtering. */
    public abstract ImmutableSetMultimap<ClassName, TypeElement> modules();

    /** Returns the entry points associated with the given a component. */
    public abstract ImmutableSetMultimap<ClassName, TypeElement> entryPoints();

    /** Returns the component entry point associated with the given a component. */
    public abstract ImmutableSetMultimap<ClassName, TypeElement> componentEntryPoints();

    @AutoValue.Builder
    abstract static class Builder {
        abstract ImmutableSetMultimap.Builder<ClassName, TypeElement> modulesBuilder();

        abstract ImmutableSetMultimap.Builder<ClassName, TypeElement> entryPointsBuilder();

        abstract ImmutableSetMultimap.Builder<ClassName, TypeElement> componentEntryPointsBuilder();

        abstract ComponentDependencies build();
    }

    /** Returns the component dependencies for the given metadata. */
    public static ComponentDependencies from(
            ImmutableSet<ComponentDescriptor> descriptors,
            ImmutableSet<AggregatedDepsMetadata> aggregatedDepsMetadata,
            ImmutableSet<AggregatedUninstallModulesMetadata> aggregatedUninstallModulesMetadata,
            ImmutableSet<AggregatedEarlyEntryPointMetadata> aggregatedEarlyEntryPointMetadata,
            Elements elements) {
        ImmutableSet<TypeElement> uninstalledModules =
                ImmutableSet.<TypeElement>builder()
                        .addAll(
                                aggregatedUninstallModulesMetadata.stream()
                                        .flatMap(metadata -> metadata.uninstallModuleElements().stream())
                                        // @AggregatedUninstallModules always references the user module, so convert to
                                        // the generated public wrapper if needed.
                                        // TODO(bcorso): Consider converting this to the public module in the processor.
                                        .map(module -> PkgPrivateMetadata.publicModule(module, elements))
                                        .collect(toImmutableSet()))
                        .addAll(
                                aggregatedDepsMetadata.stream()
                                        .flatMap(metadata -> metadata.replacedDependencies().stream())
                                        .collect(toImmutableSet()))
                        .build();

        ComponentDependencies.Builder componentDependencies = ComponentDependencies.builder();
        ImmutableSet<ClassName> componentNames =
                descriptors.stream().map(ComponentDescriptor::component).collect(toImmutableSet());
        for (AggregatedDepsMetadata metadata : aggregatedDepsMetadata) {
            for (TypeElement componentElement : metadata.componentElements()) {
                ClassName componentName = ClassName.get(componentElement);
                checkState(
                        componentNames.contains(componentName), "%s is not a valid Component.", componentName);
                switch (metadata.dependencyType()) {
                    case MODULE:
                        if (!uninstalledModules.contains(metadata.dependency())) {
                            componentDependencies.modulesBuilder().put(componentName, metadata.dependency());
                        }
                        break;
                    case ENTRY_POINT:
                        componentDependencies.entryPointsBuilder().put(componentName, metadata.dependency());
                        break;
                    case COMPONENT_ENTRY_POINT:
                        componentDependencies
                                .componentEntryPointsBuilder()
                                .put(componentName, metadata.dependency());
                        break;
                }
            }
        }

        componentDependencies
                .entryPointsBuilder()
                .putAll(
                        ClassNames.SINGLETON_COMPONENT,
                        aggregatedEarlyEntryPointMetadata.stream()
                                .map(AggregatedEarlyEntryPointMetadata::earlyEntryPoint)
                                // @AggregatedEarlyEntryPointMetadata always references the user module, so convert
                                // to the generated public wrapper if needed.
                                // TODO(bcorso): Consider converting this to the public module in the processor.
                                .map(entryPoint -> PkgPrivateMetadata.publicEarlyEntryPoint(entryPoint, elements))
                                .collect(toImmutableSet()));

        return componentDependencies.build();
    }
}
