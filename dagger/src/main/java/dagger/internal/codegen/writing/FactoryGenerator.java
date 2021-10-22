package dagger.internal.codegen.writing;

import javax.inject.Inject;

import dagger.internal.Factory;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Generates {@link Factory} implementations from {@link ProvisionBinding} instances for {@link
 * Inject} constructors.
 * <p>
 * 针对Inject修饰的构造函数转换的ProvisionBinding对象生成代码
 */
public final class FactoryGenerator extends SourceFileGenerator<ProvisionBinding> {

    private final DaggerTypes types;
    private final CompilerOptions compilerOptions;

    @Inject
    FactoryGenerator(
//            XFiler filer,
//            SourceVersion sourceVersion,
            DaggerTypes types,
//            DaggerElements elements,
            CompilerOptions compilerOptions) {
        this.types = types;
        this.compilerOptions = compilerOptions;
    }
}
