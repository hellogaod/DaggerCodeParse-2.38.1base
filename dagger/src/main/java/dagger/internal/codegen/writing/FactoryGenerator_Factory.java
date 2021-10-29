package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.compileroption.CompilerOptions;
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
public final class FactoryGenerator_Factory implements Factory<FactoryGenerator> {
    private final Provider<XFiler> filerProvider;

    private final Provider<SourceVersion> sourceVersionProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    public FactoryGenerator_Factory(Provider<XFiler> filerProvider,
                                    Provider<SourceVersion> sourceVersionProvider, Provider<DaggerTypes> typesProvider,
                                    Provider<DaggerElements> elementsProvider, Provider<CompilerOptions> compilerOptionsProvider,
                                    Provider<KotlinMetadataUtil> metadataUtilProvider) {
        this.filerProvider = filerProvider;
        this.sourceVersionProvider = sourceVersionProvider;
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
        this.metadataUtilProvider = metadataUtilProvider;
    }

    @Override
    public FactoryGenerator get() {
        return newInstance(filerProvider.get(), sourceVersionProvider.get(), typesProvider.get(), elementsProvider.get(), compilerOptionsProvider.get(), metadataUtilProvider.get());
    }

    public static FactoryGenerator_Factory create(Provider<XFiler> filerProvider,
                                                  Provider<SourceVersion> sourceVersionProvider, Provider<DaggerTypes> typesProvider,
                                                  Provider<DaggerElements> elementsProvider, Provider<CompilerOptions> compilerOptionsProvider,
                                                  Provider<KotlinMetadataUtil> metadataUtilProvider) {
        return new FactoryGenerator_Factory(filerProvider, sourceVersionProvider, typesProvider, elementsProvider, compilerOptionsProvider, metadataUtilProvider);
    }

    public static FactoryGenerator newInstance(XFiler filer, SourceVersion sourceVersion,
                                               DaggerTypes types, DaggerElements elements, CompilerOptions compilerOptions,
                                               KotlinMetadataUtil metadataUtil) {
        return new FactoryGenerator(filer, sourceVersion, types, elements, compilerOptions, metadataUtil);
    }

}
