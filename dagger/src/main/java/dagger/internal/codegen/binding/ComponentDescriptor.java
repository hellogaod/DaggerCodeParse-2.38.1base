package dagger.internal.codegen.binding;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import dagger.Component;
import dagger.Module;
import dagger.Subcomponent;
import dagger.internal.codegen.base.ComponentAnnotation;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.producers.CancellationPolicy;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.Scope;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableMap;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.internal.codegen.langmodel.DaggerTypes.isFutureType;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.type.TypeKind.VOID;

/**
 * A component declaration.
 *
 * <p>Represents one type annotated with {@code @Component}, {@code Subcomponent},
 * {@code @ProductionComponent}, or {@code @ProductionSubcomponent}.
 *
 * <p>When validating bindings installed in modules, a {@link ComponentDescriptor} can also
 * represent a synthetic component for the module, where there is an entry point for each binding in
 * the module.
 */
@AutoValue
public abstract class ComponentDescriptor {
    /**
     * The annotation that specifies that {@link #typeElement()} is a component.
     */
    public abstract ComponentAnnotation annotation();

    /**
     * Returns {@code true} if this is a subcomponent.
     */
    public final boolean isSubcomponent() {
        return annotation().isSubcomponent();
    }

    /**
     * Returns {@code true} if this is a production component or subcomponent, or a
     * {@code @ProducerModule} when doing module binding validation.
     */
    public final boolean isProduction() {
        return annotation().isProduction();
    }

    /**
     * Returns {@code true} if this is a real component, and not a fictional one used to validate
     * module bindings.
     */
    public final boolean isRealComponent() {
        return annotation().isRealComponent();
    }

    /**
     * The element that defines the component. This is the element to which the {@link #annotation()}
     * was applied.
     */
    public abstract TypeElement typeElement();

    /**
     * The set of component dependencies listed in {@link Component#dependencies} or {@link
     * dagger.producers.ProductionComponent#dependencies()}.
     */
    public abstract ImmutableSet<ComponentRequirement> dependencies();

    /**
     * The non-abstract {@link #modules()} and the {@link #dependencies()}.
     * <p>
     * 当前Component 关联的所有非abstact修饰的module类和dependencies生成ComponentRequirement对象集合
     */
    public final ImmutableSet<ComponentRequirement> dependenciesAndConcreteModules() {
        return Stream.concat(
                moduleTypes().stream()
                        //不是abstract修饰的module类
                        .filter(dep -> !dep.getModifiers().contains(ABSTRACT))
                        .map(module -> ComponentRequirement.forModule(module.asType())),
                dependencies().stream())
                .collect(toImmutableSet());
    }

    /**
     * The {@link ModuleDescriptor modules} declared in {@link Component#modules()} and reachable by
     * traversing {@link Module#includes()}.
     */
    public abstract ImmutableSet<ModuleDescriptor> modules();

    /**
     * The types of the {@link #modules()}.
     * <p>
     * 获取当前component关联的所有的module节点
     */
    public final ImmutableSet<TypeElement> moduleTypes() {
        return modules().stream().map(ModuleDescriptor::moduleElement).collect(toImmutableSet());
    }


    /**
     * The types for which the component will need instances if all of its bindings are used. For the
     * types the component will need in a given binding graph, use {@link
     * BindingGraph#componentRequirements()}.
     *
     * <ul>
     *   <li>{@linkplain #modules()} modules} with concrete instance bindings
     *   <li>Bound instances
     *   <li>{@linkplain #dependencies() dependencies}
     * </ul>
     */
    @Memoized
    ImmutableSet<ComponentRequirement> requirements() {
        ImmutableSet.Builder<ComponentRequirement> requirements = ImmutableSet.builder();

        //1.筛选module需要被实例化，将该module类生成ComponentRequirement对象并且收集到requirements集合中
        modules().stream()
                .filter(
                        module ->
                                module.bindings().stream().anyMatch(ContributionBinding::requiresModuleInstance))
                .map(module -> ComponentRequirement.forModule(module.moduleElement().asType()))
                .forEach(requirements::add);

        //2.(Production)Component#dependencies生成ComponentRequirement对象，并且收集
        requirements.addAll(dependencies());

        //3.creator类里面如果存在unvalidatedSetterMethods在unvalidatedSetterMethods里面找；如果不存在，在unvalidatedFactoryParameters里面找
        //找什么？：使用@BindsInstance修饰的方法或方法参数生成的ComponentRequirement集合
        requirements.addAll(
                creatorDescriptor()
                        .map(ComponentCreatorDescriptor::boundInstanceRequirements)
                        .orElse(ImmutableSet.of()));

        return requirements.build();
    }

    /**
     * This component's {@linkplain #dependencies() dependencies} keyed by each provision or
     * production method defined by that dependency. Note that the dependencies' types are not simply
     * the enclosing type of the method; a method may be declared by a supertype of the actual
     * dependency.
     */
    public abstract ImmutableMap<ExecutableElement, ComponentRequirement>
    dependenciesByDependencyMethod();

