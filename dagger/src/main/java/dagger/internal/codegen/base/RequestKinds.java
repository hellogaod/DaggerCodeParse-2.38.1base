package dagger.internal.codegen.base;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import com.squareup.javapoet.TypeName;

import javax.inject.Provider;
import javax.lang.model.type.TypeMirror;

import dagger.Lazy;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.producers.Produced;
import dagger.producers.Producer;
import dagger.spi.model.RequestKind;

import static com.google.auto.common.MoreTypes.asDeclared;
import static com.google.auto.common.MoreTypes.isType;
import static com.google.auto.common.MoreTypes.isTypeOf;
import static com.google.common.base.Preconditions.checkArgument;
import static dagger.internal.codegen.javapoet.TypeNames.lazyOf;
import static dagger.internal.codegen.javapoet.TypeNames.listenableFutureOf;
import static dagger.internal.codegen.javapoet.TypeNames.producedOf;
import static dagger.internal.codegen.javapoet.TypeNames.producerOf;
import static dagger.internal.codegen.javapoet.TypeNames.providerOf;
import static dagger.internal.codegen.langmodel.DaggerTypes.checkTypePresent;
import static dagger.spi.model.RequestKind.LAZY;
import static dagger.spi.model.RequestKind.PRODUCED;
import static dagger.spi.model.RequestKind.PRODUCER;
import static dagger.spi.model.RequestKind.PROVIDER;
import static dagger.spi.model.RequestKind.PROVIDER_OF_LAZY;
import static javax.lang.model.type.TypeKind.DECLARED;

/**
 * Utility methods for {@link RequestKind}s.
 * <p>
 * TypeMirror根据requestKind类型，重新包裹或者去包裹
 */
public final class RequestKinds {

    /**
     * Returns the type of a request of this kind for a key with a given type.
     */
    public static TypeMirror requestType(
            RequestKind requestKind, TypeMirror type, DaggerTypes types) {
        switch (requestKind) {
            case INSTANCE:
                return type;

            case PROVIDER_OF_LAZY:
                return types.wrapType(requestType(LAZY, type, types), Provider.class);

            case FUTURE:
                return types.wrapType(type, ListenableFuture.class);

            default:
                return types.wrapType(type, frameworkClass(requestKind));
        }
    }

    /**
     * Returns the type of a request of this kind for a key with a given type.
     * <p>
     * 根据RequestKind给keyType加外层包裹，例如T，改成Lazy<T>
     */
    public static TypeName requestTypeName(RequestKind requestKind, TypeName keyType) {
        switch (requestKind) {
            case INSTANCE:
                return keyType;

            case PROVIDER:
                return providerOf(keyType);

            case LAZY:
                return lazyOf(keyType);

            case PROVIDER_OF_LAZY:
                return providerOf(lazyOf(keyType));

            case PRODUCER:
                return producerOf(keyType);

            case PRODUCED:
                return producedOf(keyType);

            case FUTURE:
                return listenableFutureOf(keyType);

            default:
                throw new AssertionError(requestKind);
        }
    }

    private static final ImmutableMap<RequestKind, Class<?>> FRAMEWORK_CLASSES =
            ImmutableMap.of(
                    PROVIDER, Provider.class,
                    LAZY, Lazy.class,
                    PRODUCER, Producer.class,
                    PRODUCED, Produced.class);

