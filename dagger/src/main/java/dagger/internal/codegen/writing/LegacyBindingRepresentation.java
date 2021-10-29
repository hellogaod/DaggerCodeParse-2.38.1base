package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.Binding;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: LegacyBindingRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 10:03
 * Description:
 * History:
 */
class LegacyBindingRepresentation {

    private final BindingGraph graph;
    private final boolean isFastInit;
    private final Binding binding;
    private final ComponentImplementation componentImplementation;
    private final ComponentMethodRequestRepresentation.Factory
            componentMethodRequestRepresentationFactory;
    private final DelegateRequestRepresentation.Factory delegateRequestRepresentationFactory;
    private final DerivedFromFrameworkInstanceRequestRepresentation.Factory
            derivedFromFrameworkInstanceRequestRepresentationFactory;
    private final ImmediateFutureRequestRepresentation.Factory
            immediateFutureRequestRepresentationFactory;
    private final MembersInjectionRequestRepresentation.Factory
            membersInjectionRequestRepresentationFactory;
    private final PrivateMethodRequestRepresentation.Factory
            privateMethodRequestRepresentationFactory;
    private final AssistedPrivateMethodRequestRepresentation.Factory
            assistedPrivateMethodRequestRepresentationFactory;
    private final ProducerNodeInstanceRequestRepresentation.Factory
            producerNodeInstanceRequestRepresentationFactory;
    private final ProviderInstanceRequestRepresentation.Factory
            providerInstanceRequestRepresentationFactory;
    private final UnscopedDirectInstanceRequestRepresentationFactory
            unscopedDirectInstanceRequestRepresentationFactory;
    private final ProducerFromProviderCreationExpression.Factory
            producerFromProviderCreationExpressionFactory;
    private final UnscopedFrameworkInstanceCreationExpressionFactory
            unscopedFrameworkInstanceCreationExpressionFactory;
    private final SwitchingProviders switchingProviders;

    @AssistedInject
    LegacyBindingRepresentation(
            @Assisted boolean isFastInit,
            @Assisted Binding binding,
            @Assisted SwitchingProviders switchingProviders,
            BindingGraph graph,
            ComponentImplementation componentImplementation,
            ComponentMethodRequestRepresentation.Factory componentMethodRequestRepresentationFactory,
            DelegateRequestRepresentation.Factory delegateRequestRepresentationFactory,
            DerivedFromFrameworkInstanceRequestRepresentation.Factory
                    derivedFromFrameworkInstanceRequestRepresentationFactory,
            ImmediateFutureRequestRepresentation.Factory immediateFutureRequestRepresentationFactory,
            MembersInjectionRequestRepresentation.Factory membersInjectionRequestRepresentationFactory,
            PrivateMethodRequestRepresentation.Factory privateMethodRequestRepresentationFactory,
            AssistedPrivateMethodRequestRepresentation.Factory
                    assistedPrivateMethodRequestRepresentationFactory,
            ProducerNodeInstanceRequestRepresentation.Factory
                    producerNodeInstanceRequestRepresentationFactory,
            ProviderInstanceRequestRepresentation.Factory providerInstanceRequestRepresentationFactory,
            UnscopedDirectInstanceRequestRepresentationFactory
                    unscopedDirectInstanceRequestRepresentationFactory,
            ProducerFromProviderCreationExpression.Factory producerFromProviderCreationExpressionFactory,
            UnscopedFrameworkInstanceCreationExpressionFactory
                    unscopedFrameworkInstanceCreationExpressionFactory,
            DaggerTypes types) {
        this.isFastInit = isFastInit;
        this.binding = binding;
        this.switchingProviders = switchingProviders;
        this.graph = graph;
        this.componentImplementation = componentImplementation;
        this.componentMethodRequestRepresentationFactory = componentMethodRequestRepresentationFactory;
        this.delegateRequestRepresentationFactory = delegateRequestRepresentationFactory;
        this.derivedFromFrameworkInstanceRequestRepresentationFactory =
                derivedFromFrameworkInstanceRequestRepresentationFactory;
        this.immediateFutureRequestRepresentationFactory = immediateFutureRequestRepresentationFactory;
        this.membersInjectionRequestRepresentationFactory =
                membersInjectionRequestRepresentationFactory;
        this.privateMethodRequestRepresentationFactory = privateMethodRequestRepresentationFactory;
        this.producerNodeInstanceRequestRepresentationFactory =
                producerNodeInstanceRequestRepresentationFactory;
        this.providerInstanceRequestRepresentationFactory =
                providerInstanceRequestRepresentationFactory;
        this.unscopedDirectInstanceRequestRepresentationFactory =
                unscopedDirectInstanceRequestRepresentationFactory;
        this.producerFromProviderCreationExpressionFactory =
                producerFromProviderCreationExpressionFactory;
        this.unscopedFrameworkInstanceCreationExpressionFactory =
                unscopedFrameworkInstanceCreationExpressionFactory;
        this.assistedPrivateMethodRequestRepresentationFactory =
                assistedPrivateMethodRequestRepresentationFactory;
    }

    @AssistedFactory
    static interface Factory {
        LegacyBindingRepresentation create(
                boolean isFastInit,
                Binding binding,
                SwitchingProviders switchingProviders
        );
    }
}
