package dagger.internal.codegen;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CheckReturnValue;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XProcessingEnv;
import androidx.room.compiler.processing.XProcessingStep;
import androidx.room.compiler.processing.XRoundEnv;
import androidx.room.compiler.processing.javac.JavacBasicAnnotationProcessor;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.internal.codegen.base.ClearableCache;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.InjectBindingRegistry;
import dagger.internal.codegen.binding.MembersInjectionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.bindinggraphvalidation.BindingGraphValidationModule;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.componentgenerator.ComponentGeneratorModule;
import dagger.internal.codegen.validation.BindingMethodProcessingStep;
import dagger.internal.codegen.validation.BindingMethodValidatorsModule;
import dagger.internal.codegen.validation.BindsInstanceProcessingStep;
import dagger.internal.codegen.validation.ExternalBindingGraphPlugins;
import dagger.internal.codegen.validation.InjectBindingRegistryModule;
import dagger.internal.codegen.validation.MonitoringModuleProcessingStep;
import dagger.internal.codegen.validation.MultibindingAnnotationsProcessingStep;
import dagger.internal.codegen.validation.ValidationBindingGraphPlugins;
import dagger.spi.model.BindingGraphPlugin;

import static net.ltgt.gradle.incap.IncrementalAnnotationProcessorType.ISOLATING;

/**
 * The annotation processor responsible for generating the classes that drive the Dagger 2.0
 * implementation.
 *
 * <p>TODO(gak): give this some better documentation
 */
@IncrementalAnnotationProcessor(ISOLATING)
@AutoService(Processor.class)
public class ComponentProcessor extends JavacBasicAnnotationProcessor {
    private final Optional<ImmutableSet<BindingGraphPlugin>> testingPlugins;

    @Inject
    InjectBindingRegistry injectBindingRegistry;
    @Inject
    SourceFileGenerator<ProvisionBinding> factoryGenerator;
    @Inject
    SourceFileGenerator<MembersInjectionBinding> membersInjectorGenerator;
    @Inject
    ImmutableList<XProcessingStep> processingSteps;
    @Inject
    ValidationBindingGraphPlugins validationBindingGraphPlugins;
    @Inject
    ExternalBindingGraphPlugins externalBindingGraphPlugins;
    @Inject
    Set<ClearableCache> clearableCaches;

    public ComponentProcessor() {
        this.testingPlugins = Optional.empty();
    }

    private ComponentProcessor(Iterable<BindingGraphPlugin> testingPlugins) {
        this.testingPlugins = Optional.of(ImmutableSet.copyOf(testingPlugins));
    }

    /**
     * Creates a component processor that uses given {@link BindingGraphPlugin}s instead of loading
     * them from a {@link java.util.ServiceLoader}.
     */
    @VisibleForTesting
    public static ComponentProcessor forTesting(BindingGraphPlugin... testingPlugins) {
        return forTesting(Arrays.asList(testingPlugins));
    }

    /**
     * Creates a component processor that uses given {@link BindingGraphPlugin}s instead of loading
     * them from a {@link java.util.ServiceLoader}.
     */
    @VisibleForTesting
    public static ComponentProcessor forTesting(Iterable<BindingGraphPlugin> testingPlugins) {
        return new ComponentProcessor(testingPlugins);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {//第一个执行

        return SourceVersion.latestSupported();
    }

//    @Override
//    public Set<String> getSupportedOptions() {//第三个执行
//
//       return super.getSupportedOptions();
//    }

    @Override
    public Iterable<XProcessingStep> processingSteps() {//第二个执行

        ProcessorComponent.factory()
                .create(getXProcessingEnv(),testingPlugins.orElseGet(this::loadExternalPlugins))
                .inject(this);

//        validationBindingGraphPlugins.initializePlugins();
//        externalBindingGraphPlugins.initializePlugins();

        return processingSteps;
    }
    
    private ImmutableSet<BindingGraphPlugin> loadExternalPlugins() {
        return ServiceLoaders.load(processingEnv, BindingGraphPlugin.class);
    }

    @Override
    public void postRound(XProcessingEnv env, XRoundEnv round) {//第四个执行

    }

    @Singleton
    @Component(
            modules = {
                    InjectBindingRegistryModule.class,
                    SourceFileGeneratorsModule.class,
                    ProcessingStepsModule.class,
                    ProcessingEnvironmentModule.class,
                    ComponentGeneratorModule.class,
                    BindingMethodValidatorsModule.class,
                    BindingGraphValidationModule.class
            }
    )
    interface ProcessorComponent {
        void inject(ComponentProcessor processor);//将ComponentProcessor使用Inject注解的变量实例注入ComponentProcessor

        static Factory factory() {
            return DaggerComponentProcessor_ProcessorComponent.factory();
        }

        @Component.Factory
        interface Factory {
            @CheckReturnValue
            ProcessorComponent create(
                    @BindsInstance XProcessingEnv xProcessingEnv,
                    @BindsInstance ImmutableSet<BindingGraphPlugin> externalPlugins
            );//使用@BindsInstance传递参数
        }
    }

    //各种注解处理的核心部件
    @Module
    interface ProcessingStepsModule {
        @Provides
        static ImmutableList<XProcessingStep> processingSteps(
                MapKeyProcessingStep mapKeyProcessingStep,
                InjectProcessingStep injectProcessingStep,
                AssistedInjectProcessingStep assistedInjectProcessingStep,
                AssistedFactoryProcessingStep assistedFactoryProcessingStep,
                AssistedProcessingStep assistedProcessingStep,
                MonitoringModuleProcessingStep monitoringModuleProcessingStep,
                MultibindingAnnotationsProcessingStep multibindingAnnotationsProcessingStep,
                BindsInstanceProcessingStep bindsInstanceProcessingStep,
                ModuleProcessingStep moduleProcessingStep,
                ComponentProcessingStep componentProcessingStep,
                ComponentHjarProcessingStep componentHjarProcessingStep,
                BindingMethodProcessingStep bindingMethodProcessingStep,
                CompilerOptions compilerOptions
        ) {
            return ImmutableList.of(
                    mapKeyProcessingStep,
                    injectProcessingStep,
                    assistedInjectProcessingStep,
                    assistedFactoryProcessingStep,
                    assistedProcessingStep,
                    monitoringModuleProcessingStep,
                    multibindingAnnotationsProcessingStep,
                    bindsInstanceProcessingStep,
                    moduleProcessingStep,
                    compilerOptions.headerCompilation()
                            ? componentHjarProcessingStep
                            : componentProcessingStep,
                    bindingMethodProcessingStep);
        }
    }

}
