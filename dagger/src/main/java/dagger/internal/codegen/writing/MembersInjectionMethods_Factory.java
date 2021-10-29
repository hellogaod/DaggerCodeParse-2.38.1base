package dagger.internal.codegen.writing;


import javax.annotation.Generated;
import javax.inject.Provider;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.codegen.binding.BindingGraph;
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
public final class MembersInjectionMethods_Factory implements Factory<MembersInjectionMethods> {
    private final Provider<ComponentImplementation> componentImplementationProvider;

    private final Provider<ComponentRequestRepresentations> bindingExpressionsProvider;

    private final Provider<BindingGraph> graphProvider;

    private final Provider<DaggerElements> elementsProvider;

    private final Provider<DaggerTypes> typesProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    public MembersInjectionMethods_Factory(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> bindingExpressionsProvider,
            Provider<BindingGraph> graphProvider, Provider<DaggerElements> elementsProvider,
            Provider<DaggerTypes> typesProvider, Provider<KotlinMetadataUtil> metadataUtilProvider) {
        this.componentImplementationProvider = componentImplementationProvider;
        this.bindingExpressionsProvider = bindingExpressionsProvider;
        this.graphProvider = graphProvider;
        this.elementsProvider = elementsProvider;
        this.typesProvider = typesProvider;
        this.metadataUtilProvider = metadataUtilProvider;
    }

    @Override
    public MembersInjectionMethods get() {
        return newInstance(componentImplementationProvider.get(), bindingExpressionsProvider.get(), graphProvider.get(), elementsProvider.get(), typesProvider.get(), metadataUtilProvider.get());
    }

    public static MembersInjectionMethods_Factory create(
            Provider<ComponentImplementation> componentImplementationProvider,
            Provider<ComponentRequestRepresentations> bindingExpressionsProvider,
            Provider<BindingGraph> graphProvider, Provider<DaggerElements> elementsProvider,
            Provider<DaggerTypes> typesProvider, Provider<KotlinMetadataUtil> metadataUtilProvider) {
        return new MembersInjectionMethods_Factory(componentImplementationProvider, bindingExpressionsProvider, graphProvider, elementsProvider, typesProvider, metadataUtilProvider);
    }

    public static MembersInjectionMethods newInstance(ComponentImplementation componentImplementation,
                                                      ComponentRequestRepresentations bindingExpressions, BindingGraph graph,
                                                      DaggerElements elements, DaggerTypes types, KotlinMetadataUtil metadataUtil) {
        return new MembersInjectionMethods(componentImplementation, bindingExpressions, graph, elements, types, metadataUtil);
    }
}
