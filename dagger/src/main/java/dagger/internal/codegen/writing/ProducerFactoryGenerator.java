package dagger.internal.codegen.writing;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.KeyFactory;
import dagger.internal.codegen.binding.ProductionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.producers.Producer;

/** Generates {@link Producer} implementations from {@link ProductionBinding} instances. */
public final class ProducerFactoryGenerator extends SourceFileGenerator<ProductionBinding> {
    private final CompilerOptions compilerOptions;
    private final KeyFactory keyFactory;

    @Inject
    ProducerFactoryGenerator(
            XFiler filer,
            DaggerElements elements,
            SourceVersion sourceVersion,
            CompilerOptions compilerOptions,
            KeyFactory keyFactory) {
        this.compilerOptions = compilerOptions;
        this.keyFactory = keyFactory;
    }
}
