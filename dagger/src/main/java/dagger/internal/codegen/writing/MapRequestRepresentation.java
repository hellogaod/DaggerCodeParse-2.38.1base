package dagger.internal.codegen.writing;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: MapRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 11:22
 * Description:
 * History:
 */
class MapRequestRepresentation {


    private final ProvisionBinding binding;
//    private final ImmutableMap<DependencyRequest, ContributionBinding> dependencies;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final DaggerTypes types;
    private final DaggerElements elements;

    @AssistedInject
    MapRequestRepresentation(
            @Assisted ProvisionBinding binding,
            BindingGraph graph,
            ComponentRequestRepresentations componentRequestRepresentations,
            DaggerTypes types,
            DaggerElements elements) {
//        super(binding);
        this.binding = binding;
//        BindingKind bindingKind = this.binding.kind();
//        checkArgument(bindingKind.equals(MULTIBOUND_MAP), bindingKind);
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.types = types;
        this.elements = elements;
//        this.dependencies =
//                Maps.toMap(binding.dependencies(), dep -> graph.contributionBinding(dep.key()));
    }

    @AssistedFactory
    static interface Factory {
        MapRequestRepresentation create(ProvisionBinding binding);
    }
}
