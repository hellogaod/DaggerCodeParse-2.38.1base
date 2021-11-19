package dagger.internal.codegen.binding;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;

import static com.google.auto.common.MoreElements.asType;
import static com.google.common.base.Preconditions.checkArgument;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.internal.codegen.langmodel.DaggerElements.getAnnotationMirror;
import static dagger.internal.codegen.langmodel.DaggerElements.isAnnotationPresent;

/**
 * Enumeration of the kinds of modules.
 */
public enum ModuleKind {
    /**
     * {@code @Module}
     */
    MODULE(TypeNames.MODULE),

    /**
     * {@code @ProducerModule}
     */
    PRODUCER_MODULE(TypeNames.PRODUCER_MODULE);

    /**
     * Returns the annotations for modules of the given kinds.
     * <p>
     * ModuleKind类型转换ClassName类型
     */
    public static ImmutableSet<ClassName> annotationsFor(Set<ModuleKind> kinds) {
        return kinds.stream().map(ModuleKind::annotation).collect(toImmutableSet());
    }

    /**
     * Returns the kind of an annotated element if it is annotated with one of the module {@linkplain
     * #annotation() annotations}.
     * <p>
     * element最多只能使用一个Module注解，否则会报错。
     *
     * @throws IllegalArgumentException if the element is annotated with more than one of the module
     *                                  annotations
     */
    public static Optional<ModuleKind> forAnnotatedElement(TypeElement element) {
        Set<ModuleKind> kinds = EnumSet.noneOf(ModuleKind.class);
        for (ModuleKind kind : values()) {
            if (isAnnotationPresent(element, kind.annotation())) {
                kinds.add(kind);
            }
        }

        if (kinds.size() > 1) {
            throw new IllegalArgumentException(
                    element + " cannot be annotated with more than one of " + annotationsFor(kinds));
        }
        return kinds.stream().findAny();
    }

    //检查moduleElement是否使用了Module注解，两种情况：
    //1.是Kotlin文件，并且是Companion 对象，对该对象的父类进行判断是否使用了Module注解
    //2.否则，对该类判断是否使用了Module注解
    public static void checkIsModule(TypeElement moduleElement, KotlinMetadataUtil metadataUtil) {
        // If the type element is a Kotlin companion object, then assert it is a module if its enclosing
        // type is a module.
        if (metadataUtil.isCompanionObjectClass(moduleElement)) {
            checkArgument(forAnnotatedElement(asType(moduleElement.getEnclosingElement())).isPresent());
        } else {
            checkArgument(forAnnotatedElement(moduleElement).isPresent());
        }
    }

    private final ClassName moduleAnnotation;

    ModuleKind(ClassName moduleAnnotation) {
        this.moduleAnnotation = moduleAnnotation;
    }

    /**
     * Returns the annotation that marks a module of this kind.
     */
    public ClassName annotation() {
        return moduleAnnotation;
    }

    /**
     * Returns the annotation mirror for this module kind on the given type.
     * <p>
     * element类使用了moduleAnnotation注解，如果没有使用会报错
     *
     * @throws IllegalArgumentException if the annotation is not present on the type
     */
    public AnnotationMirror getModuleAnnotation(TypeElement element) {
        Optional<AnnotationMirror> result = getAnnotationMirror(element, moduleAnnotation);
        checkArgument(
                result.isPresent(), "annotation %s is not present on type %s", moduleAnnotation, element);
        return result.get();
    }

    /**
     * Returns the kinds of modules that a module of this kind is allowed to include.
     * <p>
     * 当前注解类型，决定该注解的#include里面可以使用哪种注解类型
     */
    public ImmutableSet<ModuleKind> legalIncludedModuleKinds() {
        switch (this) {//当前如果使用了Module注解，那么#include只允许使用Module注解；
            case MODULE:
                return Sets.immutableEnumSet(MODULE);
            case PRODUCER_MODULE://当前如果使用了ProducerModule注解，那么#include允许使用Module或ProducerModule注解；
                return Sets.immutableEnumSet(MODULE, PRODUCER_MODULE);
        }
        throw new AssertionError(this);
    }
}
