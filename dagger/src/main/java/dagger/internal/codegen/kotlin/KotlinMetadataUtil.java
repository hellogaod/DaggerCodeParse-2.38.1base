package dagger.internal.codegen.kotlin;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.lang.annotation.Annotation;
import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import dagger.internal.codegen.extension.DaggerCollectors;
import kotlin.Metadata;

import static com.google.auto.common.AnnotationMirrors.getAnnotatedAnnotations;
import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.common.base.Preconditions.checkState;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableMap;
import static kotlinx.metadata.Flag.Class.IS_COMPANION_OBJECT;
import static kotlinx.metadata.Flag.Class.IS_DATA;
import static kotlinx.metadata.Flag.Class.IS_OBJECT;
import static kotlinx.metadata.Flag.IS_PRIVATE;

import dagger.internal.codegen.langmodel.DaggerElements;
import kotlin.jvm.JvmStatic;
import kotlinx.metadata.Flag;

/**
 * Utility class for interacting with Kotlin Metadata.
 */
public final class KotlinMetadataUtil {

    private final KotlinMetadataFactory metadataFactory;

    @Inject
    KotlinMetadataUtil(KotlinMetadataFactory metadataFactory) {
        this.metadataFactory = metadataFactory;
    }

    /**
     * Returns {@code true} if this element has the Kotlin Metadata annotation or if it is enclosed in
     * an element that does.
     * <p>
     * element所在类或接口（本身是类或接口则表示本身），使用了Metadata注解,Kotlin文件反编译会出现Metadata元注解，这里的意思就是Kotlin语言
     */
    public boolean hasMetadata(Element element) {
        return isAnnotationPresent(DaggerElements.closestEnclosingTypeElement(element), Metadata.class);
    }

    /**
     * Returns the synthetic annotations of a Kotlin property.
     *
     * <p>Note that this method only looks for additional annotations in the synthetic property
     * method, if any, of a Kotlin property and not for annotations in its backing field.
     */
    public ImmutableCollection<? extends AnnotationMirror> getSyntheticPropertyAnnotations(
            VariableElement fieldElement, Class<? extends Annotation> annotationType) {
        return metadataFactory
                .create(fieldElement)
                .getSyntheticAnnotationMethod(fieldElement)
                .map(methodElement -> getAnnotatedAnnotations(methodElement, annotationType).asList())
                .orElse(ImmutableList.of());
    }

    /**
     * Returns {@code true} if the synthetic method for annotations is missing. This can occur when
     * the Kotlin metadata of the property reports that it contains a synthetic method for annotations
     * but such method is not found since it is synthetic and ignored by the processor.
     */
    public boolean isMissingSyntheticPropertyForAnnotations(VariableElement fieldElement) {
        return metadataFactory.create(fieldElement).isMissingSyntheticAnnotationMethod(fieldElement);
    }

    /**
     * Returns {@code true} if this type element is a Kotlin Object.
     */
    public boolean isObjectClass(TypeElement typeElement) {
        return hasMetadata(typeElement)
                && metadataFactory.create(typeElement).classMetadata().flags(IS_OBJECT);
    }

    /**
     * Returns {@code true} if this type element is a Kotlin data class.
     */
    public boolean isDataClass(TypeElement typeElement) {
        return hasMetadata(typeElement)
                && metadataFactory.create(typeElement).classMetadata().flags(IS_DATA);
    }

    /*
     * Returns {@code true} if this type element is a Kotlin Companion Object.
     *
     * 如果TypeElement是Kotlin文件，并且是Companion 对象（类似于java的静态变量类型）
     */
    public boolean isCompanionObjectClass(TypeElement typeElement) {

        return hasMetadata(typeElement)
                && metadataFactory.create(typeElement).classMetadata().flags(IS_COMPANION_OBJECT);
    }

    /**
     * Returns {@code true} if this type element is a Kotlin object or companion object.
     */
    public boolean isObjectOrCompanionObjectClass(TypeElement typeElement) {
        return isObjectClass(typeElement) || isCompanionObjectClass(typeElement);
    }

    /* Returns {@code true} if this type element has a Kotlin Companion Object. */
    public boolean hasEnclosedCompanionObject(TypeElement typeElement) {
        return hasMetadata(typeElement)
                && metadataFactory.create(typeElement).classMetadata().companionObjectName().isPresent();
    }

    /* Returns the Companion Object element enclosed by the given type element. */
    public TypeElement getEnclosedCompanionObject(TypeElement typeElement) {
        return metadataFactory
                .create(typeElement)
                .classMetadata()
                .companionObjectName()
                .map(
                        companionObjectName ->
                                ElementFilter.typesIn(typeElement.getEnclosedElements()).stream()
                                        .filter(
                                                innerType -> innerType.getSimpleName().contentEquals(companionObjectName))
                                        .collect(DaggerCollectors.onlyElement()))
                .get();
    }

    /**
     * Returns {@code true} if the given type element was declared <code>private</code> in its Kotlin
     * source.
     */
    public boolean isVisibilityPrivate(TypeElement typeElement) {
        return hasMetadata(typeElement)
                && metadataFactory.create(typeElement).classMetadata().flags(IS_PRIVATE);
    }

    /**
     * Returns {@code true} if the given type element was declared {@code internal} in its Kotlin
     * source.
     */
    public boolean isVisibilityInternal(TypeElement type) {
        return hasMetadata(type)
                && metadataFactory.create(type).classMetadata().flags(Flag.IS_INTERNAL);
    }

    /**
     * Returns {@code true} if the given executable element was declared {@code internal} in its
     * Kotlin source.
     */
    public boolean isVisibilityInternal(ExecutableElement method) {
        return hasMetadata(method)
                && metadataFactory.create(method).getFunctionMetadata(method).flags(Flag.IS_INTERNAL);
    }

    public Optional<ExecutableElement> getPropertyGetter(VariableElement fieldElement) {
        return metadataFactory.create(fieldElement).getPropertyGetter(fieldElement);
    }

    public boolean containsConstructorWithDefaultParam(TypeElement typeElement) {
        return hasMetadata(typeElement)
                && metadataFactory.create(typeElement).containsConstructorWithDefaultParam();
    }

    /**
     * Returns a map mapping all method signatures within the given class element, including methods
     * that it inherits from its ancestors, to their method names.
     */
    public ImmutableMap<String, String> getAllMethodNamesBySignature(TypeElement element) {
        checkState(
                hasMetadata(element), "Can not call getAllMethodNamesBySignature for non-Kotlin class");
        return metadataFactory.create(element).classMetadata().functionsBySignature().values().stream()
                .collect(toImmutableMap(KotlinMetadata.FunctionMetadata::signature, KotlinMetadata.FunctionMetadata::name));
    }

    /**
     * Returns {@code true} if the <code>@JvmStatic</code> annotation is present in the given element.
     */
    public static boolean isJvmStaticPresent(ExecutableElement element) {
        return isAnnotationPresent(element, JvmStatic.class);
    }
}
