package dagger.internal.codegen.base;


import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;
import static com.google.auto.common.AnnotationMirrors.getAnnotationValuesWithDefaults;

/**
 * Utility methods for working with {@link AnnotationValue} instances.
 */
public final class MoreAnnotationValues {

    /**
     * Returns the list of values represented by an array annotation value.
     *
     * @throws IllegalArgumentException unless {@code annotationValue} represents an array
     */
    public static ImmutableList<AnnotationValue> asAnnotationValues(AnnotationValue annotationValue) {
        return annotationValue.accept(AS_ANNOTATION_VALUES, null);
    }

    private static final AnnotationValueVisitor<ImmutableList<AnnotationValue>, String>
            AS_ANNOTATION_VALUES =
            new SimpleAnnotationValueVisitor8<ImmutableList<AnnotationValue>, String>() {
                @Override
                public ImmutableList<AnnotationValue> visitArray(
                        List<? extends AnnotationValue> vals, String elementName) {
                    return ImmutableList.copyOf(vals);
                }

                @Override
                protected ImmutableList<AnnotationValue> defaultAction(Object o, String elementName) {
                    throw new IllegalArgumentException(elementName + " is not an array: " + o);
                }
            };

    /**
     * Returns the type represented by an annotation value.
     *
     * @throws IllegalArgumentException unless {@code annotationValue} represents a single type
     */
    public static TypeMirror asType(AnnotationValue annotationValue) {
        return AS_TYPE.visit(annotationValue);
    }

    private static final AnnotationValueVisitor<TypeMirror, Void> AS_TYPE =
            new SimpleAnnotationValueVisitor8<TypeMirror, Void>() {
                @Override
                public TypeMirror visitType(TypeMirror t, Void p) {
                    return t;
                }

                @Override
                protected TypeMirror defaultAction(Object o, Void p) {
                    throw new TypeNotPresentException(o.toString(), null);
                }
            };

    /**
     * Returns the int value of an annotation
     */
    public static int getIntValue(AnnotationMirror annotation, String valueName) {//注解值是int类型
        return (int) getAnnotationValue(annotation, valueName).getValue();
    }

    /**
     * Returns an optional int value of an annotation if the value name is present
     */
    public static Optional<Integer> getOptionalIntValue(
            AnnotationMirror annotation, String valueName) {
        //注解值是可选int类型，不存在返回empty；存在返回可选int类型
        return isValuePresent(annotation, valueName)
                ? Optional.of(getIntValue(annotation, valueName))
                : Optional.empty();
    }

    /**
     * Returns the String value of an annotation
     */
    public static String getStringValue(AnnotationMirror annotation, String valueName) {
        //注解值是String类型
        return (String) getAnnotationValue(annotation, valueName).getValue();
    }

    /**
     * Returns an optional String value of an annotation if the value name is present
     */
    public static Optional<String> getOptionalStringValue(
            AnnotationMirror annotation, String valueName) {
        //注解值是String可选类型，不存在则返回empty，存在返回可选String
        return isValuePresent(annotation, valueName)
                ? Optional.of(getStringValue(annotation, valueName))
                : Optional.empty();
    }

    /**
     * Returns the int array value of an annotation
     */
    public static int[] getIntArrayValue(AnnotationMirror annotation, String valueName) {//注解值是int数组类型
        return asAnnotationValues(getAnnotationValue(annotation, valueName)).stream()
                .mapToInt(it -> (int) it.getValue())
                .toArray();
    }


    /**
     * Returns the String array value of an annotation
     */
    public static String[] getStringArrayValue(AnnotationMirror annotation, String valueName) {//注解值是String数组类型
        return asAnnotationValues(getAnnotationValue(annotation, valueName)).stream()
                .map(it -> (String) it.getValue())
                .toArray(String[]::new);
    }

    //annotation注解是否有valuename值，存在返回true；否则返回false
    private static boolean isValuePresent(AnnotationMirror annotation, String valueName) {
        return getAnnotationValuesWithDefaults(annotation).keySet().stream()
                .anyMatch(member -> member.getSimpleName().contentEquals(valueName));
    }

    private MoreAnnotationValues() {
    }
}
