package dagger.hilt.processor.internal;

import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.Optional;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Utility class for aggregating metadata.
 */
public final class AggregatedElements {

    /**
     * Returns the class name of the proxy or {@link Optional#empty()} if a proxy is not needed.
     */
    public static Optional<ClassName> aggregatedElementProxyName(TypeElement aggregatedElement) {
        if (aggregatedElement.getModifiers().contains(PUBLIC)) {
            // Public aggregated elements do not have proxies.
            return Optional.empty();
        }
        ClassName name = ClassName.get(aggregatedElement);
        // To avoid going over the class name size limit, just prepend a single character.
        return Optional.of(name.peerClass("_" + name.simpleName()));
    }

    /**
     * Returns back the set of input {@code aggregatedElements} with all proxies unwrapped.
     */
    public static ImmutableSet<TypeElement> unwrapProxies(
            ImmutableSet<TypeElement> aggregatedElements, Elements elements) {
        return aggregatedElements.stream()
                .map(aggregatedElement -> unwrapProxy(aggregatedElement, elements))
                .collect(toImmutableSet());
    }

    private static TypeElement unwrapProxy(TypeElement element, Elements elements) {
        return Processors.hasAnnotation(element, ClassNames.AGGREGATED_ELEMENT_PROXY)
                ? Processors.getAnnotationClassValue(
                elements,
                Processors.getAnnotationMirror(element, ClassNames.AGGREGATED_ELEMENT_PROXY),
                "value")
                : element;
    }

    /**
     * Returns all aggregated elements in the aggregating package after validating them.
     */
    public static ImmutableSet<TypeElement> from(
            String aggregatingPackage, ClassName aggregatingAnnotation, Elements elements) {
        PackageElement packageElement = elements.getPackageElement(aggregatingPackage);

        if (packageElement == null) {
            return ImmutableSet.of();
        }

        // packageElement包如果存在，那么该包下没有使用@AggregatedElementProxy修饰的节点必须存在，并且该包下所有节点不需使用aggregatingAnnotation注解修饰；
        ImmutableSet<TypeElement> aggregatedElements =
                packageElement.getEnclosedElements().stream()
                        .map(MoreElements::asType)
                        // We're only interested in returning the original deps here. Proxies will be generated
                        // (if needed) and swapped just before generating @ComponentTreeDeps.
                        .filter(
                                element -> !Processors.hasAnnotation(element, ClassNames.AGGREGATED_ELEMENT_PROXY))
                        .collect(toImmutableSet());

        ProcessorErrors.checkState(
                !aggregatedElements.isEmpty(),
                packageElement,
                "No dependencies found. Did you remove code in package %s?",
                packageElement);

        for (TypeElement aggregatedElement : aggregatedElements) {
            ProcessorErrors.checkState(
                    Processors.hasAnnotation(aggregatedElement, aggregatingAnnotation),
                    aggregatedElement,
                    "Expected element, %s, to be annotated with @%s, but only found: %s.",
                    aggregatedElement.getSimpleName(),
                    aggregatingAnnotation,
                    aggregatedElement.getAnnotationMirrors());
        }

        return aggregatedElements;
    }

    private AggregatedElements() {
    }
}