    /**
     * The {@linkplain #dependencies() component dependency} that defines a method.
     */
    public final ComponentRequirement getDependencyThatDefinesMethod(Element method) {
        checkArgument(
                method instanceof ExecutableElement, "method must be an executable element: %s", method);
        return checkNotNull(
                dependenciesByDependencyMethod().get(method), "no dependency implements %s", method);
    }

    /**
     * The scopes of the component.
     */
    public abstract ImmutableSet<Scope> scopes();

    /**
     * All {@link Subcomponent}s which are direct children of this component. This includes
     * subcomponents installed from {@link Module#subcomponents()} as well as subcomponent {@linkplain
     * #childComponentsDeclaredByFactoryMethods() factory methods} and {@linkplain
     * #childComponentsDeclaredByBuilderEntryPoints() builder methods}.
     */
    public final ImmutableSet<ComponentDescriptor> childComponents() {
        return ImmutableSet.<ComponentDescriptor>builder()
                .addAll(childComponentsDeclaredByFactoryMethods().values())//componentMethod返回类型是subcomponent
                .addAll(childComponentsDeclaredByBuilderEntryPoints().values())//componentMethod返回类型是subcomponent.builder
                //component节点componentAnnotation#modules、该module关联的moduleAnnotaton#includes所有module节点包含的moduleAnnotation#subcomponents
                .addAll(childComponentsDeclaredByModules())
                .build();
    }

    /**
     * All {@linkplain Subcomponent direct child} components that are declared by a {@linkplain
     * Module#subcomponents() module's subcomponents}.
     * <p>
     * 通过module#subcomponents里面的所有component类生成的对象
     */
    abstract ImmutableSet<ComponentDescriptor> childComponentsDeclaredByModules();

    /**
     * All {@linkplain Subcomponent direct child} components that are declared by a subcomponent
     * factory method.
     * <p>
     * component类中的方法有返回类型是一个component类（就是一个subcomponent类），这里转换收集：factory method
     */
    public abstract ImmutableBiMap<ComponentMethodDescriptor, ComponentDescriptor>
    childComponentsDeclaredByFactoryMethods();

    /**
     * Returns a map of {@link #childComponents()} indexed by {@link #typeElement()}.
     */
    @Memoized
    public ImmutableMap<TypeElement, ComponentDescriptor> childComponentsByElement() {
        return Maps.uniqueIndex(childComponents(), ComponentDescriptor::typeElement);
    }

    /**
     * Returns the factory method that declares a child component.
     */
    final Optional<ComponentMethodDescriptor> getFactoryMethodForChildComponent(
            ComponentDescriptor childComponent) {
        return Optional.ofNullable(
                childComponentsDeclaredByFactoryMethods().inverse().get(childComponent));
    }

    /**
     * All {@linkplain Subcomponent direct child} components that are declared by a subcomponent
     * builder method.
     * <p>
     * 当前component 方法的返回类型是subcomponent.Builder）：Builder method
     */
    abstract ImmutableBiMap<ComponentMethodDescriptor, ComponentDescriptor>
    childComponentsDeclaredByBuilderEntryPoints();

    //针对所有的子component类进行筛选，该item必须有一个creator类（Builder或Factory），表示当前子component类是通过creator中的factoryMethod方法创建的
    private final Supplier<ImmutableMap<TypeElement, ComponentDescriptor>>
            childComponentsByBuilderType =
            Suppliers.memoize(
                    () ->
                            childComponents().stream()
                                    .filter(child -> child.creatorDescriptor().isPresent())
                                    .collect(
                                            toImmutableMap(
                                                    child -> child.creatorDescriptor().get().typeElement(),
                                                    child -> child)));

    /**
     * Returns the child component with the given builder type.
     */
    final ComponentDescriptor getChildComponentWithBuilderType(TypeElement builderType) {
        return checkNotNull(
                childComponentsByBuilderType.get().get(builderType),
                "no child component found for builder type %s",
                builderType.getQualifiedName());
    }

    public abstract ImmutableSet<ComponentMethodDescriptor> componentMethods();

    /**
     * Returns the first component method associated with this binding request, if one exists.
     */
    public Optional<ComponentMethodDescriptor> firstMatchingComponentMethod(BindingRequest request) {
        return Optional.ofNullable(firstMatchingComponentMethods().get(request));
    }

    @Memoized
    ImmutableMap<BindingRequest, ComponentMethodDescriptor>
    firstMatchingComponentMethods() {
        //componentMethod入口方法的参数生成BindingRequest集合
        Map<BindingRequest, ComponentMethodDescriptor> methods = new HashMap<>();
        for (ComponentMethodDescriptor method : entryPointMethods()) {
            methods.putIfAbsent(BindingRequest.bindingRequest(method.dependencyRequest().get()), method);
        }
        return ImmutableMap.copyOf(methods);
    }

    /**
     * The entry point methods on the component type. Each has a {@link DependencyRequest}.
     * <p>
     * component类型的入口方法：方法存在依赖
     */
    public final ImmutableSet<ComponentMethodDescriptor> entryPointMethods() {
        return componentMethods()
                .stream()
                .filter(method -> method.dependencyRequest().isPresent())
                .collect(toImmutableSet());
    }

