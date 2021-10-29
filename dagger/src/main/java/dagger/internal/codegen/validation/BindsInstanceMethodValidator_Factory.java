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
public final class BindsInstanceMethodValidator_Factory implements Factory<BindsInstanceMethodValidator> {
    private final Provider<InjectionAnnotations> injectionAnnotationsProvider;

    public BindsInstanceMethodValidator_Factory(
            Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        this.injectionAnnotationsProvider = injectionAnnotationsProvider;
    }

    @Override
    public BindsInstanceMethodValidator get() {
        return newInstance(injectionAnnotationsProvider.get());
    }

    public static BindsInstanceMethodValidator_Factory create(
            Provider<InjectionAnnotations> injectionAnnotationsProvider) {
        return new BindsInstanceMethodValidator_Factory(injectionAnnotationsProvider);
    }

    public static BindsInstanceMethodValidator newInstance(
            InjectionAnnotations injectionAnnotations) {
        return new BindsInstanceMethodValidator(injectionAnnotations);
    }
}
