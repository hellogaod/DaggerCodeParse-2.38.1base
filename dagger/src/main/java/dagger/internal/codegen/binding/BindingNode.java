package dagger.internal.codegen.binding;


import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Optional;

import javax.lang.model.element.Element;

import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.multibindings.Multibinds;
import dagger.spi.model.BindingKind;
import dagger.spi.model.ComponentPath;
import dagger.spi.model.DaggerElement;
import dagger.spi.model.DaggerTypeElement;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.Key;
import dagger.spi.model.Scope;

import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.BindingType.PRODUCTION;

/**
 * An implementation of {@link dagger.spi.model.Binding} that also exposes {@link
 * BindingDeclaration}s associated with the binding.
 * <p>
 * 绑定的节点转换的描述对象
 */
// TODO(dpb): Consider a supertype of dagger.spi.model.Binding that
// dagger.internal.codegen.binding.Binding
// could also implement.
@AutoValue
public abstract class BindingNode implements dagger.spi.model.Binding {

    public static BindingNode create(
            ComponentPath component,
            Binding delegate,
            ImmutableSet<MultibindingDeclaration> multibindingDeclarations,
            ImmutableSet<OptionalBindingDeclaration> optionalBindingDeclarations,
            ImmutableSet<SubcomponentDeclaration> subcomponentDeclarations,
            BindingDeclarationFormatter bindingDeclarationFormatter) {
        BindingNode node =
                new AutoValue_BindingNode(
                        component,
                        delegate,
                        multibindingDeclarations,
                        optionalBindingDeclarations,
                        subcomponentDeclarations);
        node.bindingDeclarationFormatter = checkNotNull(bindingDeclarationFormatter);
        return node;
    }

    private BindingDeclarationFormatter bindingDeclarationFormatter;

    public abstract Binding delegate();

    public abstract ImmutableSet<MultibindingDeclaration> multibindingDeclarations();

    public abstract ImmutableSet<OptionalBindingDeclaration> optionalBindingDeclarations();

    public abstract ImmutableSet<SubcomponentDeclaration> subcomponentDeclarations();

    /**
     * The {@link Element}s (other than the binding's {@link #bindingElement()}) that are associated
     * with the binding.
     *
     * <ul>
     *   <li>{@linkplain BindsOptionalOf optional binding} declarations
     *   <li>{@linkplain Module#subcomponents() module subcomponent} declarations
     *   <li>{@linkplain Multibinds multibinding} declarations
     * </ul>
     */
    public final Iterable<BindingDeclaration> associatedDeclarations() {
        return Iterables.concat(
                multibindingDeclarations(), optionalBindingDeclarations(), subcomponentDeclarations());
    }

    @Override
    public Key key() {
        return delegate().key();
    }

    @Override
    public ImmutableSet<DependencyRequest> dependencies() {
        return delegate().dependencies();
    }

    @Override
    public Optional<DaggerElement> bindingElement() {
        return delegate().bindingElement().map(DaggerElement::fromJava);
    }

    @Override
    public Optional<DaggerTypeElement> contributingModule() {
        return delegate().contributingModule().map(DaggerTypeElement::fromJava);
    }

    @Override
    public boolean requiresModuleInstance() {
        return delegate().requiresModuleInstance();
    }

    @Override
    public Optional<Scope> scope() {
        return delegate().scope();
    }

    @Override
    public boolean isNullable() {
        return delegate().isNullable();
    }

    @Override
    public boolean isProduction() {
        return delegate().bindingType().equals(PRODUCTION);
    }

    @Override
    public BindingKind kind() {
        return delegate().kind();
    }

    @Override
    public final String toString() {
        return bindingDeclarationFormatter.format(delegate());
    }
}
