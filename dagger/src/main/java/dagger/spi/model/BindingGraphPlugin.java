package dagger.spi.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

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
     * Initializes this plugin with a {@link Filer} that it can use to write Java or other files based
     * on the binding graph. This will be called once per instance of this plugin, before any graph is
     * {@linkplain #visitGraph(BindingGraph, DiagnosticReporter) visited}.
     *
     * @see javax.annotation.processing.ProcessingEnvironment#getFiler()
     */
    default void initFiler(Filer filer) {}

    /**
     * Initializes this plugin with a {@link Types} instance. This will be called once per instance of
     * this plugin, before any graph is {@linkplain #visitGraph(BindingGraph, DiagnosticReporter)
     * visited}.
     *
     * @see javax.annotation.processing.ProcessingEnvironment#getTypeUtils()
     */
    default void initTypes(Types types) {}

    /**
     * Initializes this plugin with a {@link Elements} instance. This will be called once per instance
     * of this plugin, before any graph is {@linkplain #visitGraph(BindingGraph, DiagnosticReporter)
     * visited}.
     *
     * @see javax.annotation.processing.ProcessingEnvironment#getElementUtils()
     */
    default void initElements(Elements elements) {}

    /**
     * Initializes this plugin with a filtered view of the options passed on the {@code javac}
     * command-line for all keys from {@link #supportedOptions()}. This will be called once per
     * instance of this plugin, before any graph is {@linkplain #visitGraph(BindingGraph,
     * DiagnosticReporter) visited}.
     *
     * @see javax.annotation.processing.ProcessingEnvironment#getOptions()
     */
    default void initOptions(Map<String, String> options) {}

    /**
     * Returns the annotation-processing options that this plugin uses to configure behavior.
     *
     * @see javax.annotation.processing.Processor#getSupportedOptions()
     */
    default Set<String> supportedOptions() {
        return Collections.emptySet();
    }

    /**
     * A distinguishing name of the plugin that will be used in diagnostics printed to the {@link
     * Messager}. By default, the {@linkplain Class#getCanonicalName() fully qualified name} of the
     * plugin is used.
     */
    default String pluginName() {
        return getClass().getCanonicalName();
    }
}
