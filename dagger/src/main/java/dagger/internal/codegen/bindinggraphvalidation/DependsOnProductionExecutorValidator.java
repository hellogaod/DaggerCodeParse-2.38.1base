package dagger.internal.codegen.bindinggraphvalidation;

import javax.inject.Inject;

import dagger.internal.codegen.binding.KeyFactory;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.spi.model.Binding;
import dagger.spi.model.BindingGraph;
import dagger.spi.model.BindingGraphPlugin;
import dagger.spi.model.DiagnosticReporter;
import dagger.spi.model.Key;

import static dagger.internal.codegen.extension.DaggerStreams.instancesOf;
import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Reports an error on all bindings that depend explicitly on the {@code @Production Executor} key.
 */
// TODO(dpb,beder): Validate this during @Inject/@Provides/@Produces validation.
class DependsOnProductionExecutorValidator implements BindingGraphPlugin {

    private final CompilerOptions compilerOptions;
    private final KeyFactory keyFactory;

    @Inject
    DependsOnProductionExecutorValidator(
            CompilerOptions compilerOptions,
            KeyFactory keyFactory) {
        this.compilerOptions = compilerOptions;
        this.keyFactory = keyFactory;
    }

    @Override
    public String pluginName() {
        return "Dagger/DependsOnProductionExecutor";
    }

    @Override
    public void visitGraph(BindingGraph bindingGraph, DiagnosticReporter diagnosticReporter) {
        //是否使用了Produces注解
        if (!compilerOptions.usesProducers()) {
            return;
        }

        Key productionImplementationExecutorKey = keyFactory.forProductionImplementationExecutor();
        Key productionExecutorKey = keyFactory.forProductionExecutor();

        bindingGraph.network().nodes().stream()
                //找到MaybeBinding节点
                .flatMap(instancesOf(BindingGraph.MaybeBinding.class))
                //找到MaybeBinding节点的key是productionExecutorKey的节点
                .filter(node -> node.key().equals(productionExecutorKey))
                //筛选出MaybeBinding节点的key是productionExecutorKey的节点，该MaybeBinding节点中的Binding声明对象
                .flatMap(productionExecutor -> bindingGraph.requestingBindings(productionExecutor).stream())
                //筛选出MaybeBinding节点的key是productionExecutorKey的节点，但是该MaybeBinding节点中的Binding声明对象的key不是productionImplementationExecutorKey
                .filter(binding -> !binding.key().equals(productionImplementationExecutorKey))
                .forEach(binding -> reportError(diagnosticReporter, binding));
    }

    private void reportError(DiagnosticReporter diagnosticReporter, Binding binding) {
        diagnosticReporter.reportBinding(
                ERROR, binding, "%s may not depend on the production executor", binding.key());
    }
}
