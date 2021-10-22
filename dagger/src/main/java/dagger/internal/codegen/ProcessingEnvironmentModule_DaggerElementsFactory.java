package dagger.internal.codegen;

import javax.annotation.Generated;
import javax.inject.Provider;

import androidx.room.compiler.processing.XProcessingEnv;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class ProcessingEnvironmentModule_DaggerElementsFactory implements Factory<DaggerElements> {
    private final Provider<XProcessingEnv> xProcessingEnvProvider;

    public ProcessingEnvironmentModule_DaggerElementsFactory(
            Provider<XProcessingEnv> xProcessingEnvProvider) {
        this.xProcessingEnvProvider = xProcessingEnvProvider;
    }

    @Override
    public DaggerElements get() {
        return daggerElements(xProcessingEnvProvider.get());
    }

    public static ProcessingEnvironmentModule_DaggerElementsFactory create(
            Provider<XProcessingEnv> xProcessingEnvProvider) {
        return new ProcessingEnvironmentModule_DaggerElementsFactory(xProcessingEnvProvider);
    }

    public static DaggerElements daggerElements(XProcessingEnv xProcessingEnv) {
        return Preconditions.checkNotNullFromProvides(ProcessingEnvironmentModule.daggerElements(xProcessingEnv));
    }
}
