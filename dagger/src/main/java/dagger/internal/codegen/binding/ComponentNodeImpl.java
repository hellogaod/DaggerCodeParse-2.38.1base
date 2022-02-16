package dagger.internal.codegen.binding;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

import dagger.spi.model.BindingGraph;
import dagger.spi.model.ComponentPath;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.Scope;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/**
 * An implementation of {@link BindingGraph.ComponentNode} that also exposes the {@link ComponentDescriptor}.
 */
@AutoValue
public abstract class ComponentNodeImpl implements BindingGraph.ComponentNode {

    public static BindingGraph.ComponentNode create(
            ComponentPath componentPath, ComponentDescriptor componentDescriptor) {
        return new AutoValue_ComponentNodeImpl(componentPath, componentDescriptor);
    }

    @Override
    public final boolean isSubcomponent() {
        return componentDescriptor().isSubcomponent();
    }

    @Override
    public boolean isRealComponent() {
        return componentDescriptor().isRealComponent();
    }

    @Override
    public final ImmutableSet<DependencyRequest> entryPoints() {
        return componentDescriptor().entryPointMethods().stream()
                .map(method -> method.dependencyRequest().get())
                .collect(toImmutableSet());
    }

    @Override
    public ImmutableSet<Scope> scopes() {
        return componentDescriptor().scopes();
    }

    public abstract ComponentDescriptor componentDescriptor();

    @Override
    public final String toString() {
        return componentPath().toString();
    }
}
