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

    public BindingMethodProcessingStep_Factory(Provider<XMessager> messagerProvider) {
        this.messagerProvider = messagerProvider;
    }

    @Override
    public BindingMethodProcessingStep get() {
        return newInstance(messagerProvider.get());
    }

    public static BindingMethodProcessingStep_Factory create(Provider<XMessager> messagerProvider) {
        return new BindingMethodProcessingStep_Factory(messagerProvider);
    }

    public static BindingMethodProcessingStep newInstance(XMessager messager) {
        return new BindingMethodProcessingStep(messager);
    }
}
