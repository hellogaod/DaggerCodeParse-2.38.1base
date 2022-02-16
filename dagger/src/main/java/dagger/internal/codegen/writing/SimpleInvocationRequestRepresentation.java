package dagger.internal.codegen.writing;


import dagger.internal.codegen.binding.ContributionBinding;

import static com.google.common.base.Preconditions.checkNotNull;

/** A simple binding expression for instance requests. Does not scope. */
abstract class SimpleInvocationRequestRepresentation extends RequestRepresentation {
    private final ContributionBinding binding;

    SimpleInvocationRequestRepresentation(ContributionBinding binding) {
        this.binding = checkNotNull(binding);
    }

    @Override
    boolean requiresMethodEncapsulation() {
        return !binding.dependencies().isEmpty();
    }
}
