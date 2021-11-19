package dagger.internal.codegen.base;


import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.javapoet.TypeNames;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;
import static com.google.auto.common.MoreTypes.asTypeElement;
import static com.google.common.base.Preconditions.checkArgument;
import static dagger.internal.codegen.base.MoreAnnotationValues.asAnnotationValues;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.langmodel.DaggerElements.getAnyAnnotation;

/**
 * A {@code @Module} or {@code @ProducerModule} annotation.
 * <p>
 * Module或ProducerModule生成当前ModuleAnnotation类,包括了 includes里面的值，subcomponents里的值
 */
@AutoValue
public abstract class ModuleAnnotation {
    private static final ImmutableSet<ClassName> MODULE_ANNOTATIONS =
            ImmutableSet.of(TypeNames.MODULE, TypeNames.PRODUCER_MODULE);

    /**
     * The annotation itself.
     */
    // This does not use AnnotationMirrors.equivalence() because we want the actual annotation
    // instance.
    public abstract AnnotationMirror annotation();

    /**
     * The simple name of the annotation.
     */
    public String annotationName() {
        return annotation().getAnnotationType().asElement().getSimpleName().toString();
    }

    /**
     * The types specified in the {@code includes} attribute.
     * <p>
     * 将Module注解includes方法里面的值转换成对应的类
     *
     * @throws IllegalArgumentException if any of the values are error types
     */
    @Memoized
    public ImmutableList<TypeElement> includes() {
        return includesAsAnnotationValues().stream()
                .map(MoreAnnotationValues::asType)
                .map(MoreTypes::asTypeElement)
                .collect(toImmutableList());
    }

    /**
     * The values specified in the {@code includes} attribute.
     * <p>
     * 获取Module注解includes方法里面的值
     */
    @Memoized
    public ImmutableList<AnnotationValue> includesAsAnnotationValues() {
        return asAnnotationValues(getAnnotationValue(annotation(), "includes"));
    }

    /**
     * The types specified in the {@code subcomponents} attribute.
     * <p>
     * 把Module里面subcomponent方法中的值转换成集合类
     *
     * @throws IllegalArgumentException if any of the values are error types
     */
    @Memoized
    public ImmutableList<TypeElement> subcomponents() {
        return subcomponentsAsAnnotationValues().stream()
                .map(MoreAnnotationValues::asType)
                .map(MoreTypes::asTypeElement)
                .collect(toImmutableList());
    }

    /**
     * The values specified in the {@code subcomponents} attribute.
     * <p>
     * 获取当前注解，subcomponents里面的值
     */
    @Memoized
    public ImmutableList<AnnotationValue> subcomponentsAsAnnotationValues() {
        return asAnnotationValues(getAnnotationValue(annotation(), "subcomponents"));
    }

    /**
     * Returns {@code true} if the argument is a {@code @Module} or {@code @ProducerModule}.
     * <p>
     * 判断是否Module或ProducerModule
     */
    public static boolean isModuleAnnotation(AnnotationMirror annotation) {
        return MODULE_ANNOTATIONS.stream()
                .map(ClassName::canonicalName)
                .anyMatch(asTypeElement(annotation.getAnnotationType()).getQualifiedName()::contentEquals);
    }

    /**
     * The module annotation types.
     */
    public static ImmutableSet<ClassName> moduleAnnotations() {
        return MODULE_ANNOTATIONS;
    }

    /**
     * Creates an object that represents a {@code @Module} or {@code @ProducerModule}.
     *
     * @throws IllegalArgumentException if {@link #isModuleAnnotation(AnnotationMirror)} returns
     *                                  {@code false}
     */
    public static ModuleAnnotation moduleAnnotation(AnnotationMirror annotation) {
        checkArgument(
                isModuleAnnotation(annotation),
                "%s is not a Module or ProducerModule annotation",
                annotation);
        return new AutoValue_ModuleAnnotation(annotation);
    }

    /**
     * Returns an object representing the {@code @Module} or {@code @ProducerModule} annotation if one
     * annotates {@code typeElement}.
     * <p>
     * 对使用Module或ProducerModule的typeElement类生成当前的ModuleAnnotation对象
     */
    public static Optional<ModuleAnnotation> moduleAnnotation(TypeElement typeElement) {
        return getAnyAnnotation(typeElement, TypeNames.MODULE, TypeNames.PRODUCER_MODULE)
                .map(ModuleAnnotation::moduleAnnotation);
    }
}
