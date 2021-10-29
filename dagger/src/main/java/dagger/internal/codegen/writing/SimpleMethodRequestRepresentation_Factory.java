package dagger.internal.codegen.writing;

import javax.annotation.Generated;
import javax.inject.Provider;
import javax.lang.model.SourceVersion;

import dagger.internal.DaggerGenerated;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;

@DaggerGenerated
@Generated(
        value = "dagger.internal.codegen.ComponentProcessor",
        comments = "https://dagger.dev"
)
@SuppressWarnings({
        "unchecked",
        "rawtypes"
})
public final class SimpleMethodRequestRepresentation_Factory {
    private final Provider<MembersInjectionMethods> membersInjectionMethodsProvider;

    private final Provider<CompilerOptions> compilerOptionsProvider;

    private final Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider;

    private final Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider;

    private final Provider<SourceVersion> sourceVersionProvider;

    private final Provider<KotlinMetadataUtil> metadataUtilProvider;

    private final Provider<ComponentImplementation> componentImplementationProvider;

    public SimpleMethodRequestRepresentation_Factory(
            Provider<MembersInjectionMethods> membersInjectionMethodsProvider,
            Provider<CompilerOptions> compilerOptionsProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider,
            Provider<SourceVersion> sourceVersionProvider,
            Provider<KotlinMetadataUtil> metadataUtilProvider,
            Provider<ComponentImplementation> componentImplementationProvider) {
        this.membersInjectionMethodsProvider = membersInjectionMethodsProvider;
        this.compilerOptionsProvider = compilerOptionsProvider;
        this.componentRequestRepresentationsProvider = componentRequestRepresentationsProvider;
        this.componentRequirementExpressionsProvider = componentRequirementExpressionsProvider;
        this.sourceVersionProvider = sourceVersionProvider;
        this.metadataUtilProvider = metadataUtilProvider;
        this.componentImplementationProvider = componentImplementationProvider;
    }

    public SimpleMethodRequestRepresentation get(ProvisionBinding binding) {
        return newInstance(binding, membersInjectionMethodsProvider.get(), compilerOptionsProvider.get(), componentRequestRepresentationsProvider.get(), componentRequirementExpressionsProvider.get(), sourceVersionProvider.get(), metadataUtilProvider.get(), componentImplementationProvider.get());
    }

    public static SimpleMethodRequestRepresentation_Factory create(
            Provider<MembersInjectionMethods> membersInjectionMethodsProvider,
            Provider<CompilerOptions> compilerOptionsProvider,
            Provider<ComponentRequestRepresentations> componentRequestRepresentationsProvider,
            Provider<ComponentRequirementExpressions> componentRequirementExpressionsProvider,
            Provider<SourceVersion> sourceVersionProvider,
            Provider<KotlinMetadataUtil> metadataUtilProvider,
            Provider<ComponentImplementation> componentImplementationProvider) {
        return new SimpleMethodRequestRepresentation_Factory(membersInjectionMethodsProvider, compilerOptionsProvider, componentRequestRepresentationsProvider, componentRequirementExpressionsProvider, sourceVersionProvider, metadataUtilProvider, componentImplementationProvider);
    }

    public static SimpleMethodRequestRepresentation newInstance(ProvisionBinding binding,
                                                                Object membersInjectionMethods, CompilerOptions compilerOptions,
                                                                ComponentRequestRepresentations componentRequestRepresentations,
                                                                ComponentRequirementExpressions componentRequirementExpressions, SourceVersion sourceVersion,
                                                                KotlinMetadataUtil metadataUtil, ComponentImplementation componentImplementation) {
        return new SimpleMethodRequestRepresentation(binding, (MembersInjectionMethods) membersInjectionMethods, compilerOptions, componentRequestRepresentations, componentRequirementExpressions, sourceVersion, metadataUtil, componentImplementation);
    }
}
