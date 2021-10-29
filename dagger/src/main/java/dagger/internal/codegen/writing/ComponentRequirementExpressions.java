package dagger.internal.codegen.writing;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.langmodel.DaggerElements;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ComponentRequirementExpressions
 * Author: 佛学徒
 * Date: 2021/10/25 11:18
 * Description:
 * History:
 */
@PerComponentImplementation
public final class ComponentRequirementExpressions {

    // TODO(dpb,ronshapiro): refactor this and ComponentRequestRepresentations into a
    // HierarchicalComponentMap<K, V>, or perhaps this use a flattened ImmutableMap, built from its
    // parents? If so, maybe make ComponentRequirementExpression.Factory create it.

    private final Optional<ComponentRequirementExpressions> parent;
//    private final Map<ComponentRequirement, ComponentRequirementExpression>
//            componentRequirementExpressions = new HashMap<>();
    private final BindingGraph graph;
//    private final ShardImplementation componentShard;
    private final ModuleProxies moduleProxies;

    @Inject
    ComponentRequirementExpressions(
            @ParentComponent Optional<ComponentRequirementExpressions> parent,
            BindingGraph graph,
            ComponentImplementation componentImplementation,
            DaggerElements elements,
            ModuleProxies moduleProxies) {
        this.parent = parent;
        this.graph = graph;
        // All component requirements go in the componentShard.
//        this.componentShard = componentImplementation.getComponentShard();
        this.moduleProxies = moduleProxies;
    }

}
