package dagger.internal.codegen;

import javax.inject.Inject;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;

/**
 * A processing step that emits the API of a generated component, without any actual implementation.
 *
 * <p>When compiling a header jar (hjar), Bazel needs to run annotation processors that generate
 * API, like Dagger, to see what code they might output. Full {@link BindingGraph} analysis is
 * costly and unnecessary from the perspective of the header compiler; it's sole goal is to pass
 * along a slimmed down version of what will be the jar for a particular compilation, whether or not
 * that compilation succeeds. If it does not, the compilation pipeline will fail, even if header
 * compilation succeeded.
 * <p>
 * 在编译头文件 jar (hjar) 时，Bazel 需要运行生成 API 的注解处理器，例如 Dagger，以查看它们可能输出什么代码。
 * 从头文件编译器的角度来看，完整的 {@link BindingGraph} 分析是昂贵且不必要的； 它的唯一目标是传递特定编译的 jar 的精简版本，
 * 无论编译是否成功。 如果没有，即使头文件编译成功，编译管道也会失败。
 *
 * <p>The components emitted by this processing step include all of the API elements exposed by the
 * normal step. Method bodies are omitted as Turbine ignores them entirely.
 */
final class ComponentHjarProcessingStep extends TypeCheckingProcessingStep<XTypeElement> {

    private final XMessager messager;

    @Inject
    ComponentHjarProcessingStep(
            XMessager messager
    ) {
        this.messager = messager;
    }
}
