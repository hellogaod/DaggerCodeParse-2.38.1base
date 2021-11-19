package dagger.internal.codegen.validation;


import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import androidx.room.compiler.processing.XMessager;
import dagger.Component;
import dagger.Provides;
import dagger.internal.codegen.base.SourceFileGenerationException;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.Binding;
import dagger.internal.codegen.binding.BindingFactory;
import dagger.internal.codegen.binding.InjectBindingRegistry;
import dagger.internal.codegen.binding.KeyFactory;
import dagger.internal.codegen.binding.MembersInjectionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.Key;

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

    // TODO(dpb): make the SourceFileGenerators fields so they don't have to be passed in
    @Override
    public void generateSourcesForRequiredBindings(
            SourceFileGenerator<ProvisionBinding> factoryGenerator,
            SourceFileGenerator<MembersInjectionBinding> membersInjectorGenerator)
            throws SourceFileGenerationException {

    }

    @Override
    public Optional<ProvisionBinding> tryRegisterConstructor(ExecutableElement constructorElement) {
        return tryRegisterConstructor(constructorElement, Optional.empty(), false);
    }

    @CanIgnoreReturnValue
    private Optional<ProvisionBinding> tryRegisterConstructor(
            ExecutableElement constructorElement,
            Optional<TypeMirror> resolvedType,
            boolean warnIfNotAlreadyGenerated) {

        ValidationReport report = injectValidator.validateConstructor(constructorElement);
        report.printMessagesTo(messager);
//        if (!report.isClean()) {
//
//        }
        return Optional.empty();
    }

    @Override
    public Optional<MembersInjectionBinding> tryRegisterMembersInjectedType(TypeElement typeElement) {
        return tryRegisterMembersInjectedType(typeElement, Optional.empty(), false);
    }

    @CanIgnoreReturnValue
    private Optional<MembersInjectionBinding> tryRegisterMembersInjectedType(
            TypeElement typeElement,
            Optional<TypeMirror> resolvedType,
            boolean warnIfNotAlreadyGenerated) {
        ValidationReport report = injectValidator.validateMembersInjectionType(typeElement);
        report.printMessagesTo(messager);
//        if (!report.isClean()) {
//
//        }

        return Optional.empty();
    }

    @Override
    public Optional<ProvisionBinding> getOrFindProvisionBinding(Key key) {
        return Optional.empty();
    }

    @Override
    public Optional<MembersInjectionBinding> getOrFindMembersInjectionBinding(Key key) {
        return Optional.empty();
    }

    @Override
    public Optional<ProvisionBinding> getOrFindMembersInjectorProvisionBinding(Key key) {
        return Optional.empty();
    }
}
