package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.KeyFactory;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class ComponentNames_Factory implements Factory<ComponentNames> {
    private final Provider<BindingGraph> graphProvider;

    private final Provider<KeyFactory> keyFactoryProvider;

    public ComponentNames_Factory(Provider<BindingGraph> graphProvider,
                                  Provider<KeyFactory> keyFactoryProvider) {
        this.graphProvider = graphProvider;
        this.keyFactoryProvider = keyFactoryProvider;
    }

    @Override
    public ComponentNames get() {
        return newInstance(graphProvider.get(), keyFactoryProvider.get());
    }

    public static ComponentNames_Factory create(Provider<BindingGraph> graphProvider,
                                                Provider<KeyFactory> keyFactoryProvider) {
        return new ComponentNames_Factory(graphProvider, keyFactoryProvider);
    }

    public static ComponentNames newInstance(BindingGraph graph, KeyFactory keyFactory) {
        return new ComponentNames(graph, keyFactory);
    }
}
