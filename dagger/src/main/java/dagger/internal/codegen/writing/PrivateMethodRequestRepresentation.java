package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: PrivateMethodRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 11:08
 * Description:
 * History:
 */
class PrivateMethodRequestRepresentation {

//    private final ShardImplementation shardImplementation;
    private final ContributionBinding binding;
    private final BindingRequest request;
    private final RequestRepresentation wrappedRequestRepresentation;
    private final CompilerOptions compilerOptions;
    private final DaggerTypes types;
    private String methodName;

    @AssistedInject
    PrivateMethodRequestRepresentation(
            @Assisted BindingRequest request,
            @Assisted ContributionBinding binding,
            @Assisted RequestRepresentation wrappedRequestRepresentation,
            ComponentImplementation componentImplementation,
            DaggerTypes types,
            CompilerOptions compilerOptions) {
//        super(componentImplementation.shardImplementation(binding), types);
        this.binding = checkNotNull(binding);
        this.request = checkNotNull(request);
        this.wrappedRequestRepresentation = checkNotNull(wrappedRequestRepresentation);
//        this.shardImplementation = componentImplementation.shardImplementation(binding);
        this.compilerOptions = compilerOptions;
        this.types = types;
    }


    @AssistedFactory
    static interface Factory {
        PrivateMethodRequestRepresentation create(
                BindingRequest request,
                ContributionBinding binding,
                RequestRepresentation wrappedRequestRepresentation);
    }
}
