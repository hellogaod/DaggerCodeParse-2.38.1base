package dagger.internal.codegen.binding;


import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

import javax.lang.model.element.TypeElement;

import dagger.spi.model.Key;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getOnlyElement;

/**
 * The collection of bindings that have been resolved for a key. For valid graphs, contains exactly
 * one binding.
 *
 * <p>Separate {@link ResolvedBindings} instances should be used if a {@link
 * MembersInjectionBinding} and a {@link ProvisionBinding} for the same key exist in the same
 * component. (This will only happen if a type has an {@code @Inject} constructor and members, the
 * component has a members injection method, and the type is also requested normally.)
 */
@AutoValue
abstract class ResolvedBindings {
    /**
     * The binding key for which the {@link #bindings()} have been resolved.
     */
    abstract Key key();

    /**
     * The {@link ContributionBinding}s for {@link #key()} indexed by the component that owns the
     * binding. Each key in the multimap is a part of the same component ancestry.
     *
     * 表示成员注入所依赖的绑定
     */
    abstract ImmutableSetMultimap<TypeElement, ContributionBinding> allContributionBindings();

    /**
     * The {@link MembersInjectionBinding}s for {@link #key()} indexed by the component that owns the
     * binding.  Each key in the map is a part of the same component ancestry.
     *
     * 表示需要成员注入-成员绑定
     */
    abstract ImmutableMap<TypeElement, MembersInjectionBinding> allMembersInjectionBindings();

    /**
     * The multibinding declarations for {@link #key()}.
     */
    abstract ImmutableSet<MultibindingDeclaration> multibindingDeclarations();

    /**
     * The subcomponent declarations for {@link #key()}.
     */
    abstract ImmutableSet<SubcomponentDeclaration> subcomponentDeclarations();

    /**
     * The optional binding declarations for {@link #key()}.
     */
    abstract ImmutableSet<OptionalBindingDeclaration> optionalBindingDeclarations();

    // Computing the hash code is an expensive operation.
    @Memoized
    @Override
    public abstract int hashCode();

    // Suppresses ErrorProne warning that hashCode was overridden w/o equals
    @Override
    public abstract boolean equals(Object other);

    /**
     * All bindings for {@link #key()}, indexed by the component that owns the binding.
     * <p>
     * 如果存在成员注入的绑定，返回该成员注入绑定；否则返回贡献类型的绑定
     */
    final ImmutableSetMultimap<TypeElement, ? extends Binding> allBindings() {
        return !allMembersInjectionBindings().isEmpty()
                ? allMembersInjectionBindings().asMultimap()
                : allContributionBindings();
    }

    /**
     * All bindings for {@link #key()}, regardless of which component owns them.
     */
    final ImmutableCollection<? extends Binding> bindings() {
        return allBindings().values();
    }

    /**
     * {@code true} if there are no {@link #bindings()}, {@link #multibindingDeclarations()}, {@link
     * #optionalBindingDeclarations()}, or {@link #subcomponentDeclarations()}.
     */
    final boolean isEmpty() {
        return allMembersInjectionBindings().isEmpty()
                && allContributionBindings().isEmpty()
                && multibindingDeclarations().isEmpty()
                && optionalBindingDeclarations().isEmpty()
                && subcomponentDeclarations().isEmpty();
    }

    /**
     * All bindings for {@link #key()} that are owned by a component.
     */
    ImmutableSet<? extends Binding> bindingsOwnedBy(ComponentDescriptor component) {
        return allBindings().get(component.typeElement());
    }

    /**
     * All contribution bindings, regardless of owning component. Empty if this is a members-injection
     * binding.
     */
    @Memoized
    ImmutableSet<ContributionBinding> contributionBindings() {
        // TODO(ronshapiro): consider optimizing ImmutableSet.copyOf(Collection) for small immutable
        // collections so that it doesn't need to call toArray(). Even though this method is memoized,
        // toArray() can take ~150ms for large components, and there are surely other places in the
        // processor that can benefit from this.
        return ImmutableSet.copyOf(allContributionBindings().values());
    }

    /**
     * The component that owns {@code binding}.
     */
    final TypeElement owningComponent(ContributionBinding binding) {
        checkArgument(
                contributionBindings().contains(binding),
                "binding is not resolved for %s: %s",
                key(),
                binding);
        return getOnlyElement(allContributionBindings().inverse().get(binding));
    }

    /**
     * Creates a {@link ResolvedBindings} for contribution bindings.
     * <p>
     * 相对来说最全的一个ResolvedBindings
     */
    static ResolvedBindings forContributionBindings(
            Key key,
            Multimap<TypeElement, ContributionBinding> contributionBindings,
            Iterable<MultibindingDeclaration> multibindings,
            Iterable<SubcomponentDeclaration> subcomponentDeclarations,
            Iterable<OptionalBindingDeclaration> optionalBindingDeclarations) {
        return new AutoValue_ResolvedBindings(
                key,
                ImmutableSetMultimap.copyOf(contributionBindings),
                ImmutableMap.of(),
                ImmutableSet.copyOf(multibindings),
                ImmutableSet.copyOf(subcomponentDeclarations),
                ImmutableSet.copyOf(optionalBindingDeclarations));
    }

    /**
     * Creates a {@link ResolvedBindings} for members injection bindings.
     * <p>
     * 表示component类中存在有且仅有的一个参数的方法，该方法参数表示一个成员注入component，该方法注册进入InjectBindingRegisgtry中那个，并且生成了一个MembersInjectionBinding
     * <p>
     * 这里有（1）一个key（方法参数生成），（2）一个allMembersInjectionBindings ，K：当前component类；V：该component类中有且仅有的一个参数方法生成的MembersInjectionBinding绑定
     */
    static ResolvedBindings forMembersInjectionBinding(
            Key key,
            ComponentDescriptor owningComponent,
            MembersInjectionBinding ownedMembersInjectionBinding) {
        return new AutoValue_ResolvedBindings(
                key,
                ImmutableSetMultimap.of(),
                ImmutableMap.of(owningComponent.typeElement(), ownedMembersInjectionBinding),
                ImmutableSet.of(),
                ImmutableSet.of(),
                ImmutableSet.of());
    }

    /**
     * Creates a {@link ResolvedBindings} appropriate for when there are no bindings for the key.
     * <p>
     * 只有一个key，其他啥都没有
     */
    static ResolvedBindings noBindings(Key key) {
        return new AutoValue_ResolvedBindings(
                key,
                ImmutableSetMultimap.of(),
                ImmutableMap.of(),
                ImmutableSet.of(),
                ImmutableSet.of(),
                ImmutableSet.of());
    }
}
