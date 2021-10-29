package dagger.internal.codegen.validation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
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
public final class ComponentCreatorValidator_Factory implements Factory<ComponentCreatorValidator> {
    private final Provider<DaggerElements> elementsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    public ComponentCreatorValidator_Factory(Provider<DaggerElements> elementsProvider,
                                             Provider<DaggerTypes> typesProvider, Provider<KotlinMetadataUtil> metadataUtilProvider) {
        this.elementsProvider = elementsProvider;
        this.typesProvider = typesProvider;
        this.metadataUtilProvider = metadataUtilProvider;
    }

    @Override
    public ComponentCreatorValidator get() {
        return newInstance(elementsProvider.get(), typesProvider.get(), metadataUtilProvider.get());
    }

    public static ComponentCreatorValidator_Factory create(Provider<DaggerElements> elementsProvider,
                                                           Provider<DaggerTypes> typesProvider, Provider<KotlinMetadataUtil> metadataUtilProvider) {
        return new ComponentCreatorValidator_Factory(elementsProvider, typesProvider, metadataUtilProvider);
    }

    public static ComponentCreatorValidator newInstance(DaggerElements elements, DaggerTypes types,
                                                        KotlinMetadataUtil metadataUtil) {
        return new ComponentCreatorValidator(elements, types, metadataUtil);
    }
}
