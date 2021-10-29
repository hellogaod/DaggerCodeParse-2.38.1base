package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
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
public final class MembersInjectionRequestRepresentation_Factory {
    private final Provider<MembersInjectionMethods> membersInjectionMethodsProvider;

    public MembersInjectionRequestRepresentation_Factory(
            Provider<MembersInjectionMethods> membersInjectionMethodsProvider) {
        this.membersInjectionMethodsProvider = membersInjectionMethodsProvider;
    }

    public MembersInjectionRequestRepresentation get(MembersInjectionBinding binding) {
        return newInstance(binding, membersInjectionMethodsProvider.get());
    }

    public static MembersInjectionRequestRepresentation_Factory create(
            Provider<MembersInjectionMethods> membersInjectionMethodsProvider) {
        return new MembersInjectionRequestRepresentation_Factory(membersInjectionMethodsProvider);
    }

    public static MembersInjectionRequestRepresentation newInstance(MembersInjectionBinding binding,
                                                                    Object membersInjectionMethods) {
        return new MembersInjectionRequestRepresentation(binding, (MembersInjectionMethods) membersInjectionMethods);
    }
}
