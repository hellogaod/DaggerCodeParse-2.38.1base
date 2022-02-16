package dagger.internal.codegen.binding;


import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.internal.codegen.base.ContributionType;
import dagger.internal.codegen.base.FrameworkTypes;
import dagger.internal.codegen.base.MapType;
import dagger.internal.codegen.base.OptionalType;
import dagger.internal.codegen.base.RequestKinds;
import dagger.internal.codegen.base.SetType;
import dagger.internal.codegen.base.SimpleAnnotationMirror;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.multibindings.Multibinds;
import dagger.producers.Produced;
import dagger.producers.Producer;
import dagger.producers.Production;
import dagger.producers.internal.ProductionImplementation;
import dagger.producers.monitoring.ProductionComponentMonitor;
import dagger.spi.model.DaggerAnnotation;
import dagger.spi.model.Key;
import dagger.spi.model.RequestKind;

import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.auto.common.MoreTypes.asExecutable;
import static com.google.auto.common.MoreTypes.isType;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.base.RequestKinds.extractKeyType;
import static dagger.internal.codegen.binding.MapKeys.getMapKey;
import static dagger.internal.codegen.binding.MapKeys.mapKeyType;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.internal.codegen.extension.Optionals.firstPresent;
import static dagger.internal.codegen.langmodel.DaggerTypes.isFutureType;
import static dagger.internal.codegen.langmodel.DaggerTypes.unwrapType;
import static dagger.spi.model.DaggerType.fromJava;
import static java.util.Arrays.asList;
import static javax.lang.model.element.ElementKind.METHOD;

/**
 * A factory for {@link Key}s.
 */
public final class KeyFactory {
    private final DaggerTypes types;
    private final DaggerElements elements;
    private final InjectionAnnotations injectionAnnotations;

    @Inject
    KeyFactory(
            DaggerTypes types,
            DaggerElements elements,
            InjectionAnnotations injectionAnnotations
    ) {
        this.types = checkNotNull(types);
        this.elements = checkNotNull(elements);
        this.injectionAnnotations = injectionAnnotations;
    }

    //如果是原始类型，那么包装成PrimitiveType类型，例如int包装成Integer
    private TypeMirror boxPrimitives(TypeMirror type) {
        return type.getKind().isPrimitive() ? types.boxedClass((PrimitiveType) type).asType() : type;
    }

    //Set包裹当前类型
    private DeclaredType setOf(TypeMirror elementType) {
        return types.getDeclaredType(elements.getTypeElement(Set.class), boxPrimitives(elementType));
    }

    //Map包裹
    private DeclaredType mapOf(TypeMirror keyType, TypeMirror valueType) {
        return types.getDeclaredType(
                elements.getTypeElement(Map.class), boxPrimitives(keyType), boxPrimitives(valueType));
    }

    /**
     * Returns {@code Map<KeyType, FrameworkType<ValueType>>}.
     */
    private TypeMirror mapOfFrameworkType(
            TypeMirror keyType, TypeElement frameworkType, TypeMirror valueType) {
        return mapOf(keyType, types.getDeclaredType(frameworkType, boxPrimitives(valueType)));
    }

    //生成Key对象
    Key forQualifiedType(Optional<AnnotationMirror> qualifier, TypeMirror type) {
        return Key.builder(fromJava(boxPrimitives(type)))
                .qualifier(qualifier.map(DaggerAnnotation::fromJava))
                .build();
    }

    public Key forInjectConstructorWithResolvedType(TypeMirror type) {
        return Key.builder(fromJava(type)).build();
    }

    public Key forMembersInjectedType(TypeMirror type) {
        return Key.builder(fromJava(type)).build();
    }

    /**
     * Returns the base key bound by a {@link BindsOptionalOf} method.
     */
    Key forBindsOptionalOfMethod(ExecutableElement method, TypeElement contributingModule) {
        checkArgument(isAnnotationPresent(method, BindsOptionalOf.class));
        return forBindingMethod(method, contributingModule, Optional.empty());
    }

    public Key forSubcomponentCreator(TypeMirror creatorType) {
        return Key.builder(fromJava(creatorType)).build();
    }

    //Provides修饰的方法生成的Key对象，使用到了Provider作为架构类型
    public Key forProvidesMethod(ExecutableElement method, TypeElement contributingModule) {
        return forBindingMethod(
                method, contributingModule, Optional.of(elements.getTypeElement(Provider.class)));
    }

