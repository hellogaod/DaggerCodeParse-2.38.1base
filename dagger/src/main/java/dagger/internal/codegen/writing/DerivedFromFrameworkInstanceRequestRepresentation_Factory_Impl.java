package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.FrameworkType;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class DerivedFromFrameworkInstanceRequestRepresentation_Factory_Impl implements DerivedFromFrameworkInstanceRequestRepresentation.Factory {
    private final DerivedFromFrameworkInstanceRequestRepresentation_Factory delegateFactory;

    DerivedFromFrameworkInstanceRequestRepresentation_Factory_Impl(
            DerivedFromFrameworkInstanceRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public DerivedFromFrameworkInstanceRequestRepresentation create(BindingRequest request,
                                                                    FrameworkType frameworkType) {
        return delegateFactory.get(request, frameworkType);
    }

    public static Provider<DerivedFromFrameworkInstanceRequestRepresentation.Factory> create(
            DerivedFromFrameworkInstanceRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new DerivedFromFrameworkInstanceRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
