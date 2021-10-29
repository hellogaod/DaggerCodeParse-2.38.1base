package dagger.internal.codegen.validation;

import javax.inject.Inject;

import dagger.internal.codegen.binding.MethodSignatureFormatter;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

public final class ComponentDescriptorValidator {

    private final DaggerElements elements;
    private final DaggerTypes types;
    private final CompilerOptions compilerOptions;
    private final MethodSignatureFormatter methodSignatureFormatter;
    private final ComponentHierarchyValidator componentHierarchyValidator;
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    ComponentDescriptorValidator(
            DaggerElements elements,
            DaggerTypes types,
            CompilerOptions compilerOptions,
            MethodSignatureFormatter methodSignatureFormatter,
            ComponentHierarchyValidator componentHierarchyValidator,
            KotlinMetadataUtil metadataUtil) {
        this.elements = elements;
        this.types = types;
        this.compilerOptions = compilerOptions;
        this.methodSignatureFormatter = methodSignatureFormatter;
        this.componentHierarchyValidator = componentHierarchyValidator;
        this.metadataUtil = metadataUtil;
    }
}
