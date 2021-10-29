package dagger.internal.codegen.validation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.MethodSignatureFormatter;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class ComponentDescriptorValidator_Factory implements Factory<ComponentDescriptorValidator> {
    private final Provider<DaggerElements> elementsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    private final Provider<MethodSignatureFormatter> methodSignatureFormatterProvider;

    private final Provider<ComponentHierarchyValidator> componentHierarchyValidatorProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    public ComponentDescriptorValidator_Factory(Provider<DaggerElements> elementsProvider,
                                                Provider<DaggerTypes> typesProvider, Provider<CompilerOptions> compilerOptionsProvider,
                                                Provider<MethodSignatureFormatter> methodSignatureFormatterProvider,
                                                Provider<ComponentHierarchyValidator> componentHierarchyValidatorProvider,
                                                Provider<KotlinMetadataUtil> metadataUtilProvider) {
        this.elementsProvider = elementsProvider;
        this.typesProvider = typesProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
        this.methodSignatureFormatterProvider = methodSignatureFormatterProvider;
        this.componentHierarchyValidatorProvider = componentHierarchyValidatorProvider;
        this.metadataUtilProvider = metadataUtilProvider;
    }

    @Override
    public ComponentDescriptorValidator get() {
        return newInstance(elementsProvider.get(), typesProvider.get(), compilerOptionsProvider.get(), methodSignatureFormatterProvider.get(), componentHierarchyValidatorProvider.get(), metadataUtilProvider.get());
    }

    public static ComponentDescriptorValidator_Factory create(
            Provider<DaggerElements> elementsProvider, Provider<DaggerTypes> typesProvider,
            Provider<CompilerOptions> compilerOptionsProvider,
            Provider<MethodSignatureFormatter> methodSignatureFormatterProvider,
            Provider<ComponentHierarchyValidator> componentHierarchyValidatorProvider,
            Provider<KotlinMetadataUtil> metadataUtilProvider) {
        return new ComponentDescriptorValidator_Factory(elementsProvider, typesProvider, compilerOptionsProvider, methodSignatureFormatterProvider, componentHierarchyValidatorProvider, metadataUtilProvider);
    }

    public static ComponentDescriptorValidator newInstance(DaggerElements elements, DaggerTypes types,
                                                           CompilerOptions compilerOptions, MethodSignatureFormatter methodSignatureFormatter,
                                                           Object componentHierarchyValidator, KotlinMetadataUtil metadataUtil) {
        return new ComponentDescriptorValidator(elements, types, compilerOptions, methodSignatureFormatter, (ComponentHierarchyValidator) componentHierarchyValidator, metadataUtil);
    }
}
