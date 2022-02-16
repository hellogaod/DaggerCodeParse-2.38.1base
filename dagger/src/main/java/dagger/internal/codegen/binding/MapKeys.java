package dagger.internal.codegen.binding;


import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.NoSuchElementException;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor6;

import dagger.MapKey;
import dagger.internal.codegen.base.MapKeyAccessibility;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.auto.common.AnnotationMirrors.getAnnotatedAnnotations;
import static com.google.auto.common.AnnotationMirrors.getAnnotationValuesWithDefaults;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static dagger.internal.codegen.base.MapKeyAccessibility.isMapKeyPubliclyAccessible;
import static dagger.internal.codegen.binding.SourceFiles.elementBasedClassName;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * Methods for extracting {@link MapKey} annotations and key code blocks from binding elements.
 */
public final class MapKeys {

    /**
     * If {@code bindingElement} is annotated with a {@link MapKey} annotation, returns it.
     * <p>
     * 使用MapKey注解修饰的注解，当且仅当存在一个
     *
     * @throws IllegalArgumentException if the element is annotated with more than one {@code MapKey}
     *                                  annotation
     */
    static Optional<AnnotationMirror> getMapKey(Element bindingElement) {
        ImmutableSet<? extends AnnotationMirror> mapKeys = getMapKeys(bindingElement);
        return mapKeys.isEmpty()
                ? Optional.empty()
                : Optional.<AnnotationMirror>of(getOnlyElement(mapKeys));
    }

    /**
     * Returns all of the {@link MapKey} annotations that annotate {@code bindingElement}.
     * <p>
     * 返回bindingElement节点上的被MapKey修饰的注解集
     */
    public static ImmutableSet<? extends AnnotationMirror> getMapKeys(Element bindingElement) {
        return getAnnotatedAnnotations(bindingElement, MapKey.class);
    }

    /**
     * Returns the annotation value if {@code mapKey}'s type is annotated with
     * {@link MapKey @MapKey(unwrapValue = true)}.
     * <p>
     * 如果@MapKey(unwrapValue = true),获取当前使用MapKey修饰的注解里面的有且仅有的唯一方法的值
     *
     * @throws IllegalArgumentException if {@code mapKey}'s type is not annotated with
     *                                  {@link MapKey @MapKey} at all.
     */
    static Optional<? extends AnnotationValue> unwrapValue(AnnotationMirror mapKey) {
        MapKey mapKeyAnnotation = mapKey.getAnnotationType().asElement().getAnnotation(MapKey.class);
        checkArgument(
                mapKeyAnnotation != null, "%s is not annotated with @MapKey", mapKey.getAnnotationType());

        return mapKeyAnnotation.unwrapValue()
                ? Optional.of(getOnlyElement(getAnnotationValuesWithDefaults(mapKey).values()))
                : Optional.empty();
    }

    //如果mapKeyAnnotation使用的是@MapKey(unwrapValue = true)（或者@MapKey形式）&& mapKeyAnnotation有且仅有的唯一注解值方法存在 ，那么获取该方法的返回类型；
    //否则返回mapKeyAnnotation原本的类型即可
    static TypeMirror mapKeyType(AnnotationMirror mapKeyAnnotation, DaggerTypes types) {

        return unwrapValue(mapKeyAnnotation).isPresent()
                ? getUnwrappedMapKeyType(mapKeyAnnotation.getAnnotationType(), types)
                : mapKeyAnnotation.getAnnotationType();
    }

    /**
     * Returns a code block for {@code binding}'s {@link ContributionBinding#mapKeyAnnotation() map
     * key}. If for whatever reason the map key is not accessible from within {@code requestingClass}
     * (i.e. it has a package-private {@code enum} from a different package), this will return an
     * invocation of a proxy-method giving it access.
     *
     * @throws IllegalStateException if {@code binding} is not a {@link dagger.multibindings.IntoMap
     *                               map} contribution.
     */
    public static CodeBlock getMapKeyExpression(
            ContributionBinding binding, ClassName requestingClass, DaggerElements elements) {
        AnnotationMirror mapKeyAnnotation = binding.mapKeyAnnotation().get();
        return MapKeyAccessibility.isMapKeyAccessibleFrom(
                mapKeyAnnotation, requestingClass.packageName())
                ? directMapKeyExpression(mapKeyAnnotation, elements)
                : CodeBlock.of("$T.create()", mapKeyProxyClassName(binding));
    }

