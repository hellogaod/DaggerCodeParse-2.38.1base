package dagger.internal.codegen.binding;

import javax.inject.Inject;

/** Converts {@link BindingGraph}s to {@link dagger.spi.model.BindingGraph}s. */
final class BindingGraphConverter {
    private final BindingDeclarationFormatter bindingDeclarationFormatter;

    @Inject
    BindingGraphConverter(BindingDeclarationFormatter bindingDeclarationFormatter) {
        this.bindingDeclarationFormatter = bindingDeclarationFormatter;
    }
}
