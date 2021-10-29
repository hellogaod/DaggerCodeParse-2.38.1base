package dagger.internal.codegen.componentgenerator;


import javax.annotation.Generated;
import javax.inject.Provider;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.langmodel.DaggerElements;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class ComponentGenerator_Factory implements Factory<ComponentGenerator> {
    private final Provider<XFiler> filerProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<SourceVersion> sourceVersionProvider;

    private final Provider<TopLevelImplementationComponent.Factory> topLevelImplementationComponentFactoryProvider;

    public ComponentGenerator_Factory(Provider<XFiler> filerProvider,
                                      Provider<DaggerElements> elementsProvider, Provider<SourceVersion> sourceVersionProvider,
                                      Provider<TopLevelImplementationComponent.Factory> topLevelImplementationComponentFactoryProvider) {
        this.filerProvider = filerProvider;
        this.elementsProvider = elementsProvider;
        this.sourceVersionProvider = sourceVersionProvider;
        this.topLevelImplementationComponentFactoryProvider = topLevelImplementationComponentFactoryProvider;
    }

    @Override
    public ComponentGenerator get() {
        return newInstance(filerProvider.get(), elementsProvider.get(), sourceVersionProvider.get(), topLevelImplementationComponentFactoryProvider.get());
    }

    public static ComponentGenerator_Factory create(Provider<XFiler> filerProvider,
                                                    Provider<DaggerElements> elementsProvider, Provider<SourceVersion> sourceVersionProvider,
                                                    Provider<TopLevelImplementationComponent.Factory> topLevelImplementationComponentFactoryProvider) {
        return new ComponentGenerator_Factory(filerProvider, elementsProvider, sourceVersionProvider, topLevelImplementationComponentFactoryProvider);
    }

    public static ComponentGenerator newInstance(XFiler filer, DaggerElements elements,
                                                 SourceVersion sourceVersion,
                                                 TopLevelImplementationComponent.Factory topLevelImplementationComponentFactory) {
        return new ComponentGenerator(filer, elements, sourceVersion, topLevelImplementationComponentFactory);
    }
}
