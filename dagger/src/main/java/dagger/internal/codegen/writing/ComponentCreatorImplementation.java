package dagger.internal.codegen.writing;


import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import dagger.internal.codegen.binding.ComponentRequirement;

/**
 * The implementation of a component creator type.
 */
@AutoValue
public abstract class ComponentCreatorImplementation {

    /**
     * Creates a new {@link ComponentCreatorImplementation}.
     */
    public static ComponentCreatorImplementation create(
            TypeSpec spec, ClassName name, ImmutableMap<ComponentRequirement, FieldSpec> fields) {
        return new AutoValue_ComponentCreatorImplementation(spec, name, fields);
    }

    /**
     * The type spec for the creator implementation.
     */
    public abstract TypeSpec spec();

    /**
     * The name of the creator implementation class.
     */
    public abstract ClassName name();

    /**
     * All fields that are present in this implementation.
     */
    abstract ImmutableMap<ComponentRequirement, FieldSpec> fields();
}
