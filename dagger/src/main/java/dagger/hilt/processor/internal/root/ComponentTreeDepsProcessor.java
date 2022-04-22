package dagger.hilt.processor.internal.root;


import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import dagger.hilt.android.processor.internal.androidentrypoint.AndroidEntryPointMetadata;
import dagger.hilt.android.processor.internal.androidentrypoint.ApplicationGenerator;
import dagger.hilt.processor.internal.BaseProcessor;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.ComponentDescriptor;
import dagger.hilt.processor.internal.ComponentNames;
import dagger.hilt.processor.internal.ProcessorErrors;
import dagger.hilt.processor.internal.Processors;
import dagger.hilt.processor.internal.aggregateddeps.AggregatedDepsMetadata;
import dagger.hilt.processor.internal.aggregateddeps.ComponentDependencies;
import dagger.hilt.processor.internal.aliasof.AliasOfPropagatedDataMetadata;
import dagger.hilt.processor.internal.aliasof.AliasOfs;
import dagger.hilt.processor.internal.definecomponent.DefineComponentClassesMetadata;
import dagger.hilt.processor.internal.definecomponent.DefineComponents;
import dagger.hilt.processor.internal.earlyentrypoint.AggregatedEarlyEntryPointMetadata;
import dagger.hilt.processor.internal.uninstallmodules.AggregatedUninstallModulesMetadata;

import static com.google.auto.common.MoreElements.asType;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.hilt.processor.internal.HiltCompilerOptions.useAggregatingRootProcessor;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING;

/**
 * Processor that outputs dagger components based on transitive build deps.
 *
 * 处理@ComponentTreeDeps注解修饰的节点
 */
@IncrementalAnnotationProcessor(ISOLATING)
@AutoService(Processor.class)
public final class ComponentTreeDepsProcessor extends BaseProcessor {
    private final Set<ClassName> componentTreeDepNames = new HashSet<>();
    private final Set<ClassName> processed = new HashSet<>();
    private final DefineComponents defineComponents = DefineComponents.create();

