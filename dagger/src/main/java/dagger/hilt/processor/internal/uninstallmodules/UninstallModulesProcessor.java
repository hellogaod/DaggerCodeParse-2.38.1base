package dagger.hilt.processor.internal.uninstallmodules;


import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;

import java.util.Set;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.BaseProcessor;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.ProcessorErrors;
import dagger.hilt.processor.internal.Processors;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING;

/** Validates {@link dagger.hilt.android.testing.UninstallModules} usages. */
@IncrementalAnnotationProcessor(ISOLATING)
@AutoService(Processor.class)
public final class UninstallModulesProcessor extends BaseProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(ClassNames.UNINSTALL_MODULES.toString());
    }

    @Override
    public void processEach(TypeElement annotation, Element element) throws Exception {
        // TODO(bcorso): Consider using RootType to check this?
        // TODO(bcorso): Loosen this restriction to allow defining sets of ignored modules in libraries.
        ProcessorErrors.checkState(
                MoreElements.isType(element)
                        && Processors.hasAnnotation(element, ClassNames.HILT_ANDROID_TEST),
                element,
                "@%s should only be used on test classes annotated with @%s, but found: %s",
                annotation.getSimpleName(),
                ClassNames.HILT_ANDROID_TEST.simpleName(),
                element);

        TypeElement testElement = MoreElements.asType(element);
        ImmutableList<TypeElement> uninstallModules =
                Processors.getAnnotationClassValues(
                        getElementUtils(),
                        Processors.getAnnotationMirror(testElement, ClassNames.UNINSTALL_MODULES),
                        "value");

        checkModulesHaveInstallIn(testElement, uninstallModules);
        checkModulesDontOriginateFromTest(testElement, uninstallModules);

        new AggregatedUninstallModulesGenerator(testElement, uninstallModules, getProcessingEnv())
                .generate();
    }

    private void checkModulesHaveInstallIn(
            TypeElement testElement, ImmutableList<TypeElement> uninstallModules) {
        ImmutableList<TypeElement> invalidModules =
                uninstallModules.stream()
                        .filter(
                                module ->
                                        !(Processors.hasAnnotation(module, ClassNames.MODULE)
                                                && Processors.hasAnnotation(module, ClassNames.INSTALL_IN)))
                        .collect(toImmutableList());

        ProcessorErrors.checkState(
                invalidModules.isEmpty(),
                // TODO(b/152801981): Point to the annotation value rather than the annotated element.
                testElement,
                "@UninstallModules should only include modules annotated with both @Module and @InstallIn, "
                        + "but found: %s.",
                invalidModules);
    }

    private void checkModulesDontOriginateFromTest(
            TypeElement testElement, ImmutableList<TypeElement> uninstallModules) {
        ImmutableList<ClassName> invalidModules =
                uninstallModules.stream()
                        .filter(
                                module ->
                                        Processors.getOriginatingTestElement(module, getElementUtils()).isPresent())
                        .map(ClassName::get)
                        .collect(toImmutableList());

        ProcessorErrors.checkState(
                invalidModules.isEmpty(),
                // TODO(b/152801981): Point to the annotation value rather than the annotated element.
                testElement,
                "@UninstallModules should not contain test modules, but found: %s",
                invalidModules);
    }
}