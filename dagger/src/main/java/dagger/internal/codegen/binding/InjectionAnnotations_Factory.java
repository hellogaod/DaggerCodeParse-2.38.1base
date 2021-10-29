package dagger.internal.codegen.binding;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class InjectionAnnotations_Factory implements Factory<InjectionAnnotations> {

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<KotlinMetadataUtil> kotlinMetadataUtilProvider;

    public InjectionAnnotations_Factory(Provider<DaggerElements> elementsProvider,
                                        Provider<KotlinMetadataUtil> kotlinMetadataUtilProvider) {
        this.elementsProvider = elementsProvider;
        this.kotlinMetadataUtilProvider = kotlinMetadataUtilProvider;
    }

    @Override
    public InjectionAnnotations get() {
        return newInstance(elementsProvider.get(), kotlinMetadataUtilProvider.get());
    }

    public static InjectionAnnotations_Factory create(Provider<DaggerElements> elementsProvider,
                                                      Provider<KotlinMetadataUtil> kotlinMetadataUtilProvider) {
        return new InjectionAnnotations_Factory(elementsProvider, kotlinMetadataUtilProvider);
    }

    public static InjectionAnnotations newInstance(DaggerElements elements,
                                                   KotlinMetadataUtil kotlinMetadataUtil) {
        return new InjectionAnnotations(elements, kotlinMetadataUtil);
    }
}
