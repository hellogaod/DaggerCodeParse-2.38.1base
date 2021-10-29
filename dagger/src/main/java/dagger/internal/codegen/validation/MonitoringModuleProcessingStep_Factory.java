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

    private final Provider<MonitoringModuleGenerator> monitoringModuleGeneratorProvider;

    public MonitoringModuleProcessingStep_Factory(Provider<XMessager> messagerProvider,
                                                  Provider<MonitoringModuleGenerator> monitoringModuleGeneratorProvider) {
        this.messagerProvider = messagerProvider;
        this.monitoringModuleGeneratorProvider = monitoringModuleGeneratorProvider;
    }

    @Override
    public MonitoringModuleProcessingStep get() {
        return newInstance(messagerProvider.get(), monitoringModuleGeneratorProvider.get());
    }

    public static MonitoringModuleProcessingStep_Factory create(Provider<XMessager> messagerProvider,
                                                                Provider<MonitoringModuleGenerator> monitoringModuleGeneratorProvider) {
        return new MonitoringModuleProcessingStep_Factory(messagerProvider, monitoringModuleGeneratorProvider);
    }

    public static MonitoringModuleProcessingStep newInstance(XMessager messager,
                                                             Object monitoringModuleGenerator) {
        return new MonitoringModuleProcessingStep(messager, (MonitoringModuleGenerator) monitoringModuleGenerator);
    }
}
