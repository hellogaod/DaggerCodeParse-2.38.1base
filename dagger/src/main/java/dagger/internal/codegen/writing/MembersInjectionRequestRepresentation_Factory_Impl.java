package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import dagger.internal.codegen.binding.MembersInjectionBinding;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class MembersInjectionRequestRepresentation_Factory_Impl implements MembersInjectionRequestRepresentation.Factory {
    private final MembersInjectionRequestRepresentation_Factory delegateFactory;

    MembersInjectionRequestRepresentation_Factory_Impl(
            MembersInjectionRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public MembersInjectionRequestRepresentation create(MembersInjectionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<MembersInjectionRequestRepresentation.Factory> create(
            MembersInjectionRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new MembersInjectionRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
