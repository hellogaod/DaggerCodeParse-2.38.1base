package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class OptionalFactories_Factory implements Factory<OptionalFactories> {
    private final Provider<OptionalFactories.PerGeneratedFileCache> perGeneratedFileCacheProvider;

    private final Provider<ComponentImplementation> componentImplementationProvider;

    public OptionalFactories_Factory(
            Provider<OptionalFactories.PerGeneratedFileCache> perGeneratedFileCacheProvider,
            Provider<ComponentImplementation> componentImplementationProvider) {
        this.perGeneratedFileCacheProvider = perGeneratedFileCacheProvider;
        this.componentImplementationProvider = componentImplementationProvider;
    }

    @Override
    public OptionalFactories get() {
        return newInstance(perGeneratedFileCacheProvider.get(), componentImplementationProvider.get());
    }

    public static OptionalFactories_Factory create(
            Provider<OptionalFactories.PerGeneratedFileCache> perGeneratedFileCacheProvider,
            Provider<ComponentImplementation> componentImplementationProvider) {
        return new OptionalFactories_Factory(perGeneratedFileCacheProvider, componentImplementationProvider);
    }

    public static OptionalFactories newInstance(Object perGeneratedFileCache,
                                                ComponentImplementation componentImplementation) {
        return new OptionalFactories((OptionalFactories.PerGeneratedFileCache) perGeneratedFileCache, componentImplementation);
    }
}
