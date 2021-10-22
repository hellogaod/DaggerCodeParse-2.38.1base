package dagger.internal.codegen;


import java.util.Map;

import javax.annotation.Generated;
import javax.inject.Provider;

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
public final class ProcessingEnvironmentModule_ProcessingOptionsFactory implements Factory<Map<String, String>> {
    private final Provider<XProcessingEnv> xProcessingEnvProvider;

    public ProcessingEnvironmentModule_ProcessingOptionsFactory(
            Provider<XProcessingEnv> xProcessingEnvProvider) {
        this.xProcessingEnvProvider = xProcessingEnvProvider;
    }

    @Override
    public Map<String, String> get() {
        return processingOptions(xProcessingEnvProvider.get());
    }

    public static ProcessingEnvironmentModule_ProcessingOptionsFactory create(
            Provider<XProcessingEnv> xProcessingEnvProvider) {
        return new ProcessingEnvironmentModule_ProcessingOptionsFactory(xProcessingEnvProvider);
    }

    public static Map<String, String> processingOptions(XProcessingEnv xProcessingEnv) {
        return Preconditions.checkNotNullFromProvides(ProcessingEnvironmentModule.processingOptions(xProcessingEnv));
    }
}
