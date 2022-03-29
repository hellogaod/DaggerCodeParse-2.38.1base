package dagger.internal.codegen.writing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.FrameworkType;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.producers.internal.Producers;
import dagger.spi.model.Key;

/**
 * Binding expression for producer node instances.
 * 1. 当前被key匹配上的是ProvisionBinding对象，当前key的type是Producer< T>类型；
 * - （1）当前被key匹配上的是ProvisionBinding对象作为参数；（2）ProducerFromProviderCreationExpression作为参数；
 * 2. 当前被key匹配的是ProductionBinding对象，并且当前的key的type使用了FrameworkType（Provider< T>或Producer< T>类型）包裹：
 * - （1）当前被key匹配的ProductionBinding对象作为参数；
 * - （2）FrameworkInstanceSupplier对象作为参数，该对象来源 - 判断当前ProductionBinding对象没有依赖 && ProductionBinding对象没有使用Scope注解限定范围：
 * - ① 满足条件，再根据当前ProductionBinding对象BindingKind类型判断：
 * - a. key匹配上的是@Multibinds修饰的bindingMethod方法或@Provides或@Produces或@Binds修饰的bindingMethod，该bindingMethod还是用了@IntoMap或@IntoSet或@ElementsIntoSet：ParameterizedStaticMethod对象作为实际参数 ;
 * - b.除了条件a外的其他条件:StaticMethod对象作为实际参数；
 * - ② 条件不满足：使用FrameworkFieldInitializer对象作为实际参数，该参数对象使用FrameworkInstanceCreationExpression对象作为参数，FrameworkInstanceCreationExpression对象根据是否使用了Scope注解，将表达式使用SingleCheck.provider或DoubleCheck.provider包裹，FrameworkInstanceCreationExpression对象实现类来源 - 根据当前ProductionBinding对象的BindingKind类型判断：
 * - a.component节点是production的componentAnnotation#dependencies里面的节点的无参返回类型不是void的方法生成ProductionBinding对象：DependencyMethodProducerCreationExpression作为实际参数；
 * - b.Produces修饰的方法生成的ProductionBinding对象：ProducerCreationExpression作为实现参数；
 * - c.如果key及其变异匹配上（1）@Provides或@Produces或@Binds修饰的bindingMethod，该bindingMethod还是用了@IntoMap或@IntoSet或@ElementsIntoSet;（2）@Multibinds修饰的bindingMethod方法。该key的type是Set< T>，那么生成的Binding对象:SetFactoryCreationExpression作为实现参数；
 * - d.如果key及其变异匹配上（1）@Provides或@Produces或@Binds修饰的bindingMethod，该bindingMethod还是用了@IntoMap或@IntoSet或@ElementsIntoSet;（2）@Multibinds修饰的bindingMethod方法。该key的type是Map<K,V>，那么生成的Binding对象:MapFactoryCreationExpression作为实现参数；
 */
final class ProducerNodeInstanceRequestRepresentation
        extends FrameworkInstanceRequestRepresentation {
    private final ComponentImplementation.ShardImplementation shardImplementation;
    private final Key key;
    private final ProducerEntryPointView producerEntryPointView;

    @AssistedInject
    ProducerNodeInstanceRequestRepresentation(
            @Assisted ContributionBinding binding,
            @Assisted FrameworkInstanceSupplier frameworkInstanceSupplier,
            DaggerTypes types,
            DaggerElements elements,
            ComponentImplementation componentImplementation) {
        super(binding, frameworkInstanceSupplier, types, elements);
        this.shardImplementation = componentImplementation.shardImplementation(binding);
        this.key = binding.key();
        this.producerEntryPointView = new ProducerEntryPointView(shardImplementation, types);
    }

    @Override
    protected FrameworkType frameworkType() {
        return FrameworkType.PRODUCER_NODE;
    }

    @Override
    Expression getDependencyExpression(ClassName requestingClass) {
        Expression result = super.getDependencyExpression(requestingClass);
        shardImplementation.addCancellation(
                key,
                CodeBlock.of(
                        "$T.cancel($L, $N);",
                        Producers.class,
                        result.codeBlock(),
                        ComponentImplementation.MAY_INTERRUPT_IF_RUNNING_PARAM));
        return result;
    }

    @Override
    Expression getDependencyExpressionForComponentMethod(
            ComponentDescriptor.ComponentMethodDescriptor componentMethod, ComponentImplementation component) {
        return producerEntryPointView
                .getProducerEntryPointField(this, componentMethod, component.name())
                .orElseGet(
                        () -> super.getDependencyExpressionForComponentMethod(componentMethod, component));
    }

    @AssistedFactory
    static interface Factory {
        ProducerNodeInstanceRequestRepresentation create(
                ContributionBinding binding,
                FrameworkInstanceSupplier frameworkInstanceSupplier
        );
    }
}
