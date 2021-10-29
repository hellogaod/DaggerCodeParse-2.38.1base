package dagger.internal.codegen.validation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.InjectionAnnotations;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class MembersInjectionValidator_Factory implements Factory<MembersInjectionValidator> {

    private final Provider<InjectionAnnotations> injectionAnnotationsProvider;

    public MembersInjectionValidator_Factory(
            Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        this.injectionAnnotationsProvider = injectionAnnotationsProvider;
    }

    @Override
    public MembersInjectionValidator get() {
        return newInstance(injectionAnnotationsProvider.get());
    }

    public static MembersInjectionValidator_Factory create(
            Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        return new MembersInjectionValidator_Factory(injectionAnnotationsProvider);
    }

    public static MembersInjectionValidator newInstance(InjectionAnnotations injectionAnnotations) {
        return new MembersInjectionValidator(injectionAnnotations);
    }
}
