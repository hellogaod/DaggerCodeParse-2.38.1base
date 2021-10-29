package dagger.internal.codegen.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic;

import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * A {@linkplain ValidationReport validator} for {@link Inject}-annotated elements and the types
 * that contain them.
 */
@Singleton
public final class InjectValidator {

    private final DaggerTypes types;
    private final DaggerElements elements;
    private final CompilerOptions compilerOptions;
    private final DependencyRequestValidator dependencyRequestValidator;
    private final Optional<Diagnostic.Kind> privateAndStaticInjectionDiagnosticKind;
    private final InjectionAnnotations injectionAnnotations;
    private final KotlinMetadataUtil metadataUtil;
    private final Map<ExecutableElement, ValidationReport> reports = new HashMap<>();

    @Inject
    InjectValidator(
            DaggerTypes types,
            DaggerElements elements,
            DependencyRequestValidator dependencyRequestValidator,
            CompilerOptions compilerOptions,
            InjectionAnnotations injectionAnnotations,
            KotlinMetadataUtil metadataUtil
    ) {
        this(
                types,
                elements,
                compilerOptions,
                dependencyRequestValidator,
                Optional.empty(),
                injectionAnnotations,
                metadataUtil);
    }

    private InjectValidator(
            DaggerTypes types,
            DaggerElements elements,
            CompilerOptions compilerOptions,
            DependencyRequestValidator dependencyRequestValidator,
            Optional<Diagnostic.Kind> privateAndStaticInjectionDiagnosticKind,
            InjectionAnnotations injectionAnnotations,
            KotlinMetadataUtil metadataUtil) {
        this.types = types;
        this.elements = elements;
        this.compilerOptions = compilerOptions;
        this.dependencyRequestValidator = dependencyRequestValidator;
        this.privateAndStaticInjectionDiagnosticKind = privateAndStaticInjectionDiagnosticKind;
        this.injectionAnnotations = injectionAnnotations;
        this.metadataUtil = metadataUtil;
    }

    public InjectValidator whenGeneratingCode() {
        return this;
    }
}
