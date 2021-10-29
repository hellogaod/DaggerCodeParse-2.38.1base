package dagger.internal.codegen.validation;


import javax.inject.Inject;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
import androidx.room.compiler.processing.XTypeElement;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.langmodel.DaggerElements;

/** Generates a monitoring module for use with production components. */
final class MonitoringModuleGenerator extends SourceFileGenerator<XTypeElement> {

    @Inject
    MonitoringModuleGenerator(
            XFiler filer,
            DaggerElements elements,
            SourceVersion sourceVersion
    ) {

    }
}
