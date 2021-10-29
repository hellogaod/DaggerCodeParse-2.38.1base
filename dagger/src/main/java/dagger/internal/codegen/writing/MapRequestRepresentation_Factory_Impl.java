package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import dagger.internal.codegen.binding.ProvisionBinding;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class MapRequestRepresentation_Factory_Impl implements MapRequestRepresentation.Factory {
    private final MapRequestRepresentation_Factory delegateFactory;

    MapRequestRepresentation_Factory_Impl(MapRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public MapRequestRepresentation create(ProvisionBinding binding) {
        return delegateFactory.get(binding);
    }

    public static Provider<MapRequestRepresentation.Factory> create(
            MapRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new MapRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
