package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import dagger.internal.codegen.binding.ContributionBinding;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class SubcomponentCreatorRequestRepresentation_Factory_Impl implements SubcomponentCreatorRequestRepresentation.Factory {
    private final SubcomponentCreatorRequestRepresentation_Factory delegateFactory;

    SubcomponentCreatorRequestRepresentation_Factory_Impl(
            SubcomponentCreatorRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public SubcomponentCreatorRequestRepresentation create(ContributionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<SubcomponentCreatorRequestRepresentation.Factory> create(
            SubcomponentCreatorRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new SubcomponentCreatorRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
