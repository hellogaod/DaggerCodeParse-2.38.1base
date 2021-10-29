package dagger.internal.codegen.validation;

import javax.inject.Inject;

public final class CompositeBindingGraphPlugin {

    /** Factory class for {@link CompositeBindingGraphPlugin}. */
    public static final class Factory {
        private final DiagnosticMessageGenerator.Factory messageGeneratorFactory;

        @Inject
        Factory(DiagnosticMessageGenerator.Factory messageGeneratorFactory) {
            this.messageGeneratorFactory = messageGeneratorFactory;
        }
    }
}
