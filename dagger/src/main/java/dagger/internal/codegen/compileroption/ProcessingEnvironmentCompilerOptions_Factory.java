package dagger.internal.codegen.compileroption;


import java.util.Map;

import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;

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

    private final Provider<Map<String, String>> optionsProvider;


    public ProcessingEnvironmentCompilerOptions_Factory(Provider<Map<String, String>> optionsProvider) {
        this.optionsProvider = optionsProvider;
    }

    @Override
    public ProcessingEnvironmentCompilerOptions get() {
        return newInstance(optionsProvider.get());
    }

    public static ProcessingEnvironmentCompilerOptions_Factory create(Provider<Map<String, String>> optionsProvider) {
        return new ProcessingEnvironmentCompilerOptions_Factory(optionsProvider);
    }

    public static ProcessingEnvironmentCompilerOptions newInstance(Map<String, String> options) {
        return new ProcessingEnvironmentCompilerOptions(options);
    }
}
