package dagger.internal.codegen.validation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.InjectionAnnotations;
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
public final class InjectValidator_Factory implements Factory<InjectValidator> {
    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<DependencyRequestValidator> dependencyRequestValidatorProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    private final Provider<InjectionAnnotations> injectionAnnotationsProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    public InjectValidator_Factory(Provider<DaggerTypes> typesProvider,
                                   Provider<DaggerElements> elementsProvider,
                                   Provider<DependencyRequestValidator> dependencyRequestValidatorProvider,
                                   Provider<CompilerOptions> compilerOptionsProvider,
                                   Provider<InjectionAnnotations> injectionAnnotationsProvider,
                                   Provider<KotlinMetadataUtil> metadataUtilProvider) {
        this.typesProvider = typesProvider;
        this.elementsProvider = elementsProvider;
        this.dependencyRequestValidatorProvider = dependencyRequestValidatorProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
        this.injectionAnnotationsProvider = injectionAnnotationsProvider;
        this.metadataUtilProvider = metadataUtilProvider;
    }

    @Override
    public InjectValidator get() {
        return newInstance(typesProvider.get(), elementsProvider.get(), dependencyRequestValidatorProvider.get(), compilerOptionsProvider.get(), injectionAnnotationsProvider.get(), metadataUtilProvider.get());
    }

    public static InjectValidator_Factory create(Provider<DaggerTypes> typesProvider,
                                                 Provider<DaggerElements> elementsProvider,
                                                 Provider<DependencyRequestValidator> dependencyRequestValidatorProvider,
                                                 Provider<CompilerOptions> compilerOptionsProvider,
                                                 Provider<InjectionAnnotations> injectionAnnotationsProvider,
                                                 Provider<KotlinMetadataUtil> metadataUtilProvider) {
        return new InjectValidator_Factory(typesProvider, elementsProvider, dependencyRequestValidatorProvider, compilerOptionsProvider, injectionAnnotationsProvider, metadataUtilProvider);
    }

    public static InjectValidator newInstance(DaggerTypes types, DaggerElements elements,
                                              Object dependencyRequestValidator, CompilerOptions compilerOptions,
                                              InjectionAnnotations injectionAnnotations, KotlinMetadataUtil metadataUtil) {
        return new InjectValidator(types, elements, (DependencyRequestValidator) dependencyRequestValidator, compilerOptions, injectionAnnotations, metadataUtil);
    }
}
