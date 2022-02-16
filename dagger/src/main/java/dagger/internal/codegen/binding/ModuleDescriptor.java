package dagger.internal.codegen.binding;


import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Traverser;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import dagger.internal.codegen.base.ClearableCache;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.multibindings.Multibinds;
import dagger.producers.Produces;
import dagger.spi.model.Key;

import static com.google.auto.common.MoreElements.asExecutable;
import static com.google.auto.common.MoreElements.getPackage;
import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.collect.Iterables.transform;
import static dagger.internal.codegen.base.ModuleAnnotation.moduleAnnotation;
import static dagger.internal.codegen.base.Util.reentrantComputeIfAbsent;
import static dagger.internal.codegen.binding.SourceFiles.classFileName;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.internal.codegen.langmodel.DaggerElements.getMethodDescriptor;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.NONE;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * Contains metadata that describes a module.
 */
@AutoValue
public abstract class ModuleDescriptor {

    public abstract TypeElement moduleElement();

    abstract ImmutableSet<TypeElement> includedModules();

    public abstract ImmutableSet<ContributionBinding> bindings();

    /**
     * The multibinding declarations contained in this module.
     */
    abstract ImmutableSet<MultibindingDeclaration> multibindingDeclarations();

    /**
     * The {@link Module#subcomponents() subcomponent declarations} contained in this module.
     */
    abstract ImmutableSet<SubcomponentDeclaration> subcomponentDeclarations();

    /**
     * The {@link Binds} method declarations that define delegate bindings.
     */
    abstract ImmutableSet<DelegateDeclaration> delegateDeclarations();

    /**
     * The {@link BindsOptionalOf} method declarations that define optional bindings.
     */
    abstract ImmutableSet<OptionalBindingDeclaration> optionalDeclarations();

    /**
     * The kind of the module.
     */
    public abstract ModuleKind kind();

    /**
     * Returns all of the bindings declared in this module.
     */
    @Memoized
    public ImmutableSet<BindingDeclaration> allBindingDeclarations() {
        return ImmutableSet.<BindingDeclaration>builder()
                .addAll(bindings())
                .addAll(delegateDeclarations())
                .addAll(multibindingDeclarations())
                .addAll(optionalDeclarations())
                .addAll(subcomponentDeclarations())
                .build();
    }

    /**
     * Returns the keys of all bindings declared by this module.
     */
    ImmutableSet<Key> allBindingKeys() {
        return allBindingDeclarations().stream().map(BindingDeclaration::key).collect(toImmutableSet());
    }

    /**
     * A {@link ModuleDescriptor} factory.
     */
    @Singleton
    public static final class Factory implements ClearableCache {
        private final DaggerElements elements;
        private final KotlinMetadataUtil metadataUtil;
        private final BindingFactory bindingFactory;
        private final MultibindingDeclaration.Factory multibindingDeclarationFactory;
        private final DelegateDeclaration.Factory bindingDelegateDeclarationFactory;
        private final SubcomponentDeclaration.Factory subcomponentDeclarationFactory;
        private final OptionalBindingDeclaration.Factory optionalBindingDeclarationFactory;
        private final Map<TypeElement, ModuleDescriptor> cache = new HashMap<>();

        @Inject
        Factory(
                DaggerElements elements,
                KotlinMetadataUtil metadataUtil,
                BindingFactory bindingFactory,
                MultibindingDeclaration.Factory multibindingDeclarationFactory,
                DelegateDeclaration.Factory bindingDelegateDeclarationFactory,
                SubcomponentDeclaration.Factory subcomponentDeclarationFactory,
                OptionalBindingDeclaration.Factory optionalBindingDeclarationFactory) {
            this.elements = elements;
            this.metadataUtil = metadataUtil;
            this.bindingFactory = bindingFactory;
            this.multibindingDeclarationFactory = multibindingDeclarationFactory;
            this.bindingDelegateDeclarationFactory = bindingDelegateDeclarationFactory;
            this.subcomponentDeclarationFactory = subcomponentDeclarationFactory;
            this.optionalBindingDeclarationFactory = optionalBindingDeclarationFactory;
        }

