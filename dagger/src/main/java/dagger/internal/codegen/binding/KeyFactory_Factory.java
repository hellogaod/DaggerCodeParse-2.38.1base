package dagger.internal.codegen.binding;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class KeyFactory_Factory implements Factory<KeyFactory> {

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<InjectionAnnotations> injectionAnnotationsProvider;

    public KeyFactory_Factory(Provider<DaggerTypes> typesProvider,
                              Provider<DaggerElements> elementsProvider,
                              Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
        this.injectionAnnotationsProvider = injectionAnnotationsProvider;
    }

    @Override
    public KeyFactory get() {
        return newInstance(typesProvider.get(), elementsProvider.get(), injectionAnnotationsProvider.get());
    }

    public static KeyFactory_Factory create(Provider<DaggerTypes> typesProvider,
                                            Provider<DaggerElements> elementsProvider,
                                            Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        return new KeyFactory_Factory(typesProvider, elementsProvider, injectionAnnotationsProvider);
    }

    public static KeyFactory newInstance(DaggerTypes types, DaggerElements elements,
                                         InjectionAnnotations injectionAnnotations) {
        return new KeyFactory(types, elements, injectionAnnotations);
    }
}
