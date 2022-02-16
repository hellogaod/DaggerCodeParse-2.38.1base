package dagger.spi.model;


import com.google.common.collect.ImmutableSet;

import java.util.Optional;

/**
 * The association between a {@link Key} and the way in which instances of the key are provided.
 * Includes any {@linkplain DependencyRequest dependencies} that are needed in order to provide the
 * instances.
 *
 * <p>If a binding is owned by more than one component, there is one {@code Binding} for every
 * owning component.
 */
public interface Binding extends BindingGraph.MaybeBinding {
    @Override
    ComponentPath componentPath();

    /**
     * @deprecated This always returns {@code Optional.of(this)}.
     */
    @Override
    @Deprecated
    default Optional<Binding> binding() {
        return Optional.of(this);
    }

    /**
     * The dependencies of this binding. The order of the dependencies corresponds to the order in
     * which they will be injected when the binding is requested.
     */
    ImmutableSet<DependencyRequest> dependencies();

    /**
     * The {@link DaggerElement} that declares this binding. Absent for
     * {@linkplain BindingKind binding kinds} that are not always declared by exactly one element.
     *
     * <p>For example, consider {@link BindingKind#MULTIBOUND_SET}. A component with many
     * {@code @IntoSet} bindings for the same key will have a synthetic binding that depends on all
     * contributions, but with no identifiying binding element. A {@code @Multibinds} method will also
     * contribute a synthetic binding, but since multiple {@code @Multibinds} methods can coexist in
     * the same component (and contribute to one single binding), it has no binding element.
     */
    Optional<DaggerElement> bindingElement();

    /**
     * The {@link DaggerTypeElement} of the module which contributes this binding. Absent for bindings
     * that have no {@link #bindingElement() binding element}.
     */
    Optional<DaggerTypeElement> contributingModule();

    /**
     * Returns {@code true} if using this binding requires an instance of the {@link
     * #contributingModule()}.
     */
    boolean requiresModuleInstance();

    /**
     * The scope of this binding if it has one.
     */
    Optional<Scope> scope();

    /**
     * Returns {@code true} if this binding may provide {@code null} instead of an instance of {@link
     * #key()}. Nullable bindings cannot be requested from {@linkplain DependencyRequest#isNullable()
     * non-nullable dependency requests}.
     */
    boolean isNullable();

    /**
     * Returns {@code true} if this is a production binding, e.g. an {@code @Produces} method.
     */
    boolean isProduction();

    /**
     * The kind of binding this instance represents.
     */
    BindingKind kind();
}
