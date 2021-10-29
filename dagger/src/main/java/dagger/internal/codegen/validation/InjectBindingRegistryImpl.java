package dagger.internal.codegen.validation;


import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.room.compiler.processing.XMessager;
import dagger.Component;
import dagger.Provides;
import dagger.internal.codegen.binding.BindingFactory;
import dagger.internal.codegen.binding.InjectBindingRegistry;
import dagger.internal.codegen.binding.KeyFactory;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Maintains the collection of provision bindings from {@link Inject} constructors and members
 * injection bindings from {@link Inject} fields and methods known to the annotation processor.
 * Note that this registry <b>does not</b> handle any explicit bindings (those from {@link Provides}
 * methods, {@link Component} dependencies, etc.).
 * <p>
 * 收集Inject注解的具体实现
 */
@Singleton
final class InjectBindingRegistryImpl implements InjectBindingRegistry {
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final XMessager messager;
    private final InjectValidator injectValidator;
//    private final InjectValidator injectValidatorWhenGeneratingCode;
    private final KeyFactory keyFactory;
    private final BindingFactory bindingFactory;
    private final CompilerOptions compilerOptions;

    @Inject
    InjectBindingRegistryImpl(
            DaggerElements elements,
            DaggerTypes types,
            XMessager messager,
            InjectValidator injectValidator,
            KeyFactory keyFactory,
            BindingFactory bindingFactory,
            CompilerOptions compilerOptions) {

        this.elements = elements;
        this.types = types;
        this.messager = messager;
        this.injectValidator = injectValidator;
//        this.injectValidatorWhenGeneratingCode = injectValidator.whenGeneratingCode();
        this.keyFactory = keyFactory;
        this.bindingFactory = bindingFactory;
        this.compilerOptions = compilerOptions;
    }
}
