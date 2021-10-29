package dagger.internal.codegen.writing;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.Key;

import static com.google.common.base.Preconditions.checkNotNull;

/** Manages the member injection methods for a component. */
@PerComponentImplementation
final class MembersInjectionMethods {

//    private final Map<Key, Expression> injectMethodExpressions = new LinkedHashMap<>();
    private final ComponentImplementation componentImplementation;
    private final ComponentRequestRepresentations bindingExpressions;
    private final BindingGraph graph;
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    MembersInjectionMethods(
            ComponentImplementation componentImplementation,
            ComponentRequestRepresentations bindingExpressions,
            BindingGraph graph,
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil metadataUtil) {
        this.componentImplementation = checkNotNull(componentImplementation);
        this.bindingExpressions = checkNotNull(bindingExpressions);
        this.graph = checkNotNull(graph);
        this.elements = checkNotNull(elements);
        this.types = checkNotNull(types);
        this.metadataUtil = metadataUtil;
    }


}
