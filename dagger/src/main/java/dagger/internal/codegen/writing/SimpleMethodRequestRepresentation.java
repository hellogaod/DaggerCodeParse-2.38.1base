package dagger.internal.codegen.writing;

import javax.lang.model.SourceVersion;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: SimpleMethodRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 11:24
 * Description:
 * History:
 */
class SimpleMethodRequestRepresentation {

    private final CompilerOptions compilerOptions;
    private final ProvisionBinding provisionBinding;
    private final ComponentRequestRepresentations componentRequestRepresentations;
    private final MembersInjectionMethods membersInjectionMethods;
    private final ComponentRequirementExpressions componentRequirementExpressions;
    private final SourceVersion sourceVersion;
    private final KotlinMetadataUtil metadataUtil;
//    private final ShardImplementation shardImplementation;

    @AssistedInject
    SimpleMethodRequestRepresentation(
            @Assisted ProvisionBinding binding,
            MembersInjectionMethods membersInjectionMethods,
            CompilerOptions compilerOptions,
            ComponentRequestRepresentations componentRequestRepresentations,
            ComponentRequirementExpressions componentRequirementExpressions,
            SourceVersion sourceVersion,
            KotlinMetadataUtil metadataUtil,
            ComponentImplementation componentImplementation) {
//        super(binding);
        this.compilerOptions = compilerOptions;
        this.provisionBinding = binding;
        this.metadataUtil = metadataUtil;
//        checkArgument(
//                provisionBinding.implicitDependencies().isEmpty(),
//                "framework deps are not currently supported");
//        checkArgument(provisionBinding.bindingElement().isPresent());
        this.componentRequestRepresentations = componentRequestRepresentations;
        this.membersInjectionMethods = membersInjectionMethods;
        this.componentRequirementExpressions = componentRequirementExpressions;
        this.sourceVersion = sourceVersion;
//        this.shardImplementation = componentImplementation.shardImplementation(binding);
    }

    @AssistedFactory
    static interface Factory {
        SimpleMethodRequestRepresentation create(ProvisionBinding binding);
    }
}
