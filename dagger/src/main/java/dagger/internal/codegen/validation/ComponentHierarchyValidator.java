package dagger.internal.codegen.validation;

import com.google.common.base.Joiner;

import javax.inject.Inject;

import dagger.internal.codegen.compileroption.CompilerOptions;

/** Validates the relationships between parent components and subcomponents. */
final class ComponentHierarchyValidator {
    private static final Joiner COMMA_SEPARATED_JOINER = Joiner.on(", ");
    private final CompilerOptions compilerOptions;

    @Inject
    ComponentHierarchyValidator(CompilerOptions compilerOptions) {
        this.compilerOptions = compilerOptions;
    }
}
