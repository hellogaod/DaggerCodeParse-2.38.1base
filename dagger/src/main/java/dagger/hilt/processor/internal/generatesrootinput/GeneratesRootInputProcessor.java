package dagger.hilt.processor.internal.generatesrootinput;


import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import dagger.hilt.processor.internal.BaseProcessor;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.ProcessorErrors;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;

/**
 * Processes the annotations annotated with {@link dagger.hilt.GeneratesRootInput} which generate
 * input for components and should be processed before component creation.
 */
@IncrementalAnnotationProcessor(ISOLATING)
@AutoService(Processor.class)
public final class GeneratesRootInputProcessor extends BaseProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(ClassNames.GENERATES_ROOT_INPUT.toString());
    }

    @Override
    public void processEach(TypeElement annotation, Element element) throws Exception {
        ProcessorErrors.checkState(
                element.getKind().equals(ElementKind.ANNOTATION_TYPE),
                element,
                "%s should only annotate other annotations. However, it was found annotating %s",
                annotation,
                element);

        new GeneratesRootInputPropagatedDataGenerator(this.getProcessingEnv(), element).generate();
    }
}
