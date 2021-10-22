package dagger.internal.codegen;

import javax.annotation.Generated;
import javax.inject.Provider;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XProcessingEnv;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class ProcessingEnvironmentModule_SourceVersionFactory implements Factory<SourceVersion> {
    private final Provider<XProcessingEnv> xProcessingEnvProvider;

    public ProcessingEnvironmentModule_SourceVersionFactory(
            Provider<XProcessingEnv> xProcessingEnvProvider) {
        this.xProcessingEnvProvider = xProcessingEnvProvider;
    }

    @Override
    public SourceVersion get() {
        return sourceVersion(xProcessingEnvProvider.get());
    }

    public static ProcessingEnvironmentModule_SourceVersionFactory create(
            Provider<XProcessingEnv> xProcessingEnvProvider) {
        return new ProcessingEnvironmentModule_SourceVersionFactory(xProcessingEnvProvider);
    }

    public static SourceVersion sourceVersion(XProcessingEnv xProcessingEnv) {
        return Preconditions.checkNotNullFromProvides(ProcessingEnvironmentModule.sourceVersion(xProcessingEnv));
    }
}
