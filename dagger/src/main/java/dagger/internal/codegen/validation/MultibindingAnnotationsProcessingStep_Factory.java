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


    private final Provider<XMessager> messagerProvider;

    public MultibindingAnnotationsProcessingStep_Factory(
            Provider<XMessager> messagerProvider) {
        this.messagerProvider = messagerProvider;
    }

    @Override
    public MultibindingAnnotationsProcessingStep get() {
        return newInstance(messagerProvider.get());
    }

    public static MultibindingAnnotationsProcessingStep_Factory create(
            Provider<XMessager> messagerProvider) {
        return new MultibindingAnnotationsProcessingStep_Factory(messagerProvider);
    }

    public static MultibindingAnnotationsProcessingStep newInstance(XMessager messager) {
        return new MultibindingAnnotationsProcessingStep(messager);
    }
}