        public ModuleDescriptor create(TypeElement moduleElement) {
            return reentrantComputeIfAbsent(cache, moduleElement, this::createUncached);
        }

        //如果缓存不存在，那么新建并且缓存到cache
        public ModuleDescriptor createUncached(TypeElement moduleElement) {

            //module类中使用Provides或Produces注解修饰的方法生成的绑定
            ImmutableSet.Builder<ContributionBinding> bindings = ImmutableSet.builder();

            //module类中使用Binds修饰的方法:Binds修饰的方法有且仅有一个参数
            ImmutableSet.Builder<DelegateDeclaration> delegates = ImmutableSet.builder();

            //module类中使用Multibinds修饰的方法：Multibinds修饰的方法返回类型要么是Set要么是Map类型
            ImmutableSet.Builder<MultibindingDeclaration> multibindingDeclarations =
                    ImmutableSet.builder();

            //module类中使用BindsOptionalOf修饰的方法
            ImmutableSet.Builder<OptionalBindingDeclaration> optionalDeclarations =
                    ImmutableSet.builder();

            //遍历module类的所有方法
            for (ExecutableElement moduleMethod : methodsIn(elements.getAllMembers(moduleElement))) {

                if (isAnnotationPresent(moduleMethod, Provides.class)) {
                    bindings.add(bindingFactory.providesMethodBinding(moduleMethod, moduleElement));
                }

                if (isAnnotationPresent(moduleMethod, Produces.class)) {
                    bindings.add(bindingFactory.producesMethodBinding(moduleMethod, moduleElement));
                }

                if (isAnnotationPresent(moduleMethod, Binds.class)) {
                    delegates.add(bindingDelegateDeclarationFactory.create(moduleMethod, moduleElement));
                }

                if (isAnnotationPresent(moduleMethod, Multibinds.class)) {
                    multibindingDeclarations.add(
                            multibindingDeclarationFactory.forMultibindsMethod(moduleMethod, moduleElement));
                }

                if (isAnnotationPresent(moduleMethod, BindsOptionalOf.class)) {
                    optionalDeclarations.add(
                            optionalBindingDeclarationFactory.forMethod(moduleMethod, moduleElement));
                }
            }

            //如果module类中存在Kotlin Companion Object.
            if (metadataUtil.hasEnclosedCompanionObject(moduleElement)) {
                collectCompanionModuleBindings(moduleElement, bindings);
            }

            return new AutoValue_ModuleDescriptor(
                    moduleElement,//module类
                    //收集modules，遍历moduleElement及其父类；①收集module#includes；
                    // ②如果使用了ContributesAndroidInjector的方法，那么将 其所在父类 + "_" + 该方法首字母大写 拼接的名称作为一个module节点加入
                    ImmutableSet.copyOf(collectIncludedModules(new LinkedHashSet<>(), moduleElement)),
                    bindings.build(),//使用Provides和Produces修饰的方法生成的绑定
                    multibindingDeclarations.build(),//使用Multibinds修饰的方法生成的绑定
                    subcomponentDeclarationFactory.forModule(moduleElement),//moduleAnnotation#subcomponents中的subcomponent类生成的SubcomponentDeclaration对象集合
                    delegates.build(),//Binds修饰的方法生成的绑定
                    optionalDeclarations.build(),//BindsOptionalOf修饰的方法生成的绑定
                    ModuleKind.forAnnotatedElement(moduleElement).get());//ModuleKind：是Module注解还是ProducerModule注解
        }

