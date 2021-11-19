package dagger.internal.codegen.validation;


import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.compileroption.ValidationType;

/**
 * Validates a {@link BindingGraph}.
 */
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

    /**
     * Returns {@code true} if validation or analysis is required on the full binding graph.
     * <p>
     * 如果需要对完整绑定图进行验证或分析，则返回 {@code true}。
     */
    public boolean shouldDoFullBindingGraphValidation(TypeElement component) {
        return requiresFullBindingGraphValidation()
                || compilerOptions.pluginsVisitFullBindingGraphs(component);
    }

    private boolean requiresFullBindingGraphValidation() {
        return !compilerOptions.fullBindingGraphValidationType().equals(ValidationType.NONE);
    }
}
