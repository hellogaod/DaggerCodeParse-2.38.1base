package dagger.internal.codegen.componentgenerator;

import dagger.Binds;
import dagger.Module;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.ComponentDescriptor;

/**
 * Provides bindings needed to generated the component.
 */
@Module(subcomponents = TopLevelImplementationComponent.class)
public interface ComponentGeneratorModule {

    @Binds
    abstract SourceFileGenerator<BindingGraph> componentGenerator(ComponentGenerator generator);

    @Binds
    abstract SourceFileGenerator<ComponentDescriptor> componentHjarGenerator(
            ComponentHjarGenerator hjarGenerator);
}
