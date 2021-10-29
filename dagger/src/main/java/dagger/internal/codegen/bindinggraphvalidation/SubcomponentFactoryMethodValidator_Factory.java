package dagger.internal.codegen.bindinggraphvalidation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
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
public final class SubcomponentFactoryMethodValidator_Factory implements Factory<SubcomponentFactoryMethodValidator> {
    private final Provider<DaggerTypes> typesProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    public SubcomponentFactoryMethodValidator_Factory(Provider<DaggerTypes> typesProvider,
                                                      Provider<KotlinMetadataUtil> metadataUtilProvider) {
        this.typesProvider = typesProvider;
        this.metadataUtilProvider = metadataUtilProvider;
    }

    @Override
    public SubcomponentFactoryMethodValidator get() {
        return newInstance(typesProvider.get(), metadataUtilProvider.get());
    }

    public static SubcomponentFactoryMethodValidator_Factory create(
            Provider<DaggerTypes> typesProvider, Provider<KotlinMetadataUtil> metadataUtilProvider) {
        return new SubcomponentFactoryMethodValidator_Factory(typesProvider, metadataUtilProvider);
    }

    public static SubcomponentFactoryMethodValidator newInstance(DaggerTypes types,
                                                                 KotlinMetadataUtil metadataUtil) {
        return new SubcomponentFactoryMethodValidator(types, metadataUtil);
    }
}