    /**
     * Returns the {@link RequestKind} that matches the wrapping types (if any) of {@code type}.
     * <p>
     * 根据type类型，得出响应的RequestKind
     */
    public static RequestKind getRequestKind(TypeMirror type) {

        checkTypePresent(type);//检查type不可是ErrorType类型

        //isType():表示void类型，原始类型，数组，类或接口类型。除此之外都是false

        //非void，原始类型，数组，接口或类
        // || 不是类或接口
        // || type不存在泛型类型
        //符合以上条件的都是Instance类型
        if (!isType(type) // TODO(b/147320669): isType check can be removed once this bug is fixed.
                || !type.getKind().equals(DECLARED)
                || asDeclared(type).getTypeArguments().isEmpty()) {
            // If the type is not a declared type (i.e. class or interface) with type arguments, then we
            // know it can't be a parameterized type of one of the framework classes, so return INSTANCE.
            return RequestKind.INSTANCE;
        }
        //遍历framework_class，type对应的RequestKind类型
        for (RequestKind kind : FRAMEWORK_CLASSES.keySet()) {

            //isTypeOf(Class,TypeMirror)：例如isTypeOf(List,List<String>)返回true
            if (isTypeOf(frameworkClass(kind), type)) {
                //如果type最外层是Provider，并且剥离Provider后的type类型又使用了Lazy
                if (kind.equals(PROVIDER) && getRequestKind(DaggerTypes.unwrapType(type)).equals(LAZY)) {
                    return PROVIDER_OF_LAZY;
                }
                return kind;
            }
        }
        return RequestKind.INSTANCE;
    }

    /**
     * Unwraps the framework class(es) of {@code requestKind} from {@code type}. If {@code
     * requestKind} is {@link RequestKind#INSTANCE}, this acts as an identity function.
     *
     * @throws TypeNotPresentException  if {@code type} is an {@link javax.lang.model.type.ErrorType},
     *                                  which may mean that the type will be generated in a later round of processing
     * @throws IllegalArgumentException if {@code type} is not wrapped with {@code requestKind}'s
     *                                  framework class(es).
     */
    public static TypeMirror extractKeyType(TypeMirror type) {
        //先根据type判断RequestKind类型，在根据RequestKind类型，剥离掉外层的泛型类型，例如Provider<Lazy<T>>,会返回T
        return extractKeyType(getRequestKind(type), type);
    }

    //根据requestKind剥离type外衣（这里的外衣仅仅是Provider<T>，Lazy<T>或者Provider<Lazy<T>>等符合FRAMEWORK_CLASSES的参数）
    private static TypeMirror extractKeyType(RequestKind requestKind, TypeMirror type) {
        switch (requestKind) {
            case INSTANCE:
                return type;
            case PROVIDER_OF_LAZY:
                return extractKeyType(LAZY, extractKeyType(PROVIDER, type));
            default:
                checkArgument(isType(type));
                return DaggerTypes.unwrapType(type);
        }
    }

    /**
     * A dagger- or {@code javax.inject}-defined class for {@code requestKind} that that can wrap
     * another type but share the same {@link dagger.spi.model.Key}.
     *
     * <p>For example, {@code Provider<String>} and {@code Lazy<String>} can both be requested if a
     * key exists for {@code String}; they all share the same key.
     *
     * <p>This concept is not well defined and should probably be removed and inlined into the cases
     * that need it. For example, {@link RequestKind#PROVIDER_OF_LAZY} has <em>2</em> wrapping
     * classes, and {@link RequestKind#FUTURE} is wrapped with a {@link ListenableFuture}, but for
     * historical/implementation reasons has not had an associated framework class.
     * <p>
     * RequestKind对应其Class类型
     */
    public static Class<?> frameworkClass(RequestKind requestKind) {
        Class<?> result = FRAMEWORK_CLASSES.get(requestKind);
        checkArgument(result != null, "no framework class for %s", requestKind);
        return result;
    }

    /**
     * Returns {@code true} if requests for {@code requestKind} can be satisfied by a production
     * binding.
     * <p>
     * 满足修饰Production绑定的请求类型
     */
    public static boolean canBeSatisfiedByProductionBinding(RequestKind requestKind) {
        switch (requestKind) {
            case INSTANCE:
            case PROVIDER:
            case LAZY:
            case PROVIDER_OF_LAZY:
            case MEMBERS_INJECTION:
                return false;
            case PRODUCER:
            case PRODUCED:
            case FUTURE:
                return true;
        }
        throw new AssertionError();
    }

    private RequestKinds() {
    }
}