    /**
     * Returns a descriptor for the creator type for this component type, if the user defined one.
     */
    public abstract Optional<ComponentCreatorDescriptor> creatorDescriptor();

    /**
     * Returns {@code true} for components that have a creator, either because the user {@linkplain
     * #creatorDescriptor() specified one} or because it's a top-level component with an implicit
     * builder.
     */
    public final boolean hasCreator() {
        return !isSubcomponent() || creatorDescriptor().isPresent();
    }

    /**
     * Returns the {@link CancellationPolicy} for this component, or an empty optional if either the
     * component is not a production component or no {@code CancellationPolicy} annotation is present.
     */
    public final Optional<CancellationPolicy> cancellationPolicy() {
        return isProduction()
                ? Optional.ofNullable(typeElement().getAnnotation(CancellationPolicy.class))
                : Optional.empty();
    }

    @Memoized
    @Override
    public int hashCode() {
        // TODO(b/122962745): Only use typeElement().hashCode()
        return Objects.hash(typeElement(), annotation());
    }

    // TODO(ronshapiro): simplify the equality semantics
    @Override
    public abstract boolean equals(Object obj);

    /**
     * A component method.
     */
    @AutoValue
    public abstract static class ComponentMethodDescriptor {
        /**
         * The method itself. Note that this may be declared on a supertype of the component.
         */
        public abstract ExecutableElement methodElement();

        /**
         * The dependency request for production, provision, and subcomponent creator methods. Absent
         * for subcomponent factory methods.
         */
        public abstract Optional<DependencyRequest> dependencyRequest();

        /**
         * The subcomponent for subcomponent factory methods and subcomponent creator methods.
         */
        public abstract Optional<ComponentDescriptor> subcomponent();

        /**
         * Returns the return type of {@link #methodElement()} as resolved in the {@link
         * ComponentDescriptor#typeElement() component type}. If there are no type variables in the
         * return type, this is the equivalent of {@code methodElement().getReturnType()}.
         */
        public TypeMirror resolvedReturnType(DaggerTypes types) {
            checkState(dependencyRequest().isPresent());

            TypeMirror returnType = methodElement().getReturnType();
            if (returnType.getKind().isPrimitive() || returnType.getKind().equals(VOID)) {
                return returnType;
            }
            return BindingRequest.bindingRequest(dependencyRequest().get())
                    .requestedType(dependencyRequest().get().key().type().java(), types);
        }

        /**
         * A {@link ComponentMethodDescriptor}builder for a method.
         */
        public static Builder builder(ExecutableElement method) {
            return new AutoValue_ComponentDescriptor_ComponentMethodDescriptor.Builder()
                    .methodElement(method);
        }

        /**
         * A builder of {@link ComponentMethodDescriptor}s.
         */
        @AutoValue.Builder
        @CanIgnoreReturnValue
        public interface Builder {//componentMethod方法参数不会超过1个。

            /**
             * @see ComponentMethodDescriptor#methodElement()
             */
            Builder methodElement(ExecutableElement methodElement);//方法节点

            /**
             * @see ComponentMethodDescriptor#dependencyRequest()
             * <p>
             * 如果方法无参，则将当前方法类型作为type生成当前依赖的key；如果方法存在参数（只会存在一个），则将参数类型作为当前依赖的key
             */
            Builder dependencyRequest(DependencyRequest dependencyRequest);

            /**
             * @see ComponentMethodDescriptor#subcomponent()
             * <p>
             * 当前方法返回类型是一个subcomponent类（或者是一个subcomponent中的creator节点），生成ComponentDescriptor对象
             * <p>
             * 如果方法返回类型是一个subcomponent类，那么dependencyRequest参数为Optional.empty()
             */
            Builder subcomponent(ComponentDescriptor subcomponent);

            /**
             * Builds the descriptor.
             */
            @CheckReturnValue
            ComponentMethodDescriptor build();
        }
    }


    /**
     * No-argument methods defined on {@link Object} that are ignored for contribution.
     */
    private static final ImmutableSet<String> NON_CONTRIBUTING_OBJECT_METHOD_NAMES =
            ImmutableSet.of("toString", "hashCode", "clone", "getClass");

    /**
     * Returns {@code true} if a method could be a component entry point but not a members-injection
     * method.
     * <p>
     * 如果component#dependencies里面的方法是无参并且返回类型不是void，那么可以作为一个component的入口点，而不是成员注入方法
     */
    static boolean isComponentContributionMethod(DaggerElements elements, ExecutableElement method) {
        return method.getParameters().isEmpty()//无参
                && !method.getReturnType().getKind().equals(VOID)//返回类型不是void
                && !elements.getTypeElement(Object.class).equals(method.getEnclosingElement())//方法所在类不是Object
                && !NON_CONTRIBUTING_OBJECT_METHOD_NAMES.contains(method.getSimpleName().toString());//不是Object的默认方法，例如hashCode
    }

    /**
     * Returns {@code true} if a method could be a component production entry point.
     */
    static boolean isComponentProductionMethod(DaggerElements elements, ExecutableElement method) {
        return isComponentContributionMethod(elements, method) && isFutureType(method.getReturnType());
    }
}
