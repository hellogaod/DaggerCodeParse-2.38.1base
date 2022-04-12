package dagger.hilt.processor.internal.definecomponent;


import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.BaseProcessor;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;

import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING;

/**
 * A processor for {@link dagger.hilt.DefineComponent} and {@link
 * dagger.hilt.DefineComponent.Builder}.
 *
 * @DefineComponent注解和@DefineComponent.Builder注解修饰的节点处理
 */
@IncrementalAnnotationProcessor(ISOLATING)
@AutoService(Processor.class)
public final class DefineComponentProcessor extends BaseProcessor {
    private final DefineComponentMetadatas componentMetadatas = DefineComponentMetadatas.create();
    private final DefineComponentBuilderMetadatas componentBuilderMetadatas =
            DefineComponentBuilderMetadatas.create(componentMetadatas);

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(
                ClassNames.DEFINE_COMPONENT.toString(), ClassNames.DEFINE_COMPONENT_BUILDER.toString());
    }

    @Override
    protected void processEach(TypeElement annotation, Element element) throws Exception {
        if (ClassName.get(annotation).equals(ClassNames.DEFINE_COMPONENT)) {
            // TODO(bcorso): For cycles we currently process each element in the cycle. We should skip
            // processing of subsequent elements in a cycle, but this requires ensuring that the first
            // element processed is always the same so that our failure tests are stable.
            DefineComponentMetadatas.DefineComponentMetadata metadata = componentMetadatas.get(element);
            generateFile("component", metadata.component());
        } else if (ClassName.get(annotation).equals(ClassNames.DEFINE_COMPONENT_BUILDER)) {
            DefineComponentBuilderMetadatas.DefineComponentBuilderMetadata metadata = componentBuilderMetadatas.get(element);
            generateFile("builder", metadata.builder());
        } else {
            throw new AssertionError("Unhandled annotation type: " + annotation);
        }
    }

    private void generateFile(String member, TypeElement typeElement) throws IOException {
        Processors.generateAggregatingClass(
                ClassNames.DEFINE_COMPONENT_CLASSES_PACKAGE,
                AnnotationSpec.builder(ClassNames.DEFINE_COMPONENT_CLASSES)
                        .addMember(member, "$S", typeElement.getQualifiedName())
                        .build(),
                typeElement,
                getClass(),
                getProcessingEnv());
    }
}

