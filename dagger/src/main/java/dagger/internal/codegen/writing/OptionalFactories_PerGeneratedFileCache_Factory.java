package dagger.internal.codegen.writing;


import javax.annotation.Generated;

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
public final class OptionalFactories_PerGeneratedFileCache_Factory implements Factory<OptionalFactories.PerGeneratedFileCache> {
    @Override
    public OptionalFactories.PerGeneratedFileCache get() {
        return newInstance();
    }

    public static OptionalFactories_PerGeneratedFileCache_Factory create() {
        return InstanceHolder.INSTANCE;
    }

    public static OptionalFactories.PerGeneratedFileCache newInstance() {
        return new OptionalFactories.PerGeneratedFileCache();
    }

    private static final class InstanceHolder {
        private static final OptionalFactories_PerGeneratedFileCache_Factory INSTANCE = new OptionalFactories_PerGeneratedFileCache_Factory();
    }
}
