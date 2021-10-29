package dagger.internal.codegen;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.ProductionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.writing.ProducerFactoryGenerator;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class SourceFileGeneratorsModule_ProducerFactoryGeneratorFactory implements Factory<SourceFileGenerator<ProductionBinding>> {
    private final Provider<ProducerFactoryGenerator> generatorProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    public SourceFileGeneratorsModule_ProducerFactoryGeneratorFactory(
            Provider<ProducerFactoryGenerator> generatorProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        this.generatorProvider = generatorProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    @Override
    public SourceFileGenerator<ProductionBinding> get() {
        return producerFactoryGenerator(generatorProvider.get(), compilerOptionsProvider.get());
    }

    public static SourceFileGeneratorsModule_ProducerFactoryGeneratorFactory create(
            Provider<ProducerFactoryGenerator> generatorProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        return new SourceFileGeneratorsModule_ProducerFactoryGeneratorFactory(generatorProvider, compilerOptionsProvider);
    }

    public static SourceFileGenerator<ProductionBinding> producerFactoryGenerator(
            ProducerFactoryGenerator generator, CompilerOptions compilerOptions) {
        return Preconditions.checkNotNullFromProvides(SourceFileGeneratorsModule.producerFactoryGenerator(generator, compilerOptions));
    }
}
