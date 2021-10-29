package dagger.internal.codegen.writing;


import java.util.Optional;

import javax.annotation.Generated;
import javax.inject.Provider;

import androidx.room.compiler.processing.XMessager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
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
public final class ComponentImplementation_Factory implements Factory<ComponentImplementation> {
    private final Provider<Optional<ComponentImplementation>> parentProvider;

    private final Provider<ComponentImplementation.ChildComponentImplementationFactory> childComponentImplementationFactoryProvider;

    private final Provider<ComponentRequestRepresentations> bindingExpressionsProvider;

    private final Provider<ComponentCreatorImplementationFactory> componentCreatorImplementationFactoryProvider;

    private final Provider<BindingGraph> graphProvider;

    private final Provider<ComponentNames> componentNamesProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    private final Provider<XMessager> messagerProvider;

    public ComponentImplementation_Factory(Provider<Optional<ComponentImplementation>> parentProvider,
                                           Provider<ComponentImplementation.ChildComponentImplementationFactory> childComponentImplementationFactoryProvider,
                                           Provider<ComponentRequestRepresentations> bindingExpressionsProvider,
                                           Provider<ComponentCreatorImplementationFactory> componentCreatorImplementationFactoryProvider,
                                           Provider<BindingGraph> graphProvider, Provider<ComponentNames> componentNamesProvider,
                                           Provider<CompilerOptions> compilerOptionsProvider, Provider<DaggerElements> elementsProvider,
                                           Provider<DaggerTypes> typesProvider, Provider<KotlinMetadataUtil> metadataUtilProvider,
                                           Provider<XMessager> messagerProvider) {
        this.parentProvider = parentProvider;
        this.childComponentImplementationFactoryProvider = childComponentImplementationFactoryProvider;
        this.bindingExpressionsProvider = bindingExpressionsProvider;
        this.componentCreatorImplementationFactoryProvider = componentCreatorImplementationFactoryProvider;
        this.graphProvider = graphProvider;
        this.componentNamesProvider = componentNamesProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
        this.elementsProvider = elementsProvider;
        this.typesProvider = typesProvider;
        this.metadataUtilProvider = metadataUtilProvider;
        this.messagerProvider = messagerProvider;
    }

    @Override
    public ComponentImplementation get() {
        return newInstance(parentProvider.get(), childComponentImplementationFactoryProvider.get(), bindingExpressionsProvider, componentCreatorImplementationFactoryProvider, graphProvider.get(), componentNamesProvider.get(), compilerOptionsProvider.get(), elementsProvider.get(), typesProvider.get(), metadataUtilProvider.get(), messagerProvider.get());
    }

    public static ComponentImplementation_Factory create(
            Provider<Optional<ComponentImplementation>> parentProvider,
            Provider<ComponentImplementation.ChildComponentImplementationFactory> childComponentImplementationFactoryProvider,
            Provider<ComponentRequestRepresentations> bindingExpressionsProvider,
            Provider<ComponentCreatorImplementationFactory> componentCreatorImplementationFactoryProvider,
            Provider<BindingGraph> graphProvider, Provider<ComponentNames> componentNamesProvider,
            Provider<CompilerOptions> compilerOptionsProvider, Provider<DaggerElements> elementsProvider,
            Provider<DaggerTypes> typesProvider, Provider<KotlinMetadataUtil> metadataUtilProvider,
            Provider<XMessager> messagerProvider) {
        return new ComponentImplementation_Factory(parentProvider, childComponentImplementationFactoryProvider, bindingExpressionsProvider, componentCreatorImplementationFactoryProvider, graphProvider, componentNamesProvider, compilerOptionsProvider, elementsProvider, typesProvider, metadataUtilProvider, messagerProvider);
    }

    public static ComponentImplementation newInstance(Optional<ComponentImplementation> parent,
                                                      ComponentImplementation.ChildComponentImplementationFactory childComponentImplementationFactory,
                                                      Provider<ComponentRequestRepresentations> bindingExpressionsProvider,
                                                      Provider<ComponentCreatorImplementationFactory> componentCreatorImplementationFactoryProvider,
                                                      BindingGraph graph, ComponentNames componentNames, CompilerOptions compilerOptions,
                                                      DaggerElements elements, DaggerTypes types, KotlinMetadataUtil metadataUtil,
                                                      XMessager messager) {
        return new ComponentImplementation(parent, childComponentImplementationFactory, bindingExpressionsProvider, componentCreatorImplementationFactoryProvider, graph, componentNames, compilerOptions, elements, types, metadataUtil, messager);
    }
}
