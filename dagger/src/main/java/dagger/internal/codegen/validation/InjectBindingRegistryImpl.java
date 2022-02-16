package dagger.internal.codegen.validation;


import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import androidx.room.compiler.processing.XMessager;
import dagger.Component;
import dagger.MembersInjector;
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static dagger.internal.codegen.base.Keys.isValidImplicitProvisionKey;
import static dagger.internal.codegen.base.Keys.isValidMembersInjectionKey;
import static dagger.internal.codegen.binding.AssistedInjectionAnnotations.assistedInjectedConstructors;
import static dagger.internal.codegen.binding.InjectionAnnotations.injectedConstructors;
import static dagger.internal.codegen.binding.SourceFiles.generatedClassNameForBinding;
import static dagger.internal.codegen.langmodel.DaggerTypes.unwrapType;

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
    private final InjectValidator injectValidatorWhenGeneratingCode;
    private final KeyFactory keyFactory;
    private final BindingFactory bindingFactory;
    private final CompilerOptions compilerOptions;

    //绑定收集器,并且生成代码
    final class BindingsCollection<B extends Binding> {

        private final Class<?> factoryClass;

        //绑定Map：k，绑定的key，v：该k的绑定
        private final Map<Key, B> bindingsByKey = Maps.newLinkedHashMap();

        //收集绑定用于生成代码的节点
        private final Deque<B> bindingsRequiringGeneration = new ArrayDeque<>();

        //在绑定生成代码时用于收集binding.key
        private final Set<Key> materializedBindingKeys = Sets.newLinkedHashSet();

        BindingsCollection(Class<?> factoryClass) {
            this.factoryClass = factoryClass;
        }

        //遍历bindingsRequiringGeneration，先校验binding.key里面的java类型（Inject校验），校验通过生成代码
        void generateBindings(SourceFileGenerator<B> generator) throws SourceFileGenerationException {
            //
            for (B binding = bindingsRequiringGeneration.poll();
                 binding != null;
                 binding = bindingsRequiringGeneration.poll()) {

                //绑定中不存在未解析情况
                checkState(!binding.unresolved().isPresent());

                //binding.key里面包含的java类型如果进行Inject校验（校验java类型的构造函数使用Inject情况，变量和方法）
                if (injectValidatorWhenGeneratingCode.isValidType(binding.key().type().java())) {
                    generator.generate(binding);
                }

                materializedBindingKeys.add(binding.key());
            }
            // Because Elements instantiated across processing rounds are not guaranteed to be equals() to
            // the logically same element, clear the cache after generating
            bindingsByKey.clear();
        }

        /**
         * Returns a previously cached binding.
         */
        B getBinding(Key key) {
            return bindingsByKey.get(key);
        }

        /**
         * Caches the binding and generates it if it needs generation.
         * <p>
         * 当前绑定关系注册（缓存）进入本类，并且满足生成代码的条件放入bindingsRequiringGeneration集合中
         */
        void tryRegisterBinding(B binding, boolean warnIfNotAlreadyGenerated) {

            tryToCacheBinding(binding);

            //如果不存在未解析情况，直接使用本绑定；存在未解析情况，那么使用未解析的绑定
            @SuppressWarnings("unchecked")
            B maybeUnresolved =
                    binding.unresolved().isPresent() ? (B) binding.unresolved().get() : binding;
            tryToGenerateBinding(maybeUnresolved, warnIfNotAlreadyGenerated);
        }

        /**
         * Tries to generate a binding, not generating if it already is generated. For resolved
         * bindings, this will try to generate the unresolved version of the binding.
         */
        void tryToGenerateBinding(B binding, boolean warnIfNotAlreadyGenerated) {
            if (shouldGenerateBinding(binding)) {

                bindingsRequiringGeneration.offer(binding);

                if (compilerOptions.warnIfInjectionFactoryNotGeneratedUpstream()
                        && warnIfNotAlreadyGenerated) {
                    messager.printMessage(
                            Diagnostic.Kind.NOTE,
                            String.format(
                                    "Generating a %s for %s. "
                                            + "Prefer to run the dagger processor over that class instead.",
                                    factoryClass.getSimpleName(),
                                    types.erasure(binding.key().type().java()))); // erasure to strip <T> from msgs.
                }
            }
        }

        /**
         * Returns true if the binding needs to be generated.
         * <p>
         * 返回true表示需要可以用于生成代码，返回true条件：
         * 1.绑定不存在未解析情况 && 2.3.之前该绑定在本对象中没有被处理过 && 4.该绑定所生成的类不存在
         */
        private boolean shouldGenerateBinding(B binding) {
            return !binding.unresolved().isPresent()
                    && !materializedBindingKeys.contains(binding.key())
                    && !bindingsRequiringGeneration.contains(binding)
                    && elements.getTypeElement(generatedClassNameForBinding(binding)) == null;
        }


        /**
         * Caches the binding for future lookups by key.
         * <p>
         * 当前绑定存入bindingsByKey，K：当前绑定的key，v：k所在的绑定
         * <p>
         * 能被注册进入当前类只有两种情况：1.绑定中存在未解析情况；或 2. 绑定节点存在且没有使用泛型
         * <p>
         * 如果bindingsByKey以及存在了当前绑定的key，那么会报错。
         */
        private void tryToCacheBinding(B binding) {
            // We only cache resolved bindings or unresolved bindings w/o type arguments.
            // Unresolved bindings w/ type arguments aren't valid for the object graph.
            if (binding.unresolved().isPresent()
                    || binding.bindingTypeElement().get().getTypeParameters().isEmpty()) {
                Key key = binding.key();
                Binding previousValue = bindingsByKey.put(key, binding);
                checkState(previousValue == null || binding.equals(previousValue),
                        "couldn't register %s. %s was already registered for %s",
                        binding, previousValue, key);
            }
        }
    }

    private final BindingsCollection<ProvisionBinding> provisionBindings =
            new BindingsCollection<>(Provider.class);
    private final BindingsCollection<MembersInjectionBinding> membersInjectionBindings =
            new BindingsCollection<>(MembersInjector.class);

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
        this.injectValidatorWhenGeneratingCode = injectValidator.whenGeneratingCode();
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
        //调用该方法用于生成代码

        provisionBindings.generateBindings(factoryGenerator);
        membersInjectionBindings.generateBindings(membersInjectorGenerator);
    }

    /**
     * Registers the binding for generation and later lookup. If the binding is resolved, we also
     * attempt to register an unresolved version of it.
     * <p>
     * 注册（或收集）provision绑定,用于稍后的代码生成
     */
    private void registerBinding(ProvisionBinding binding, boolean warnIfNotAlreadyGenerated) {
        provisionBindings.tryRegisterBinding(binding, warnIfNotAlreadyGenerated);
    }


    /**
     * Registers the binding for generation and later lookup. If the binding is resolved, we also
     * attempt to register an unresolved version of it.
     */
    private void registerBinding(MembersInjectionBinding binding, boolean warnIfNotAlreadyGenerated) {
        /*
         * We generate MembersInjector classes for types with @Inject constructors only if they have any
         * injection sites.
         *
         * We generate MembersInjector classes for types without @Inject constructors only if they have
         * local (non-inherited) injection sites.
         *
         * Warn only when registering bindings post-hoc for those types.
         */
        if (warnIfNotAlreadyGenerated) {
            //hasInjectConstructor = true：如果绑定的membersInjectedType的构造函数使用了Inject注解（并且没有没有使用AssistedInject的构造函数）
            boolean hasInjectConstructor =
                    !(injectedConstructors(binding.membersInjectedType()).isEmpty()
                            && assistedInjectedConstructors(binding.membersInjectedType()).isEmpty());

            //warnIfNotAlreadyGenerated = true：
            // 1.如果hasInjectConstructor = true，那么判断当前绑定的injectionSites不为空；
            // 2.如果hasInjectConstructor = false，那么判断injectionSites每个item里面的element所在的父类都是membersInjectedType
            warnIfNotAlreadyGenerated =
                    hasInjectConstructor
                            ? !binding.injectionSites().isEmpty()
                            : binding.hasLocalInjectionSites();
        }

        membersInjectionBindings.tryRegisterBinding(binding, warnIfNotAlreadyGenerated);
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

        TypeElement typeElement = MoreElements.asType(constructorElement.getEnclosingElement());
        DeclaredType type = MoreTypes.asDeclared(typeElement.asType());

        //对当前构造函数所在的类生成Key
        Key key = keyFactory.forInjectConstructorWithResolvedType(type);

        //如果已经处理过了，并且存储于provisionBindings，那么直接获取并返回
        ProvisionBinding cachedBinding = provisionBindings.getBinding(key);
        if (cachedBinding != null) {
            return Optional.of(cachedBinding);
        }

        //没有验证通过，返回空
        ValidationReport report = injectValidator.validateConstructor(constructorElement);
        report.printMessagesTo(messager);
        if (!report.isClean()) {
            return Optional.empty();
        }

        //验证通过
        //对使用AssistedInject或Inject的构造函数生成一个ProvisionBinding绑定
        ProvisionBinding binding = bindingFactory.injectionBinding(constructorElement, resolvedType);
        registerBinding(binding, warnIfNotAlreadyGenerated);

        //当前构造函数所在的父节点中还存在Inject修饰的普通方法和变量生成的InjectionSite对象集合进行进一步操作
        if (!binding.injectionSites().isEmpty()) {
            tryRegisterMembersInjectedType(typeElement, resolvedType, warnIfNotAlreadyGenerated);
        }
        return Optional.of(binding);
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

        DeclaredType type = MoreTypes.asDeclared(typeElement.asType());

        Key key = keyFactory.forInjectConstructorWithResolvedType(type);

        //缓存中存在，直接返回
        MembersInjectionBinding cachedBinding = membersInjectionBindings.getBinding(key);
        if (cachedBinding != null) {
            return Optional.of(cachedBinding);
        }

        ValidationReport report = injectValidator.validateMembersInjectionType(typeElement);
        report.printMessagesTo(messager);
        if (!report.isClean()) {
            return Optional.empty();
        }

        //对使用Inject修饰的变量或方法所在类生成一个MembersInjectionBinding对象
        MembersInjectionBinding binding = bindingFactory.membersInjectionBinding(type, resolvedType);
        registerBinding(binding, warnIfNotAlreadyGenerated);

        //遍历该类非Object对象
        for (Optional<DeclaredType> supertype = types.nonObjectSuperclass(type);
             supertype.isPresent();
             supertype = types.nonObjectSuperclass(supertype.get())) {

            getOrFindMembersInjectionBinding(keyFactory.forMembersInjectedType(supertype.get()));
        }

        return Optional.of(binding);
    }

    @Override
    public Optional<ProvisionBinding> getOrFindProvisionBinding(Key key) {
        checkNotNull(key);

        if (!isValidImplicitProvisionKey(key, types)) {
            return Optional.empty();
        }

        ProvisionBinding binding = provisionBindings.getBinding(key);
        if (binding != null) {
            return Optional.of(binding);
        }

        // ok, let's see if we can find an @Inject constructor
        //节点上所用使用AssistedInject或Inject的构造函数集合
        TypeElement element = MoreElements.asType(types.asElement(key.type().java()));
        ImmutableSet<ExecutableElement> injectConstructors =
                ImmutableSet.<ExecutableElement>builder()
                        .addAll(injectedConstructors(element))
                        .addAll(assistedInjectedConstructors(element))
                        .build();

        switch (injectConstructors.size()) {
            case 0:
                // No constructor found.
                return Optional.empty();
            case 1:
                return tryRegisterConstructor(
                        Iterables.getOnlyElement(injectConstructors), Optional.of(key.type().java()), true);
            default:
                throw new IllegalStateException("Found multiple @Inject constructors: "
                        + injectConstructors);
        }
    }

    @Override
    public Optional<MembersInjectionBinding> getOrFindMembersInjectionBinding(Key key) {
        checkNotNull(key);
        // TODO(gak): is checking the kind enough?
        checkArgument(isValidMembersInjectionKey(key));

        MembersInjectionBinding binding = membersInjectionBindings.getBinding(key);
        if (binding != null) {
            return Optional.of(binding);
        }

        return tryRegisterMembersInjectedType(
                MoreTypes.asTypeElement(key.type().java()), Optional.of(key.type().java()), true);
    }

    @Override
    public Optional<ProvisionBinding> getOrFindMembersInjectorProvisionBinding(Key key) {

        //如果使用了Qualifier修饰 || 当前key表示多重绑定
        if (!isValidMembersInjectionKey(key)) {
            return Optional.empty();
        }

        //membersInject转换成ProvisionBinding
        Key membersInjectionKey = keyFactory.forMembersInjectedType(unwrapType(key.type().java()));
        return getOrFindMembersInjectionBinding(membersInjectionKey)
                .map(binding -> bindingFactory.membersInjectorBinding(key, binding));
    }
}
