package dagger.internal.codegen.validation;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.FormatMethod;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.spi.model.BindingGraph;
import dagger.spi.model.DiagnosticReporter;

import static com.google.common.collect.Lists.asList;
import static dagger.internal.codegen.base.ElementFormatter.elementToString;
import static dagger.internal.codegen.langmodel.DaggerElements.transitivelyEncloses;
import static javax.tools.Diagnostic.Kind.ERROR;

final class DiagnosticReporterFactory {
    private final XMessager messager;
    private final DiagnosticMessageGenerator.Factory diagnosticMessageGeneratorFactory;

    @Inject
    DiagnosticReporterFactory(
            XMessager messager,
            DiagnosticMessageGenerator.Factory diagnosticMessageGeneratorFactory
    ) {
        this.messager = messager;
        this.diagnosticMessageGeneratorFactory = diagnosticMessageGeneratorFactory;
    }

    /** Creates a reporter for a binding graph and a plugin. */
    DiagnosticReporterImpl reporter(
            BindingGraph graph, String pluginName, boolean reportErrorsAsWarnings) {
        return new DiagnosticReporterImpl(graph, pluginName, reportErrorsAsWarnings);
    }

    /**
     * A {@link DiagnosticReporter} that keeps track of which {@linkplain Diagnostic.Kind kinds} of
     * diagnostics were reported.
     */
    final class DiagnosticReporterImpl implements DiagnosticReporter {
        private final String plugin;
        private final TypeElement rootComponent;
        private final boolean reportErrorsAsWarnings;
        private final ImmutableSet.Builder<Diagnostic.Kind> reportedDiagnosticKinds =
                ImmutableSet.builder();
        private final DiagnosticMessageGenerator diagnosticMessageGenerator;

        DiagnosticReporterImpl(BindingGraph graph, String plugin, boolean reportErrorsAsWarnings) {
            this.plugin = plugin;
            this.reportErrorsAsWarnings = reportErrorsAsWarnings;
            this.rootComponent = graph.rootComponentNode().componentPath().currentComponent().java();
            this.diagnosticMessageGenerator = diagnosticMessageGeneratorFactory.create(graph);
        }

        /** Returns which {@linkplain Diagnostic.Kind kinds} of diagnostics were reported. */
        ImmutableSet<Diagnostic.Kind> reportedDiagnosticKinds() {
            return reportedDiagnosticKinds.build();
        }

        @Override
        public void reportComponent(
                Diagnostic.Kind diagnosticKind, BindingGraph.ComponentNode componentNode, String messageFormat) {
            StringBuilder message = new StringBuilder(messageFormat);
            diagnosticMessageGenerator.appendComponentPathUnlessAtRoot(message, componentNode);
            // TODO(dpb): Report at the component node component.
            printMessage(diagnosticKind, message, rootComponent);
        }

        @Override
        @FormatMethod
        public void reportComponent(
                Diagnostic.Kind diagnosticKind,
                BindingGraph.ComponentNode componentNode,
                String messageFormat,
                Object firstArg,
                Object... moreArgs) {
            reportComponent(
                    diagnosticKind, componentNode, formatMessage(messageFormat, firstArg, moreArgs));
        }

        // TODO(ronshapiro): should this also include the binding element?
        @Override
        public void reportBinding(
                Diagnostic.Kind diagnosticKind, BindingGraph.MaybeBinding binding, String message) {
            printMessage(
                    diagnosticKind, message + diagnosticMessageGenerator.getMessage(binding), rootComponent);
        }

        @Override
        public void reportBinding(
                Diagnostic.Kind diagnosticKind,
                BindingGraph.MaybeBinding binding,
                String messageFormat,
                Object firstArg,
                Object... moreArgs) {
            reportBinding(diagnosticKind, binding, formatMessage(messageFormat, firstArg, moreArgs));
        }

        @Override
        public void reportDependency(
                Diagnostic.Kind diagnosticKind, BindingGraph.DependencyEdge dependencyEdge, String message) {
            printMessage(
                    diagnosticKind,
                    message + diagnosticMessageGenerator.getMessage(dependencyEdge),
                    rootComponent);
        }

        @Override
        public void reportDependency(
                Diagnostic.Kind diagnosticKind,
                BindingGraph.DependencyEdge dependencyEdge,
                String messageFormat,
                Object firstArg,
                Object... moreArgs) {
            reportDependency(
                    diagnosticKind, dependencyEdge, formatMessage(messageFormat, firstArg, moreArgs));
        }

        @Override
        public void reportSubcomponentFactoryMethod(
                Diagnostic.Kind diagnosticKind,
                BindingGraph.ChildFactoryMethodEdge childFactoryMethodEdge,
                String message) {
            printMessage(diagnosticKind, message, childFactoryMethodEdge.factoryMethod().java());
        }

        @Override
        public void reportSubcomponentFactoryMethod(
                Diagnostic.Kind diagnosticKind,
                BindingGraph.ChildFactoryMethodEdge childFactoryMethodEdge,
                String messageFormat,
                Object firstArg,
                Object... moreArgs) {
            reportSubcomponentFactoryMethod(
                    diagnosticKind, childFactoryMethodEdge, formatMessage(messageFormat, firstArg, moreArgs));
        }

        private String formatMessage(String messageFormat, Object firstArg, Object[] moreArgs) {
            return String.format(messageFormat, asList(firstArg, moreArgs).toArray());
        }

        void printMessage(
                Diagnostic.Kind diagnosticKind,
                CharSequence message,
                @NullableDecl Element elementToReport) {
            if (diagnosticKind.equals(ERROR) && reportErrorsAsWarnings) {
                diagnosticKind = Diagnostic.Kind.WARNING;
            }
            reportedDiagnosticKinds.add(diagnosticKind);
            StringBuilder fullMessage = new StringBuilder();
            appendBracketPrefix(fullMessage, plugin);

            if (elementToReport != null && !transitivelyEncloses(rootComponent, elementToReport)) {
                appendBracketPrefix(fullMessage, elementToString(elementToReport));
                elementToReport = rootComponent;
            }

            XConverters.toJavac(messager)
                    .printMessage(diagnosticKind, fullMessage.append(message), elementToReport);
        }

        private void appendBracketPrefix(StringBuilder message, String prefix) {
            message.append(String.format("[%s] ", prefix));
        }
    }
}
