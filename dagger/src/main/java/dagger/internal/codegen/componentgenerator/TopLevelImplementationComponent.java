package dagger.internal.codegen.componentgenerator;

import dagger.BindsInstance;
import dagger.Subcomponent;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.writing.ComponentImplementation;
import dagger.internal.codegen.writing.PerGeneratedFile;
import dagger.internal.codegen.writing.TopLevel;

/**
 * A shared subcomponent for a top-level {@link ComponentImplementation} and any nested child
 * implementations.
 */
@PerGeneratedFile
@Subcomponent
// This only needs to be public because the type is referenced by generated component.
public interface TopLevelImplementationComponent {
    CurrentImplementationSubcomponent.Builder currentImplementationSubcomponentBuilder();

    /** Returns the builder for {@link TopLevelImplementationComponent}. */
    @Subcomponent.Factory
    interface Factory {
        TopLevelImplementationComponent create(@BindsInstance @TopLevel BindingGraph bindingGraph);
    }
}
