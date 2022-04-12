package dagger.hilt.processor.internal;


// TODO(bcorso): Reduce the visibility of this class and return ClassNames instead.
// TODO(erichang): Rename this class so it doesn't conflict with
// dagger.internal.codegen.ComponentDescriptor

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.Optional;

/** Represents a single component in the hierarchy. */
@AutoValue
public abstract class ComponentDescriptor {
    public static Builder builder() {
        return new AutoValue_ComponentDescriptor.Builder()
                .scopes(ImmutableSet.of());
    }

    /** Returns the {@link ClassName} for this component descriptor. */
    public abstract ClassName component();

    /** Returns the {@link ClassName}s for the scopes of this component descriptor. */
    public abstract ImmutableSet<ClassName> scopes();

    /** Returns the {@link ClassName} for the creator interface. if it exists. */
    public abstract Optional<ClassName> creator();

    /** Returns the {@link ClassName} for the parent, if it exists. */
    public abstract Optional<ComponentDescriptor> parent();

    /** Returns {@code true} if the descriptor represents a root component. */
    public boolean isRoot() {
        return !parent().isPresent();
    }

    /**
     * Returns {@code true} if the given {@link ComponentDescriptor} represents the same {@link
     * #component()}.
     */
    // TODO(b/144939893): Remove equals and hashcode once we have unique ComponentDescriptor instances
    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ComponentDescriptor)) {
            return false;
        }
        ComponentDescriptor that = (ComponentDescriptor) obj;

        // Only check the component name, which should map 1:1 to each component descriptor created
        // by DefineComponents#componentDescriptor(Element). However, if users are building their own
        // ComponentDescriptors manually, then this might not be true. We should lock down the builder
        // method to avoid that.
        return component().equals(that.component());
    }

    @Override
    public final int hashCode() {
        return component().hashCode();
    }

    /** Builder for ComponentDescriptor. */
    @AutoValue.Builder
    public interface Builder {
        Builder component(ClassName component);

        Builder scopes(ImmutableSet<ClassName> scopes);

        Builder scopes(ClassName... scopes);

        Builder creator(ClassName creator);

        Builder parent(ComponentDescriptor parent);


        ComponentDescriptor build();
    }
}
