package dagger.internal.codegen.base;


/**
 * A template class that provides a framework for properly handling IO while generating source files
 * from an annotation processor. Particularly, it makes a best effort to ensure that files that fail
 * to write successfully are deleted.
 * <p>
 * 文件生成的模板类
 *
 * @param <T> The input type from which source is to be generated.
 */
public abstract class SourceFileGenerator<T> {
}
