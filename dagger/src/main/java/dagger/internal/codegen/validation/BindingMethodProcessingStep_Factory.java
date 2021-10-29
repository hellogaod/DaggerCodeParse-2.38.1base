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
public final class BindingMethodProcessingStep_Factory implements Factory<BindingMethodProcessingStep> {

    private final Provider<XMessager> messagerProvider;

    private final Provider<AnyBindingMethodValidator> anyBindingMethodValidatorProvider;

    public BindingMethodProcessingStep_Factory(Provider<XMessager> messagerProvider,
                                               Provider<AnyBindingMethodValidator> anyBindingMethodValidatorProvider) {
        this.messagerProvider = messagerProvider;
        this.anyBindingMethodValidatorProvider = anyBindingMethodValidatorProvider;
    }

    @Override
    public BindingMethodProcessingStep get() {
        return newInstance(messagerProvider.get(), anyBindingMethodValidatorProvider.get());
    }

    public static BindingMethodProcessingStep_Factory create(Provider<XMessager> messagerProvider,
                                                             Provider<AnyBindingMethodValidator> anyBindingMethodValidatorProvider) {
        return new BindingMethodProcessingStep_Factory(messagerProvider, anyBindingMethodValidatorProvider);
    }

    public static BindingMethodProcessingStep newInstance(XMessager messager,
                                                          AnyBindingMethodValidator anyBindingMethodValidator) {
        return new BindingMethodProcessingStep(messager, anyBindingMethodValidator);
    }
}