    @Override
    public ImmutableSet<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(ClassNames.COMPONENT_TREE_DEPS.toString());
    }

    @Override
    public void processEach(TypeElement annotation, Element element) {
        componentTreeDepNames.add(ClassName.get(asType(element)));
    }

    @Override
    public void postRoundProcess(RoundEnvironment roundEnv) throws Exception {
        ImmutableSet<ComponentTreeDepsMetadata> componentTreeDepsToProcess =
                componentTreeDepNames.stream()
                        .filter(className -> !processed.contains(className))
                        .map(className -> getElementUtils().getTypeElement(className.canonicalName()))
                        .map(element -> ComponentTreeDepsMetadata.from(element, getElementUtils()))
                        .collect(toImmutableSet());

        for (ComponentTreeDepsMetadata metadata : componentTreeDepsToProcess) {
            processComponentTreeDeps(metadata);
        }
    }

    private void processComponentTreeDeps(ComponentTreeDepsMetadata metadata) throws IOException {
        TypeElement metadataElement = getElementUtils().getTypeElement(metadata.name().canonicalName());
        try {
            // We choose a name for the generated components/wrapper based off of the originating element
            // annotated with @ComponentTreeDeps. This is close to but isn't necessarily a "real" name of
            // a root, since with shared test components, even for single roots, the component tree deps
            // will be moved to a shared package with a deduped name.
            ClassName renamedRoot = Processors.removeNameSuffix(metadataElement, "_ComponentTreeDeps");
            ComponentNames componentNames = ComponentNames.withRenaming(rootName -> renamedRoot);

            //如果是Default类
            boolean isDefaultRoot = ClassNames.DEFAULT_ROOT.equals(renamedRoot);

            //@AggregatedRoot#root中的节点生成Root对象
            ImmutableSet<Root> roots =
                    //metadata.aggregatedRootDeps():dagger.hilt.internal.aggregatedroot.codegen包下使用@AggregatedRoot修饰的节点;
                    AggregatedRootMetadata.from(metadata.aggregatedRootDeps(), processingEnv).stream()
                            .map(AggregatedRootMetadata::rootElement)
                            .map(rootElement -> Root.create(rootElement, getProcessingEnv()))
                            .collect(toImmutableSet());

            // TODO(bcorso): For legacy reasons, a lot of the generating code requires a "root" as input
            // since we used to assume 1 root per component tree. Now that each ComponentTreeDeps may
            // represent multiple roots, we should refactor this logic.
            Root root =
                    isDefaultRoot
                            ? Root.createDefaultRoot(getProcessingEnv())
                            // Non-default roots should only ever be associated with one root element
                            : getOnlyElement(roots);

            ImmutableSet<ComponentDescriptor> componentDescriptors =
                    defineComponents.getComponentDescriptors(
                            DefineComponentClassesMetadata.from(
                                    metadata.defineComponentDeps(), getElementUtils()));

            ComponentTree tree = ComponentTree.from(componentDescriptors);

            ComponentDependencies deps =
                    ComponentDependencies.from(
                            componentDescriptors,
                            AggregatedDepsMetadata.from(metadata.aggregatedDeps(), getElementUtils()),
                            AggregatedUninstallModulesMetadata.from(
                                    metadata.aggregatedUninstallModulesDeps(), getElementUtils()),
                            AggregatedEarlyEntryPointMetadata.from(
                                    metadata.aggregatedEarlyEntryPointDeps(), getElementUtils()),
                            getElementUtils());

            AliasOfs aliasOfs =
                    AliasOfs.create(
                            AliasOfPropagatedDataMetadata.from(metadata.aliasOfDeps(), getElementUtils()),
                            componentDescriptors);

            RootMetadata rootMetadata =
                    RootMetadata.create(root, tree, deps, aliasOfs, getProcessingEnv());

            generateComponents(metadata, rootMetadata, componentNames);

            // Generate a creator for the early entry point if there is a default component available.
            if (isDefaultRoot) {
                EarlySingletonComponentCreatorGenerator.generate(getProcessingEnv());
            }

            if (root.isTestRoot()) {
                // Generate test related classes for each test root that uses this component.
                ImmutableList<RootMetadata> rootMetadatas =
                        roots.stream()
                                .map(test -> RootMetadata.create(test, tree, deps, aliasOfs, getProcessingEnv()))
                                .collect(toImmutableList());
                generateTestComponentData(metadataElement, rootMetadatas, componentNames);
            } else {
                generateApplication(root.element());
            }

            setProcessingState(metadata, root);
        } catch (Exception e) {
            processed.add(metadata.name());
            throw e;
        }
    }

    private void setProcessingState(ComponentTreeDepsMetadata metadata, Root root) {
        processed.add(metadata.name());
    }

    private void generateComponents(
            ComponentTreeDepsMetadata metadata, RootMetadata rootMetadata, ComponentNames componentNames)
            throws IOException {
        RootGenerator.generate(metadata, rootMetadata, componentNames, getProcessingEnv());
    }

    private void generateTestComponentData(
            TypeElement metadataElement,
            ImmutableList<RootMetadata> rootMetadatas,
            ComponentNames componentNames)
            throws IOException {
        for (RootMetadata rootMetadata : rootMetadatas) {
            // TODO(bcorso): Consider moving this check earlier into processEach.
            TypeElement testElement = rootMetadata.testRootMetadata().testElement();
            ProcessorErrors.checkState(
                    testElement.getModifiers().contains(PUBLIC),
                    testElement,
                    "Hilt tests must be public, but found: %s",
                    testElement);
            new TestComponentDataGenerator(
                    getProcessingEnv(), metadataElement, rootMetadata, componentNames)
                    .generate();
        }
    }

    private void generateApplication(TypeElement rootElement) throws IOException {
        // The generated application references the generated component so they must be generated
        // in the same build unit. Thus, we only generate the application here if we're using the
        // Hilt Gradle plugin's aggregating task. If we're using the aggregating processor, we need
        // to generate the application within AndroidEntryPointProcessor instead.
        if (!useAggregatingRootProcessor(getProcessingEnv())) {
            AndroidEntryPointMetadata metadata =
                    AndroidEntryPointMetadata.of(getProcessingEnv(), rootElement);
            new ApplicationGenerator(getProcessingEnv(), metadata).generate();
        }
    }
}