    public Key forProducesMethod(ExecutableElement method, TypeElement contributingModule) {
        return forBindingMethod(
                method, contributingModule, Optional.of(elements.getTypeElement(Producer.class)));
    }

    /**
     * Returns the key bound by a {@link Binds} method.
     * <p>
     * Binds修饰的方法和该方法所在的module类生成一个key
     */
    Key forBindsMethod(ExecutableElement method, TypeElement contributingModule) {

        checkArgument(isAnnotationPresent(method, Binds.class));

        return forBindingMethod(method, contributingModule, Optional.empty());
    }

    private Key forBindingMethod(
            ExecutableElement method,
            TypeElement contributingModule,
            Optional<TypeElement> frameworkType) {

        checkArgument(method.getKind().equals(METHOD));

        ExecutableType methodType =
                MoreTypes.asExecutable(
                        types.asMemberOf(MoreTypes.asDeclared(contributingModule.asType()), method));

        ContributionType contributionType = ContributionType.fromBindingElement(method);

        TypeMirror returnType = methodType.getReturnType();

        // 架构类型存在 && 架构类型是Producer （&& 方法返回类型是正常的类型）
        if (frameworkType.isPresent()
                && frameworkType.get().equals(elements.getTypeElement(Producer.class))
                && isType(returnType)) {
            //如果方法返回类型是ListenableFuture 或 FluentFuture
            if (isFutureType(methodType.getReturnType())) {

                //返回类型 = 获取ListenableFuture<T> 或 FluentFuture<T>,里面的T
                returnType = getOnlyElement(MoreTypes.asDeclared(returnType).getTypeArguments());

            }
            //如果返回类型是Set<T> && 该方法使用了ElementsIntoSet注解修饰
            else if (contributionType.equals(ContributionType.SET_VALUES)
                    && SetType.isSet(returnType)) {

                SetType setType = SetType.from(returnType);

                //如果Set<T>里面的T是ListenableFuture<E> 或 FluentFuture<E>类型
                if (isFutureType(setType.elementType())) {

                    //返回类型 = Set<E>
                    returnType =
                            types.getDeclaredType(
                                    elements.getTypeElement(Set.class), unwrapType(setType.elementType()));
                }
            }
        }

        //keyType根据使用注解情况判断使用哪种类型；
        // ①如果使用了IntoSet，那么使用Set<returnType>;
        // ②如果使用了@ElementsIntoSet修饰,那么本身就是一个Set<T>,直接使用该Set<T>即可；
        //③如果使用了IntoMap修饰，那么该方法一定会用到MapKey修饰的注解修饰，则使用Map<K,V>
        // ----K:MapKey注解修饰的注解里面的有且仅有的唯一方法的返回类型（不存在，直接使用MapKey注解修饰的注解类型；），V：使用frameworkType(如果存在,不存在直接使用returnType即可) 包裹returnType
        //④其他情况直接使用returnType；
        TypeMirror keyType = bindingMethodKeyType(returnType, method, contributionType, frameworkType);

        //生成Key对象，这里加入了方法是否使用了Qualifier注解修饰的注解
        Key key = forMethod(method, keyType);

        //如果该方法没有使用IntoMap或IntoSet或ElementsIntoSet修饰，直接返回key；否则还需要加上multibindingContributionIdentifier属性（传入了该方法名和该方法所在module类名）
        return contributionType.equals(ContributionType.UNIQUE)
                ? key
                : key.toBuilder()
                .multibindingContributionIdentifier(
                        new Key.MultibindingContributionIdentifier(method, contributingModule))
                .build();
    }

    /**
     * Returns the key for a {@link Multibinds @Multibinds} method.
     * <p>
     * Multibinds修饰的方法生成Key，①：是否使用了Qualifier注解修饰的注解修饰；②type：方法返回类型如果是Set，就用该类型作为type；如果是Map<K,V>，那么改成Map<K,Provider<V>>
     *
     * <p>The key's type is either {@code Set<T>} or {@code Map<K, Provider<V>>}. The latter works
     * even for maps used by {@code Producer}s.
     */
    Key forMultibindsMethod(ExecutableType executableType, ExecutableElement method) {

        checkArgument(method.getKind().equals(METHOD), "%s must be a method", method);

        TypeMirror returnType = executableType.getReturnType();
        TypeMirror keyType =
                MapType.isMap(returnType)
                        ? mapOfFrameworkType(
                        MapType.from(returnType).keyType(),
                        elements.getTypeElement(Provider.class),
                        MapType.from(returnType).valueType())
                        : returnType;
        return forMethod(method, keyType);
    }

