package dagger.internal.codegen;


import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.Sets;

import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.BindingFactory;
import dagger.internal.codegen.binding.DelegateDeclaration;
import dagger.internal.codegen.binding.ProductionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.validation.ModuleValidator;
import dagger.internal.codegen.validation.TypeCheckingProcessingStep;
import dagger.internal.codegen.writing.InaccessibleMapKeyProxyGenerator;
import dagger.internal.codegen.writing.ModuleGenerator;

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

}
