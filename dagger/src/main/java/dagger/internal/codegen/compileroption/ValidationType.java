package dagger.internal.codegen.compileroption;


import java.util.Optional;

import javax.tools.Diagnostic;

/**
 * Allows options to control how component process validates things such as scope cycles or
 * nullability.
 */
public enum ValidationType {
    ERROR,
    WARNING,
    NONE;

    public Optional<Diagnostic.Kind> diagnosticKind() {
        switch (this) {
            case ERROR:
                return Optional.of(Diagnostic.Kind.ERROR);
            case WARNING:
                return Optional.of(Diagnostic.Kind.WARNING);
            default:
                return Optional.empty();
        }
    }
}
