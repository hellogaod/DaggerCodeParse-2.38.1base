package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;

import javax.lang.model.type.TypeMirror;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.Binding;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.BindsTypeChecker;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.RequestKind;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.base.RequestKinds.requestType;
import static dagger.internal.codegen.binding.BindingRequest.bindingRequest;
import static dagger.internal.codegen.langmodel.Accessibility.isTypeAccessibleFrom;
import static dagger.spi.model.BindingKind.DELEGATE;


/**
 * A {@link dagger.internal.codegen.writing.RequestRepresentation} for {@code @Binds} methods.
 * <p>
 * 1. 当前被key匹配上的是ProvisionBinding对象，并且该key生成的BindingRequest的RequestKind类型是PROVIDER，该ProvisionBinding对象是@Binds修饰的bindingMethod方法生成，并且该方法没有使用Scope注解（或Scope注解比当前ProvisionBinding对象的依赖匹配到的Binding对象强）：
 * - 当前ProvisionBinding对象和RequestKind.PROVIDER作为参数；
 * 2. 当前被key匹配上的是ProvisionBinding对象，并且该key生成的BindingRequest的RequestKind类型是INSTANCE，并且该ProvisionBinding对象是@Binds修饰的bindingMethod方法生成,该bindingMethod方法没有使用Scope注解（或Scope注解比当前ProvisionBinding对象的依赖匹配到的Binding对象强）：
 * - 当前ProvisionBinding对象和RequestKind.INSTANCE作为参数；
 */
final class DelegateRequestRepresentation extends RequestRepresentation {

    private final ContributionBinding binding;
    private final RequestKind requestKind;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerTypes types;
    private final BindsTypeChecker bindsTypeChecker;

    @AssistedInject
    DelegateRequestRepresentation(
            @Assisted ContributionBinding binding,
            @Assisted RequestKind requestKind,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types,
            DaggerElements elements) {

        this.binding = checkNotNull(binding);
        this.requestKind = checkNotNull(requestKind);
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.types = types;
        this.bindsTypeChecker = new BindsTypeChecker(types, elements);
    }

    /**
     * Returns {@code true} if the {@code @Binds} binding's scope is stronger than the scope of the
     * binding it depends on.
     * <p>
     * 如果 {@code @Binds} 绑定的作用域强于它所依赖的绑定作用域，则返回 {@code true}。
     */
    static boolean isBindsScopeStrongerThanDependencyScope(
            ContributionBinding bindsBinding, BindingGraph graph) {
        checkArgument(bindsBinding.kind().equals(DELEGATE));
        Binding dependencyBinding =
                graph.contributionBinding(getOnlyElement(bindsBinding.dependencies()).key());
        ScopeKind bindsScope = ScopeKind.get(bindsBinding);
        ScopeKind dependencyScope = ScopeKind.get(dependencyBinding);
        return bindsScope.isStrongerScopeThan(dependencyScope);
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {


        Expression delegateExpression =
                componentRequestRepresentations.getDependencyExpression(
                        bindingRequest(getOnlyElement(binding.dependencies()).key(), requestKind),
                        requestingClass);

        TypeMirror contributedType = binding.contributedType();
        switch (requestKind) {
            case INSTANCE://1.使用的是該kind類型
                return instanceRequiresCast(delegateExpression, requestingClass)
                        ? delegateExpression.castTo(contributedType)
                        : delegateExpression;
            default:
                return castToRawTypeIfNecessary(
                        delegateExpression, requestType(requestKind, contributedType, types));
        }
    }

    private boolean instanceRequiresCast(Expression delegateExpression, ClassName requestingClass) {
        // delegateExpression.type() could be Object if expression is satisfied with a raw
        // Provider's get() method.
        return !bindsTypeChecker.isAssignable(
                delegateExpression.type(), binding.contributedType(), binding.contributionType())
                && isTypeAccessibleFrom(binding.contributedType(), requestingClass.packageName());
    }

    /**
     * If {@code delegateExpression} can be assigned to {@code desiredType} safely, then {@code
     * delegateExpression} is returned unchanged. If the {@code delegateExpression} is already a raw
     * type, returns {@code delegateExpression} as well, as casting would have no effect. Otherwise,
     * returns a {@link Expression#castTo(TypeMirror) casted} version of {@code delegateExpression}
     * to the raw type of {@code desiredType}.
     */
    // TODO(ronshapiro): this probably can be generalized for usage in InjectionMethods
    private Expression castToRawTypeIfNecessary(
            Expression delegateExpression, TypeMirror desiredType) {
        if (types.isAssignable(delegateExpression.type(), desiredType)) {
            return delegateExpression;
        }
        return delegateExpression.castTo(types.erasure(desiredType));
    }

    private enum ScopeKind {//ordinal 序列越在前越强壮
        UNSCOPED,
        SINGLE_CHECK,//如果是Reusable注解，表示singlecheck
        DOUBLE_CHECK,
        ;

        //scope是不是Reusable，如果是则表示SINGLE_CHECK，否则表示DOUBLE_CHECK；UNSCOPED表示不存在scope注解修饰
        static ScopeKind get(Binding binding) {
            return binding
                    .scope()
                    .map(scope -> scope.isReusable() ? SINGLE_CHECK : DOUBLE_CHECK)
                    .orElse(UNSCOPED);
        }

        boolean isStrongerScopeThan(ScopeKind other) {
            return this.ordinal() > other.ordinal();
        }
    }

    @AssistedFactory
    static interface Factory {
        DelegateRequestRepresentation create(
                ContributionBinding binding,
                RequestKind requestKind
        );
    }
}
