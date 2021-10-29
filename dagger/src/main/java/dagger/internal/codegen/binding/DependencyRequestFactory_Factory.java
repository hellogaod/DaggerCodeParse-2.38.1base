package dagger.internal.codegen.binding;

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
public final class DependencyRequestFactory_Factory implements Factory<DependencyRequestFactory> {

    private final Provider<KeyFactory> keyFactoryProvider;

    private final Provider<InjectionAnnotations> injectionAnnotationsProvider;

    public DependencyRequestFactory_Factory(Provider<KeyFactory> keyFactoryProvider,
                                            Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        this.keyFactoryProvider = keyFactoryProvider;
        this.injectionAnnotationsProvider = injectionAnnotationsProvider;
    }

    @Override
    public DependencyRequestFactory get() {
        return newInstance(keyFactoryProvider.get(), injectionAnnotationsProvider.get());
    }

    public static DependencyRequestFactory_Factory create(Provider<KeyFactory> keyFactoryProvider,
                                                          Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        return new DependencyRequestFactory_Factory(keyFactoryProvider, injectionAnnotationsProvider);
    }

    public static DependencyRequestFactory newInstance(KeyFactory keyFactory,
                                                       InjectionAnnotations injectionAnnotations) {
        return new DependencyRequestFactory(keyFactory, injectionAnnotations);
    }
}
