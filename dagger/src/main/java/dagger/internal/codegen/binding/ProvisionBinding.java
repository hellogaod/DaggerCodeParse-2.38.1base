package dagger.internal.codegen.binding;


import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.Optional;

import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.spi.model.BindingKind;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.Key;
import dagger.spi.model.Scope;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.spi.model.BindingKind.COMPONENT_PROVISION;
import static dagger.spi.model.BindingKind.PROVISION;

/**
 * A value object representing the mechanism by which a {@link Key} can be provided.
 */
@AutoValue
public abstract class ProvisionBinding extends ContributionBinding {

    @Override
    @Memoized
    public ImmutableSet<DependencyRequest> explicitDependencies() {
        return ImmutableSet.<DependencyRequest>builder()
                .addAll(provisionDependencies())
                .addAll(membersInjectionDependencies())
                .build();
    }

    /**
     * Dependencies necessary to invoke an {@code @Inject} constructor or {@code @Provides} method.
     */
    public abstract ImmutableSet<DependencyRequest> provisionDependencies();

    @Memoized
    ImmutableSet<DependencyRequest> membersInjectionDependencies() {
        return injectionSites()
                .stream()
                .flatMap(i -> i.dependencies().stream())
                .collect(toImmutableSet());
    }

    /**
     * {@link MembersInjectionBinding.InjectionSite}s for all {@code @Inject} members if {@link #kind()} is {@link
     * BindingKind#INJECTION}, otherwise empty.
     */
    public abstract ImmutableSortedSet<MembersInjectionBinding.InjectionSite> injectionSites();

    @Override
    public BindingType bindingType() {
        return BindingType.PROVISION;
    }

    //使用了泛型，并且类型和类型不匹配，例如类型是List<T>,但是节点使用的是List
    @Override
    public abstract Optional<ProvisionBinding> unresolved();

    // TODO(ronshapiro): we should be able to remove this, but AutoValue barks on the Builder's scope
    // method, saying that the method doesn't correspond to a property of ProvisionBinding
    @Override
    public abstract Optional<Scope> scope();

    public static Builder builder() {
        return new AutoValue_ProvisionBinding.Builder()
                .provisionDependencies(ImmutableSet.of())
                .injectionSites(ImmutableSortedSet.of());
    }

    @Override
    public abstract Builder toBuilder();

    private static final ImmutableSet<BindingKind> KINDS_TO_CHECK_FOR_NULL =
            ImmutableSet.of(PROVISION, COMPONENT_PROVISION);

    public boolean shouldCheckForNull(CompilerOptions compilerOptions) {
        //component节点或@Provides修饰的bindingMethod方法生成的ProvisionBinding对象
        return KINDS_TO_CHECK_FOR_NULL.contains(kind())
                && !contributedPrimitiveType().isPresent()
                && !nullableType().isPresent()
                && compilerOptions.doCheckForNulls();
    }

    // Profiling determined that this method is called enough times that memoizing it had a measurable
    // performance improvement for large components.
    @Memoized
    @Override
    public boolean requiresModuleInstance() {
        return super.requiresModuleInstance();
    }

    @Memoized
    @Override
    public abstract int hashCode();

    // TODO(ronshapiro,dpb): simplify the equality semantics
    @Override
    public abstract boolean equals(Object obj);

    /**
     * A {@link ProvisionBinding} builder.
     */
    @AutoValue.Builder
    @CanIgnoreReturnValue
    public abstract static class Builder
            extends ContributionBinding.Builder<ProvisionBinding, Builder> {

        @Override
        public Builder dependencies(Iterable<DependencyRequest> dependencies) {
            return provisionDependencies(dependencies);
        }

        abstract Builder provisionDependencies(Iterable<DependencyRequest> provisionDependencies);

        public abstract Builder injectionSites(ImmutableSortedSet<MembersInjectionBinding.InjectionSite> injectionSites);

        @Override
        public abstract Builder unresolved(ProvisionBinding unresolved);

        public abstract Builder scope(Optional<Scope> scope);
    }
}
