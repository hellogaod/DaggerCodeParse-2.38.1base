package dagger.internal.codegen.base;


import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.Collection;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.javapoet.TypeNames;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;
import static com.google.auto.common.MoreElements.asType;
import static com.google.auto.common.MoreTypes.asTypeElements;
import static dagger.internal.codegen.base.MoreAnnotationValues.asAnnotationValues;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.javapoet.TypeNames.PRODUCER_MODULE;
import static dagger.internal.codegen.langmodel.DaggerElements.getAnyAnnotation;
import static dagger.internal.codegen.langmodel.DaggerTypes.isTypeOf;

/**
 * A {@code @Component}, {@code @Subcomponent}, {@code @ProductionComponent}, or
 * {@code @ProductionSubcomponent} annotation, or a {@code @Module} or {@code @ProducerModule}
 * annotation that is being treated as a component annotation when validating full binding graphs
 * for modules.
 * <p>
 * Component注解信息,重点是两个继承ComponentAnnotation的子类，FictionalComponentAnnotation和RealComponentAnnotation
 */
public abstract class ComponentAnnotation {

    /**
     * The root component annotation types.
     */
    private static final ImmutableSet<ClassName> ROOT_COMPONENT_ANNOTATIONS =
            ImmutableSet.of(TypeNames.COMPONENT, TypeNames.PRODUCTION_COMPONENT);

    /**
     * The subcomponent annotation types.
     */
    private static final ImmutableSet<ClassName> SUBCOMPONENT_ANNOTATIONS =
            ImmutableSet.of(TypeNames.SUBCOMPONENT, TypeNames.PRODUCTION_SUBCOMPONENT);

    // TODO(erichang): Move ComponentCreatorAnnotation into /base and use that here?
    /**
     * The component/subcomponent creator annotation types.
     */
    private static final ImmutableSet<ClassName> CREATOR_ANNOTATIONS =
            ImmutableSet.of(
                    TypeNames.COMPONENT_BUILDER,
                    TypeNames.COMPONENT_FACTORY,
                    TypeNames.PRODUCTION_COMPONENT_BUILDER,
                    TypeNames.PRODUCTION_COMPONENT_FACTORY,
                    TypeNames.SUBCOMPONENT_BUILDER,
                    TypeNames.SUBCOMPONENT_FACTORY,
                    TypeNames.PRODUCTION_SUBCOMPONENT_BUILDER,
                    TypeNames.PRODUCTION_SUBCOMPONENT_FACTORY);

    /**
     * All component annotation types.
     */
    private static final ImmutableSet<ClassName> ALL_COMPONENT_ANNOTATIONS =
            ImmutableSet.<ClassName>builder()
                    .addAll(ROOT_COMPONENT_ANNOTATIONS)
                    .addAll(SUBCOMPONENT_ANNOTATIONS)
                    .build();

    /**
     * All component and creator annotation types.
     */
    private static final ImmutableSet<ClassName> ALL_COMPONENT_AND_CREATOR_ANNOTATIONS =
            ImmutableSet.<ClassName>builder()
                    .addAll(ALL_COMPONENT_ANNOTATIONS)
                    .addAll(CREATOR_ANNOTATIONS)
                    .build();


    /**
     * The annotation itself.
     */
    public abstract AnnotationMirror annotation();

    /**
     * The simple name of the annotation type.
     */
    public String simpleName() {
        return MoreAnnotationMirrors.simpleName(annotation()).toString();
    }

    /**
     * Returns {@code true} if the annotation is a {@code @Subcomponent} or
     * {@code @ProductionSubcomponent}.
     */
    public abstract boolean isSubcomponent();

    /**
     * Returns {@code true} if the annotation is a {@code @ProductionComponent},
     * {@code @ProductionSubcomponent}, or {@code @ProducerModule}.
     */
    public abstract boolean isProduction();

    /**
     * Returns {@code true} if the annotation is a real component annotation and not a module
     * annotation.
     */
    public abstract boolean isRealComponent();


    /**
     * The values listed as {@code dependencies}.
     * <p>
     * Component#dependencies里面的注解值
     */
    public abstract ImmutableList<AnnotationValue> dependencyValues();

    /**
     * The types listed as {@code dependencies}.
     * <p>
     * Component#dependencies里面的注解值以TypeMirror形式展示
     */
    public ImmutableList<TypeMirror> dependencyTypes() {
        return dependencyValues().stream().map(MoreAnnotationValues::asType).collect(toImmutableList());
    }

