package dagger.internal.codegen.writing;

import com.squareup.javapoet.CodeBlock;

import javax.inject.Inject;

import dagger.internal.codegen.binding.ComponentRequirement;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;


/**
 * A factory for creating unscoped creation expressions for a provision or production binding.
 *
 * <p>A creation expression is responsible for creating the factory for a given binding (e.g. by
 * calling the generated factory create method, {@code Foo_Factory.create(...)}). Note that this
 * class does not handle scoping of these factories (e.g. wrapping in {@code
 * DoubleCheck.provider()}).
 */
final class UnscopedFrameworkInstanceCreationExpressionFactory {
    private final ComponentImplementation componentImplementation;
    private final ComponentRequirementExpressions componentRequirementExpressions;
    private final AnonymousProviderCreationExpression.Factory
            anonymousProviderCreationExpressionFactory;
    private final DelegatingFrameworkInstanceCreationExpression.Factory
            delegatingFrameworkInstanceCreationExpressionFactory;
    private final DependencyMethodProducerCreationExpression.Factory
            dependencyMethodProducerCreationExpressionFactory;
    private final DependencyMethodProviderCreationExpression.Factory
            dependencyMethodProviderCreationExpressionFactory;
    private final InjectionOrProvisionProviderCreationExpression.Factory
            injectionOrProvisionProviderCreationExpressionFactory;
    private final MapFactoryCreationExpression.Factory mapFactoryCreationExpressionFactory;
    private final MembersInjectorProviderCreationExpression.Factory
            membersInjectorProviderCreationExpressionFactory;
    private final OptionalFactoryInstanceCreationExpression.Factory
            optionalFactoryInstanceCreationExpressionFactory;
    private final ProducerCreationExpression.Factory producerCreationExpressionFactory;
    private final SetFactoryCreationExpression.Factory setFactoryCreationExpressionFactory;

    @Inject
    UnscopedFrameworkInstanceCreationExpressionFactory(
            ComponentImplementation componentImplementation,
            ComponentRequirementExpressions componentRequirementExpressions,
            AnonymousProviderCreationExpression.Factory anonymousProviderCreationExpressionFactory,
            DelegatingFrameworkInstanceCreationExpression.Factory
                    delegatingFrameworkInstanceCreationExpressionFactory,
            DependencyMethodProducerCreationExpression.Factory
                    dependencyMethodProducerCreationExpressionFactory,
            DependencyMethodProviderCreationExpression.Factory
                    dependencyMethodProviderCreationExpressionFactory,
            InjectionOrProvisionProviderCreationExpression.Factory
                    injectionOrProvisionProviderCreationExpressionFactory,
            MapFactoryCreationExpression.Factory mapFactoryCreationExpressionFactory,
            MembersInjectorProviderCreationExpression.Factory
                    membersInjectorProviderCreationExpressionFactory,
            OptionalFactoryInstanceCreationExpression.Factory
                    optionalFactoryInstanceCreationExpressionFactory,
            ProducerCreationExpression.Factory producerCreationExpressionFactory,
            SetFactoryCreationExpression.Factory setFactoryCreationExpressionFactory) {
        this.componentImplementation = componentImplementation;
        this.componentRequirementExpressions = componentRequirementExpressions;
        this.anonymousProviderCreationExpressionFactory = anonymousProviderCreationExpressionFactory;
        this.delegatingFrameworkInstanceCreationExpressionFactory =
                delegatingFrameworkInstanceCreationExpressionFactory;
        this.dependencyMethodProducerCreationExpressionFactory =
                dependencyMethodProducerCreationExpressionFactory;
        this.dependencyMethodProviderCreationExpressionFactory =
                dependencyMethodProviderCreationExpressionFactory;
        this.injectionOrProvisionProviderCreationExpressionFactory =
                injectionOrProvisionProviderCreationExpressionFactory;
        this.mapFactoryCreationExpressionFactory = mapFactoryCreationExpressionFactory;
        this.membersInjectorProviderCreationExpressionFactory =
                membersInjectorProviderCreationExpressionFactory;
        this.optionalFactoryInstanceCreationExpressionFactory =
                optionalFactoryInstanceCreationExpressionFactory;
        this.producerCreationExpressionFactory = producerCreationExpressionFactory;
        this.setFactoryCreationExpressionFactory = setFactoryCreationExpressionFactory;
    }

    /**
     * Returns an unscoped creation expression for a {@link javax.inject.Provider} for provision
     * bindings or a {@link dagger.producers.Producer} for production bindings.
     */
    FrameworkFieldInitializer.FrameworkInstanceCreationExpression create(ContributionBinding binding) {
        switch (binding.kind()) {
            case COMPONENT:
                // The cast can be removed when we drop java 7 source support
                return new InstanceFactoryCreationExpression(
                        () ->
                                CodeBlock.of(
                                        "($T) $L",
                                        binding.key().type().java(),
                                        componentImplementation.componentFieldReference()));

            case BOUND_INSTANCE:
                return instanceFactoryCreationExpression(
                        binding, ComponentRequirement.forBoundInstance(binding));

            case COMPONENT_DEPENDENCY:
                return instanceFactoryCreationExpression(
                        binding, ComponentRequirement.forDependency(binding.key().type().java()));

            case COMPONENT_PROVISION:
                return dependencyMethodProviderCreationExpressionFactory.create(binding);

            case SUBCOMPONENT_CREATOR:
                return anonymousProviderCreationExpressionFactory.create(binding);

            case ASSISTED_FACTORY:
            case ASSISTED_INJECTION:
            case INJECTION:
            case PROVISION:
                return injectionOrProvisionProviderCreationExpressionFactory.create(binding);

            case COMPONENT_PRODUCTION:
                return dependencyMethodProducerCreationExpressionFactory.create(binding);

            case PRODUCTION:
                return producerCreationExpressionFactory.create(binding);

            case MULTIBOUND_SET:
                return setFactoryCreationExpressionFactory.create(binding);

            case MULTIBOUND_MAP:
                return mapFactoryCreationExpressionFactory.create(binding);

            case DELEGATE:
                return delegatingFrameworkInstanceCreationExpressionFactory.create(binding);

            case OPTIONAL:
                return optionalFactoryInstanceCreationExpressionFactory.create(binding);

            case MEMBERS_INJECTOR:
                return membersInjectorProviderCreationExpressionFactory.create((ProvisionBinding) binding);

            default:
                throw new AssertionError(binding);
        }
    }

    private InstanceFactoryCreationExpression instanceFactoryCreationExpression(
            ContributionBinding binding, ComponentRequirement componentRequirement) {
        return new InstanceFactoryCreationExpression(
                binding.nullableType().isPresent(),
                () ->
                        componentRequirementExpressions.getExpressionDuringInitialization(
                                componentRequirement, componentImplementation.name()));
    }
}
