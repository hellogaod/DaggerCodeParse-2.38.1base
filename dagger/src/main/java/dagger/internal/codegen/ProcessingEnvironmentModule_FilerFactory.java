package dagger.internal.codegen;


import javax.annotation.Generated;
import javax.inject.Provider;

import androidx.room.compiler.processing.XFiler;
import androidx.room.compiler.processing.XProcessingEnv;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.codegen.compileroption.CompilerOptions;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class ProcessingEnvironmentModule_FilerFactory implements Factory<XFiler> {

    private final Provider<CompilerOptions> compilerOptionsProvider;

    private final Provider<XProcessingEnv> xProcessingEnvProvider;

    public ProcessingEnvironmentModule_FilerFactory(Provider<CompilerOptions> compilerOptionsProvider,
                                                    Provider<XProcessingEnv> xProcessingEnvProvider) {
        this.compilerOptionsProvider = compilerOptionsProvider;
        this.xProcessingEnvProvider = xProcessingEnvProvider;
    }

    @Override
    public XFiler get() {
        return filer(compilerOptionsProvider.get(), xProcessingEnvProvider.get());
    }

    public static ProcessingEnvironmentModule_FilerFactory create(
            Provider<CompilerOptions> compilerOptionsProvider,
            Provider<XProcessingEnv> xProcessingEnvProvider) {
        return new ProcessingEnvironmentModule_FilerFactory(compilerOptionsProvider, xProcessingEnvProvider);
    }

    public static XFiler filer(CompilerOptions compilerOptions, XProcessingEnv xProcessingEnv) {
        return Preconditions.checkNotNullFromProvides(ProcessingEnvironmentModule.filer(compilerOptions, xProcessingEnv));
    }
}
