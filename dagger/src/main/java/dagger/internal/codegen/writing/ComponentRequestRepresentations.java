package dagger.internal.codegen.writing;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.base.Preconditions.checkNotNull;

@PerComponentImplementation
public final class ComponentRequestRepresentations {

    private final Optional<ComponentRequestRepresentations> parent;
    private final BindingGraph graph;
    private final ComponentImplementation componentImplementation;
    private final ComponentRequirementExpressions componentRequirementExpressions;
    private final LegacyBindingRepresentation.Factory legacyBindingRepresentationFactory;
    private final DaggerTypes types;
    private final CompilerOptions compilerOptions;
//    private final Map<BindingRequest, RequestRepresentation> expressions = new HashMap<>();
//    private final Map<Binding, BindingRepresentation> representations = new HashMap<>();
//    private final SwitchingProviders switchingProviders;

    @Inject
    ComponentRequestRepresentations(
            @ParentComponent Optional<ComponentRequestRepresentations> parent,
            BindingGraph graph,
            ComponentImplementation componentImplementation,
            ComponentRequirementExpressions componentRequirementExpressions,
            LegacyBindingRepresentation.Factory legacyBindingRepresentationFactory,
            DaggerTypes types,
            CompilerOptions compilerOptions) {
        this.parent = parent;
        this.graph = graph;
        this.componentImplementation = componentImplementation;
        this.legacyBindingRepresentationFactory = legacyBindingRepresentationFactory;
        this.componentRequirementExpressions = checkNotNull(componentRequirementExpressions);
        this.types = types;
        this.compilerOptions = compilerOptions;
//        this.switchingProviders = new SwitchingProviders(componentImplementation, types);
    }
}
