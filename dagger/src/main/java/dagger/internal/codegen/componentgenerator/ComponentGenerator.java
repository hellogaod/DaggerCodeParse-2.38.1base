package dagger.internal.codegen.componentgenerator;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.TypeSpec;

import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
import dagger.Component;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.writing.ComponentImplementation;

/** Generates the implementation of the abstract types annotated with {@link Component}. */
final class ComponentGenerator extends SourceFileGenerator<BindingGraph> {
    private final TopLevelImplementationComponent.Factory topLevelImplementationComponentFactory;

    @Inject
    ComponentGenerator(
            XFiler filer,
            DaggerElements elements,
            SourceVersion sourceVersion,
            TopLevelImplementationComponent.Factory topLevelImplementationComponentFactory
    ) {

        this.topLevelImplementationComponentFactory = topLevelImplementationComponentFactory;
    }

    public ImmutableList<TypeSpec.Builder> topLevelTypes(BindingGraph bindingGraph) {
        ComponentImplementation componentImplementation =
                topLevelImplementationComponentFactory
                        .create(bindingGraph)
                        .currentImplementationSubcomponentBuilder()
                        .bindingGraph(bindingGraph)
                        .parentImplementation(Optional.empty())
                        .parentRequestRepresentations(Optional.empty())
                        .parentRequirementExpressions(Optional.empty())
                        .build()
                        .componentImplementation();

        return  ImmutableList.of();
    }
}
