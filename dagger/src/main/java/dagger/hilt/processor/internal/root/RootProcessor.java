package dagger.hilt.processor.internal.root;


import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;

import java.util.Arrays;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.BadInputException;
import dagger.hilt.processor.internal.BaseProcessor;
import dagger.hilt.processor.internal.aggregateddeps.AggregatedDepsMetadata;
import dagger.hilt.processor.internal.aliasof.AliasOfPropagatedDataMetadata;
import dagger.hilt.processor.internal.definecomponent.DefineComponentClassesMetadata;
import dagger.hilt.processor.internal.earlyentrypoint.AggregatedEarlyEntryPointMetadata;
import dagger.hilt.processor.internal.generatesrootinput.GeneratesRootInputs;
import dagger.hilt.processor.internal.root.ir.AggregatedDepsIr;
import dagger.hilt.processor.internal.root.ir.AggregatedEarlyEntryPointIr;
import dagger.hilt.processor.internal.root.ir.AggregatedRootIr;
import dagger.hilt.processor.internal.root.ir.AggregatedRootIrValidator;
import dagger.hilt.processor.internal.root.ir.AggregatedUninstallModulesIr;
import dagger.hilt.processor.internal.root.ir.AliasOfPropagatedDataIr;
import dagger.hilt.processor.internal.root.ir.ComponentTreeDepsIr;
import dagger.hilt.processor.internal.root.ir.ComponentTreeDepsIrCreator;
import dagger.hilt.processor.internal.root.ir.DefineComponentClassesIr;
import dagger.hilt.processor.internal.root.ir.InvalidRootsException;
import dagger.hilt.processor.internal.root.ir.ProcessedRootSentinelIr;
import dagger.hilt.processor.internal.uninstallmodules.AggregatedUninstallModulesMetadata;

import static com.google.common.base.Preconditions.checkState;
import static dagger.hilt.processor.internal.HiltCompilerOptions.isCrossCompilationRootValidationDisabled;
import static dagger.hilt.processor.internal.HiltCompilerOptions.isSharedTestComponentsEnabled;
import static dagger.hilt.processor.internal.HiltCompilerOptions.useAggregatingRootProcessor;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.AGGREGATING;
import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.DYNAMIC;
import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING;

/**
 * Processor that outputs dagger components based on transitive build deps.
 * <p>
 * 集中处理@HiltAndroidApp、@HiltAndroidTest和@InternalTestRoot三种注解
 */
@IncrementalAnnotationProcessor(DYNAMIC)
@AutoService(Processor.class)
public final class RootProcessor extends BaseProcessor {

