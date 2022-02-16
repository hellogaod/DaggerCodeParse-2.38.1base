package dagger.internal.codegen.validation;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;

import androidx.room.compiler.processing.XFiler;
import androidx.room.compiler.processing.XTypeElement;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.Module;
import dagger.Provides;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.SourceFiles;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.multibindings.Multibinds;
import dagger.producers.ProductionScope;
import dagger.producers.monitoring.Monitors;
import dagger.producers.monitoring.ProductionComponentMonitor;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static dagger.internal.codegen.javapoet.TypeNames.PRODUCTION_COMPONENT_MONITOR_FACTORY;
import static dagger.internal.codegen.javapoet.TypeNames.providerOf;
import static dagger.internal.codegen.javapoet.TypeNames.setOf;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Generates a monitoring module for use with production components.
 */
final class MonitoringModuleGenerator extends SourceFileGenerator<XTypeElement> {

    @Inject
    MonitoringModuleGenerator(
            XFiler filer,
            DaggerElements elements,
            SourceVersion sourceVersion
    ) {
        super(filer, elements, sourceVersion);
    }

    @Override
    public Element originatingElement(XTypeElement componentElement) {
        return XConverters.toJavac(componentElement);
    }

    @Override
    public ImmutableList<TypeSpec.Builder> topLevelTypes(XTypeElement componentElement) {

        return ImmutableList.of(
                //源节点路径（如果是内部A类，则使用XX_A表示路径） 拼接  "_MonitoringModule"
                classBuilder(SourceFiles.generatedMonitoringModuleName(componentElement))
                        .addAnnotation(Module.class)
                        .addModifiers(ABSTRACT)
                        .addMethod(privateConstructor())
                        .addMethod(setOfFactories())
                        .addMethod(monitor(componentElement)));
    }

    private MethodSpec privateConstructor() {
        return constructorBuilder().addModifiers(PRIVATE).build();
    }

    private MethodSpec setOfFactories() {
        return methodBuilder("setOfFactories")
                .addAnnotation(Multibinds.class)
                .addModifiers(ABSTRACT)
                .returns(setOf(PRODUCTION_COMPONENT_MONITOR_FACTORY))
                .build();
    }

    private MethodSpec monitor(XTypeElement componentElement) {
        return methodBuilder("monitor")
                .returns(ProductionComponentMonitor.class)
                .addModifiers(STATIC)
                .addAnnotation(Provides.class)
                .addAnnotation(ProductionScope.class)
                .addParameter(providerOf(componentElement.getType().getTypeName()), "component")
                .addParameter(providerOf(setOf(PRODUCTION_COMPONENT_MONITOR_FACTORY)), "factories")
                .addStatement("return $T.createMonitorForComponent(component, factories)", Monitors.class)
                .build();
    }
}
