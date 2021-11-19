package dagger.internal.codegen.validation;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import javax.inject.Inject;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import dagger.internal.codegen.javapoet.TypeNames;

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


    @Override
    public ImmutableSet<ClassName> annotationClassNames() {
        return ImmutableSet.of(TypeNames.PRODUCTION_COMPONENT, TypeNames.PRODUCTION_SUBCOMPONENT);
    }


    @Override
    protected void process(XTypeElement productionComponent, ImmutableSet<ClassName> annotations) {

    }
}
