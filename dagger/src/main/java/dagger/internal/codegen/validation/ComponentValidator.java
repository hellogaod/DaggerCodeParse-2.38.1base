package dagger.internal.codegen.validation;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.TypeElement;

import dagger.internal.codegen.base.ClearableCache;
import dagger.internal.codegen.binding.DependencyRequestFactory;
import dagger.internal.codegen.binding.MethodSignatureFormatter;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.internal.codegen.validation.ComponentCreatorValidator;
import dagger.internal.codegen.validation.DependencyRequestValidator;
import dagger.internal.codegen.validation.ModuleValidator;
import dagger.internal.codegen.validation.ValidationReport;

@Singleton
public final class ComponentValidator implements ClearableCache {

    private final DaggerElements elements;
    private final DaggerTypes types;
    private final ModuleValidator moduleValidator;
    private final ComponentCreatorValidator creatorValidator;
    private final DependencyRequestValidator dependencyRequestValidator;
    private final MembersInjectionValidator membersInjectionValidator;
    private final MethodSignatureFormatter methodSignatureFormatter;
    private final DependencyRequestFactory dependencyRequestFactory;
    private final Map<TypeElement, ValidationReport> reports = new HashMap<>();
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    ComponentValidator(
            DaggerElements elements,
            DaggerTypes types,
            ModuleValidator moduleValidator,
            ComponentCreatorValidator creatorValidator,
            DependencyRequestValidator dependencyRequestValidator,
            MembersInjectionValidator membersInjectionValidator,
            MethodSignatureFormatter methodSignatureFormatter,
            DependencyRequestFactory dependencyRequestFactory,
            KotlinMetadataUtil metadataUtil) {
        this.elements = elements;
        this.types = types;
        this.moduleValidator = moduleValidator;
        this.creatorValidator = creatorValidator;
        this.dependencyRequestValidator = dependencyRequestValidator;
        this.membersInjectionValidator = membersInjectionValidator;
        this.methodSignatureFormatter = methodSignatureFormatter;
        this.dependencyRequestFactory = dependencyRequestFactory;
        this.metadataUtil = metadataUtil;
    }

    @Override
    public void clearCache() {

    }
}
