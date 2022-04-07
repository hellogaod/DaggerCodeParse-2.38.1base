package dagger.hilt.processor.internal;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;

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
}