    private boolean processed;
    private GeneratesRootInputs generatesRootInputs;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        generatesRootInputs = new GeneratesRootInputs(processingEnvironment);
    }

    @Override
    public ImmutableSet<String> additionalProcessingOptions() {
        return useAggregatingRootProcessor(getProcessingEnv())
                ? ImmutableSet.of(AGGREGATING.getProcessorOption())
                : ImmutableSet.of(ISOLATING.getProcessorOption());
    }

    @Override
    public ImmutableSet<String> getSupportedAnnotationTypes() {
        return ImmutableSet.<String>builder()
                .addAll(
                        Arrays.stream(RootType.values())
                                .map(rootType -> rootType.className().toString())
                                .collect(toImmutableSet()))
                .build();
    }

    @Override
    public void processEach(TypeElement annotation, Element element) throws Exception {
        TypeElement rootElement = MoreElements.asType(element);
        // TODO(bcorso): Move this logic into a separate isolating processor to avoid regenerating it
        // for unrelated changes in Gradle.
        RootType rootType = RootType.of(rootElement);

        if (rootType.isTestRoot()) {
            new TestInjectorGenerator(
                    getProcessingEnv(), TestRootMetadata.of(getProcessingEnv(), rootElement))
                    .generate();
        }

        TypeElement originatingRootElement =
                Root.create(rootElement, getProcessingEnv()).originatingRootElement();

        new AggregatedRootGenerator(rootElement, originatingRootElement, annotation, getProcessingEnv())
                .generate();
    }

    @Override
    public void postRoundProcess(RoundEnvironment roundEnv) throws Exception {
        if (!useAggregatingRootProcessor(getProcessingEnv())) {
            return;
        }

        Set<Element> newElements = generatesRootInputs.getElementsToWaitFor(roundEnv);
        if (processed) {
            checkState(
                    newElements.isEmpty(),
                    "Found extra modules after compilation: %s\n"
                            + "(If you are adding an annotation processor that generates root input for hilt, "
                            + "the annotation must be annotated with @dagger.hilt.GeneratesRootInput.\n)",
                    newElements);
        } else if (newElements.isEmpty()) {
            processed = true;

            ImmutableSet<AggregatedRootIr> rootsToProcess = rootsToProcess();
            if (rootsToProcess.isEmpty()) {
                return;
            }

            // Generate an @ComponentTreeDeps for each unique component tree.
            ComponentTreeDepsGenerator componentTreeDepsGenerator =
                    new ComponentTreeDepsGenerator(getProcessingEnv());

            for (ComponentTreeDepsMetadata metadata : componentTreeDepsMetadatas(rootsToProcess)) {
                componentTreeDepsGenerator.generate(metadata);
            }

            // Generate a sentinel for all processed roots.
            for (AggregatedRootIr ir : rootsToProcess) {
                TypeElement rootElement = getElementUtils().getTypeElement(ir.getRoot().canonicalName());
                new ProcessedRootSentinelGenerator(rootElement, getProcessingEnv()).generate();
            }
        }
    }

    //@AggregatedRoot#root的值筛选出不存在与@ProcessedRootSentinel#roots
    private ImmutableSet<AggregatedRootIr> rootsToProcess() {

        //fqName:dagger.hilt.internal.processedrootsentinel.codegen包下使用@ProcessedRootSentinel注解修饰的节点
        //roots:@ProcessedRootSentinel注解的roots值
        ImmutableSet<ProcessedRootSentinelIr> processedRoots =
                ProcessedRootSentinelMetadata.from(getElementUtils()).stream()
                        .map(ProcessedRootSentinelMetadata::toIr)
                        .collect(toImmutableSet());

        //dagger.hilt.internal.aggregatedroot.codegen包下使用@AggregatedRoot修饰的节点,
        // 以及@AggregatedRoot#root中的节点、@AggregatedRoot#originatingRoot中的节点、@AggregatedRoot#rootAnnotation中的节点，
        ImmutableSet<AggregatedRootIr> aggregatedRoots =
                AggregatedRootMetadata.from(processingEnv).stream()
                        .map(AggregatedRootMetadata::toIr)
                        .collect(toImmutableSet());

        //false
        boolean isCrossCompilationRootValidationDisabled =
                isCrossCompilationRootValidationDisabled(
                        aggregatedRoots.stream()
                                .map(ir -> getElementUtils().getTypeElement(ir.getRoot().canonicalName()))
                                .collect(toImmutableSet()),
                        processingEnv);
        try {
            return ImmutableSet.copyOf(
                    AggregatedRootIrValidator.rootsToProcess(
                            isCrossCompilationRootValidationDisabled, processedRoots, aggregatedRoots));
        } catch (InvalidRootsException ex) {
            throw new BadInputException(ex.getMessage());
        }
    }

    private ImmutableSet<ComponentTreeDepsMetadata> componentTreeDepsMetadatas(
            ImmutableSet<AggregatedRootIr> aggregatedRoots) {

        ImmutableSet<DefineComponentClassesIr> defineComponentDeps =
                DefineComponentClassesMetadata.from(getElementUtils()).stream()
                        .map(DefineComponentClassesMetadata::toIr)
                        .collect(toImmutableSet());

        ImmutableSet<AliasOfPropagatedDataIr> aliasOfDeps =
                AliasOfPropagatedDataMetadata.from(getElementUtils()).stream()
                        .map(AliasOfPropagatedDataMetadata::toIr)
                        .collect(toImmutableSet());

        ImmutableSet<AggregatedDepsIr> aggregatedDeps =
                AggregatedDepsMetadata.from(getElementUtils()).stream()
                        .map(AggregatedDepsMetadata::toIr)
                        .collect(toImmutableSet());

        ImmutableSet<AggregatedUninstallModulesIr> aggregatedUninstallModulesDeps =
                AggregatedUninstallModulesMetadata.from(getElementUtils()).stream()
                        .map(AggregatedUninstallModulesMetadata::toIr)
                        .collect(toImmutableSet());

        ImmutableSet<AggregatedEarlyEntryPointIr> aggregatedEarlyEntryPointDeps =
                AggregatedEarlyEntryPointMetadata.from(getElementUtils()).stream()
                        .map(AggregatedEarlyEntryPointMetadata::toIr)
                        .collect(toImmutableSet());

        // We should be guaranteed that there are no mixed roots, so check if this is prod or test.
        boolean isTest = aggregatedRoots.stream().anyMatch(AggregatedRootIr::isTestRoot);
        Set<ComponentTreeDepsIr> componentTreeDeps =
                ComponentTreeDepsIrCreator.components(
                        //@AggregatedRoot#rootAnnotation中的注解如果是@HiltAndroidTest或@InternalTestRoot
                        isTest,
                        //false
                        isSharedTestComponentsEnabled(processingEnv),
                        //dagger.hilt.internal.aggregatedroot.codegen包下使用@AggregatedRoot（@AggregatedRoot#root的值筛选出不存在与@ProcessedRootSentinel#roots）修饰的节点生成的AggregatedRootIr对象
                        aggregatedRoots,
                        //dagger.hilt.processor.internal.definecomponent.codegen包下使用@DefineComponentClasses注解的节点生成对象
                        defineComponentDeps,
                        //dagger.hilt.processor.internal.aliasof.codegen包下使用@AliasOfPropagatedData修饰的节点生成的对象
                        aliasOfDeps,
                        //hilt_aggregated_deps包下使用@AggregatedDeps修饰的节点生成的对象
                        aggregatedDeps,
                        //dagger.hilt.android.internal.uninstallmodules.codegen包下使用@AggregatedUninstallModules修饰的节点生成对象
                        aggregatedUninstallModulesDeps,
                        //dagger.hilt.android.internal.earlyentrypoint.codegen包下使用@AggregatedEarlyEntryPoint修饰的节点生成对象
                        aggregatedEarlyEntryPointDeps);
        return componentTreeDeps.stream()
                .map(it -> ComponentTreeDepsMetadata.from(it, getElementUtils()))
                .collect(toImmutableSet());
    }
}
