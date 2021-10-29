package dagger.internal.codegen.validation;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.base.ElementFormatter;
import dagger.internal.codegen.binding.DependencyRequestFormatter;
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
public final class DiagnosticMessageGenerator_Factory_Factory implements Factory<DiagnosticMessageGenerator.Factory> {
    private final Provider<DaggerTypes> typesProvider;

    private final Provider<DependencyRequestFormatter> dependencyRequestFormatterProvider;

    private final Provider<ElementFormatter> elementFormatterProvider;

    public DiagnosticMessageGenerator_Factory_Factory(Provider<DaggerTypes> typesProvider,
                                                      Provider<DependencyRequestFormatter> dependencyRequestFormatterProvider,
                                                      Provider<ElementFormatter> elementFormatterProvider) {
        this.typesProvider = typesProvider;
        this.dependencyRequestFormatterProvider = dependencyRequestFormatterProvider;
        this.elementFormatterProvider = elementFormatterProvider;
    }

    @Override
    public DiagnosticMessageGenerator.Factory get() {
        return newInstance(typesProvider.get(), dependencyRequestFormatterProvider.get(), elementFormatterProvider.get());
    }

    public static DiagnosticMessageGenerator_Factory_Factory create(
            Provider<DaggerTypes> typesProvider,
            Provider<DependencyRequestFormatter> dependencyRequestFormatterProvider,
            Provider<ElementFormatter> elementFormatterProvider) {
        return new DiagnosticMessageGenerator_Factory_Factory(typesProvider, dependencyRequestFormatterProvider, elementFormatterProvider);
    }

    public static DiagnosticMessageGenerator.Factory newInstance(DaggerTypes types,
                                                                 DependencyRequestFormatter dependencyRequestFormatter, ElementFormatter elementFormatter) {
        return new DiagnosticMessageGenerator.Factory(types, dependencyRequestFormatter, elementFormatter);
    }
}
