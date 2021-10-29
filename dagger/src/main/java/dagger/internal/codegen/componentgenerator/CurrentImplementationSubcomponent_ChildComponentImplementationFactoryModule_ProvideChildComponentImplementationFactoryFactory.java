package dagger.internal.codegen.componentgenerator;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.codegen.writing.ComponentImplementation;
import dagger.internal.codegen.writing.ComponentRequestRepresentations;
import dagger.internal.codegen.writing.ComponentRequirementExpressions;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class CurrentImplementationSubcomponent_ChildComponentImplementationFactoryModule_ProvideChildComponentImplementationFactoryFactory implements Factory<ComponentImplementation.ChildComponentImplementationFactory> {
    private final Provider<CurrentImplementationSubcomponent.Builder> currentImplementationSubcomponentBuilderProvider;

    private final Provider<ComponentImplementation> componentImplementatationProvider;

    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    private final Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider;

    public CurrentImplementationSubcomponent_ChildComponentImplementationFactoryModule_ProvideChildComponentImplementationFactoryFactory(
            Provider<CurrentImplementationSubcomponent.Builder> currentImplementationSubcomponentBuilderProvider,
            Provider<ComponentImplementation> componentImplementatationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider) {
        this.currentImplementationSubcomponentBuilderProvider = currentImplementationSubcomponentBuilderProvider;
        this.componentImplementatationProvider = componentImplementatationProvider;
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
        this.componentRequirementExpressionsProvider = componentRequirementExpressionsProvider;
    }

    @Override
    public ComponentImplementation.ChildComponentImplementationFactory get() {
        return provideChildComponentImplementationFactory(currentImplementationSubcomponentBuilderProvider.get(), componentImplementatationProvider, componentRequestRepresentationsProvider, componentRequirementExpressionsProvider);
    }

    public static CurrentImplementationSubcomponent_ChildComponentImplementationFactoryModule_ProvideChildComponentImplementationFactoryFactory create(
            Provider<CurrentImplementationSubcomponent.Builder> currentImplementationSubcomponentBuilderProvider,
            Provider<ComponentImplementation> componentImplementatationProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider) {
        return new CurrentImplementationSubcomponent_ChildComponentImplementationFactoryModule_ProvideChildComponentImplementationFactoryFactory(currentImplementationSubcomponentBuilderProvider, componentImplementatationProvider, componentRequestRepresentationsProvider, componentRequirementExpressionsProvider);
    }

    public static ComponentImplementation.ChildComponentImplementationFactory provideChildComponentImplementationFactory(
            CurrentImplementationSubcomponent.Builder currentImplementationSubcomponentBuilder,
            Provider<ComponentImplementation> componentImplementatation,
            Provider<ComponentRequestRepresentations> componentRequestRepresentations,
            Provider<ComponentRequirementExpressions> componentRequirementExpressions) {
        return Preconditions.checkNotNullFromProvides(CurrentImplementationSubcomponent.ChildComponentImplementationFactoryModule.provideChildComponentImplementationFactory(currentImplementationSubcomponentBuilder, componentImplementatation, componentRequestRepresentations, componentRequirementExpressions));
    }
}
