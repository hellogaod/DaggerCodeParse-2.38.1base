package dagger.internal.codegen.writing;


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
public final class ModuleProxies_Factory implements Factory<ModuleProxies> {
    private final Provider<DaggerElements> elementsProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    public ModuleProxies_Factory(Provider<DaggerElements> elementsProvider,
                                 Provider<KotlinMetadataUtil> metadataUtilProvider) {
        this.elementsProvider = elementsProvider;
        this.metadataUtilProvider = metadataUtilProvider;
    }

    @Override
    public ModuleProxies get() {
        return newInstance(elementsProvider.get(), metadataUtilProvider.get());
    }

    public static ModuleProxies_Factory create(Provider<DaggerElements> elementsProvider,
                                               Provider<KotlinMetadataUtil> metadataUtilProvider) {
        return new ModuleProxies_Factory(elementsProvider, metadataUtilProvider);
    }

    public static ModuleProxies newInstance(DaggerElements elements,
                                            KotlinMetadataUtil metadataUtil) {
        return new ModuleProxies(elements, metadataUtil);
    }
}
