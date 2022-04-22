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

/**
 * Represents information needed to create a component (i.e. modules, entry points, etc)
 */
@AutoValue
public abstract class ComponentDependencies {
    private static Builder builder() {
        return new AutoValue_ComponentDependencies.Builder();
    }

    /**
     * Returns the modules for a component, without any filtering.
     */
    public abstract ImmutableSetMultimap<ClassName, TypeElement> modules();

    /**
     * Returns the entry points associated with the given a component.
     */
    public abstract ImmutableSetMultimap<ClassName, TypeElement> entryPoints();

    /**
     * Returns the component entry point associated with the given a component.
     */
    public abstract ImmutableSetMultimap<ClassName, TypeElement> componentEntryPoints();

    @AutoValue.Builder
    abstract static class Builder {
        abstract ImmutableSetMultimap.Builder<ClassName, TypeElement> modulesBuilder();

        abstract ImmutableSetMultimap.Builder<ClassName, TypeElement> entryPointsBuilder();

        abstract ImmutableSetMultimap.Builder<ClassName, TypeElement> componentEntryPointsBuilder();

        abstract ComponentDependencies build();
    }

    /**
     * Returns the component dependencies for the given metadata.
     */
    public static ComponentDependencies from(
            ImmutableSet<ComponentDescriptor> descriptors,
            ImmutableSet<AggregatedDepsMetadata> aggregatedDepsMetadata,
            ImmutableSet<AggregatedUninstallModulesMetadata> aggregatedUninstallModulesMetadata,
            ImmutableSet<AggregatedEarlyEntryPointMetadata> aggregatedEarlyEntryPointMetadata,
            Elements elements) {

        //收集
        //1. @AggregatedUninstallModules#uninstallModules中的节点使用了@UninstallModules，并且也使用了@Module修饰;
        //2. @AggregatedDeps#replaces中的使用了@Module修饰；
        ImmutableSet<TypeElement> uninstalledModules =
                ImmutableSet.<TypeElement>builder()
                        .addAll(
                                aggregatedUninstallModulesMetadata.stream()
                                        //@AggregatedUninstallModules#uninstallModules中的节点使用了@UninstallModules，并且也使用了@Module修饰
                                        .flatMap(metadata -> metadata.uninstallModuleElements().stream())
                                        // @AggregatedUninstallModules always references the user module, so convert to
                                        // the generated public wrapper if needed.
                                        // TODO(bcorso): Consider converting this to the public module in the processor.
                                        .map(module -> PkgPrivateMetadata.publicModule(module, elements))
                                        .collect(toImmutableSet()))
                        .addAll(
                                aggregatedDepsMetadata.stream()
                                        //@AggregatedDeps#replaces中的使用了@Module修饰
                                        .flatMap(metadata -> metadata.replacedDependencies().stream())
                                        .collect(toImmutableSet()))
                        .build();

        ComponentDependencies.Builder componentDependencies = ComponentDependencies.builder();

        //@DefineComponent修饰的节点
        ImmutableSet<ClassName> componentNames =
                descriptors.stream().map(ComponentDescriptor::component).collect(toImmutableSet());

        for (AggregatedDepsMetadata metadata : aggregatedDepsMetadata) {
            //@AggregatedDeps#components中的节点
            for (TypeElement componentElement : metadata.componentElements()) {

                ClassName componentName = ClassName.get(componentElement);

                //项目中：所有@AggregatedDeps#components中的节点 必须存在于 所有@DefineComponent修饰的节点
                checkState(
                        componentNames.contains(componentName), "%s is not a valid Component.", componentName);

                switch (metadata.dependencyType()) {
                    case MODULE://@AggregatedDeps#modules
                        if (!uninstalledModules.contains(metadata.dependency())) {
                            componentDependencies.modulesBuilder().put(componentName, metadata.dependency());
                        }
                        break;
                    case ENTRY_POINT://@AggregatedDeps#entryPoints
                        componentDependencies.entryPointsBuilder().put(componentName, metadata.dependency());
                        break;
                    case COMPONENT_ENTRY_POINT://@AggregatedDeps#componentEntryPoints
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
                        //@AggregatedEarlyEntryPoint#earlyEntryPoint
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
