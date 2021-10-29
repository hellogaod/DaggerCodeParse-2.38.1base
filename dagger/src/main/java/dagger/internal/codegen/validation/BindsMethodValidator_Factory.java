package dagger.internal.codegen.validation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.BindsTypeChecker;
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
public final class BindsMethodValidator_Factory implements Factory<BindsMethodValidator> {

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<KotlinMetadataUtil> kotlinMetadataUtilProvider;

    private final Provider<BindsTypeChecker> bindsTypeCheckerProvider;

    private final Provider<DependencyRequestValidator> dependencyRequestValidatorProvider;

    private final Provider<InjectionAnnotations> injectionAnnotationsProvider;

    public BindsMethodValidator_Factory(Provider<DaggerElements> elementsProvider,
                                        Provider<DaggerTypes> typesProvider, Provider<KotlinMetadataUtil> kotlinMetadataUtilProvider,
                                        Provider<BindsTypeChecker> bindsTypeCheckerProvider,
                                        Provider<DependencyRequestValidator> dependencyRequestValidatorProvider,
                                        Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        this.elementsProvider = elementsProvider;
        this.typesProvider = typesProvider;
        this.kotlinMetadataUtilProvider = kotlinMetadataUtilProvider;
        this.bindsTypeCheckerProvider = bindsTypeCheckerProvider;
        this.dependencyRequestValidatorProvider = dependencyRequestValidatorProvider;
        this.injectionAnnotationsProvider = injectionAnnotationsProvider;
    }

    @Override
    public BindsMethodValidator get() {
        return newInstance(elementsProvider.get(), typesProvider.get(), kotlinMetadataUtilProvider.get(), bindsTypeCheckerProvider.get(), dependencyRequestValidatorProvider.get(), injectionAnnotationsProvider.get());
    }

    public static BindsMethodValidator_Factory create(Provider<DaggerElements> elementsProvider,
                                                      Provider<DaggerTypes> typesProvider, Provider<KotlinMetadataUtil> kotlinMetadataUtilProvider,
                                                      Provider<BindsTypeChecker> bindsTypeCheckerProvider,
                                                      Provider<DependencyRequestValidator> dependencyRequestValidatorProvider,
                                                      Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        return new BindsMethodValidator_Factory(elementsProvider, typesProvider, kotlinMetadataUtilProvider, bindsTypeCheckerProvider, dependencyRequestValidatorProvider, injectionAnnotationsProvider);
    }

    public static BindsMethodValidator newInstance(DaggerElements elements, DaggerTypes types,
                                                   KotlinMetadataUtil kotlinMetadataUtil, BindsTypeChecker bindsTypeChecker,
                                                   Object dependencyRequestValidator, InjectionAnnotations injectionAnnotations) {
        return new BindsMethodValidator(elements, types, kotlinMetadataUtil, bindsTypeChecker, (DependencyRequestValidator) dependencyRequestValidator, injectionAnnotations);
    }
}
