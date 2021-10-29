package dagger.internal.codegen.writing;

import javax.inject.Inject;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: UnscopedFrameworkInstanceCreationExpressionFactory
 * Author: 佛学徒
 * Date: 2021/10/25 11:27
 * Description:
 * History:
 */
class UnscopedFrameworkInstanceCreationExpressionFactory {
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
}
