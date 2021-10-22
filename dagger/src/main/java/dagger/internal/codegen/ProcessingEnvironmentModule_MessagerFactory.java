package dagger.internal.codegen;


import javax.annotation.Generated;
import javax.inject.Provider;

import androidx.room.compiler.processing.XMessager;
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
public final class ProcessingEnvironmentModule_MessagerFactory implements Factory<XMessager> {
    private final Provider<XProcessingEnv> xProcessingEnvProvider;

    public ProcessingEnvironmentModule_MessagerFactory(
            Provider<XProcessingEnv> xProcessingEnvProvider) {
        this.xProcessingEnvProvider = xProcessingEnvProvider;
    }

    @Override
    public XMessager get() {
        return messager(xProcessingEnvProvider.get());
    }

    public static ProcessingEnvironmentModule_MessagerFactory create(
            Provider<XProcessingEnv> xProcessingEnvProvider) {
        return new ProcessingEnvironmentModule_MessagerFactory(xProcessingEnvProvider);
    }

    public static XMessager messager(XProcessingEnv xProcessingEnv) {
        return Preconditions.checkNotNullFromProvides(ProcessingEnvironmentModule.messager(xProcessingEnv));
    }
}
