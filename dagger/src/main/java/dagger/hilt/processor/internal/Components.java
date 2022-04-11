package dagger.hilt.processor.internal;


import com.google.auto.common.MoreElements;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/**
 * Helper methods for defining components and the component hierarchy.
 */
public final class Components {


    public static AnnotationSpec getInstallInAnnotationSpec(ImmutableSet<ClassName> components) {
        Preconditions.checkArgument(!components.isEmpty());
        AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassNames.INSTALL_IN);
        components.forEach(component -> builder.addMember("value", "$T.class", component));
        return builder.build();
    }

    /**
     * Returns the {@link dagger.hilt.InstallIn} components for a given element.
     */
    public static ImmutableSet<ClassName> getComponents(Elements elements, Element element) {
        ImmutableSet<ClassName> components;
        if (Processors.hasAnnotation(element, ClassNames.INSTALL_IN)
                || Processors.hasAnnotation(element, ClassNames.TEST_INSTALL_IN)) {
            components = getHiltInstallInComponents(elements, element);
        } else {
            // Check the enclosing element in case it passed in module is a companion object. This helps
            // in cases where the element was arrived at by checking a binding method and moving outward.
            Element enclosing = element.getEnclosingElement();
            if (enclosing != null
                    && MoreElements.isType(enclosing)
                    && MoreElements.isType(element)
                    && Processors.hasAnnotation(enclosing, ClassNames.MODULE)
                    && KotlinMetadataUtils.getMetadataUtil().isCompanionObjectClass(
                    MoreElements.asType(element))) {
                return getComponents(elements, enclosing);
            }
            if (Processors.hasErrorTypeAnnotation(element)) {
                throw new BadInputException(
                        "Error annotation found on element " + element + ". Look above for compilation errors",
                        element);
            } else {
                throw new BadInputException(
                        String.format(
                                "An @InstallIn annotation is required for: %s.",
                                element),
                        element);
            }
        }

        return components;
    }

    private static ImmutableSet<ClassName> getHiltInstallInComponents(
            Elements elements, Element element) {
        Preconditions.checkArgument(
                Processors.hasAnnotation(element, ClassNames.INSTALL_IN)
                        || Processors.hasAnnotation(element, ClassNames.TEST_INSTALL_IN));

        //获取@InstallIn的value值，或者@TestInstallIn的components值
        ImmutableSet<TypeElement> components =
                ImmutableSet.copyOf(
                        Processors.hasAnnotation(element, ClassNames.INSTALL_IN)
                                ? Processors.getAnnotationClassValues(
                                elements,
                                Processors.getAnnotationMirror(element, ClassNames.INSTALL_IN),
                                "value")
                                : Processors.getAnnotationClassValues(
                                elements,
                                Processors.getAnnotationMirror(element, ClassNames.TEST_INSTALL_IN),
                                "components"));

        //@InstallIn的value值或者@TestInstallIn的components值类型必须使用@DefineComponent修饰
        ImmutableSet<TypeElement> undefinedComponents =
                components.stream()
                        .filter(component -> !Processors.hasAnnotation(component, ClassNames.DEFINE_COMPONENT))
                        .collect(toImmutableSet());

        ProcessorErrors.checkState(
                undefinedComponents.isEmpty(),
                element,
                "@InstallIn, can only be used with @DefineComponent-annotated classes, but found: %s",
                undefinedComponents);

        return components.stream().map(ClassName::get).collect(toImmutableSet());
    }

}
