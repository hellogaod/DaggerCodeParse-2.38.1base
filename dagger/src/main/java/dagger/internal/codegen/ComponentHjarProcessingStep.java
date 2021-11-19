package dagger.internal.codegen;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.ComponentDescriptorFactory;
import dagger.internal.codegen.validation.ComponentCreatorValidator;
import dagger.internal.codegen.validation.ComponentValidator;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;
import dagger.internal.codegen.validation.ValidationReport;

import static com.google.auto.common.MoreElements.asType;
import static com.google.common.collect.Sets.union;
import static dagger.internal.codegen.base.ComponentAnnotation.rootComponentAnnotations;
import static dagger.internal.codegen.binding.ComponentCreatorAnnotation.rootComponentCreatorAnnotations;
import static java.util.Collections.disjoint;

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
    private final ComponentValidator componentValidator;
    private final ComponentCreatorValidator creatorValidator;
    private final ComponentDescriptorFactory componentDescriptorFactory;
    private final SourceFileGenerator<ComponentDescriptor> componentGenerator;

    @Inject
    ComponentHjarProcessingStep(
            XMessager messager,
            ComponentValidator componentValidator,
            ComponentCreatorValidator creatorValidator,
            ComponentDescriptorFactory componentDescriptorFactory,
            SourceFileGenerator<ComponentDescriptor> componentGenerator) {
        this.messager = messager;
        this.componentValidator = componentValidator;
        this.creatorValidator = creatorValidator;
        this.componentDescriptorFactory = componentDescriptorFactory;
        this.componentGenerator = componentGenerator;
    }


    @Override
    public Set<ClassName> annotationClassNames() {
        return union(rootComponentAnnotations(), rootComponentCreatorAnnotations());
    }

    // TODO(ronshapiro): Validation might not even be necessary. We should measure it and figure out
    // if it's worth seeing if removing it will still work. We could potentially add a new catch
    // clause for any exception that's not TypeNotPresentException and ignore the component entirely
    // in that case.
    @Override
    protected void process(XTypeElement xElement, ImmutableSet<ClassName> annotations) {
        // TODO(bcorso): Remove conversion to javac type and use XProcessing throughout.
        TypeElement element = XConverters.toJavac(xElement);
        if (!disjoint(annotations, rootComponentAnnotations())) {
            processRootComponent(element);
        }
        if (!disjoint(annotations, rootComponentCreatorAnnotations())) {
            processRootCreator(element);
        }
    }

    private void processRootComponent(TypeElement element) {
        ValidationReport validationReport = componentValidator.validate(element);
        validationReport.printMessagesTo(messager);
//        if (validationReport.isClean()) {
//            componentGenerator.generate(
//                    componentDescriptorFactory.rootComponentDescriptor(element), messager);
//        }
    }

    private void processRootCreator(TypeElement creator) {
        creatorValidator.validate(asType(creator)).printMessagesTo(messager);
    }
}
