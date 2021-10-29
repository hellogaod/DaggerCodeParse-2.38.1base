package dagger.internal.codegen.validation;


import javax.annotation.Generated;
import javax.inject.Provider;

import androidx.room.compiler.processing.XMessager;
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
public final class BindsInstanceProcessingStep_Factory implements Factory<BindsInstanceProcessingStep> {

    private final Provider<BindsInstanceMethodValidator> methodValidatorProvider;

    private final Provider<BindsInstanceParameterValidator> parameterValidatorProvider;

    private final Provider<XMessager> messagerProvider;

    public BindsInstanceProcessingStep_Factory(
            Provider<BindsInstanceMethodValidator> methodValidatorProvider,
            Provider<BindsInstanceParameterValidator> parameterValidatorProvider,
            Provider<XMessager> messagerProvider) {
        this.methodValidatorProvider = methodValidatorProvider;
        this.parameterValidatorProvider = parameterValidatorProvider;
        this.messagerProvider = messagerProvider;
    }

    @Override
    public BindsInstanceProcessingStep get() {
        return newInstance(methodValidatorProvider.get(), parameterValidatorProvider.get(), messagerProvider.get());
    }

    public static BindsInstanceProcessingStep_Factory create(
            Provider<BindsInstanceMethodValidator> methodValidatorProvider,
            Provider<BindsInstanceParameterValidator> parameterValidatorProvider,
            Provider<XMessager> messagerProvider) {
        return new BindsInstanceProcessingStep_Factory(methodValidatorProvider, parameterValidatorProvider, messagerProvider);
    }

    public static BindsInstanceProcessingStep newInstance(Object methodValidator,
                                                          Object parameterValidator, XMessager messager) {
        return new BindsInstanceProcessingStep((BindsInstanceMethodValidator) methodValidator, (BindsInstanceParameterValidator) parameterValidator, messager);
    }
}
