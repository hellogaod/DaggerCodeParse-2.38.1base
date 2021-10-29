package dagger.internal.codegen.writing;

import javax.inject.Inject;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: UnscopedDirectInstanceRequestRepresentationFactory
 * Author: 佛学徒
 * Date: 2021/10/25 11:14
 * Description:
 * History:
 */
class UnscopedDirectInstanceRequestRepresentationFactory {

    private final AssistedFactoryRequestRepresentation.Factory
            assistedFactoryRequestRepresentationFactory;
    private final ComponentInstanceRequestRepresentation.Factory
            componentInstanceRequestRepresentationFactory;
    private final ComponentProvisionRequestRepresentation.Factory
            componentProvisionRequestRepresentationFactory;
    private final ComponentRequirementRequestRepresentation.Factory
            componentRequirementRequestRepresentationFactory;
    private final DelegateRequestRepresentation.Factory delegateRequestRepresentationFactory;
    private final MapRequestRepresentation.Factory mapRequestRepresentationFactory;
    private final OptionalRequestRepresentation.Factory optionalRequestRepresentationFactory;
    private final SetRequestRepresentation.Factory setRequestRepresentationFactory;
    private final SimpleMethodRequestRepresentation.Factory simpleMethodRequestRepresentationFactory;
    private final SubcomponentCreatorRequestRepresentation.Factory
            subcomponentCreatorRequestRepresentationFactory;

    @Inject
    UnscopedDirectInstanceRequestRepresentationFactory(
            ComponentImplementation componentImplementation,
            AssistedFactoryRequestRepresentation.Factory assistedFactoryRequestRepresentationFactory,
            ComponentInstanceRequestRepresentation.Factory componentInstanceRequestRepresentationFactory,
            ComponentProvisionRequestRepresentation.Factory
                    componentProvisionRequestRepresentationFactory,
            ComponentRequirementRequestRepresentation.Factory
                    componentRequirementRequestRepresentationFactory,
            DelegateRequestRepresentation.Factory delegateRequestRepresentationFactory,
            MapRequestRepresentation.Factory mapRequestRepresentationFactory,
            OptionalRequestRepresentation.Factory optionalRequestRepresentationFactory,
            SetRequestRepresentation.Factory setRequestRepresentationFactory,
            SimpleMethodRequestRepresentation.Factory simpleMethodRequestRepresentationFactory,
            SubcomponentCreatorRequestRepresentation.Factory
                    subcomponentCreatorRequestRepresentationFactory) {
        this.assistedFactoryRequestRepresentationFactory = assistedFactoryRequestRepresentationFactory;
        this.componentInstanceRequestRepresentationFactory =
                componentInstanceRequestRepresentationFactory;
        this.componentProvisionRequestRepresentationFactory =
                componentProvisionRequestRepresentationFactory;
        this.componentRequirementRequestRepresentationFactory =
                componentRequirementRequestRepresentationFactory;
        this.delegateRequestRepresentationFactory = delegateRequestRepresentationFactory;
        this.mapRequestRepresentationFactory = mapRequestRepresentationFactory;
        this.optionalRequestRepresentationFactory = optionalRequestRepresentationFactory;
        this.setRequestRepresentationFactory = setRequestRepresentationFactory;
        this.simpleMethodRequestRepresentationFactory = simpleMethodRequestRepresentationFactory;
        this.subcomponentCreatorRequestRepresentationFactory =
                subcomponentCreatorRequestRepresentationFactory;
    }
}
