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
        ImmutableSet<ClassName> defineComponentScopes =
                componentDescriptors.stream()
                        .flatMap(descriptor -> descriptor.scopes().stream())
                        .collect(toImmutableSet());

        ImmutableSetMultimap.Builder<ClassName, ClassName> builder = ImmutableSetMultimap.builder();
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

    private final ImmutableSetMultimap<ClassName, ClassName> defineComponentScopeToAliases;

    private AliasOfs(ImmutableSetMultimap<ClassName, ClassName> defineComponentScopeToAliases) {
        this.defineComponentScopeToAliases = defineComponentScopeToAliases;
    }

    public ImmutableSet<ClassName> getAliasesFor(ClassName defineComponentScope) {
        return defineComponentScopeToAliases.get(defineComponentScope);
    }
}
