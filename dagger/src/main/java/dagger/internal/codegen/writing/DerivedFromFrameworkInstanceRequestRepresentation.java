package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.FrameworkType;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.BindingRequest.bindingRequest;

/**
 * A binding expression that depends on a framework instance.
 * 1. 当前被key匹配上的是ProvisionBinding对象，key的type如果是Lazy< T>或Produced< T>或Provider<Lazy< T>>;
 * - 当前key和FrameworkType.PROVIDER作为参数；
 * 2. 当前被key匹配上的是ProductionBinding对象，并且key没有使用FrameworkType(Provider< T>或Producer< T>类型)包裹；
 * - 当前key和FrameworkType.PRODUCER_NODE作为参数；
 * 3. 当前被key匹配上的是ProvisionBinding对象，并且当前key的Requestkind是INSTANCE类型，当前被key匹配上的是ProvisionBinding对象的BindingKind类型：（1）MEMBERS_INJECTOR（当前ProvisionBinding对象是MembersInjector< T>的T类型生成MembersInjectionBinding然后转换成ProvisionBinding对象的）；（2）不是ASSISTED_INJECTION（ProvisionBinding对象不是@AssistedInject修饰的构造函数生成）；（3）ProvisionBinding对象使用Scope注解修饰或者当前ProvisionBinding对象是@AssistedFactory修饰的节点生成；
 * - 参数binding.key(), RequestKind.INSTANCE生成BindingRequest对象；FrameworkType.PROVIDER；
 */
final class DerivedFromFrameworkInstanceRequestRepresentation extends RequestRepresentation {
    private final BindingRequest bindingRequest;
    private final BindingRequest frameworkRequest;
    private final FrameworkType frameworkType;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerTypes types;

    @AssistedInject
    DerivedFromFrameworkInstanceRequestRepresentation(
            @Assisted BindingRequest bindingRequest,
            @Assisted FrameworkType frameworkType,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types) {
        this.bindingRequest = checkNotNull(bindingRequest);
        this.frameworkType = checkNotNull(frameworkType);
        this.frameworkRequest = bindingRequest(bindingRequest.key(), frameworkType);
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.types = types;
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {
        return frameworkType.to(
                bindingRequest.requestKind(),
                componentRequestRepresentations.getDependencyExpression(frameworkRequest, requestingClass),
                types);
    }

    @Override
    Expression getDependencyExpressionForComponentMethod(
            ComponentDescriptor.ComponentMethodDescriptor componentMethod, ComponentImplementation component) {
        Expression frameworkInstance =
                componentRequestRepresentations.getDependencyExpressionForComponentMethod(
                        frameworkRequest, componentMethod, component);
        return frameworkType.to(bindingRequest.requestKind(), frameworkInstance, types);
    }

    @AssistedFactory
    static interface Factory {
        DerivedFromFrameworkInstanceRequestRepresentation create(
                BindingRequest request, FrameworkType frameworkType);
    }

}
