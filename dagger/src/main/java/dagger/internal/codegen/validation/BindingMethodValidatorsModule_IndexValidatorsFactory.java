package dagger.internal.codegen.validation;


import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.ClassName;

import java.util.Set;

import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class BindingMethodValidatorsModule_IndexValidatorsFactory implements Factory<ImmutableMap<ClassName, BindingMethodValidator>> {
    private final Provider<Set<BindingMethodValidator>> validatorsProvider;

    public BindingMethodValidatorsModule_IndexValidatorsFactory(
            Provider<Set<BindingMethodValidator>> validatorsProvider) {
        this.validatorsProvider = validatorsProvider;
    }

    @Override
    public ImmutableMap<ClassName, BindingMethodValidator> get() {
        return indexValidators(validatorsProvider.get());
    }

    public static BindingMethodValidatorsModule_IndexValidatorsFactory create(
            Provider<Set<BindingMethodValidator>> validatorsProvider) {
        return new BindingMethodValidatorsModule_IndexValidatorsFactory(validatorsProvider);
    }

    public static ImmutableMap<ClassName, BindingMethodValidator> indexValidators(
            Set<BindingMethodValidator> validators) {
        return Preconditions.checkNotNullFromProvides(BindingMethodValidatorsModule.indexValidators(validators));
    }
}
