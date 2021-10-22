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

    private final Provider<XMessager> messagerProvider;

    public BindsInstanceProcessingStep_Factory(Provider<XMessager> messagerProvider) {

        this.messagerProvider = messagerProvider;
    }

    @Override
    public BindsInstanceProcessingStep get() {
        return newInstance(messagerProvider.get());
    }

    public static BindsInstanceProcessingStep_Factory create(Provider<XMessager> messagerProvider) {
        return new BindsInstanceProcessingStep_Factory(messagerProvider);
    }

    public static BindsInstanceProcessingStep newInstance(XMessager messager) {
        return new BindsInstanceProcessingStep(messager);
    }
}
