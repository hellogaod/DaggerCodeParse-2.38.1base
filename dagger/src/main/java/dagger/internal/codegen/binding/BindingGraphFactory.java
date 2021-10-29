package dagger.internal.codegen.binding;

import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.internal.codegen.base.ClearableCache;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.langmodel.DaggerElements;

/** A factory for {@link BindingGraph} objects. */
@Singleton
public final class BindingGraphFactory  implements ClearableCache {

    private final DaggerElements elements;
    private final InjectBindingRegistry injectBindingRegistry;
    private final KeyFactory keyFactory;
    private final BindingFactory bindingFactory;
    private final ModuleDescriptor.Factory moduleDescriptorFactory;
    private final BindingGraphConverter bindingGraphConverter;

    private final CompilerOptions compilerOptions;

    @Inject
    BindingGraphFactory(
            DaggerElements elements,
            InjectBindingRegistry injectBindingRegistry,
            KeyFactory keyFactory,
            BindingFactory bindingFactory,
            ModuleDescriptor.Factory moduleDescriptorFactory,
            BindingGraphConverter bindingGraphConverter,
            CompilerOptions compilerOptions) {
        this.elements = elements;
        this.injectBindingRegistry = injectBindingRegistry;
        this.keyFactory = keyFactory;
        this.bindingFactory = bindingFactory;
        this.moduleDescriptorFactory = moduleDescriptorFactory;
        this.bindingGraphConverter = bindingGraphConverter;
        this.compilerOptions = compilerOptions;
    }

    @Override
    public void clearCache() {

    }
}
