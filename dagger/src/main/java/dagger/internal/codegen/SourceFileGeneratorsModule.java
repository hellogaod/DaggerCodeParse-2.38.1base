package dagger.internal.codegen;


import dagger.Module;
import dagger.Provides;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.MembersInjectionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.writing.FactoryGenerator;
import dagger.internal.codegen.writing.HjarSourceFileGenerator;
import dagger.internal.codegen.writing.MembersInjectorGenerator;

@Module
abstract class SourceFileGeneratorsModule {
    @Provides
    static SourceFileGenerator<ProvisionBinding> factoryGenerator(
            FactoryGenerator generator,
            CompilerOptions compilerOptions) {
        return hjarWrapper(generator, compilerOptions);
    }

    @Provides
    static SourceFileGenerator<MembersInjectionBinding> membersInjectorGenerator(
            MembersInjectorGenerator generator,
            CompilerOptions compilerOptions
    ) {
        return hjarWrapper(generator, compilerOptions);
    }

    private static <T> SourceFileGenerator<T> hjarWrapper(
            SourceFileGenerator<T> generator, CompilerOptions compilerOptions) {
        return compilerOptions.headerCompilation()
                ? HjarSourceFileGenerator.wrap(generator)
                : generator;
    }
}
