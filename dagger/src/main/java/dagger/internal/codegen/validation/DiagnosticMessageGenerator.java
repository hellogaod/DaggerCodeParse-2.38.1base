package dagger.internal.codegen.validation;


import javax.inject.Inject;

import dagger.internal.codegen.base.ElementFormatter;
import dagger.internal.codegen.binding.DependencyRequestFormatter;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Helper class for generating diagnostic messages.
 */
public final class DiagnosticMessageGenerator {

    /**
     * Injectable factory for {@code DiagnosticMessageGenerator}.
     */
    public static final class Factory {
        private final DaggerTypes types;
        private final DependencyRequestFormatter dependencyRequestFormatter;
        private final ElementFormatter elementFormatter;

        @Inject
        Factory(
                DaggerTypes types,
                DependencyRequestFormatter dependencyRequestFormatter,
                ElementFormatter elementFormatter) {
            this.types = types;
            this.dependencyRequestFormatter = dependencyRequestFormatter;
            this.elementFormatter = elementFormatter;
        }
    }
}
