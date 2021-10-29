package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class UnwrappedMapKeyGenerator_Factory implements Factory<UnwrappedMapKeyGenerator> {
    private final Provider<XFiler> filerProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<SourceVersion> sourceVersionProvider;

    public UnwrappedMapKeyGenerator_Factory(Provider<XFiler> filerProvider,
                                            Provider<DaggerElements> elementsProvider, Provider<SourceVersion> sourceVersionProvider) {
        this.filerProvider = filerProvider;
        this.elementsProvider = elementsProvider;
        this.sourceVersionProvider = sourceVersionProvider;
    }

    @Override
    public UnwrappedMapKeyGenerator get() {
        return newInstance(filerProvider.get(), elementsProvider.get(), sourceVersionProvider.get());
    }

    public static UnwrappedMapKeyGenerator_Factory create(Provider<XFiler> filerProvider,
                                                          Provider<DaggerElements> elementsProvider, Provider<SourceVersion> sourceVersionProvider) {
        return new UnwrappedMapKeyGenerator_Factory(filerProvider, elementsProvider, sourceVersionProvider);
    }

    public static UnwrappedMapKeyGenerator newInstance(XFiler filer, DaggerElements elements,
                                                       SourceVersion sourceVersion) {
        return new UnwrappedMapKeyGenerator(filer, elements, sourceVersion);
    }
}