    /**
     * The types listed as {@code dependencies}.
     *
     * @throws IllegalArgumentException if any of {@link #dependencyTypes()} are error types
     */
    public ImmutableList<TypeElement> dependencies() {
        return asTypeElements(dependencyTypes()).asList();
    }

    /**
     * The values listed as {@code modules}.
     */
    public abstract ImmutableList<AnnotationValue> moduleValues();

    /**
     * The types listed as {@code modules}.
     */
    public ImmutableList<TypeMirror> moduleTypes() {
        return moduleValues().stream().map(MoreAnnotationValues::asType).collect(toImmutableList());
    }

    /**
     * The types listed as {@code modules}.
     *
     * @throws IllegalArgumentException if any of {@link #moduleTypes()} are error types
     */
    public ImmutableSet<TypeElement> modules() {
        return asTypeElements(moduleTypes());
    }

    //获取当前annotation()注解中，参数是parameterName的注解值
    protected final ImmutableList<AnnotationValue> getAnnotationValues(String parameterName) {
        return asAnnotationValues(getAnnotationValue(annotation(), parameterName));
    }

    /**
     * Returns an object representing a root component annotation, not a subcomponent annotation, if
     * one is present on {@code typeElement}.
     * <p>
     * TypeElement使用的(Producetion)Component注解生成ComponentAnnotation对象
     */
    public static Optional<ComponentAnnotation> rootComponentAnnotation(TypeElement typeElement) {
        return anyComponentAnnotation(typeElement, ROOT_COMPONENT_ANNOTATIONS);
    }

    /**
     * Returns an object representing a subcomponent annotation, if one is present on {@code
     * typeElement}.
     * <p>
     * 对 typeElement类如果使用了(Production)Subcomponent注解，对该注解生成ComponentAnnotation
     */
    public static Optional<ComponentAnnotation> subcomponentAnnotation(TypeElement typeElement) {
        return anyComponentAnnotation(typeElement, SUBCOMPONENT_ANNOTATIONS);
    }

    /**
     * Returns an object representing a root component or subcomponent annotation, if one is present
     * on {@code typeElement}.
     * <p>
     * 对 TypeElement上如果使用了(Producer)Component 或 (Producer)Subcomponent注解，对该注解生成ComponentAnnotation对象
     */
    public static Optional<ComponentAnnotation> anyComponentAnnotation(TypeElement typeElement) {
        return anyComponentAnnotation(typeElement, ALL_COMPONENT_ANNOTATIONS);
    }

    //从typeElement类中获取到使用了annotations注解中的注解（第一个即可），并且生成ComponentAnnotation对象
    private static Optional<ComponentAnnotation> anyComponentAnnotation(
            TypeElement typeElement, Collection<ClassName> annotations) {
        //map前面将typeElement中的注解在annotations中校验并获取第一个，然后转换成AnnotationMirror
        //map方法是将转换成的AnnotationMirror生成RealComponentAnnotation对象
        return getAnyAnnotation(typeElement, annotations).map(ComponentAnnotation::componentAnnotation);
    }

    /**
     * Returns {@code true} if the argument is a component annotation.
     * <p>
     * 判断传递的annotation参数类型是不是(Producter)Component 或者(Producter)Subcomponent类型
     */
    public static boolean isComponentAnnotation(AnnotationMirror annotation) {
        return ALL_COMPONENT_ANNOTATIONS.stream()
                .anyMatch(annotationClass -> isTypeOf(annotationClass, annotation.getAnnotationType()));
    }

    /**
     * Creates an object representing a component or subcomponent annotation.
     * <p>
     * 创建component or subComponent 注解的ComponentAnnotation类
     */
    public static ComponentAnnotation componentAnnotation(AnnotationMirror annotation) {

        //生成一个RealComponentAnnotation对象，注解使用annotation
        RealComponentAnnotation.Builder annotationBuilder =
                RealComponentAnnotation.builder().annotation(annotation);

        if (isTypeOf(TypeNames.COMPONENT, annotation.getAnnotationType())) {
            return annotationBuilder.isProduction(false).isSubcomponent(false).build();
        }
        if (isTypeOf(TypeNames.SUBCOMPONENT, annotation.getAnnotationType())) {
            return annotationBuilder.isProduction(false).isSubcomponent(true).build();
        }
        if (isTypeOf(TypeNames.PRODUCTION_COMPONENT, annotation.getAnnotationType())) {
            return annotationBuilder.isProduction(true).isSubcomponent(false).build();
        }
        if (isTypeOf(TypeNames.PRODUCTION_SUBCOMPONENT, annotation.getAnnotationType())) {
            return annotationBuilder.isProduction(true).isSubcomponent(true).build();
        }
        throw new IllegalArgumentException(
                annotation
                        + " must be a Component, Subcomponent, ProductionComponent, "
                        + "or ProductionSubcomponent annotation");
    }

