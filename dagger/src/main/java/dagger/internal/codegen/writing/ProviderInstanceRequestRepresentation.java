package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.FrameworkType;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/** Binding expression for provider instances. */
final class ProviderInstanceRequestRepresentation extends FrameworkInstanceRequestRepresentation {

    @AssistedInject
    ProviderInstanceRequestRepresentation(
            @Assisted ContributionBinding binding,
            @Assisted FrameworkInstanceSupplier frameworkInstanceSupplier,
            DaggerTypes types,
            DaggerElements elements) {
        super(binding, frameworkInstanceSupplier, types, elements);
    }

    @Override
    protected FrameworkType frameworkType() {
        return FrameworkType.PROVIDER;
    }

    @AssistedFactory
    static interface Factory {
        ProviderInstanceRequestRepresentation create(
                ContributionBinding binding,
                FrameworkInstanceSupplier frameworkInstanceSupplier
        );
    }
}