    private TypeMirror bindingMethodKeyType(
            TypeMirror returnType,
            ExecutableElement method,
            ContributionType contributionType,
            Optional<TypeElement> frameworkType) {

        switch (contributionType) {
            case UNIQUE://没有使用到contributionType里面的注解，直接返回方法返回类型
                return returnType;
            case SET://如果使用了@IntoSet注解修饰，返回Set<方法返回类型>
                return setOf(returnType);
            case MAP://如果使用了@IntoMap注解修饰（一定会使用到MapKey注解修饰的注解修饰本方法），
                // 返回类型是Map<K,V>,K:MapKey注解修饰的注解里面有且仅有的一个方法返回类型，如果该返回类型不存在，那么直接使用MapKey注解修饰的注解类型
                //V：存在frameworkType框架类型使用框架类型包裹返回类型，不存在框架类型，直接使用返回类型

                //Mapkey修饰的注解的注解里面的有且仅有的唯一方法返回类型（如果没有或者条件不符合，则使用该注解自己的类型）作为Key，
                // value使用当前方法返回类型（frameworkType存在的话将该方法返回类型包裹）
                TypeMirror mapKeyType = mapKeyType(getMapKey(method).get(), types);
                return frameworkType.isPresent()
                        ? mapOfFrameworkType(mapKeyType, frameworkType.get(), returnType)
                        : mapOf(mapKeyType, returnType);

            case SET_VALUES://如果使用了@ElementsIntoSet修饰，那么当前方法的返回类型必须是Set，直接返回该返回类型即可
                // TODO(gak): do we want to allow people to use "covariant return" here?
                checkArgument(SetType.isSet(returnType));
                return returnType;
        }
        throw new AssertionError();
    }

    private Key forMethod(ExecutableElement method, TypeMirror keyType) {
        return forQualifiedType(injectionAnnotations.getQualifier(method), keyType);
    }

    // TODO(ronshapiro): Remove these conveniences which are simple wrappers around Key.Builder
    Key forType(TypeMirror type) {
        return Key.builder(fromJava(type)).build();
    }

    Key forComponentMethod(ExecutableElement componentMethod) {
        checkArgument(componentMethod.getKind().equals(METHOD));
        return forMethod(componentMethod, componentMethod.getReturnType());
    }

    Key forSubcomponentCreatorMethod(
            ExecutableElement subcomponentCreatorMethod, DeclaredType declaredContainer) {
        checkArgument(subcomponentCreatorMethod.getKind().equals(METHOD));
        ExecutableType resolvedMethod =
                asExecutable(types.asMemberOf(declaredContainer, subcomponentCreatorMethod));
        return Key.builder(fromJava(resolvedMethod.getReturnType())).build();
    }

    Key forProductionComponentMethod(ExecutableElement componentMethod) {
        checkArgument(componentMethod.getKind().equals(METHOD));
        TypeMirror returnType = componentMethod.getReturnType();
        TypeMirror keyType =
                isFutureType(returnType)
                        ? getOnlyElement(MoreTypes.asDeclared(returnType).getTypeArguments())
                        : returnType;
        return forMethod(componentMethod, keyType);
    }

    //Executor作为type，ProductionImplementation表示qualifier属性
    public Key forProductionImplementationExecutor() {
        return Key.builder(fromJava(elements.getTypeElement(Executor.class).asType()))
                .qualifier(
                        DaggerAnnotation.fromJava(
                                SimpleAnnotationMirror.of(elements.getTypeElement(ProductionImplementation.class))))
                .build();
    }

    public Key forProductionExecutor() {
        return Key.builder(fromJava(elements.getTypeElement(Executor.class).asType()))
                .qualifier(DaggerAnnotation.fromJava(SimpleAnnotationMirror.of(elements.getTypeElement(Production.class))))
                .build();
    }

