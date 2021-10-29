package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
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
public final class InaccessibleMapKeyProxyGenerator_Factory implements Factory<InaccessibleMapKeyProxyGenerator> {
    private final Provider<XFiler> filerProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<SourceVersion> sourceVersionProvider;

    public InaccessibleMapKeyProxyGenerator_Factory(Provider<XFiler> filerProvider,
                                                    Provider<DaggerTypes> typesProvider, Provider<DaggerElements> elementsProvider,
                                                    Provider<SourceVersion> sourceVersionProvider) {
        this.filerProvider = filerProvider;
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
        this.sourceVersionProvider = sourceVersionProvider;
    }

    @Override
    public InaccessibleMapKeyProxyGenerator get() {
        return newInstance(filerProvider.get(), typesProvider.get(), elementsProvider.get(), sourceVersionProvider.get());
    }

    public static InaccessibleMapKeyProxyGenerator_Factory create(Provider<XFiler> filerProvider,
                                                                  Provider<DaggerTypes> typesProvider, Provider<DaggerElements> elementsProvider,
                                                                  Provider<SourceVersion> sourceVersionProvider) {
        return new InaccessibleMapKeyProxyGenerator_Factory(filerProvider, typesProvider, elementsProvider, sourceVersionProvider);
    }

    public static InaccessibleMapKeyProxyGenerator newInstance(XFiler filer, DaggerTypes types,
                                                               DaggerElements elements, SourceVersion sourceVersion) {
        return new InaccessibleMapKeyProxyGenerator(filer, types, elements, sourceVersion);
    }
}
