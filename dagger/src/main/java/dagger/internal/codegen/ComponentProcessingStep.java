package dagger.internal.codegen;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.squareup.javapoet.ClassName;

import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.BindingGraphFactory;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.ComponentDescriptorFactory;
import dagger.internal.codegen.validation.BindingGraphValidator;
import dagger.internal.codegen.validation.ComponentCreatorValidator;
import dagger.internal.codegen.validation.ComponentDescriptorValidator;
import dagger.internal.codegen.validation.ComponentValidator;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;
import dagger.internal.codegen.validation.ValidationReport;

import static com.google.auto.common.MoreElements.asType;
import static com.google.common.collect.Sets.union;
import static dagger.internal.codegen.base.ComponentAnnotation.allComponentAnnotations;
import static dagger.internal.codegen.base.ComponentAnnotation.rootComponentAnnotations;
import static dagger.internal.codegen.base.ComponentAnnotation.subcomponentAnnotations;
import static dagger.internal.codegen.binding.ComponentCreatorAnnotation.allCreatorAnnotations;
import static java.util.Collections.disjoint;

/**
 * A {@link BasicAnnotationProcessor.ProcessingStep} that is responsible for dealing with a component or production component
 * as part of the {@link ComponentProcessor}.
 */
final class ComponentProcessingStep extends TypeCheckingProcessingStep<XTypeElement> {

    private final XMessager messager;
    private final ComponentValidator componentValidator;
    private final ComponentCreatorValidator creatorValidator;
    private final ComponentDescriptorValidator componentDescriptorValidator;
    private final ComponentDescriptorFactory componentDescriptorFactory;
    private final BindingGraphFactory bindingGraphFactory;
    private final SourceFileGenerator<BindingGraph> componentGenerator;
    private final BindingGraphValidator bindingGraphValidator;

    @Inject
    ComponentProcessingStep(
            XMessager messager,
            ComponentValidator componentValidator,
            ComponentCreatorValidator creatorValidator,
            ComponentDescriptorValidator componentDescriptorValidator,
            ComponentDescriptorFactory componentDescriptorFactory,
            BindingGraphFactory bindingGraphFactory,
            SourceFileGenerator<BindingGraph> componentGenerator,
            BindingGraphValidator bindingGraphValidator) {
        this.messager = messager;
        this.componentValidator = componentValidator;
        this.creatorValidator = creatorValidator;
        this.componentDescriptorValidator = componentDescriptorValidator;
        this.componentDescriptorFactory = componentDescriptorFactory;
        this.bindingGraphFactory = bindingGraphFactory;
        this.componentGenerator = componentGenerator;
        this.bindingGraphValidator = bindingGraphValidator;
    }

    @Override
    public Set<ClassName> annotationClassNames() {
        //第一个表示component相关注解，第二个表示component的builder或factory相关注解
        return union(allComponentAnnotations(), allCreatorAnnotations());
    }

    @Override
    protected void process(XTypeElement xElement, ImmutableSet<ClassName> annotations) {

        // TODO(bcorso): Remove conversion to javac type and use XProcessing throughout.
        TypeElement element = XConverters.toJavac(xElement);
        //如果存在于component相关注解中
        if (!disjoint(annotations, rootComponentAnnotations())) {
            processRootComponent(element);
        }

        //如果存在于subcomponent相关注解中
        if (!disjoint(annotations, subcomponentAnnotations())) {
            processSubcomponent(element);
        }
        //如果存在于Builder或Factory相关注解中
        if (!disjoint(annotations, allCreatorAnnotations())) {
            processCreator(element);
        }
    }

    private void processRootComponent(TypeElement component) {
        if (!isComponentValid(component)) {
            return;
        }
        //重头戏，生成ComponentDescriptor对象
        ComponentDescriptor componentDescriptor =
                componentDescriptorFactory.rootComponentDescriptor(component);

        if (!isValid(componentDescriptor)) {
            return;
        }

        if (!validateFullBindingGraph(componentDescriptor)) {
            return;
        }
        BindingGraph bindingGraph = bindingGraphFactory.create(componentDescriptor, false);
        if (bindingGraphValidator.isValid(bindingGraph.topLevelBindingGraph())) {
            generateComponent(bindingGraph);
        }
    }

    private void processSubcomponent(TypeElement subcomponent) {
        if (!isComponentValid(subcomponent)) {
            return;
        }
        ComponentDescriptor subcomponentDescriptor =
                componentDescriptorFactory.subcomponentDescriptor(subcomponent);
        // TODO(dpb): ComponentDescriptorValidator for subcomponents, as we do for root components.
        validateFullBindingGraph(subcomponentDescriptor);
    }

    private void generateComponent(BindingGraph bindingGraph) {
        componentGenerator.generate(bindingGraph, messager);
    }

    //校验component 的Builder或Factory注解
    private void processCreator(Element creator) {
        creatorValidator.validate(MoreElements.asType(creator)).printMessagesTo(messager);
    }

    //校验component
    private boolean isComponentValid(Element component) {
        ValidationReport report = componentValidator.validate(asType(component));
        report.printMessagesTo(messager);
        return report.isClean();
    }

    @CanIgnoreReturnValue
    private boolean validateFullBindingGraph(ComponentDescriptor componentDescriptor) {

        TypeElement component = componentDescriptor.typeElement();

        if (!bindingGraphValidator.shouldDoFullBindingGraphValidation(component)) {
            return true;
        }

        BindingGraph fullBindingGraph = bindingGraphFactory.create(componentDescriptor, true);
        return bindingGraphValidator.isValid(fullBindingGraph.topLevelBindingGraph());
    }

    private boolean isValid(ComponentDescriptor componentDescriptor) {
        ValidationReport componentDescriptorReport =
                componentDescriptorValidator.validate(componentDescriptor);
        componentDescriptorReport.printMessagesTo(messager);
        return componentDescriptorReport.isClean();
    }
}
