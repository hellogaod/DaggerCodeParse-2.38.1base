package dagger.internal.codegen.componentgenerator;

import java.util.Optional;

import javax.inject.Provider;

import dagger.BindsInstance;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.writing.ComponentImplementation;
import dagger.internal.codegen.writing.ComponentRequestRepresentations;
import dagger.internal.codegen.writing.ComponentRequirementExpressions;
import dagger.internal.codegen.writing.ParentComponent;
import dagger.internal.codegen.writing.PerComponentImplementation;

/**
 * A subcomponent that injects all objects that are responsible for creating a single {@link
 * ComponentImplementation} instance. Each child {@link ComponentImplementation} will have its own
 * instance of {@link CurrentImplementationSubcomponent}.
 */
@Subcomponent(
        modules = CurrentImplementationSubcomponent.ChildComponentImplementationFactoryModule.class)
@PerComponentImplementation
// This only needs to be public because the type is referenced by generated component.
public interface CurrentImplementationSubcomponent {
    ComponentImplementation componentImplementation();

    /**
     * A module to bind the {@link ComponentImplementation.ChildComponentImplementationFactory}.
     */
    @Module
    interface ChildComponentImplementationFactoryModule {
        @Provides
        static ComponentImplementation.ChildComponentImplementationFactory provideChildComponentImplementationFactory(
                CurrentImplementationSubcomponent.Builder currentImplementationSubcomponentBuilder,
                Provider<ComponentImplementation> componentImplementatation,
                Provider<ComponentRequestRepresentations> componentRequestRepresentations,
                Provider<ComponentRequirementExpressions> componentRequirementExpressions) {
            return childGraph ->
                    currentImplementationSubcomponentBuilder
                            .bindingGraph(childGraph)
                            .parentImplementation(Optional.of(componentImplementatation.get()))
                            .parentRequestRepresentations(Optional.of(componentRequestRepresentations.get()))
                            .parentRequirementExpressions(Optional.of(componentRequirementExpressions.get()))
                            .build()
                            .componentImplementation();
        }
    }

    /**
     * Returns the builder for {@link CurrentImplementationSubcomponent}.
     */
    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        Builder bindingGraph(BindingGraph bindingGraph);

        @BindsInstance
        Builder parentImplementation(
                @ParentComponent Optional<ComponentImplementation> parentImplementation);

        @BindsInstance
        Builder parentRequestRepresentations(
                @ParentComponent Optional<ComponentRequestRepresentations> parentRequestRepresentations);

        @BindsInstance
        Builder parentRequirementExpressions(
                @ParentComponent Optional<ComponentRequirementExpressions> parentRequirementExpressions);

        CurrentImplementationSubcomponent build();
    }
}
