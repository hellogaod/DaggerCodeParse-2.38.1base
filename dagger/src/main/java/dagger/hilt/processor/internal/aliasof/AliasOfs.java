package dagger.hilt.processor.internal.aliasof;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.squareup.javapoet.ClassName;

import dagger.hilt.processor.internal.ComponentDescriptor;
import dagger.hilt.processor.internal.ProcessorErrors;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/**
 * Extracts a multimap of aliases annotated with {@link dagger.hilt.migration.AliasOf} mapping them
 * to scopes they are alias of.
 */
public final class AliasOfs {
    public static AliasOfs create(
            ImmutableSet<AliasOfPropagatedDataMetadata> metadatas,
            ImmutableSet<ComponentDescriptor> componentDescriptors) {

        //@DefineComponent修饰的节点同时使用的@Scope修饰的注解修饰
        ImmutableSet<ClassName> defineComponentScopes =
                componentDescriptors.stream()
                        .flatMap(descriptor -> descriptor.scopes().stream())
                        .collect(toImmutableSet());

        ImmutableSetMultimap.Builder<ClassName, ClassName> builder = ImmutableSetMultimap.builder();

        //@AliasOfPropagatedData#defineComponentScope中的节点一定存在于： @DefineComponent修饰的节点同时使用的@Scope修饰的注解修饰
        metadatas.forEach(
                metadata -> {
                    ClassName defineComponentScopeName =
                            ClassName.get(metadata.defineComponentScopeElement());
                    ClassName aliasScopeName = ClassName.get(metadata.aliasElement());
                    ProcessorErrors.checkState(
                            defineComponentScopes.contains(defineComponentScopeName),
                            metadata.aliasElement(),
                            "The scope %s cannot be an alias for %s. You can only have aliases of a scope"
                                    + " defined directly on a @DefineComponent type.",
                            aliasScopeName,
                            defineComponentScopeName);
                    builder.put(defineComponentScopeName, aliasScopeName);
                });
        return new AliasOfs(builder.build());
    }

    // k:@AliasOfPropagatedData#defineComponentScope中的节点,v:@AliasOfPropagatedData#alias中的节点（@AliasOf修饰的节点）
    private final ImmutableSetMultimap<ClassName, ClassName> defineComponentScopeToAliases;

    private AliasOfs(ImmutableSetMultimap<ClassName, ClassName> defineComponentScopeToAliases) {
        this.defineComponentScopeToAliases = defineComponentScopeToAliases;
    }

    public ImmutableSet<ClassName> getAliasesFor(ClassName defineComponentScope) {
        return defineComponentScopeToAliases.get(defineComponentScope);
    }
}
