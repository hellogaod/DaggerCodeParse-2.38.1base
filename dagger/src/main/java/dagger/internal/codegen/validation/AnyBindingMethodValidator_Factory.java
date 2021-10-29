package dagger.internal.codegen.validation;


import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.ClassName;

import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class AnyBindingMethodValidator_Factory implements Factory<AnyBindingMethodValidator> {
    private final Provider<ImmutableMap<ClassName, BindingMethodValidator>> validatorsProvider;

    public AnyBindingMethodValidator_Factory(
            Provider<ImmutableMap<ClassName, BindingMethodValidator>> validatorsProvider) {
        this.validatorsProvider = validatorsProvider;
    }

    @Override
    public AnyBindingMethodValidator get() {
        return newInstance(validatorsProvider.get());
    }

    public static AnyBindingMethodValidator_Factory create(
            Provider<ImmutableMap<ClassName, BindingMethodValidator>> validatorsProvider) {
        return new AnyBindingMethodValidator_Factory(validatorsProvider);
    }

    public static AnyBindingMethodValidator newInstance(
            ImmutableMap<ClassName, BindingMethodValidator> validators) {
        return new AnyBindingMethodValidator(validators);
    }
}
