package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.compileroption.CompilerOptions;
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


    private final Provider<DaggerTypes> typesProvider;


    private final Provider<CompilerOptions> compilerOptionsProvider;


    public FactoryGenerator_Factory(Provider<DaggerTypes> typesProvider, Provider<CompilerOptions> compilerOptionsProvider) {
        this.typesProvider = typesProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    @Override
    public FactoryGenerator get() {
        return newInstance(typesProvider.get(), compilerOptionsProvider.get());
    }


    public static FactoryGenerator_Factory create(Provider<DaggerTypes> typesProvider, Provider<CompilerOptions> compilerOptionsProvider) {
        return new FactoryGenerator_Factory(typesProvider, compilerOptionsProvider);
    }

    public static FactoryGenerator newInstance(DaggerTypes types, CompilerOptions compilerOptions) {
        return new FactoryGenerator(types, compilerOptions);
    }

}
