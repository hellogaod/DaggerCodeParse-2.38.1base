package dagger.internal.codegen;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.writing.FactoryGenerator;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class SourceFileGeneratorsModule_FactoryGeneratorFactory implements Factory<SourceFileGenerator<ProvisionBinding>> {

    private final Provider<FactoryGenerator> generatorProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    public SourceFileGeneratorsModule_FactoryGeneratorFactory(
            Provider<FactoryGenerator> generatorProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        this.generatorProvider = generatorProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    @Override
    public SourceFileGenerator<ProvisionBinding> get() {
        return factoryGenerator(generatorProvider.get(), compilerOptionsProvider.get());
    }

    public static SourceFileGeneratorsModule_FactoryGeneratorFactory create(
            Provider<FactoryGenerator> generatorProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        return new SourceFileGeneratorsModule_FactoryGeneratorFactory(generatorProvider, compilerOptionsProvider);
    }

    public static SourceFileGenerator<ProvisionBinding> factoryGenerator(FactoryGenerator generator,
                                                                         CompilerOptions compilerOptions) {
        return Preconditions.checkNotNullFromProvides(SourceFileGeneratorsModule.factoryGenerator(generator, compilerOptions));
    }
}
