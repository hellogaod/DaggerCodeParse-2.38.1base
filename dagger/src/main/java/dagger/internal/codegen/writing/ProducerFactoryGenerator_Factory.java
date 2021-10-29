package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.KeyFactory;
import dagger.internal.codegen.compileroption.CompilerOptions;
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
public final class ProducerFactoryGenerator_Factory implements Factory<ProducerFactoryGenerator> {
    private final Provider<XFiler> filerProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<SourceVersion> sourceVersionProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    private final Provider<KeyFactory> keyFactoryProvider;

    public ProducerFactoryGenerator_Factory(Provider<XFiler> filerProvider,
                                            Provider<DaggerElements> elementsProvider, Provider<SourceVersion> sourceVersionProvider,
                                            Provider<CompilerOptions> compilerOptionsProvider, Provider<KeyFactory> keyFactoryProvider) {
        this.filerProvider = filerProvider;
        this.elementsProvider = elementsProvider;
        this.sourceVersionProvider = sourceVersionProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
        this.keyFactoryProvider = keyFactoryProvider;
    }

    @Override
    public ProducerFactoryGenerator get() {
        return newInstance(filerProvider.get(), elementsProvider.get(), sourceVersionProvider.get(), compilerOptionsProvider.get(), keyFactoryProvider.get());
    }

    public static ProducerFactoryGenerator_Factory create(Provider<XFiler> filerProvider,
                                                          Provider<DaggerElements> elementsProvider, Provider<SourceVersion> sourceVersionProvider,
                                                          Provider<CompilerOptions> compilerOptionsProvider, Provider<KeyFactory> keyFactoryProvider) {
        return new ProducerFactoryGenerator_Factory(filerProvider, elementsProvider, sourceVersionProvider, compilerOptionsProvider, keyFactoryProvider);
    }

    public static ProducerFactoryGenerator newInstance(XFiler filer, DaggerElements elements,
                                                       SourceVersion sourceVersion, CompilerOptions compilerOptions, KeyFactory keyFactory) {
        return new ProducerFactoryGenerator(filer, elements, sourceVersion, compilerOptions, keyFactory);
    }
}
