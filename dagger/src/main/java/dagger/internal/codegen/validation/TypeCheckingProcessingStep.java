package dagger.internal.codegen.validation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.squareup.javapoet.ClassName;

import java.util.Map;
import java.util.Set;

import androidx.room.compiler.processing.XElement;
import androidx.room.compiler.processing.XProcessingEnv;
import androidx.room.compiler.processing.XProcessingStep;
import dagger.internal.codegen.extension.DaggerStreams;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.difference;

/**
 * A {@link XProcessingStep} that processes one element at a time and defers any for which {@link
 * TypeNotPresentException} is thrown.
 */
public abstract class TypeCheckingProcessingStep<E extends XElement> implements XProcessingStep {

    @Override
    public final ImmutableSet<String> annotations() {
        return annotationClassNames().stream().map(ClassName::canonicalName).collect(DaggerStreams.toImmutableSet());
    }

    @SuppressWarnings("unchecked") // Subclass must ensure all annotated targets are of valid type.
    @Override
    public ImmutableSet<XElement> process(
            XProcessingEnv env, Map<String, ? extends Set<? extends XElement>> elementsByAnnotation) {
        ImmutableSet.Builder<XElement> deferredElements = ImmutableSet.builder();

        //elementsByAnnotation是map类型，Key：表示注解String名称，Value：表示被Key中注解修饰的元素集合
        inverse(elementsByAnnotation)
                .forEach(
                        (element, annotations) -> {
                            try {
                                //核心点：处理当前节点和当前节点使用的所有注解
                                process((E) element, annotations);
                            } catch (TypeNotPresentException e) {
                                deferredElements.add(element);
                            }
                        });
        return deferredElements.build();
    }

    /**
     * Processes one element. If this method throws {@link TypeNotPresentException}, the element will
     * be deferred until the next round of processing.
     *
     * @param annotations the subset of {@link XProcessingStep#annotations()} that annotate {@code
     *                    element}
     */
    protected abstract void process(E xElement, ImmutableSet<ClassName> annotations);

    //返回类型——K：存放使用注解的节点，V：Set集合，当前节点使用的注解ClassName类型集合
    private ImmutableMap<XElement, ImmutableSet<ClassName>> inverse(
            Map<String, ? extends Set<? extends XElement>> elementsByAnnotation) {

        //将注解Set<ClassName>集合转换成Map<K = 注解String名称,V = ClassName>集合
        ImmutableMap<String, ClassName> annotationClassNames =
                annotationClassNames().stream()
                        .collect(DaggerStreams.toImmutableMap(ClassName::canonicalName, className -> className));

        //检查elementsByAnnotation的K=注解String名称 必须包含在annotationClassNames的K = 注解String名称 当中
        checkState(
                annotationClassNames.keySet().containsAll(elementsByAnnotation.keySet()),
                "Unexpected annotations for %s: %s",
                this.getClass().getName(),
                difference(elementsByAnnotation.keySet(), annotationClassNames.keySet())
        );

        //在elementsByAnnotation（Map类型，K存放注解String名称，V是Set类型：存放当前K注解修饰的节点）
        //转换成Map集合，K：存放使用注解的节点，V：当前节点使用的注解ClassName类型
        ImmutableSetMultimap.Builder<XElement, ClassName> builder = ImmutableSetMultimap.builder();
        elementsByAnnotation.forEach(
                (annotationName, elementSet) ->
                        elementSet.forEach(
                                element -> builder.put(element, annotationClassNames.get(annotationName))));

        return ImmutableMap.copyOf(Maps.transformValues(builder.build().asMap(), ImmutableSet::copyOf));
    }


    /**
     * Returns the set of annotations processed by this processing step.
     */
    protected abstract Set<ClassName> annotationClassNames();//需要解析的注解
}
