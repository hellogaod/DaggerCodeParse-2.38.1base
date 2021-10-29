package dagger.internal.codegen.validation;

import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.DependencyRequestFactory;
import dagger.internal.codegen.binding.MethodSignatureFormatter;
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
public final class ComponentValidator_Factory implements Factory<ComponentValidator> {
    private final Provider<DaggerElements> elementsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<ModuleValidator> moduleValidatorProvider;

    private final Provider<ComponentCreatorValidator> creatorValidatorProvider;

    private final Provider<DependencyRequestValidator> dependencyRequestValidatorProvider;

    private final Provider<MembersInjectionValidator> membersInjectionValidatorProvider;

    private final Provider<MethodSignatureFormatter> methodSignatureFormatterProvider;

    private final Provider<DependencyRequestFactory> dependencyRequestFactoryProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    public ComponentValidator_Factory(Provider<DaggerElements> elementsProvider,
                                      Provider<DaggerTypes> typesProvider, Provider<ModuleValidator> moduleValidatorProvider,
                                      Provider<ComponentCreatorValidator> creatorValidatorProvider,
                                      Provider<DependencyRequestValidator> dependencyRequestValidatorProvider,
                                      Provider<MembersInjectionValidator> membersInjectionValidatorProvider,
                                      Provider<MethodSignatureFormatter> methodSignatureFormatterProvider,
                                      Provider<DependencyRequestFactory> dependencyRequestFactoryProvider,
                                      Provider<KotlinMetadataUtil> metadataUtilProvider) {
        this.elementsProvider = elementsProvider;
        this.typesProvider = typesProvider;
        this.moduleValidatorProvider = moduleValidatorProvider;
        this.creatorValidatorProvider = creatorValidatorProvider;
        this.dependencyRequestValidatorProvider = dependencyRequestValidatorProvider;
        this.membersInjectionValidatorProvider = membersInjectionValidatorProvider;
        this.methodSignatureFormatterProvider = methodSignatureFormatterProvider;
        this.dependencyRequestFactoryProvider = dependencyRequestFactoryProvider;
        this.metadataUtilProvider = metadataUtilProvider;
    }

    @Override
    public ComponentValidator get() {
        return newInstance(elementsProvider.get(), typesProvider.get(), moduleValidatorProvider.get(), creatorValidatorProvider.get(), dependencyRequestValidatorProvider.get(), membersInjectionValidatorProvider.get(), methodSignatureFormatterProvider.get(), dependencyRequestFactoryProvider.get(), metadataUtilProvider.get());
    }

    public static ComponentValidator_Factory create(Provider<DaggerElements> elementsProvider,
                                                    Provider<DaggerTypes> typesProvider, Provider<ModuleValidator> moduleValidatorProvider,
                                                    Provider<ComponentCreatorValidator> creatorValidatorProvider,
                                                    Provider<DependencyRequestValidator> dependencyRequestValidatorProvider,
                                                    Provider<MembersInjectionValidator> membersInjectionValidatorProvider,
                                                    Provider<MethodSignatureFormatter> methodSignatureFormatterProvider,
                                                    Provider<DependencyRequestFactory> dependencyRequestFactoryProvider,
                                                    Provider<KotlinMetadataUtil> metadataUtilProvider) {
        return new ComponentValidator_Factory(elementsProvider, typesProvider, moduleValidatorProvider, creatorValidatorProvider, dependencyRequestValidatorProvider, membersInjectionValidatorProvider, methodSignatureFormatterProvider, dependencyRequestFactoryProvider, metadataUtilProvider);
    }

    public static ComponentValidator newInstance(DaggerElements elements, DaggerTypes types,
                                                 ModuleValidator moduleValidator, ComponentCreatorValidator creatorValidator,
                                                 Object dependencyRequestValidator, Object membersInjectionValidator,
                                                 MethodSignatureFormatter methodSignatureFormatter,
                                                 DependencyRequestFactory dependencyRequestFactory, KotlinMetadataUtil metadataUtil) {
        return new ComponentValidator(elements, types, moduleValidator, creatorValidator, (DependencyRequestValidator) dependencyRequestValidator, (MembersInjectionValidator) membersInjectionValidator, methodSignatureFormatter, dependencyRequestFactory, metadataUtil);
    }
}
