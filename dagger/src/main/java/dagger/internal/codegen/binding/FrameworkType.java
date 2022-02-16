package dagger.internal.codegen.binding;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Optional;

import javax.inject.Provider;
import javax.lang.model.type.TypeMirror;

import dagger.Lazy;
import dagger.internal.DoubleCheck;
import dagger.internal.ProviderOfLazy;
import dagger.internal.codegen.base.RequestKinds;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.producers.Produced;
import dagger.producers.Producer;
import dagger.producers.internal.Producers;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.RequestKind;

import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static dagger.spi.model.RequestKind.INSTANCE;

/**
 * One of the core types initialized as fields in a generated component.
 */
public enum FrameworkType {

    /**
     * A {@link Provider}.
     */
    PROVIDER {
        @Override
        public CodeBlock to(RequestKind requestKind, CodeBlock from) {
            switch (requestKind) {
                case INSTANCE:
                    return CodeBlock.of("$L.get()", from);

                case LAZY:
                    return CodeBlock.of("$T.lazy($L)", DoubleCheck.class, from);

                case PROVIDER:
                    return from;

                case PROVIDER_OF_LAZY:
                    return CodeBlock.of("$T.create($L)", ProviderOfLazy.class, from);

                case PRODUCER:
                    return CodeBlock.of("$T.producerFromProvider($L)", Producers.class, from);

                case FUTURE:
                    return CodeBlock.of("$T.immediateFuture($L)", Futures.class, to(INSTANCE, from));

                case PRODUCED:
                    return CodeBlock.of("$T.successful($L)", Produced.class, to(INSTANCE, from));

                default:
                    throw new IllegalArgumentException(
                            String.format("Cannot request a %s from a %s", requestKind, this));
            }
        }

        @Override
        public Expression to(RequestKind requestKind, Expression from, DaggerTypes types) {
            CodeBlock codeBlock = to(requestKind, from.codeBlock());

            switch (requestKind) {
                case INSTANCE:
                    return Expression.create(types.unwrapTypeOrObject(from.type()), codeBlock);

                case PROVIDER:
                    return from;

                case PROVIDER_OF_LAZY:
                    TypeMirror lazyType = types.rewrapType(from.type(), Lazy.class);
                    return Expression.create(types.wrapType(lazyType, Provider.class), codeBlock);

                case FUTURE:
                    return Expression.create(
                            types.rewrapType(from.type(), ListenableFuture.class), codeBlock);

                default:
                    return Expression.create(
                            types.rewrapType(from.type(), RequestKinds.frameworkClass(requestKind)), codeBlock);
            }
        }
    },
    /**
     * A {@link Producer}.
     */
    PRODUCER_NODE {
        @Override
        public CodeBlock to(RequestKind requestKind, CodeBlock from) {
            switch (requestKind) {
                case FUTURE:
                    return CodeBlock.of("$L.get()", from);

                case PRODUCER:
                    return from;

                default:
                    throw new IllegalArgumentException(
                            String.format("Cannot request a %s from a %s", requestKind, this));
            }
        }

        @Override
        public Expression to(RequestKind requestKind, Expression from, DaggerTypes types) {
            switch (requestKind) {
                case FUTURE:
                    return Expression.create(
                            types.rewrapType(from.type(), ListenableFuture.class),
                            to(requestKind, from.codeBlock()));

                case PRODUCER:
                    return Expression.create(from.type(), to(requestKind, from.codeBlock()));

                default:
                    throw new IllegalArgumentException(
                            String.format("Cannot request a %s from a %s", requestKind, this));
            }
        }
    };

    /**
     * Returns the framework type appropriate for fields for a given binding type.
     * <p>
     * 不同绑定类型使用不同框架类型
     */
    public static FrameworkType forBindingType(BindingType bindingType) {
        switch (bindingType) {
            case PROVISION:
                return PROVIDER;
            case PRODUCTION:
                return PRODUCER_NODE;
            case MEMBERS_INJECTION:
        }
        throw new AssertionError(bindingType);
    }

    /**
     * Returns the framework type that exactly matches the given request kind, if one exists.
     */
    public static Optional<FrameworkType> forRequestKind(RequestKind requestKind) {
        switch (requestKind) {
            case PROVIDER:
                return Optional.of(FrameworkType.PROVIDER);
            default:
                return Optional.empty();
        }
    }

    /**
     * The class of fields of this type.
     */
    public Class<?> frameworkClass() {
        switch (this) {
            case PROVIDER:
                return Provider.class;
            case PRODUCER_NODE:
                // TODO(cgdecker): Replace this with new class for representing internal producer nodes.
                // Currently the new class is CancellableProducer, but it may be changed to ProducerNode and
                // made to not implement Producer.
                return Producer.class;
        }
        throw new AssertionError("Unknown value: " + this.name());
    }

    /**
     * Returns the {@link #frameworkClass()} parameterized with a type.
     */
    public ParameterizedTypeName frameworkClassOf(TypeName valueType) {
        return ParameterizedTypeName.get(ClassName.get(frameworkClass()), valueType);
    }

    /**
     * The request kind that an instance of this framework type can satisfy directly, if any.
     */
    public RequestKind requestKind() {
        switch (this) {
            case PROVIDER:
                return RequestKind.PROVIDER;
            case PRODUCER_NODE:
                return RequestKind.PRODUCER;
        }
        throw new AssertionError("Unknown value: " + this.name());
    }

    /**
     * Returns a {@link CodeBlock} that evaluates to a requested object given an expression that
     * evaluates to an instance of this framework type.
     *
     * @param requestKind the kind of {@link DependencyRequest} that the returned expression can
     *                    satisfy
     * @param from        a {@link CodeBlock} that evaluates to an instance of this framework type
     * @throws IllegalArgumentException if a valid expression cannot be generated for {@code
     *                                  requestKind}
     */
    public abstract CodeBlock to(RequestKind requestKind, CodeBlock from);

    /**
     * Returns an {@link Expression} that evaluates to a requested object given an expression that
     * evaluates to an instance of this framework type.
     *
     * @param requestKind the kind of {@link DependencyRequest} that the returned expression can
     *                    satisfy
     * @param from        an expression that evaluates to an instance of this framework type
     * @throws IllegalArgumentException if a valid expression cannot be generated for {@code
     *                                  requestKind}
     */
    public abstract Expression to(RequestKind requestKind, Expression from, DaggerTypes types);

    @Override
    public String toString() {
        return UPPER_UNDERSCORE.to(UPPER_CAMEL, super.toString());
    }
}
