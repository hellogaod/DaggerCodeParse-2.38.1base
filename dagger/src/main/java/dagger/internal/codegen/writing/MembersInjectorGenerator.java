package dagger.internal.codegen.writing;

import javax.inject.Inject;

import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.MembersInjectionBinding;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Generates {@link MembersInjector} implementations from {@link MembersInjectionBinding} instances.
 */
public final class MembersInjectorGenerator extends SourceFileGenerator<MembersInjectionBinding> {
    private final DaggerTypes types;

    @Inject
    MembersInjectorGenerator(
//            XFiler filer,
//            DaggerElements elements,
            DaggerTypes types
//            SourceVersion sourceVersion
    ) {

        this.types = types;
    }
}
