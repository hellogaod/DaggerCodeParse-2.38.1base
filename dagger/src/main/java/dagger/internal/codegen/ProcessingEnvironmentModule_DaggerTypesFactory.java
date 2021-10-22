package dagger.internal.codegen;

import javax.annotation.Generated;
import javax.inject.Provider;

import androidx.room.compiler.processing.XProcessingEnv;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class ProcessingEnvironmentModule_DaggerTypesFactory implements Factory<DaggerTypes> {
    private final Provider<XProcessingEnv> xProcessingEnvProvider;

    private final Provider<DaggerElements> elementsProvider;

    public ProcessingEnvironmentModule_DaggerTypesFactory(
            Provider<XProcessingEnv> xProcessingEnvProvider, Provider<DaggerElements> elementsProvider) {
        this.xProcessingEnvProvider = xProcessingEnvProvider;
        this.elementsProvider = elementsProvider;
    }

    @Override
    public DaggerTypes get() {
        return daggerTypes(xProcessingEnvProvider.get(), elementsProvider.get());
    }

    public static ProcessingEnvironmentModule_DaggerTypesFactory create(
            Provider<XProcessingEnv> xProcessingEnvProvider, Provider<DaggerElements> elementsProvider) {
        return new ProcessingEnvironmentModule_DaggerTypesFactory(xProcessingEnvProvider, elementsProvider);
    }

    public static DaggerTypes daggerTypes(XProcessingEnv xProcessingEnv, DaggerElements elements) {
        return Preconditions.checkNotNullFromProvides(ProcessingEnvironmentModule.daggerTypes(xProcessingEnv, elements));
    }
}
