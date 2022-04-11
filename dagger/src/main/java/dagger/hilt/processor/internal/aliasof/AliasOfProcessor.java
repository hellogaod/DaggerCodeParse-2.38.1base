package dagger.hilt.processor.internal.aliasof;


import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;

import java.util.Set;

import javax.annotation.processing.Processor;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.BaseProcessor;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.ProcessorErrors;
import dagger.hilt.processor.internal.Processors;

import static com.google.auto.common.MoreElements.asType;
import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING;

/**
 * Processes the annotations annotated with {@link dagger.hilt.migration.AliasOf}
 *
 * @AliasOf注解修饰的注解处理
 */
@IncrementalAnnotationProcessor(ISOLATING)
@AutoService(Processor.class)
public final class AliasOfProcessor extends BaseProcessor {
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(ClassNames.ALIAS_OF.toString());
    }

    @Override
    public void processEach(TypeElement annotation, Element element) throws Exception {
        //@AliasOf必须和@Scope放在一起使用
        ProcessorErrors.checkState(
                Processors.hasAnnotation(element, ClassNames.SCOPE),
                element,
                "%s should only be used on scopes." + " However, it was found annotating %s",
                annotation,
                element);

        AnnotationMirror annotationMirror =
                Processors.getAnnotationMirror(element, ClassNames.ALIAS_OF);

        TypeElement defineComponentScope =
                Processors.getAnnotationClassValue(getElementUtils(), annotationMirror, "value");

        new AliasOfPropagatedDataGenerator(getProcessingEnv(), asType(element), defineComponentScope)
                .generate();
    }
}
