package dagger.internal.codegen.validation;

import dagger.Binds;
import dagger.Module;
import dagger.internal.codegen.binding.InjectBindingRegistry;

/**
 * Binds the {@link InjectBindingRegistry} implementation.
 */
@Module
public interface InjectBindingRegistryModule {
    @Binds InjectBindingRegistry injectBindingRegistry(InjectBindingRegistryImpl impl);
}
