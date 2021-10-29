package dagger.internal.codegen.writing;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.squareup.javapoet.ClassName;

import javax.inject.Inject;

import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.KeyFactory;
import dagger.spi.model.Key;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: ComponentNames
 * Author: 佛学徒
 * Date: 2021/10/27 8:25
 * Description:
 * History:
 */
public class ComponentNames {


//    private final ClassName rootName;
//    private final ImmutableMap<ComponentPath, String> namesByPath;
//    private final ImmutableMap<ComponentPath, String> creatorNamesByPath;
//    private final ImmutableMultimap<Key, ComponentPath> pathsByCreatorKey;

    @Inject
    ComponentNames(
            @TopLevel BindingGraph graph,
            KeyFactory keyFactory
    ) {
//        this.rootName = getRootComponentClassName(graph.componentDescriptor());
//        this.namesByPath = namesByPath(graph);
//        this.creatorNamesByPath = creatorNamesByPath(namesByPath, graph);
//        this.pathsByCreatorKey = pathsByCreatorKey(keyFactory, graph);
    }
}
