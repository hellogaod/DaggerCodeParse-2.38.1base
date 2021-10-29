package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;
import javax.lang.model.SourceVersion;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.langmodel.DaggerTypes;
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
public final class ImmediateFutureRequestRepresentation_Factory {
    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<SourceVersion> sourceVersionProvider;

    public ImmediateFutureRequestRepresentation_Factory(
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<DaggerTypes> typesProvider, Provider<SourceVersion> sourceVersionProvider) {
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
        this.typesProvider = typesProvider;
        this.sourceVersionProvider = sourceVersionProvider;
    }

    public ImmediateFutureRequestRepresentation get(Key key) {
        return newInstance(key, componentRequestRepresentationsProvider.get(), typesProvider.get(), sourceVersionProvider.get());
    }

    public static ImmediateFutureRequestRepresentation_Factory create(
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<DaggerTypes> typesProvider, Provider<SourceVersion> sourceVersionProvider) {
        return new ImmediateFutureRequestRepresentation_Factory(componentRequestRepresentationsProvider, typesProvider, sourceVersionProvider);
    }

    public static ImmediateFutureRequestRepresentation newInstance(Key key,
                                                                   ComponentRequestRepresentations componentRequestRepresentations, DaggerTypes types,
                                                                   SourceVersion sourceVersion) {
        return new ImmediateFutureRequestRepresentation(key, componentRequestRepresentations, types, sourceVersion);
    }
}