    /**
     * Creates a fictional component annotation representing a module.
     * <p>
     * 创建入口：针对一个Module注解生成的ModuleAnnotation生成一个虚拟ComponentAnnotation类
     */
    public static ComponentAnnotation fromModuleAnnotation(ModuleAnnotation moduleAnnotation) {
        return new AutoValue_ComponentAnnotation_FictionalComponentAnnotation(moduleAnnotation);
    }


    /**
     * The root component annotation types.
     */
    public static ImmutableSet<ClassName> rootComponentAnnotations() {
        return ROOT_COMPONENT_ANNOTATIONS;
    }

    /**
     * The subcomponent annotation types.
     */
    public static ImmutableSet<ClassName> subcomponentAnnotations() {
        return SUBCOMPONENT_ANNOTATIONS;
    }

    /**
     * All component annotation types.
     */
    public static ImmutableSet<ClassName> allComponentAnnotations() {
        return ALL_COMPONENT_ANNOTATIONS;
    }

    /**
     * All component and creator annotation types.
     */
    public static ImmutableSet<ClassName> allComponentAndCreatorAnnotations() {
        return ALL_COMPONENT_AND_CREATOR_ANNOTATIONS;
    }

    /**
     * An actual component annotation.
     *
     * @see FictionalComponentAnnotation
     */
    @AutoValue
    abstract static class RealComponentAnnotation extends ComponentAnnotation {

        @Override
        @Memoized
        public ImmutableList<AnnotationValue> dependencyValues() {
            //#dependencies注解值，如果是subcomponent则为空
            return isSubcomponent() ? ImmutableList.of() : getAnnotationValues("dependencies");
        }

        @Override
        @Memoized
        public ImmutableList<TypeMirror> dependencyTypes() {
            return super.dependencyTypes();
        }

        @Override
        @Memoized
        public ImmutableList<TypeElement> dependencies() {
            return super.dependencies();
        }

        @Override
        public boolean isRealComponent() {
            return true;
        }

        @Override
        @Memoized
        public ImmutableList<AnnotationValue> moduleValues() {
            //#modules注解值
            return getAnnotationValues("modules");
        }

        @Override
        @Memoized
        public ImmutableList<TypeMirror> moduleTypes() {
            return super.moduleTypes();
        }

        @Override
        @Memoized
        public ImmutableSet<TypeElement> modules() {
            return super.modules();
        }

        static Builder builder() {
            return new AutoValue_ComponentAnnotation_RealComponentAnnotation.Builder();
        }

        @AutoValue.Builder
        interface Builder {
            Builder annotation(AnnotationMirror annotation);

            Builder isSubcomponent(boolean isSubcomponent);

            Builder isProduction(boolean isProduction);

            RealComponentAnnotation build();
        }
    }

    /**
     * A fictional component annotation used to represent modules or other collections of bindings as
     * a component.
     * <p>
     * 代表Module注解或其他收集的绑定Component的虚构出来的component annotation
     */
    @AutoValue
    abstract static class FictionalComponentAnnotation extends ComponentAnnotation {

        @Override
        public AnnotationMirror annotation() {
            return moduleAnnotation().annotation();
        }

        @Override
        public boolean isSubcomponent() {
            return false;
        }

        @Override
        public boolean isProduction() {
            return ClassName.get(asType(moduleAnnotation().annotation().getAnnotationType().asElement()))
                    .equals(PRODUCER_MODULE);
        }

        //不是真实的component，虚构的
        @Override
        public boolean isRealComponent() {
            return false;
        }

        @Override
        public ImmutableList<AnnotationValue> dependencyValues() {
            return ImmutableList.of();
        }

        @Override
        public ImmutableList<AnnotationValue> moduleValues() {
            return moduleAnnotation().includesAsAnnotationValues();
        }

        @Override
        @Memoized
        public ImmutableList<TypeMirror> moduleTypes() {
            return super.moduleTypes();
        }

        @Override
        @Memoized
        public ImmutableSet<TypeElement> modules() {
            return super.modules();
        }

        public abstract ModuleAnnotation moduleAnnotation();
    }
}
