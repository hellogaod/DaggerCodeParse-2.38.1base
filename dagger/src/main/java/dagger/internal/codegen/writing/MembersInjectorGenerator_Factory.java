package dagger.internal.codegen.writing;

import javax.annotation.Generated;
import javax.inject.Provider;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
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
public final class MembersInjectorGenerator_Factory implements Factory<MembersInjectorGenerator> {

    private final Provider<XFiler> filerProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<SourceVersion> sourceVersionProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    public MembersInjectorGenerator_Factory(Provider<XFiler> filerProvider,
                                            Provider<DaggerElements> elementsProvider, Provider<DaggerTypes> typesProvider,
                                            Provider<SourceVersion> sourceVersionProvider,
                                            Provider<KotlinMetadataUtil> metadataUtilProvider) {
        this.filerProvider = filerProvider;
        this.elementsProvider = elementsProvider;
        this.typesProvider = typesProvider;
        this.sourceVersionProvider = sourceVersionProvider;
        this.metadataUtilProvider = metadataUtilProvider;
    }

    @Override
    public MembersInjectorGenerator get() {
        return newInstance(filerProvider.get(), elementsProvider.get(), typesProvider.get(), sourceVersionProvider.get(), metadataUtilProvider.get());
    }

    public static MembersInjectorGenerator_Factory create(Provider<XFiler> filerProvider,
                                                          Provider<DaggerElements> elementsProvider, Provider<DaggerTypes> typesProvider,
                                                          Provider<SourceVersion> sourceVersionProvider,
                                                          Provider<KotlinMetadataUtil> metadataUtilProvider) {
        return new MembersInjectorGenerator_Factory(filerProvider, elementsProvider, typesProvider, sourceVersionProvider, metadataUtilProvider);
    }

    public static MembersInjectorGenerator newInstance(XFiler filer, DaggerElements elements,
                                                       DaggerTypes types, SourceVersion sourceVersion, KotlinMetadataUtil metadataUtil) {
        return new MembersInjectorGenerator(filer, elements, types, sourceVersion, metadataUtil);
    }
}
