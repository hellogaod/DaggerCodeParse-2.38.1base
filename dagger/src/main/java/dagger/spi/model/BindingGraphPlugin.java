package dagger.spi.model;

import javax.annotation.processing.Messager;

/**
 * A pluggable visitor for {@link BindingGraph}.
 *
 * <p>
 * BindingGraph，表示绑定图形，将Component及其相关联的注解元素形成一个有向图形，从而促成相互关联。Dagger的核心类
 */
public interface BindingGraphPlugin {
    /**
     * Called once for each valid root binding graph encountered by the Dagger processor. May report
     * diagnostics using {@code diagnosticReporter}.
     */
    void visitGraph(BindingGraph bindingGraph, DiagnosticReporter diagnosticReporter);

    /**
     * A distinguishing name of the plugin that will be used in diagnostics printed to the {@link
     * Messager}. By default, the {@linkplain Class#getCanonicalName() fully qualified name} of the
     * plugin is used.
     */
    default String pluginName() {
        return getClass().getCanonicalName();
    }
}
