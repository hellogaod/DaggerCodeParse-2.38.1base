package dagger.spi.model;


import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.CodeBlock;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValuesWithDefaults;
import static java.util.stream.Collectors.joining;

/**
 * Utility class for qualifier transformations
 */
final class MoreAnnotationMirrors {
    /**
     * Returns a String rendering of an {@link AnnotationMirror} that includes attributes in the order
     * defined in the annotation type.
     *
     * 注解类里面的信息使用 String形式的名称 = 注解值的形式拼接展示出来
     */
    public static String toStableString(DaggerAnnotation qualifier) {
        return stableAnnotationMirrorToString(qualifier.java());
    }

    /**
     * Returns a String rendering of an {@link AnnotationMirror} that includes attributes in the order
     * defined in the annotation type. This will produce the same output for {@linkplain
     * com.google.auto.common.AnnotationMirrors#equivalence() equal} {@link AnnotationMirror}s even if
     * default values are omitted or their attributes were written in different orders, e.g.
     * {@code @A(b = "b", c = "c")} and {@code @A(c = "c", b = "b", attributeWithDefaultValue =
     * "default value")}.
     * <p>
     * 注解转换成String格式
     */
    // TODO(ronshapiro): move this to auto-common
    private static String stableAnnotationMirrorToString(AnnotationMirror qualifier) {

        StringBuilder builder = new StringBuilder("@").append(qualifier.getAnnotationType());

        //getAnnotationValuesWithDefaults():注解类中收集方法以及该方法的注解值
        ImmutableMap<ExecutableElement, AnnotationValue> elementValues =
                getAnnotationValuesWithDefaults(qualifier);

        if (!elementValues.isEmpty()) {

            ImmutableMap.Builder<String, String> namedValuesBuilder = ImmutableMap.builder();

            //将注解类中的方法对应方法值都转换成String格式
            elementValues.forEach(
                    (key, value) ->
                            namedValuesBuilder.put(
                                    key.getSimpleName().toString(), stableAnnotationValueToString(value)));
            ImmutableMap<String, String> namedValues = namedValuesBuilder.build();

            builder.append('(');
            if (namedValues.size() == 1 && namedValues.containsKey("value")) {//如果就一个value值，那么展示形式 @A(1)
                // Omit "value ="
                builder.append(namedValues.get("value"));
            } else {//如果注解收集了多个值，使用逗号分隔展示
                builder.append(Joiner.on(", ").withKeyValueSeparator("=").join(namedValues));
            }
            builder.append(')');
        }

        return builder.toString();
    }

    //对注解的值类型判断，根据不同类型返回不同的String类型，
    private static String stableAnnotationValueToString(AnnotationValue annotationValue) {

        return annotationValue.accept(
                new SimpleAnnotationValueVisitor8<String, Void>() {
                    @Override
                    protected String defaultAction(Object value, Void ignore) {
                        return value.toString();
                    }

                    @Override
                    public String visitString(String value, Void ignore) {
                        return CodeBlock.of("$S", value).toString();
                    }

                    @Override
                    public String visitAnnotation(AnnotationMirror value, Void ignore) {
                        return stableAnnotationMirrorToString(value);
                    }

                    @Override
                    public String visitArray(List<? extends AnnotationValue> value, Void ignore) {
                        return value.stream()
                                .map(MoreAnnotationMirrors::stableAnnotationValueToString)
                                .collect(joining(", ", "{", "}"));
                    }
                },
                null);
    }

    private MoreAnnotationMirrors() {
    }
}
