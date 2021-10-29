package dagger.internal.codegen.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.MembersInjectionBinding;

/**
 * Copyright (C), 2019-2021, 佛生
 * FileName: MembersInjectionRequestRepresentation
 * Author: 佛学徒
 * Date: 2021/10/25 11:00
 * Description:
 * History:
 */
class MembersInjectionRequestRepresentation {

    private final MembersInjectionBinding binding;
    private final MembersInjectionMethods membersInjectionMethods;

    @AssistedInject
    MembersInjectionRequestRepresentation(
            @Assisted MembersInjectionBinding binding,
            MembersInjectionMethods membersInjectionMethods
    ) {
        this.binding = binding;
        this.membersInjectionMethods = membersInjectionMethods;
    }


    @AssistedFactory
    static interface Factory {
        MembersInjectionRequestRepresentation create(MembersInjectionBinding binding);
    }
}
