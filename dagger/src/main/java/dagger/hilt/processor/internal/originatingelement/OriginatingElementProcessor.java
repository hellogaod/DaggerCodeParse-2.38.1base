
package dagger.hilt.processor.internal.originatingelement;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import dagger.hilt.processor.internal.BaseProcessor;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.ProcessorErrors;
import dagger.hilt.processor.internal.Processors;

import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING;

/**
 * Processes the annotations annotated with {@link dagger.hilt.codegen.OriginatingElement} to check
 * that they're only used on top-level classes and the value passed is also a top-level class.
 */
@IncrementalAnnotationProcessor(ISOLATING)
@AutoService(Processor.class)
public final class OriginatingElementProcessor extends BaseProcessor {

  @Override
  public ImmutableSet<String> getSupportedAnnotationTypes() {
    return ImmutableSet.of(ClassNames.ORIGINATING_ELEMENT.toString());
  }

  @Override
  public void processEach(TypeElement annotation, Element element) throws Exception {
    //1. @OriginatingElement修饰的节点必须是顶级类或接口，顶级-表示再上一级就是包了；
    ProcessorErrors.checkState(
        MoreElements.isType(element) && Processors.isTopLevel(element),
        element,
        "@%s should only be used to annotate top-level types, but found: %s",
        annotation.getSimpleName(),
        element);

    TypeElement originatingElementValue =
        Processors.getAnnotationClassValue(
            getElementUtils(),
            Processors.getAnnotationMirror(element, ClassNames.ORIGINATING_ELEMENT),
            "topLevelClass");

    //2. OriginatingElement#topLevelClass中的节点也必须是顶级类或接口。
    // TODO(bcorso): ProcessorErrors should allow us to point to the annotation too.
    ProcessorErrors.checkState(
        Processors.isTopLevel(originatingElementValue),
        element,
        "@%s.topLevelClass value should be a top-level class, but found: %s",
        annotation.getSimpleName(),
        originatingElementValue);
  }
}
