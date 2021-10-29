package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import dagger.spi.model.Key;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class ImmediateFutureRequestRepresentation_Factory_Impl implements ImmediateFutureRequestRepresentation.Factory {
    private final ImmediateFutureRequestRepresentation_Factory delegateFactory;

    ImmediateFutureRequestRepresentation_Factory_Impl(
            ImmediateFutureRequestRepresentation_Factory delegateFactory) {
        this.delegateFactory = delegateFactory;
    }

    @Override
    public ImmediateFutureRequestRepresentation create(Key key) {
        return delegateFactory.get(key);
    }

    public static Provider<ImmediateFutureRequestRepresentation.Factory> create(
            ImmediateFutureRequestRepresentation_Factory delegateFactory) {
        return InstanceFactory.create(new ImmediateFutureRequestRepresentation_Factory_Impl(delegateFactory));
    }
}
