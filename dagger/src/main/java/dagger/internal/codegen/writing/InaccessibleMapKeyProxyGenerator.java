package dagger.internal.codegen.writing;


import javax.inject.Inject;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Generates a class that exposes a non-{@code public} {@link
 * ContributionBinding#mapKeyAnnotation()} @MapKey} annotation.
 */
public final class InaccessibleMapKeyProxyGenerator
        extends SourceFileGenerator<ContributionBinding> {
    private final DaggerTypes types;
    private final DaggerElements elements;

    @Inject
    InaccessibleMapKeyProxyGenerator(
            XFiler filer,
            DaggerTypes types,
            DaggerElements elements,
            SourceVersion sourceVersion
    ) {

        this.types = types;
        this.elements = elements;
    }
}
