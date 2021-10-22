package dagger.internal.codegen.writing;

import dagger.internal.codegen.base.SourceFileGenerator;

/**
 * A source file generator that only writes the relevant code necessary for Bazel to create a
 * correct header (ABI) jar.
 * <p>
 * 一个源文件生成器，只编写 Bazel 所需的相关代码，以创建正确的标头 (ABI) jar。
 */
public final class HjarSourceFileGenerator<T> extends SourceFileGenerator<T> {
    private final SourceFileGenerator<T> delegate;

    private HjarSourceFileGenerator(SourceFileGenerator<T> delegate) {

        this.delegate = delegate;
    }

    public static <T> SourceFileGenerator<T> wrap(SourceFileGenerator<T> delegate) {
        return new HjarSourceFileGenerator<>(delegate);
    }
}
