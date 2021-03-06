package dagger.hilt.processor.internal.uninstallmodules;


import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.AnnotationSpec;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;

/**
 * Generates an {@link dagger.hilt.android.internal.uninstallmodules.AggregatedUninstallModules}
 * annotation.
 */
final class AggregatedUninstallModulesGenerator {

    private final ProcessingEnvironment env;
    private final TypeElement testElement;
    private final ImmutableList<TypeElement> uninstallModuleElements;

    AggregatedUninstallModulesGenerator(
            TypeElement testElement,
            ImmutableList<TypeElement> uninstallModuleElements,
            ProcessingEnvironment env) {
        this.testElement = testElement;
        this.uninstallModuleElements = uninstallModuleElements;
        this.env = env;
    }

    //生成的类在dagger.hilt.android.internal.uninstallmodules.codegen包下
//    //This class should only be referenced by generated code!This class aggregates information across multiple compilations.
//    @AggregatedUninstallModules(test = $Class名,uninstallModules = @UninstallModules#value中的节点名称)
//    @Generated("AggregatedUninstallModulesGenerator")
//    public class 包"_"拼接$Class{}
    void generate() throws IOException {
        Processors.generateAggregatingClass(
                ClassNames.AGGREGATED_UNINSTALL_MODULES_PACKAGE,
                aggregatedUninstallModulesAnnotation(),
                testElement,
                getClass(),
                env);
    }

    private AnnotationSpec aggregatedUninstallModulesAnnotation() {
        AnnotationSpec.Builder builder =
                AnnotationSpec.builder(ClassNames.AGGREGATED_UNINSTALL_MODULES);
        builder.addMember("test", "$S", testElement.getQualifiedName());
        uninstallModuleElements.stream()
                .map(TypeElement::getQualifiedName)
                .forEach(uninstallModule -> builder.addMember("uninstallModules", "$S", uninstallModule));
        return builder.build();
    }
}
