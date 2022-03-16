package dagger.internal.codegen.writing;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.binding.Binding;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.MembersInjectionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.Key;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static dagger.internal.codegen.base.Util.reentrantComputeIfAbsent;
import static dagger.internal.codegen.langmodel.Accessibility.isTypeAccessibleFrom;
import static dagger.internal.codegen.writing.ComponentImplementation.MethodSpecKind.MEMBERS_INJECTION_METHOD;
import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * Manages the member injection methods for a component.
 */
@PerComponentImplementation
final class MembersInjectionMethods {

    private final Map<Key, Expression> injectMethodExpressions = new LinkedHashMap<>();
    private final ComponentImplementation componentImplementation;
    private final ComponentRequestRepresentations bindingExpressions;
    private final BindingGraph graph;
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    MembersInjectionMethods(
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations bindingExpressions,
            BindingGraph graph,
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil metadataUtil) {
        this.componentImplementation = checkNotNull(componentImplementation);
        this.bindingExpressions = checkNotNull(bindingExpressions);
        this.graph = checkNotNull(graph);
        this.elements = checkNotNull(elements);
        this.types = checkNotNull(types);
        this.metadataUtil = metadataUtil;
    }

    /**
     * Returns the members injection {@link Expression} for the given {@link Key}, creating it if
     * necessary.
     */
    Expression getInjectExpression(Key key, CodeBlock instance, ClassName requestingClass) {

        Binding binding =
                graph.membersInjectionBinding(key).isPresent()
                        ? graph.membersInjectionBinding(key).get()
                        : graph.contributionBinding(key);

        //核心部件
        Expression expression =
                reentrantComputeIfAbsent(

                        injectMethodExpressions, key, k -> injectMethodExpression(binding, requestingClass));

        //如果是3500个绑定对象范围内表示的就是一个ShardImplementation对象
        ComponentImplementation.ShardImplementation shardImplementation = componentImplementation.shardImplementation(binding);

        //e.g.Expression.codeBlock表示新生成的Component类的重写inject方法的代码片段： injectComponentProcessor(processor);
        return Expression.create(
                expression.type(),
                shardImplementation.name().equals(requestingClass)
                        ? CodeBlock.of("$L($L)", expression.codeBlock(), instance)
                        : CodeBlock.of(
                        "$L.$L($L)",
                        shardImplementation.shardFieldReference(),
                        expression.codeBlock(),
                        instance));
    }

    //e.g.在Component类中生成injectComponentProcessor，并且该方法内部的实现
    private Expression injectMethodExpression(Binding binding, ClassName requestingClass) {

        ComponentImplementation.ShardImplementation shardImplementation = componentImplementation.shardImplementation(binding);

        TypeMirror keyType = binding.key().type().java();

        //判断当前keytype是否可以被新生成的component类所在包访问，不可以则使用Object替代
        TypeMirror membersInjectedType =
                isTypeAccessibleFrom(keyType, shardImplementation.name().packageName())
                        ? keyType
                        : elements.getTypeElement(Object.class).asType();

        TypeName membersInjectedTypeName = TypeName.get(membersInjectedType);
        Name bindingTypeName = binding.bindingTypeElement().get().getSimpleName();
        // TODO(ronshapiro): include type parameters in this name e.g. injectFooOfT, and outer class
        // simple names Foo.Builder -> injectFooBuilder
        String methodName = shardImplementation.getUniqueMethodName("inject" + bindingTypeName);
        ParameterSpec parameter = ParameterSpec.builder(membersInjectedTypeName, "instance").build();

        MethodSpec.Builder methodBuilder =
                methodBuilder(methodName)
                        .addModifiers(PRIVATE)
                        .returns(membersInjectedTypeName)
                        .addParameter(parameter);

        TypeElement canIgnoreReturnValue =
                elements.getTypeElement("com.google.errorprone.annotations.CanIgnoreReturnValue");
        if (canIgnoreReturnValue != null) {
            methodBuilder.addAnnotation(ClassName.get(canIgnoreReturnValue));
        }
        CodeBlock instance = CodeBlock.of("$N", parameter);

        //核心：方法的代码实现
        methodBuilder.addCode(

                InjectionMethods.InjectionSiteMethod.invokeAll(
                        injectionSites(binding),//表示绑定对象中使用Inject修饰的普通方法或变量
                        shardImplementation.name(),//当前生成的component类
                        instance,//赋值对象
                        membersInjectedType,//赋值对象的参数类型
                        //函数式
                        request ->
                                //这才是延伸到后面需要的方法的重头戏
                                //e.g.
                                //1.InjectBindingRegistry
                                bindingExpressions
                                        .getDependencyArgumentExpression(request, shardImplementation.name())
                                        .codeBlock(),
                        types,
                        metadataUtil));

        //return instance;
        methodBuilder.addStatement("return $L", instance);

        MethodSpec method = methodBuilder.build();
        shardImplementation.addMethod(MEMBERS_INJECTION_METHOD, method);
        return Expression.create(membersInjectedType, CodeBlock.of("$N", method));
    }

    //使用Inject修饰的变量或普通方法
    private static ImmutableSet<MembersInjectionBinding.InjectionSite> injectionSites(Binding binding) {
        if (binding instanceof ProvisionBinding) {
            return ((ProvisionBinding) binding).injectionSites();
        } else if (binding instanceof MembersInjectionBinding) {
            return ((MembersInjectionBinding) binding).injectionSites();
        }
        throw new IllegalArgumentException(binding.key().toString());
    }

}
