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
public final class MultibindingAnnotationsProcessingStep_Factory implements Factory<MultibindingAnnotationsProcessingStep> {
    private final Provider<AnyBindingMethodValidator> anyBindingMethodValidatorProvider;

    private final Provider<XMessager> messagerProvider;

    public MultibindingAnnotationsProcessingStep_Factory(
            Provider<AnyBindingMethodValidator> anyBindingMethodValidatorProvider,
            Provider<XMessager> messagerProvider) {
        this.anyBindingMethodValidatorProvider = anyBindingMethodValidatorProvider;
        this.messagerProvider = messagerProvider;
    }

    @Override
    public MultibindingAnnotationsProcessingStep get() {
        return newInstance(anyBindingMethodValidatorProvider.get(), messagerProvider.get());
    }

    public static MultibindingAnnotationsProcessingStep_Factory create(
            Provider<AnyBindingMethodValidator> anyBindingMethodValidatorProvider,
            Provider<XMessager> messagerProvider) {
        return new MultibindingAnnotationsProcessingStep_Factory(anyBindingMethodValidatorProvider, messagerProvider);
    }

    public static MultibindingAnnotationsProcessingStep newInstance(
            AnyBindingMethodValidator anyBindingMethodValidator, XMessager messager) {
        return new MultibindingAnnotationsProcessingStep(anyBindingMethodValidator, messager);
    }
}
