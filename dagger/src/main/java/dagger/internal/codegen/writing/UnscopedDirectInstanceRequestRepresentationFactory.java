package dagger.internal.codegen.writing;

import java.util.Optional;

import javax.inject.Inject;

import dagger.internal.codegen.binding.ComponentRequirement;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.spi.model.RequestKind;

/**
 * A factory for creating a binding expression for an unscoped instance.
 *
 * <p>Note that these binding expressions are for getting "direct" instances -- i.e. instances that
 * are created via constructors or modules (e.g. {@code new Foo()} or {@code
 * FooModule.provideFoo()}) as opposed to an instance created from calling a getter on a framework
 * type (e.g. {@code fooProvider.get()}). See {@link FrameworkInstanceRequestRepresentation} for
 * binding expressions that are created from framework types.
 */
final class UnscopedDirectInstanceRequestRepresentationFactory {
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

    /** Returns a direct, unscoped binding expression for a {@link RequestKind#INSTANCE} request. */
    Optional<RequestRepresentation> create(ContributionBinding binding) {
        switch (binding.kind()) {
            case DELEGATE:
                return Optional.of(
                        delegateRequestRepresentationFactory.create(binding, RequestKind.INSTANCE));

            case COMPONENT:
                return Optional.of(componentInstanceRequestRepresentationFactory.create(binding));

            case COMPONENT_DEPENDENCY:
                return Optional.of(
                        componentRequirementRequestRepresentationFactory.create(
                                binding, ComponentRequirement.forDependency(binding.key().type().java())));

            case COMPONENT_PROVISION:
                return Optional.of(
                        componentProvisionRequestRepresentationFactory.create((ProvisionBinding) binding));

            case SUBCOMPONENT_CREATOR:
                return Optional.of(subcomponentCreatorRequestRepresentationFactory.create(binding));

            case MULTIBOUND_SET:
                return Optional.of(setRequestRepresentationFactory.create((ProvisionBinding) binding));

            case MULTIBOUND_MAP:
                return Optional.of(mapRequestRepresentationFactory.create((ProvisionBinding) binding));

            case OPTIONAL:
                return Optional.of(optionalRequestRepresentationFactory.create((ProvisionBinding) binding));

            case BOUND_INSTANCE:
                return Optional.of(
                        componentRequirementRequestRepresentationFactory.create(
                                binding, ComponentRequirement.forBoundInstance(binding)));

            case ASSISTED_FACTORY:
                return Optional.of(
                        assistedFactoryRequestRepresentationFactory.create((ProvisionBinding) binding));

            case ASSISTED_INJECTION:
            case INJECTION://e.g.Inject修饰的构造函数
            case PROVISION:
                return Optional.of(
                        simpleMethodRequestRepresentationFactory.create((ProvisionBinding) binding));

            case MEMBERS_INJECTOR:
                return Optional.empty();

            case MEMBERS_INJECTION:
            case COMPONENT_PRODUCTION:
            case PRODUCTION:
                // Fall through
        }
        throw new AssertionError("Unexpected binding kind: " + binding.kind());
    }
}