    /**
     * Returns a code block for the map key annotation {@code mapKey}.
     *
     * <p>This method assumes the map key will be accessible in the context that the returned {@link
     * CodeBlock} is used. Use {@link #getMapKeyExpression(ContributionBinding, ClassName,
     * DaggerElements)} when that assumption is not guaranteed.
     *
     * @throws IllegalArgumentException if the element is annotated with more than one {@code MapKey}
     *                                  annotation
     * @throws IllegalStateException    if {@code bindingElement} is not annotated with a {@code MapKey}
     *                                  annotation
     */
    private static CodeBlock directMapKeyExpression(
            AnnotationMirror mapKey, DaggerElements elements) {
        Optional<? extends AnnotationValue> unwrappedValue = unwrapValue(mapKey);
        AnnotationExpression annotationExpression = new AnnotationExpression(mapKey);

        if (MoreTypes.asTypeElement(mapKey.getAnnotationType())
                .getQualifiedName()
                .contentEquals("dagger.android.AndroidInjectionKey")) {
            TypeElement unwrappedType =
                    elements.checkTypePresent((String) unwrappedValue.get().getValue());
            return CodeBlock.of(
                    "$T.of($S)",
                    ClassName.get("dagger.android.internal", "AndroidInjectionKeys"),
                    ClassName.get(unwrappedType).reflectionName());
        }

        if (unwrappedValue.isPresent()) {
            TypeMirror unwrappedValueType =
                    getOnlyElement(getAnnotationValuesWithDefaults(mapKey).keySet()).getReturnType();
            return annotationExpression.getValueExpression(unwrappedValueType, unwrappedValue.get());
        } else {
            return annotationExpression.getAnnotationInstanceExpression();
        }
    }

    /**
     * Returns the {@link ClassName} in which {@link #mapKeyFactoryMethod(ContributionBinding,
     * DaggerTypes, DaggerElements)} is generated.
     */
    public static ClassName mapKeyProxyClassName(ContributionBinding binding) {
        return elementBasedClassName(
                MoreElements.asExecutable(binding.bindingElement().get()), "MapKey");
    }

    /**
     * Returns the map key type for an unwrapped {@link MapKey} annotation type. If the single member
     * type is primitive, returns the boxed type.
     * <p>
     * 使用MapKey注解修饰的注解， 获取其唯一的方法的返回类型：
     * 如果返回类型是类或接口直接返回，
     * 如果是数组报错；
     * 如果是原始类型使用盒装处理，例如int使用Integer类型
     *
     * @throws IllegalArgumentException if {@code mapKeyAnnotationType} is not an annotation type or
     *                                  has more than one member, or if its single member is an array
     * @throws NoSuchElementException   if the annotation has no members
     */
    public static DeclaredType getUnwrappedMapKeyType(
            final DeclaredType mapKeyAnnotationType,
            final DaggerTypes types
    ) {
        checkArgument(
                MoreTypes.asTypeElement(mapKeyAnnotationType).getKind() == ElementKind.ANNOTATION_TYPE,
                "%s is not an annotation type",
                mapKeyAnnotationType);

        final ExecutableElement onlyElement =
                getOnlyElement(methodsIn(mapKeyAnnotationType.asElement().getEnclosedElements()));

        SimpleTypeVisitor6<DeclaredType, Void> keyTypeElementVisitor =
                new SimpleTypeVisitor6<DeclaredType, Void>() {

                    @Override
                    public DeclaredType visitArray(ArrayType t, Void p) {
                        throw new IllegalArgumentException(
                                mapKeyAnnotationType + "." + onlyElement.getSimpleName() + " cannot be an array");
                    }

                    @Override
                    public DeclaredType visitPrimitive(PrimitiveType t, Void p) {
                        return MoreTypes.asDeclared(types.boxedClass(t).asType());
                    }

                    @Override
                    public DeclaredType visitDeclared(DeclaredType t, Void p) {
                        return t;
                    }
                };
        return keyTypeElementVisitor.visit(onlyElement.getReturnType());
    }

    /**
     * A {@code static create()} method to be added to {@link
     * #mapKeyProxyClassName(ContributionBinding)} when the {@code @MapKey} annotation is not publicly
     * accessible.
     */
    public static Optional<MethodSpec> mapKeyFactoryMethod(
            ContributionBinding binding, DaggerTypes types, DaggerElements elements) {
        return binding
                .mapKeyAnnotation()
                .filter(mapKey -> !isMapKeyPubliclyAccessible(mapKey))
                .map(
                        mapKey ->
                                methodBuilder("create")
                                        .addModifiers(PUBLIC, STATIC)
                                        .returns(TypeName.get(mapKeyType(mapKey, types)))
                                        .addStatement("return $L", directMapKeyExpression(mapKey, elements))
                                        .build());
    }

    private MapKeys() {
    }
}
