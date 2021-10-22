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
public final class MonitoringModuleProcessingStep_Factory implements Factory<MonitoringModuleProcessingStep> {

    private final Provider<XMessager> messagerProvider;

    public MonitoringModuleProcessingStep_Factory(Provider<XMessager> messagerProvider) {
        this.messagerProvider = messagerProvider;
    }

    @Override
    public MonitoringModuleProcessingStep get() {
        return newInstance(messagerProvider.get());
    }

    public static MonitoringModuleProcessingStep_Factory create(Provider<XMessager> messagerProvider) {
        return new MonitoringModuleProcessingStep_Factory(messagerProvider);
    }

    public static MonitoringModuleProcessingStep newInstance(XMessager messager) {
        return new MonitoringModuleProcessingStep(messager);
    }
}
