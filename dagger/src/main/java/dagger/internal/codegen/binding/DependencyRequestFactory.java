package dagger.internal.codegen.binding;


import com.google.common.util.concurrent.ListenableFuture;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import dagger.Lazy;
import dagger.spi.model.DaggerElement;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.RequestKind;

import static com.google.auto.common.MoreTypes.isTypeOf;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.base.RequestKinds.extractKeyType;
import static dagger.internal.codegen.base.RequestKinds.getRequestKind;
import static dagger.internal.codegen.binding.ConfigurationAnnotations.getNullableType;
import static dagger.internal.codegen.langmodel.DaggerTypes.unwrapType;
import static dagger.spi.model.RequestKind.FUTURE;
import static dagger.spi.model.RequestKind.INSTANCE;

/**
 * Factory for {@link DependencyRequest}s.
 *
 * <p>Any factory method may throw {@link TypeNotPresentException} if a type is not available, which
 * may mean that the type will be generated in a later round of processing.
 */
public final class DependencyRequestFactory {
    private final KeyFactory keyFactory;
    private final InjectionAnnotations injectionAnnotations;

    @Inject
    DependencyRequestFactory(
            KeyFactory keyFactory,
            InjectionAnnotations injectionAnnotations
    ) {
        this.keyFactory = keyFactory;
        this.injectionAnnotations = injectionAnnotations;
    }

    public DependencyRequest forComponentProvisionMethod(
            ExecutableElement provisionMethod, ExecutableType provisionMethodType) {
        checkNotNull(provisionMethod);
        checkNotNull(provisionMethodType);
        checkArgument(
                provisionMethod.getParameters().isEmpty(),
                "Component provision methods must be empty: %s",
                provisionMethod);
        Optional<AnnotationMirror> qualifier = injectionAnnotations.getQualifier(provisionMethod);

        return newDependencyRequest(provisionMethod, provisionMethodType.getReturnType(), qualifier);
    }

    public DependencyRequest forComponentProductionMethod(
            ExecutableElement productionMethod,
            ExecutableType productionMethodType
    ) {
        checkNotNull(productionMethod);
        checkNotNull(productionMethodType);
        checkArgument(
                productionMethod.getParameters().isEmpty(),
                "Component production methods must be empty: %s",
                productionMethod);

        TypeMirror type = productionMethodType.getReturnType();

        Optional<AnnotationMirror> qualifier = injectionAnnotations.getQualifier(productionMethod);
        // Only a component production method can be a request for a ListenableFuture, so we
        // special-case it here.
        //isTypeOf(ListenableFuture.class, type):type是不是ListenableFuture类型
        if (isTypeOf(ListenableFuture.class, type)) {
            return DependencyRequest.builder()
                    .kind(FUTURE)
                    .key(keyFactory.forQualifiedType(qualifier, unwrapType(type)))
                    .requestElement(DaggerElement.fromJava(productionMethod))
                    .build();
        } else {
            return newDependencyRequest(productionMethod, type, qualifier);
        }
    }

    private DependencyRequest newDependencyRequest(
            Element requestElement, TypeMirror type, Optional<AnnotationMirror> qualifier) {
        RequestKind requestKind = getRequestKind(type);
        return DependencyRequest.builder()
                .kind(requestKind)
                .key(keyFactory.forQualifiedType(qualifier, extractKeyType(type)))
                .requestElement(DaggerElement.fromJava(requestElement))
                .isNullable(allowsNull(requestKind, getNullableType(requestElement)))
                .build();
    }

    /**
     * Returns {@code true} if a given request element allows null values. {@link
     * RequestKind#INSTANCE} requests must be annotated with {@code @Nullable} in order to allow null
     * values. All other request kinds implicitly allow null values because they are are wrapped
     * inside {@link Provider}, {@link Lazy}, etc.
     *
     * 如果给定的节点允许为null，返回true
     */
    private boolean allowsNull(RequestKind kind, Optional<DeclaredType> nullableType) {
        return nullableType.isPresent() || !kind.equals(INSTANCE);
    }
}
