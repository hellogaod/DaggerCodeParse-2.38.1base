package dagger.hilt.processor.internal.aggregateddeps;


import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.Processors;

/**
 * Generates the @AggregatedDeps annotated class used to pass information
 * about modules and entry points through multiple javac runs.
 */
final class AggregatedDepsGenerator {
    static final String AGGREGATING_PACKAGE = "hilt_aggregated_deps";
    private static final ClassName AGGREGATED_DEPS =
            ClassName.get("dagger.hilt.processor.internal.aggregateddeps", "AggregatedDeps");

    private final String dependencyType;
    private final TypeElement dependency;
    private final Optional<ClassName> testName;
    private final ImmutableSet<ClassName> components;
    private final ImmutableSet<ClassName> replacedDependencies;
    private final ProcessingEnvironment processingEnv;

    AggregatedDepsGenerator(
            String dependencyType,
            TypeElement dependency,
            Optional<ClassName> testName,
            ImmutableSet<ClassName> components,
            ImmutableSet<ClassName> replacedDependencies,
            ProcessingEnvironment processingEnv) {
        this.dependencyType = dependencyType;
        this.dependency = dependency;
        this.testName = testName;
        this.components = components;
        this.replacedDependencies = replacedDependencies;
        this.processingEnv = processingEnv;
    }

    //hilt_aggregated_deps包下
//    //This class should only be referenced by generated code!This class aggregates information across multiple compilations.
//    @AggregatedDeps()
//    @Generated("AggregatedDepsGenerator")
//    public class $CLASS{}
    void generate() throws IOException {
        Processors.generateAggregatingClass(
                AGGREGATING_PACKAGE, aggregatedDepsAnnotation(), dependency, getClass(), processingEnv);
    }

    private AnnotationSpec aggregatedDepsAnnotation() {
        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(AGGREGATED_DEPS);
        components.forEach(component -> annotationBuilder.addMember("components", "$S", component));
        replacedDependencies.forEach(dep -> annotationBuilder.addMember("replaces", "$S", dep));
        testName.ifPresent(test -> annotationBuilder.addMember("test", "$S", test));
        annotationBuilder.addMember(dependencyType, "$S", dependency.getQualifiedName());
        return annotationBuilder.build();
    }
}
