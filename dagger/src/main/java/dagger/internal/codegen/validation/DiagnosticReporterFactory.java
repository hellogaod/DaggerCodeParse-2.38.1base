package dagger.internal.codegen.validation;

import javax.inject.Inject;

import androidx.room.compiler.processing.XMessager;

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
}
