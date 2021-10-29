package dagger.internal.codegen.validation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class DependencyRequestValidator_Factory implements Factory<DependencyRequestValidator> {
    private final Provider<MembersInjectionValidator> membersInjectionValidatorProvider;

    private final Provider<InjectionAnnotations> injectionAnnotationsProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    private final Provider<DaggerElements> elementsProvider;

    public DependencyRequestValidator_Factory(
            Provider<MembersInjectionValidator> membersInjectionValidatorProvider,
            Provider<InjectionAnnotations> injectionAnnotationsProvider,
            Provider<KotlinMetadataUtil> metadataUtilProvider,
            Provider<DaggerElements> elementsProvider) {
        this.membersInjectionValidatorProvider = membersInjectionValidatorProvider;
        this.injectionAnnotationsProvider = injectionAnnotationsProvider;
        this.metadataUtilProvider = metadataUtilProvider;
        this.elementsProvider = elementsProvider;
    }

    @Override
    public DependencyRequestValidator get() {
        return newInstance(membersInjectionValidatorProvider.get(), injectionAnnotationsProvider.get(), metadataUtilProvider.get(), elementsProvider.get());
    }

    public static DependencyRequestValidator_Factory create(
            Provider<MembersInjectionValidator> membersInjectionValidatorProvider,
            Provider<InjectionAnnotations> injectionAnnotationsProvider,
            Provider<KotlinMetadataUtil> metadataUtilProvider,
            Provider<DaggerElements> elementsProvider) {
        return new DependencyRequestValidator_Factory(membersInjectionValidatorProvider, injectionAnnotationsProvider, metadataUtilProvider, elementsProvider);
    }

    public static DependencyRequestValidator newInstance(Object membersInjectionValidator,
                                                         InjectionAnnotations injectionAnnotations, KotlinMetadataUtil metadataUtil,
                                                         DaggerElements elements) {
        return new DependencyRequestValidator((MembersInjectionValidator) membersInjectionValidator, injectionAnnotations, metadataUtil, elements);
    }
}