    public Key forProductionComponentMonitor() {
        //ProductionComponentMonitor生成一个Key对象
        return Key.builder(fromJava(elements.getTypeElement(ProductionComponentMonitor.class).asType()))
                .build();
    }

    /**
     * If {@code requestKey} is for a {@code Map<K, V>} or {@code Map<K, Produced<V>>}, returns keys
     * for {@code Map<K, Provider<V>>} and {@code Map<K, Producer<V>>} (if Dagger-Producers is on
     * the classpath).
     * <p>
     * Map<K, V>} or   Map<K, Produced<V>>改成Map<K, Provider<V>>} and {@code Map<K, Producer<V>>
     */
    ImmutableSet<Key> implicitFrameworkMapKeys(Key requestKey) {
        return Stream.of(implicitMapProviderKeyFrom(requestKey), implicitMapProducerKeyFrom(requestKey))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toImmutableSet());
    }

    /**
     * Optionally extract a {@link Key} for the underlying provision binding(s) if such a valid key
     * can be inferred from the given key. Specifically, if the key represents a {@link Map}{@code
     * <K, V>} or {@code Map<K, Producer<V>>}, a key of {@code Map<K, Provider<V>>} will be
     * returned.
     */
    Optional<Key> implicitMapProviderKeyFrom(Key possibleMapKey) {
        return firstPresent(
                rewrapMapKey(possibleMapKey, Produced.class, Provider.class),
                wrapMapKey(possibleMapKey, Provider.class));
    }

    /**
     * Optionally extract a {@link Key} for the underlying production binding(s) if such a
     * valid key can be inferred from the given key.  Specifically, if the key represents a
     * {@link Map}{@code <K, V>} or {@code Map<K, Produced<V>>}, a key of
     * {@code Map<K, Producer<V>>} will be returned.
     */
    Optional<Key> implicitMapProducerKeyFrom(Key possibleMapKey) {
        return firstPresent(
                rewrapMapKey(possibleMapKey, Produced.class, Producer.class),
                wrapMapKey(possibleMapKey, Producer.class));
    }

    /**
     * If {@code key}'s type is {@code Map<K, CurrentWrappingClass<Bar>>}, returns a key with type
     * {@code Map<K, NewWrappingClass<Bar>>} with the same qualifier. Otherwise returns {@link
     * Optional#empty()}.
     *
     * <p>Returns {@link Optional#empty()} if {@code newWrappingClass} is not in the classpath.
     * <p>
     * Map<K, CurrentWrappingClass<Bar>>返回Map<K, NewWrappingClass<Bar>>
     *
     * @throws IllegalArgumentException if {@code newWrappingClass} is the same as {@code
     *                                  currentWrappingClass}
     */
    public Optional<Key> rewrapMapKey(
            Key possibleMapKey, Class<?> currentWrappingClass, Class<?> newWrappingClass) {
        checkArgument(!currentWrappingClass.equals(newWrappingClass));
        if (MapType.isMap(possibleMapKey)) {
            MapType mapType = MapType.from(possibleMapKey);
            if (!mapType.isRawType() && mapType.valuesAreTypeOf(currentWrappingClass)) {
                TypeElement wrappingElement = elements.getTypeElement(newWrappingClass);
                if (wrappingElement == null) {
                    // This target might not be compiled with Producers, so wrappingClass might not have an
                    // associated element.
                    return Optional.empty();
                }
                DeclaredType wrappedValueType =
                        types.getDeclaredType(
                                wrappingElement, mapType.unwrappedValueType(currentWrappingClass));
                return Optional.of(
                        possibleMapKey.toBuilder()
                                .type(fromJava(mapOf(mapType.keyType(), wrappedValueType)))
                                .build());
            }
        }
        return Optional.empty();
    }

    /**
     * If {@code key}'s type is {@code Map<K, Foo>} and {@code Foo} is not {@code WrappingClass
     * <Bar>}, returns a key with type {@code Map<K, WrappingClass<Foo>>} with the same qualifier.
     * Otherwise returns {@link Optional#empty()}.
     *
     * <p>Returns {@link Optional#empty()} if {@code WrappingClass} is not in the classpath.
     */
    private Optional<Key> wrapMapKey(Key possibleMapKey, Class<?> wrappingClass) {
        if (MapType.isMap(possibleMapKey)) {
            MapType mapType = MapType.from(possibleMapKey);
            if (!mapType.isRawType() && !mapType.valuesAreTypeOf(wrappingClass)) {
                TypeElement wrappingElement = elements.getTypeElement(wrappingClass);
                if (wrappingElement == null) {
                    // This target might not be compiled with Producers, so wrappingClass might not have an
                    // associated element.
                    return Optional.empty();
                }
                DeclaredType wrappedValueType = types.getDeclaredType(wrappingElement, mapType.valueType());
                return Optional.of(
                        possibleMapKey.toBuilder()
                                .type(fromJava(mapOf(mapType.keyType(), wrappedValueType)))
                                .build());
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the key for a binding associated with a {@link DelegateDeclaration}.
     *
     * <p>If {@code delegateDeclaration} is {@code @IntoMap}, transforms the {@code Map<K, V>} key
     * from {@link DelegateDeclaration#key()} to {@code Map<K, FrameworkType<V>>}. If {@code
     * delegateDeclaration} is not a map contribution, its key is returned.
     */
    Key forDelegateBinding(DelegateDeclaration delegateDeclaration, Class<?> frameworkType) {
        return delegateDeclaration.contributionType().equals(ContributionType.MAP)
                ? wrapMapValue(delegateDeclaration.key(), frameworkType)
                : delegateDeclaration.key();
    }

    /**
     * If {@code key}'s type is {@code Map<K, Provider<V>>}, {@code Map<K, Producer<V>>}, or {@code
     * Map<K, Produced<V>>}, returns a key with the same qualifier and {@link
     * Key#multibindingContributionIdentifier()} whose type is simply {@code Map<K, V>}.
     * <p>
     * 如果是Map<K, Provider<V>>,Map<K, Producer<V>>，Map<K, Produced<V>>返回Map<K, V>;
     * 不是Map类型不变
     *
     * <p>Otherwise, returns {@code key}.
     */
    public Key unwrapMapValueType(Key key) {
        if (MapType.isMap(key)) {
            MapType mapType = MapType.from(key);
            if (!mapType.isRawType()) {
                for (Class<?> frameworkClass : asList(Provider.class, Producer.class, Produced.class)) {
                    if (mapType.valuesAreTypeOf(frameworkClass)) {
                        return key.toBuilder()
                                .type(
                                        fromJava(mapOf(mapType.keyType(), mapType.unwrappedValueType(frameworkClass))))
                                .build();
                    }
                }
            }
        }
        return key;
    }

    /**
     * Converts a {@link Key} of type {@code Map<K, V>} to {@code Map<K, Provider<V>>}.
     */
    private Key wrapMapValue(Key key, Class<?> newWrappingClass) {
        checkArgument(
                FrameworkTypes.isFrameworkType(elements.getTypeElement(newWrappingClass).asType()));
        return wrapMapKey(key, newWrappingClass).get();
    }

    /**
     * If {@code key}'s type is {@code Set<WrappingClass<Bar>>}, returns a key with type {@code Set
     * <Bar>} with the same qualifier. Otherwise returns {@link Optional#empty()}.
     * <p>
     * Set<WrappingClass<Bar>>返回Set<Bar>
     */
    Optional<Key> unwrapSetKey(Key key, Class<?> wrappingClass) {
        if (SetType.isSet(key)) {
            SetType setType = SetType.from(key);
            if (!setType.isRawType() && setType.elementsAreTypeOf(wrappingClass)) {
                return Optional.of(
                        key.toBuilder()
                                .type(fromJava(setOf(setType.unwrappedElementType(wrappingClass))))
                                .build());
            }
        }
        return Optional.empty();
    }

    /**
     * If {@code key}'s type is {@code Optional<T>} for some {@code T}, returns a key with the same
     * qualifier whose type is {@linkplain RequestKinds#extractKeyType(RequestKind, TypeMirror)}
     * extracted} from {@code T}.
     */
    Optional<Key> unwrapOptional(Key key) {
        if (!OptionalType.isOptional(key)) {//如果不是Optional类型，返回empty
            return Optional.empty();
        }

        //如果是Optional<T>类型，那么直接使用T作为type重新生成一个key并且返回
        TypeMirror optionalValueType = OptionalType.from(key).valueType();
        return Optional.of(key.toBuilder().type(fromJava(extractKeyType(optionalValueType))).build());
    }
}
