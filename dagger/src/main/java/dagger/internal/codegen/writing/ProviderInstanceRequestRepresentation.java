package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.FrameworkType;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Binding expression for provider instances.
 * <p>
 * <p>
 * 当前被key匹配上的是ProvisionBinding对象，并且该ProvisionBinding对象（1）不是@Binds修饰的bindingMethod生成的；（2）或者使用了@Scope注解修饰的注解；
 * - 当前ProvisionBinding对象和FrameworkInstanceSupplier对象作为参数；
 * FrameworkInstanceSupplier对象来源：
 * 1. 如果（当前ProvisionBinding对象是Provides修饰的bindingMethod生成并且该方法没有参数并且所在module节点不需要实例化 || Inject修饰的构造函数（或MULTIBOUND_SET或MULTIBOUND_MAP）并且没有参数依赖） && ProvisionBinding对象没有Scope注解修饰；
 * - ① 如果key及其变异匹配上（1）@Provides或@Produces或@Binds修饰的bindingMethod，该bindingMethod还是用了@IntoMap或@IntoSet或@ElementsIntoSet；（2）@Multibinds修饰的bindingMethod方法 ： ParameterizedStaticMethod对象作为实际参数；
 * - ② 如果key及其变异匹配上Inject修饰的构造函数或Provides修饰的bindingMethod方法，并且该方法使用了泛型：ParameterizedStaticMethod作为实际参数；
 * - 除了①和②的情况：StaticMethod作为实际参数；
 * 2. 如果条件1不满足，或者条件1没有没有找到实际参数，那么使用FrameworkFieldInitializer作为实际参数，该参数会返回FrameworkInstanceCreationExpression作为参数，该参数来源：
 * - （1）①component节点生成一个ProvisionBinding绑定对象，②@BindsInstance修饰的方法或方法参数，③componentAnnotation#dependencies()里面的dependency节点：InstanceFactoryCreationExpression作为实际参数；
 * - （2）component节点不是production的componentAnnotation#dependencies里面的类的无参返回类型不是void的方法生成的ProvisionBinding：DependencyMethodProviderCreationExpression作为实际参数；
 * - （3）① component中的方法返回类型是一个subcomponent.Builder（表示的是一个Builder）,并且该subcomponent不在component关联的subcomponents集合中， 那么使用当前方法和该方法所在的component类生成一个ProvisionBinding对象；②key及其变异 匹配上component关联的module类的注解moduleAnnotation#subcomponents()里面的类生成的SubcomponentDeclaration，生成ProvisionBinding对象AnonymousProviderCreationExpression作为实际参数；
 * - （4）ASSISTED_FACTORY、ASSISTED_INJECTION、INJECTION、PROVISION：InjectionOrProvisionProviderCreationExpression作为实际参数；
 * - （5）MULTIBOUND_SET：SetFactoryCreationExpression作为实际参数；
 * - （6）MULTIBOUND_MAP：MapFactoryCreationExpression作为实际参数；
 * - （7）DELEGATE：DelegatingFrameworkInstanceCreationExpression作为实际参数；
 * - （8）OPTIONAL：OptionalFactoryInstanceCreationExpression作为实际参数；
 * - （9）MEMBERS_INJECTOR：MembersInjectorProviderCreationExpression作为实际参数；
 */
final class ProviderInstanceRequestRepresentation extends FrameworkInstanceRequestRepresentation {

    @AssistedInject
    ProviderInstanceRequestRepresentation(
            @Assisted ContributionBinding binding,
            @Assisted FrameworkInstanceSupplier frameworkInstanceSupplier,
            DaggerTypes types,
            DaggerElements elements) {
        super(binding, frameworkInstanceSupplier, types, elements);
    }

    @Override
    protected FrameworkType frameworkType() {
        return FrameworkType.PROVIDER;
    }

    @AssistedFactory
    static interface Factory {
        ProviderInstanceRequestRepresentation create(
                ContributionBinding binding,
                FrameworkInstanceSupplier frameworkInstanceSupplier
        );
    }
}
