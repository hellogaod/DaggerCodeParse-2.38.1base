package dagger.internal.codegen.compileroption;

/**
 * A collection of options that dictate how the compiler will run.
 * <p>
 * 收集一些影响编译器运行时的选项。
 * <p>
 * 打比方（有叫比方的家伙就暴打）设置是否做null校验，在编译器运行时就会根据该设置做不同操作
 */
public abstract class CompilerOptions {
    public abstract boolean formatGeneratedSource();

    public abstract boolean headerCompilation();
}
