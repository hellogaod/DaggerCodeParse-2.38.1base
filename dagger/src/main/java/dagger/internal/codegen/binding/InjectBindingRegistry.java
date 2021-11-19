package dagger.internal.codegen.binding;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import dagger.Component;
import dagger.Provides;
import dagger.internal.codegen.base.SourceFileGenerationException;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.spi.model.Key;

/**
 * Maintains the collection of provision bindings from {@link Inject} constructors and members
 * injection bindings from {@link Inject} fields and methods known to the annotation processor. Note
 * that this registry <b>does not</b> handle any explicit bindings (those from {@link Provides}
 * methods, {@link Component} dependencies, etc.).
 * <p>
 * 收集使用Inject注解的构造函数和成员注入绑定，成员注入绑定来源于使用Inject注解的变量和普通方法：
 * 1.这里构造函数使用Inject注解，等价于Module注解类使用Privider注解的方法
 * 2.成员注入绑定，表示当前类使用@Inject注解的变量（或普通方法）需要注入到当前类：
 *
 * e.g.如何注入？
 * 1.当前类A使用@Inject注解变量B
 * 2.①变量B的构造函数使用了Inject注解，或②变量B在Module注解类使用了Provider注解方法，返回类型是B
 */
public interface InjectBindingRegistry {

    /**
     * Returns a {@link ProvisionBinding} for {@code key}. If none has been registered yet, registers
     * one.
     */
    Optional<ProvisionBinding> getOrFindProvisionBinding(Key key);

    /**
     * Returns a {@link MembersInjectionBinding} for {@code key}. If none has been registered yet,
     * registers one, along with all necessary members injection bindings for superclasses.
     */
    Optional<MembersInjectionBinding> getOrFindMembersInjectionBinding(Key key);

    /**
     * Returns a {@link ProvisionBinding} for a {@link dagger.MembersInjector} of {@code key}. If none
     * has been registered yet, registers one.
     */
    Optional<ProvisionBinding> getOrFindMembersInjectorProvisionBinding(Key key);

    @CanIgnoreReturnValue
    Optional<ProvisionBinding> tryRegisterConstructor(ExecutableElement constructorElement);

    @CanIgnoreReturnValue
    Optional<MembersInjectionBinding> tryRegisterMembersInjectedType(TypeElement typeElement);

    /**
     * This method ensures that sources for all registered {@link Binding bindings} (either explicitly
     * or implicitly via {@link #getOrFindMembersInjectionBinding} or {@link
     * #getOrFindProvisionBinding}) are generated.
     */
    void generateSourcesForRequiredBindings(
            SourceFileGenerator<ProvisionBinding> factoryGenerator,
            SourceFileGenerator<MembersInjectionBinding> membersInjectorGenerator)
            throws SourceFileGenerationException;
}

