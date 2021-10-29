package dagger.internal.codegen.validation;

import javax.inject.Inject;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;

/**
 * A processing step that is responsible for generating a special module for a {@link
 * dagger.producers.ProductionComponent} or {@link dagger.producers.ProductionSubcomponent}.
 */
public final class MonitoringModuleProcessingStep extends TypeCheckingProcessingStep<XTypeElement> {

    private final XMessager messager;
    private final MonitoringModuleGenerator monitoringModuleGenerator;

    @Inject
    MonitoringModuleProcessingStep(
            XMessager messager,
            MonitoringModuleGenerator monitoringModuleGenerator
    ) {
        this.messager = messager;
        this.monitoringModuleGenerator = monitoringModuleGenerator;
    }
}
