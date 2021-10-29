package dagger.internal.codegen.validation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.InjectionAnnotations;
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
public final class ProvidesMethodValidator_Factory implements Factory<ProvidesMethodValidator> {

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<KotlinMetadataUtil> kotlinMetadataUtilProvider;

    private final Provider<DependencyRequestValidator> dependencyRequestValidatorProvider;

    private final Provider<InjectionAnnotations> injectionAnnotationsProvider;

    public ProvidesMethodValidator_Factory(Provider<DaggerElements> elementsProvider,
                                           Provider<DaggerTypes> typesProvider, Provider<KotlinMetadataUtil> kotlinMetadataUtilProvider,
                                           Provider<DependencyRequestValidator> dependencyRequestValidatorProvider,
                                           Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        this.elementsProvider = elementsProvider;
        this.typesProvider = typesProvider;
        this.kotlinMetadataUtilProvider = kotlinMetadataUtilProvider;
        this.dependencyRequestValidatorProvider = dependencyRequestValidatorProvider;
        this.injectionAnnotationsProvider = injectionAnnotationsProvider;
    }

    @Override
    public ProvidesMethodValidator get() {
        return newInstance(elementsProvider.get(), typesProvider.get(), kotlinMetadataUtilProvider.get(), dependencyRequestValidatorProvider.get(), injectionAnnotationsProvider.get());
    }

    public static ProvidesMethodValidator_Factory create(Provider<DaggerElements> elementsProvider,
                                                         Provider<DaggerTypes> typesProvider, Provider<KotlinMetadataUtil> kotlinMetadataUtilProvider,
                                                         Provider<DependencyRequestValidator> dependencyRequestValidatorProvider,
                                                         Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        return new ProvidesMethodValidator_Factory(elementsProvider, typesProvider, kotlinMetadataUtilProvider, dependencyRequestValidatorProvider, injectionAnnotationsProvider);
    }

    public static ProvidesMethodValidator newInstance(DaggerElements elements, DaggerTypes types,
                                                      KotlinMetadataUtil kotlinMetadataUtil, Object dependencyRequestValidator,
                                                      InjectionAnnotations injectionAnnotations) {
        return new ProvidesMethodValidator(elements, types, kotlinMetadataUtil, (DependencyRequestValidator) dependencyRequestValidator, injectionAnnotations);
    }
}
