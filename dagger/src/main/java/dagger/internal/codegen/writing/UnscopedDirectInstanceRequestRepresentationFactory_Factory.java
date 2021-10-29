package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class UnscopedDirectInstanceRequestRepresentationFactory_Factory implements Factory<UnscopedDirectInstanceRequestRepresentationFactory> {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<AssistedFactoryRequestRepresentation.Factory> assistedFactoryRequestRepresentationFactoryProvider;

    private final Provider<ComponentInstanceRequestRepresentation.Factory> componentInstanceRequestRepresentationFactoryProvider;

    private final Provider<ComponentProvisionRequestRepresentation.Factory> componentProvisionRequestRepresentationFactoryProvider;

    private final Provider<ComponentRequirementRequestRepresentation.Factory> componentRequirementRequestRepresentationFactoryProvider;

    private final Provider<DelegateRequestRepresentation.Factory> delegateRequestRepresentationFactoryProvider;

    private final Provider<MapRequestRepresentation.Factory> mapRequestRepresentationFactoryProvider;

    private final Provider<OptionalRequestRepresentation.Factory> optionalRequestRepresentationFactoryProvider;

    private final Provider<SetRequestRepresentation.Factory> setRequestRepresentationFactoryProvider;

    private final Provider<SimpleMethodRequestRepresentation.Factory> simpleMethodRequestRepresentationFactoryProvider;

    private final Provider<SubcomponentCreatorRequestRepresentation.Factory> subcomponentCreatorRequestRepresentationFactoryProvider;

    public UnscopedDirectInstanceRequestRepresentationFactory_Factory(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<AssistedFactoryRequestRepresentation.Factory> assistedFactoryRequestRepresentationFactoryProvider,
            Provider<ComponentInstanceRequestRepresentation.Factory> componentInstanceRequestRepresentationFactoryProvider,
            Provider<ComponentProvisionRequestRepresentation.Factory> componentProvisionRequestRepresentationFactoryProvider,
            Provider<ComponentRequirementRequestRepresentation.Factory> componentRequirementRequestRepresentationFactoryProvider,
            Provider<DelegateRequestRepresentation.Factory> delegateRequestRepresentationFactoryProvider,
            Provider<MapRequestRepresentation.Factory> mapRequestRepresentationFactoryProvider,
            Provider<OptionalRequestRepresentation.Factory> optionalRequestRepresentationFactoryProvider,
            Provider<SetRequestRepresentation.Factory> setRequestRepresentationFactoryProvider,
            Provider<SimpleMethodRequestRepresentation.Factory> simpleMethodRequestRepresentationFactoryProvider,
            Provider<SubcomponentCreatorRequestRepresentation.Factory> subcomponentCreatorRequestRepresentationFactoryProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
        this.assistedFactoryRequestRepresentationFactoryProvider = assistedFactoryRequestRepresentationFactoryProvider;
        this.componentInstanceRequestRepresentationFactoryProvider = componentInstanceRequestRepresentationFactoryProvider;
        this.componentProvisionRequestRepresentationFactoryProvider = componentProvisionRequestRepresentationFactoryProvider;
        this.componentRequirementRequestRepresentationFactoryProvider = componentRequirementRequestRepresentationFactoryProvider;
        this.delegateRequestRepresentationFactoryProvider = delegateRequestRepresentationFactoryProvider;
        this.mapRequestRepresentationFactoryProvider = mapRequestRepresentationFactoryProvider;
        this.optionalRequestRepresentationFactoryProvider = optionalRequestRepresentationFactoryProvider;
        this.setRequestRepresentationFactoryProvider = setRequestRepresentationFactoryProvider;
        this.simpleMethodRequestRepresentationFactoryProvider = simpleMethodRequestRepresentationFactoryProvider;
        this.subcomponentCreatorRequestRepresentationFactoryProvider = subcomponentCreatorRequestRepresentationFactoryProvider;
    }

    @Override
    public UnscopedDirectInstanceRequestRepresentationFactory get() {
        return newInstance(componentImplementationProvider.get(), assistedFactoryRequestRepresentationFactoryProvider.get(), componentInstanceRequestRepresentationFactoryProvider.get(), componentProvisionRequestRepresentationFactoryProvider.get(), componentRequirementRequestRepresentationFactoryProvider.get(), delegateRequestRepresentationFactoryProvider.get(), mapRequestRepresentationFactoryProvider.get(), optionalRequestRepresentationFactoryProvider.get(), setRequestRepresentationFactoryProvider.get(), simpleMethodRequestRepresentationFactoryProvider.get(), subcomponentCreatorRequestRepresentationFactoryProvider.get());
    }

    public static UnscopedDirectInstanceRequestRepresentationFactory_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<AssistedFactoryRequestRepresentation.Factory> assistedFactoryRequestRepresentationFactoryProvider,
            Provider<ComponentInstanceRequestRepresentation.Factory> componentInstanceRequestRepresentationFactoryProvider,
            Provider<ComponentProvisionRequestRepresentation.Factory> componentProvisionRequestRepresentationFactoryProvider,
            Provider<ComponentRequirementRequestRepresentation.Factory> componentRequirementRequestRepresentationFactoryProvider,
            Provider<DelegateRequestRepresentation.Factory> delegateRequestRepresentationFactoryProvider,
            Provider<MapRequestRepresentation.Factory> mapRequestRepresentationFactoryProvider,
            Provider<OptionalRequestRepresentation.Factory> optionalRequestRepresentationFactoryProvider,
            Provider<SetRequestRepresentation.Factory> setRequestRepresentationFactoryProvider,
            Provider<SimpleMethodRequestRepresentation.Factory> simpleMethodRequestRepresentationFactoryProvider,
            Provider<SubcomponentCreatorRequestRepresentation.Factory> subcomponentCreatorRequestRepresentationFactoryProvider) {
        return new UnscopedDirectInstanceRequestRepresentationFactory_Factory(componentImplementationProvider, assistedFactoryRequestRepresentationFactoryProvider, componentInstanceRequestRepresentationFactoryProvider, componentProvisionRequestRepresentationFactoryProvider, componentRequirementRequestRepresentationFactoryProvider, delegateRequestRepresentationFactoryProvider, mapRequestRepresentationFactoryProvider, optionalRequestRepresentationFactoryProvider, setRequestRepresentationFactoryProvider, simpleMethodRequestRepresentationFactoryProvider, subcomponentCreatorRequestRepresentationFactoryProvider);
    }

    public static UnscopedDirectInstanceRequestRepresentationFactory newInstance(
            ComponentImplementation componentImplementation,
            Object assistedFactoryRequestRepresentationFactory,
            Object componentInstanceRequestRepresentationFactory,
            Object componentProvisionRequestRepresentationFactory,
            Object componentRequirementRequestRepresentationFactory,
            Object delegateRequestRepresentationFactory, Object mapRequestRepresentationFactory,
            Object optionalRequestRepresentationFactory, Object setRequestRepresentationFactory,
            Object simpleMethodRequestRepresentationFactory,
            Object subcomponentCreatorRequestRepresentationFactory) {
        return new UnscopedDirectInstanceRequestRepresentationFactory(componentImplementation, (AssistedFactoryRequestRepresentation.Factory) assistedFactoryRequestRepresentationFactory, (ComponentInstanceRequestRepresentation.Factory) componentInstanceRequestRepresentationFactory, (ComponentProvisionRequestRepresentation.Factory) componentProvisionRequestRepresentationFactory, (ComponentRequirementRequestRepresentation.Factory) componentRequirementRequestRepresentationFactory, (DelegateRequestRepresentation.Factory) delegateRequestRepresentationFactory, (MapRequestRepresentation.Factory) mapRequestRepresentationFactory, (OptionalRequestRepresentation.Factory) optionalRequestRepresentationFactory, (SetRequestRepresentation.Factory) setRequestRepresentationFactory, (SimpleMethodRequestRepresentation.Factory) simpleMethodRequestRepresentationFactory, (SubcomponentCreatorRequestRepresentation.Factory) subcomponentCreatorRequestRepresentationFactory);
    }
}
