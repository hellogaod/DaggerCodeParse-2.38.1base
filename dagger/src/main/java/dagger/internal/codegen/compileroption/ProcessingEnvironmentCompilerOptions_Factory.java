package dagger.internal.codegen.compileroption;


import java.util.Map;

import javax.annotation.Generated;
import javax.inject.Provider;

import androidx.room.compiler.processing.XMessager;
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
public final class ProcessingEnvironmentCompilerOptions_Factory implements Factory<ProcessingEnvironmentCompilerOptions> {
    private final Provider<XMessager> messagerProvider;

    private final Provider<Map<String, String>> optionsProvider;

    private final Provider<DaggerElements> elementsProvider;

    public ProcessingEnvironmentCompilerOptions_Factory(Provider<XMessager> messagerProvider,
                                                        Provider<Map<String, String>> optionsProvider, Provider<DaggerElements> elementsProvider) {
        this.messagerProvider = messagerProvider;
        this.optionsProvider = optionsProvider;
        this.elementsProvider = elementsProvider;
    }

    @Override
    public ProcessingEnvironmentCompilerOptions get() {
        return newInstance(messagerProvider.get(), optionsProvider.get(), elementsProvider.get());
    }

    public static ProcessingEnvironmentCompilerOptions_Factory create(
            Provider<XMessager> messagerProvider, Provider<Map<String, String>> optionsProvider,
            Provider<DaggerElements> elementsProvider) {
        return new ProcessingEnvironmentCompilerOptions_Factory(messagerProvider, optionsProvider, elementsProvider);
    }

    public static ProcessingEnvironmentCompilerOptions newInstance(XMessager messager,
                                                                   Map<String, String> options, DaggerElements elements) {
        return new ProcessingEnvironmentCompilerOptions(messager, options, elements);
    }
}
