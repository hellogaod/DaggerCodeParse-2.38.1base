package dagger.internal.codegen;

import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.MembersInjectionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.writing.MembersInjectorGenerator;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class SourceFileGeneratorsModule_MembersInjectorGeneratorFactory implements Factory<SourceFileGenerator<MembersInjectionBinding>> {
    private final Provider<MembersInjectorGenerator> generatorProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    public SourceFileGeneratorsModule_MembersInjectorGeneratorFactory(
            Provider<MembersInjectorGenerator> generatorProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        this.generatorProvider = generatorProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
    }

    @Override
    public SourceFileGenerator<MembersInjectionBinding> get() {
        return membersInjectorGenerator(generatorProvider.get(), compilerOptionsProvider.get());
    }

    public static SourceFileGeneratorsModule_MembersInjectorGeneratorFactory create(
            Provider<MembersInjectorGenerator> generatorProvider,
            Provider<CompilerOptions> compilerOptionsProvider) {
        return new SourceFileGeneratorsModule_MembersInjectorGeneratorFactory(generatorProvider, compilerOptionsProvider);
    }

    public static SourceFileGenerator<MembersInjectionBinding> membersInjectorGenerator(
            MembersInjectorGenerator generator, CompilerOptions compilerOptions) {
        return Preconditions.checkNotNullFromProvides(SourceFileGeneratorsModule.membersInjectorGenerator(generator, compilerOptions));
    }
}
