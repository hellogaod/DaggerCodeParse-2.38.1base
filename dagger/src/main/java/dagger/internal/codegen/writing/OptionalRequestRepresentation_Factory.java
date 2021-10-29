package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;
import javax.lang.model.SourceVersion;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.ProvisionBinding;
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
public final class OptionalRequestRepresentation_Factory {
    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<SourceVersion> sourceVersionProvider;

    public OptionalRequestRepresentation_Factory(
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<DaggerTypes> typesProvider, Provider<SourceVersion> sourceVersionProvider) {
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
        this.typesProvider = typesProvider;
        this.sourceVersionProvider = sourceVersionProvider;
    }

    public OptionalRequestRepresentation get(ProvisionBinding binding) {
        return newInstance(binding, componentRequestRepresentationsProvider.get(), typesProvider.get(), sourceVersionProvider.get());
    }

    public static OptionalRequestRepresentation_Factory create(
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<DaggerTypes> typesProvider, Provider<SourceVersion> sourceVersionProvider) {
        return new OptionalRequestRepresentation_Factory(componentRequestRepresentationsProvider, typesProvider, sourceVersionProvider);
    }

    public static OptionalRequestRepresentation newInstance(ProvisionBinding binding,
                                                            ComponentRequestRepresentations componentRequestRepresentations, DaggerTypes types,
                                                            SourceVersion sourceVersion) {
        return new OptionalRequestRepresentation(binding, componentRequestRepresentations, types, sourceVersion);
    }
}
