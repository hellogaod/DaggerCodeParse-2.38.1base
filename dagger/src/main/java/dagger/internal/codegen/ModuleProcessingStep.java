package dagger.internal.codegen;


import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import androidx.room.compiler.processing.XElement;
import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XProcessingEnv;
import androidx.room.compiler.processing.XTypeElement;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.BindingFactory;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.DelegateDeclaration;
import dagger.internal.codegen.binding.ProductionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.validation.ModuleValidator;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;
import dagger.internal.codegen.validation.ValidationReport;
import dagger.internal.codegen.writing.InaccessibleMapKeyProxyGenerator;
import dagger.internal.codegen.writing.ModuleGenerator;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.internal.codegen.langmodel.DaggerElements.isAnnotationPresent;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * A {@link BasicAnnotationProcessor.ProcessingStep} that validates module classes and generates factories for binding
 * methods.
 */
final class ModuleProcessingStep extends TypeCheckingProcessingStep<XTypeElement> {

    private final XMessager messager;
    private final ModuleValidator moduleValidator;
    private final BindingFactory bindingFactory;
    private final SourceFileGenerator<ProvisionBinding> factoryGenerator;
    private final SourceFileGenerator<ProductionBinding> producerFactoryGenerator;
    private final SourceFileGenerator<TypeElement> moduleConstructorProxyGenerator;
    private final InaccessibleMapKeyProxyGenerator inaccessibleMapKeyProxyGenerator;
    private final DelegateDeclaration.Factory delegateDeclarationFactory;
    private final KotlinMetadataUtil metadataUtil;
    private final Set<TypeElement> processedModuleElements = Sets.newLinkedHashSet();

    @Inject
    ModuleProcessingStep(
            XMessager messager,
            ModuleValidator moduleValidator,
            BindingFactory bindingFactory,
            SourceFileGenerator<ProvisionBinding> factoryGenerator,
            SourceFileGenerator<ProductionBinding> producerFactoryGenerator,
            @ModuleGenerator SourceFileGenerator<TypeElement> moduleConstructorProxyGenerator,
            InaccessibleMapKeyProxyGenerator inaccessibleMapKeyProxyGenerator,
            DelegateDeclaration.Factory delegateDeclarationFactory,
            KotlinMetadataUtil metadataUtil
    ) {
        this.messager = messager;
        this.moduleValidator = moduleValidator;
        this.bindingFactory = bindingFactory;
        this.factoryGenerator = factoryGenerator;
        this.producerFactoryGenerator = producerFactoryGenerator;
        this.moduleConstructorProxyGenerator = moduleConstructorProxyGenerator;
        this.inaccessibleMapKeyProxyGenerator = inaccessibleMapKeyProxyGenerator;
        this.delegateDeclarationFactory = delegateDeclarationFactory;
        this.metadataUtil = metadataUtil;
    }

    @Override
    public ImmutableSet<ClassName> annotationClassNames() {
        return ImmutableSet.of(TypeNames.MODULE, TypeNames.PRODUCER_MODULE);
    }

    @Override
    public ImmutableSet<XElement> process(
            XProcessingEnv env, Map<String, ? extends Set<? extends XElement>> elementsByAnnotation) {

        moduleValidator.addKnownModules(
                elementsByAnnotation.values().stream()
                        .flatMap(Set::stream)
                        .map(XConverters::toJavac)
                        .map(MoreElements::asType)
                        .collect(toImmutableSet())
        );

        return super.process(env, elementsByAnnotation);
    }


    @Override
    protected void process(XTypeElement xElement, ImmutableSet<ClassName> annotations) {

        // TODO(bcorso): Remove conversion to javac type and use XProcessing throughout.
        TypeElement module = XConverters.toJavac(xElement);
        if (processedModuleElements.contains(module)) {
            return;
        }
        // For backwards compatibility, we allow a companion object to be annotated with @Module even
        // though it's no longer required. However, we skip processing the companion object itself
        // because it will now be processed when processing the companion object's enclosing class.
        if (metadataUtil.isCompanionObjectClass(module)) {
            // TODO(danysantiago): Be strict about annotating companion objects with @Module,
            //  i.e. tell user to annotate parent instead.
            return;
        }
        ValidationReport report = moduleValidator.validate(module);
        report.printMessagesTo(messager);
        if (report.isClean()) {
            generateForMethodsIn(module);
            if (metadataUtil.hasEnclosedCompanionObject(module)) {
                generateForMethodsIn(metadataUtil.getEnclosedCompanionObject(module));
            }
        }
        processedModuleElements.add(module);
    }

    private void generateForMethodsIn(TypeElement module) {
        for (ExecutableElement method : methodsIn(module.getEnclosedElements())) {
            if (isAnnotationPresent(method, TypeNames.PROVIDES)) {
                generate(factoryGenerator, bindingFactory.providesMethodBinding(method, module));
            } else if (isAnnotationPresent(method, TypeNames.PRODUCES)) {
                generate(producerFactoryGenerator, bindingFactory.producesMethodBinding(method, module));
            } else if (isAnnotationPresent(method, TypeNames.BINDS)) {
                inaccessibleMapKeyProxyGenerator.generate(bindsMethodBinding(module, method), messager);
            }
        }
        moduleConstructorProxyGenerator.generate(module, messager);
    }

    private <B extends ContributionBinding> void generate(
            SourceFileGenerator<B> generator, B binding) {
        generator.generate(binding, messager);
        inaccessibleMapKeyProxyGenerator.generate(binding, messager);
    }

    private ContributionBinding bindsMethodBinding(TypeElement module, ExecutableElement method) {
        return bindingFactory.unresolvedDelegateBinding(
                delegateDeclarationFactory.create(method, module));
    }
}
