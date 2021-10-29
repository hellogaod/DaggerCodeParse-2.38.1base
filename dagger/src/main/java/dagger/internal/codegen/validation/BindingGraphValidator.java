package dagger.internal.codegen.validation;


import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.internal.codegen.compileroption.CompilerOptions;

/** Validates a {@link BindingGraph}. */
@Singleton
public final class BindingGraphValidator {
    private final ValidationBindingGraphPlugins validationPlugins;
    private final ExternalBindingGraphPlugins externalPlugins;
    private final CompilerOptions compilerOptions;

    @Inject
    BindingGraphValidator(
            ValidationBindingGraphPlugins validationPlugins,
            ExternalBindingGraphPlugins externalPlugins,
            CompilerOptions compilerOptions) {
        this.validationPlugins = validationPlugins;
        this.externalPlugins = externalPlugins;
        this.compilerOptions = compilerOptions;
    }
}
