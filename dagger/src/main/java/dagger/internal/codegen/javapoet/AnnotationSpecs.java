package dagger.internal.codegen.javapoet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.squareup.javapoet.AnnotationSpec;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Static factories to create {@link AnnotationSpec}s.
 * <p>
 * 生成SuppressWarnings注解类
 */
public final class AnnotationSpecs {
    /**
     * Values for an {@link SuppressWarnings} annotation.
     */
    public enum Suppression {
        RAWTYPES("rawtypes"),
        UNCHECKED("unchecked"),
        FUTURE_RETURN_VALUE_IGNORED("FutureReturnValueIgnored"),
        ;

        private final String value;

        Suppression(String value) {
            this.value = value;
        }
    }

    /**
     * Creates an {@link AnnotationSpec} for {@link SuppressWarnings}.
     */
    public static AnnotationSpec suppressWarnings(Suppression first, Suppression... rest) {
        return suppressWarnings(ImmutableSet.copyOf(Lists.asList(first, rest)));
    }

    /**
     * Creates an {@link AnnotationSpec} for {@link SuppressWarnings}.
     */
    public static AnnotationSpec suppressWarnings(ImmutableSet<Suppression> suppressions) {
        checkArgument(!suppressions.isEmpty());
        AnnotationSpec.Builder builder = AnnotationSpec.builder(SuppressWarnings.class);
        suppressions.forEach(suppression -> builder.addMember("value", "$S", suppression.value));
        return builder.build();
    }

    private AnnotationSpecs() {
    }
}