        //当前module类中存在一个Kotlin Companion Object对象
        private void collectCompanionModuleBindings(
                TypeElement moduleElement,
                ImmutableSet.Builder<ContributionBinding> bindings
        ) {
            checkArgument(metadataUtil.hasEnclosedCompanionObject(moduleElement));

            TypeElement companionModule = metadataUtil.getEnclosedCompanionObject(moduleElement);

            ImmutableSet<String> bindingElementDescriptors =
                    bindings.build().stream()
                            .map(binding -> getMethodDescriptor(asExecutable(binding.bindingElement().get())))
                            .collect(toImmutableSet());

            methodsIn(elements.getAllMembers(companionModule)).stream()
                    // Binding methods in companion objects with @JvmStatic are mirrored in the enclosing
                    // class, therefore we should ignore it or else it'll be a duplicate binding.
                    .filter(method -> !KotlinMetadataUtil.isJvmStaticPresent(method))
                    // Fallback strategy for de-duping contributing bindings in the companion module with
                    // @JvmStatic by comparing descriptors. Contributing bindings are the only valid bindings
                    // a companion module can declare. See: https://youtrack.jetbrains.com/issue/KT-35104
                    // TODO(danysantiago): Checks qualifiers too.
                    .filter(method -> !bindingElementDescriptors.contains(getMethodDescriptor(method)))
                    .forEach(
                            method -> {
                                if (isAnnotationPresent(method, Provides.class)) {
                                    bindings.add(bindingFactory.providesMethodBinding(method, companionModule));
                                }
                                if (isAnnotationPresent(method, Produces.class)) {
                                    bindings.add(bindingFactory.producesMethodBinding(method, companionModule));
                                }
                            });
        }

        /**
         * Returns all the modules transitively included by given modules, including the arguments.
         * <p>
         * modules集合以及modules里面的module使用的Module#includes中的module类生成ModuleDescriptor描述类对象
         */
        ImmutableSet<ModuleDescriptor> transitiveModules(Iterable<TypeElement> modules) {
            return ImmutableSet.copyOf(
                    Traverser.forGraph(
                            (ModuleDescriptor module) -> transform(module.includedModules(), this::create))
                            .depthFirstPreOrder(transform(modules, this::create)));
        }

        //收集module类
        @CanIgnoreReturnValue
        private Set<TypeElement> collectIncludedModules(
                Set<TypeElement> includedModules,
                TypeElement moduleElement
        ) {
            TypeMirror superclass = moduleElement.getSuperclass();

            //1.module类的父类查找
            if (!superclass.getKind().equals(NONE)) {

                verify(superclass.getKind().equals(DECLARED));
                TypeElement superclassElement = MoreTypes.asTypeElement(superclass);
                if (!superclassElement.getQualifiedName().contentEquals(Object.class.getCanonicalName())) {
                    collectIncludedModules(includedModules, superclassElement);
                }
            }
            //2.①当前module#includes；②ContributesAndroidInjector 所在父类 + "_" + 当前方法名称首字母变成大写
            moduleAnnotation(moduleElement)
                    .ifPresent(
                            moduleAnnotation -> {
                                includedModules.addAll(moduleAnnotation.includes());
                                includedModules.addAll(implicitlyIncludedModules(moduleElement));
                            });
            return includedModules;
        }

        // @ContributesAndroidInjector generates a module that is implicitly included in the enclosing module
        //@ContributesAndroidInjector 生成一个隐含在封闭模块中的模块
        private ImmutableSet<TypeElement> implicitlyIncludedModules(TypeElement moduleElement) {

            TypeElement contributesAndroidInjector =
                    elements.getTypeElement("dagger.android.ContributesAndroidInjector");

            if (contributesAndroidInjector == null) {
                return ImmutableSet.of();
            }
            //如果方法上使用了ContributesAndroidInjector注解，那么将method所在父类 + "_" + 方法名称首字母变成大写
            return methodsIn(moduleElement.getEnclosedElements()).stream()
                    .filter(method -> DaggerElements.isAnnotationPresent(method, contributesAndroidInjector.asType()))
                    .map(method -> elements.checkTypePresent(implicitlyIncludedModuleName(method)))
                    .collect(toImmutableSet());
        }

        private String implicitlyIncludedModuleName(ExecutableElement method) {
            return getPackage(method).getQualifiedName()
                    + "."
                    + classFileName(ClassName.get(MoreElements.asType(method.getEnclosingElement())))
                    + "_"
                    + LOWER_CAMEL.to(UPPER_CAMEL, method.getSimpleName().toString());
        }

        @Override
        public void clearCache() {
            cache.clear();
        }
    }
}
