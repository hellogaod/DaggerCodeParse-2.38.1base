package dagger.internal.codegen.writing;

import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.langmodel.DaggerTypes;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class MembersInjectorGenerator_Factory implements Factory<MembersInjectorGenerator> {

    private final Provider<DaggerTypes> typesProvider;


    public MembersInjectorGenerator_Factory(Provider<DaggerTypes> typesProvider) {

        this.typesProvider = typesProvider;
    }

    @Override
    public MembersInjectorGenerator get() {
        return newInstance( typesProvider.get());
    }

    public static MembersInjectorGenerator_Factory create(Provider<DaggerTypes> typesProvider) {
        return new MembersInjectorGenerator_Factory(typesProvider);
    }

    public static MembersInjectorGenerator newInstance(DaggerTypes types) {
        return new MembersInjectorGenerator(types);
    }
}
