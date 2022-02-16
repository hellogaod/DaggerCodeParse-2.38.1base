package dagger.internal.codegen.binding;

import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import dagger.BindsInstance;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.DependencyRequest;

import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.auto.common.MoreTypes.asTypeElement;
import static com.google.common.base.Verify.verify;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.base.ModuleAnnotation.moduleAnnotation;
import static dagger.internal.codegen.binding.ComponentCreatorAnnotation.getCreatorAnnotations;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/**
 * A descriptor for a component <i>creator</i> type: that is, a type annotated with
 * {@code @Component.Builder} (or one of the corresponding production or subcomponent versions).
 */
@AutoValue
public abstract class ComponentCreatorDescriptor {

    /**
     * Returns the annotation marking this creator.
     * <p>
     * 当前Creator使用的注解类型
     */
    public abstract ComponentCreatorAnnotation annotation();

    /**
     * The kind of this creator.
     * <p>
     * Builder还是Factory
     */
    public final ComponentCreatorKind kind() {
        return annotation().creatorKind();
    }

    /**
     * The annotated creator type.
     * <p>
     * creator节点
     */
    public abstract TypeElement typeElement();

    /**
     * The method that creates and returns a component instance.
     * <p>
     * creator里面用于创建和返回一个component实例的方法：如果creator中的方法返回类型是当前creator所在component类或component类子类，那么表示该方法是factory方法
     * <p>
     * 并且注意，该方法的参数不受限制
     */
    public abstract ExecutableElement factoryMethod();

    /**
     * Multimap of component requirements to setter methods that set that requirement.
     * <p>
     * 组件要求的多重映射到设置该要求的 setter 方法。
     *
     * <p>In a valid creator, there will be exactly one element per component requirement, so this
     * method should only be called when validating the descriptor.
     * <p>
     * creator中除了factoryMethod外所有方法生成的集合：
     * 1.该方法有且仅有一个参数；
     * 2.K：是对该方法参数生成的ComponentRequirement，根据下面情况一次判断生成不同类型的ComponentRequirement对象：（1）如果方法或方法参数使用了BindsInstance；（2）参数类型是module类型；（3）除了（1）和（2）
     */
    abstract ImmutableSetMultimap<ComponentRequirement, ExecutableElement> unvalidatedSetterMethods();

    /**
     * Multimap of component requirements to factory method parameters that set that requirement.
     *
     * <p>In a valid creator, there will be exactly one element per component requirement, so this
     * method should only be called when validating the descriptor.
     * <p>
     * Factory方法的参数生成的ComponentRequirement对象，
     * K:是对该参数生成的ComponentRequirement，根据下面情况一次判断生成不同类型的ComponentRequirement对象：（1）如果方法或方法参数使用了BindsInstance；（2）参数类型是module类型；（3）除了（1）和（2）
     * V：Factory方法的参数
     */
    abstract ImmutableSetMultimap<ComponentRequirement, VariableElement>
    unvalidatedFactoryParameters();

    /**
     * Multimap of component requirements to elements (methods or parameters) that set that
     * requirement.
     *
     * <p>In a valid creator, there will be exactly one element per component requirement, so this
     * method should only be called when validating the descriptor.
     */
    public final ImmutableSetMultimap<ComponentRequirement, Element>
    unvalidatedRequirementElements() {
        // ComponentCreatorValidator ensures that there are either setter methods or factory method
        // parameters, but not both, so we can cheat a little here since we know that only one of
        // the two multimaps will be non-empty.
        return ImmutableSetMultimap.copyOf( // no actual copy
                unvalidatedSetterMethods().isEmpty()//为空表示是factory工厂模式，否则为builder模式
                        ? unvalidatedFactoryParameters()
                        : unvalidatedSetterMethods());
    }

    /**
     * Map of component requirements to elements (setter methods or factory method parameters) that
     * set them.
     */
    @Memoized
    ImmutableMap<ComponentRequirement, Element> requirementElements() {
        return flatten(unvalidatedRequirementElements());
    }

    /**
     * Map of component requirements to setter methods for those requirements.
     */
    @Memoized
    public ImmutableMap<ComponentRequirement, ExecutableElement> setterMethods() {
        return flatten(unvalidatedSetterMethods());
    }

    /**
     * Map of component requirements to factory method parameters for those requirements.
     */
    @Memoized
    public ImmutableMap<ComponentRequirement, VariableElement> factoryParameters() {
        return flatten(unvalidatedFactoryParameters());
    }

    private static <K, V> ImmutableMap<K, V> flatten(Multimap<K, V> multimap) {
        return ImmutableMap.copyOf(
                Maps.transformValues(multimap.asMap(), values -> getOnlyElement(values)));
    }

    /**
     * Returns the set of component requirements this creator allows the user to set.
     */
    public final ImmutableSet<ComponentRequirement> userSettableRequirements() {
        // Note: they should have been validated at the point this is used, so this set is valid.
        return unvalidatedRequirementElements().keySet();
    }

    /**
     * Returns the set of requirements for modules and component dependencies for this creator.
     */
    public final ImmutableSet<ComponentRequirement> moduleAndDependencyRequirements() {
        return userSettableRequirements().stream()
                .filter(requirement -> !requirement.isBoundInstance())
                .collect(toImmutableSet());
    }

    /**
     * Returns the set of bound instance requirements for this creator.
     */
    final ImmutableSet<ComponentRequirement> boundInstanceRequirements() {
        return userSettableRequirements().stream()
                .filter(ComponentRequirement::isBoundInstance)
                .collect(toImmutableSet());
    }

    /**
     * Returns the element in this creator that sets the given {@code requirement}.
     */
    final Element elementForRequirement(ComponentRequirement requirement) {
        return requirementElements().get(requirement);
    }

    /**
     * Creates a new {@link ComponentCreatorDescriptor} for the given creator {@code type}.
     */
    public static ComponentCreatorDescriptor create(
            DeclaredType type,
            DaggerElements elements,
            DaggerTypes types,
            DependencyRequestFactory dependencyRequestFactory) {
        //creator节点
        TypeElement typeElement = asTypeElement(type);

        //creator所在的component节点
        TypeMirror componentType = typeElement.getEnclosingElement().asType();

        //除了factoryMethod方法以外的所有方法：该方法参数有且仅有一个
        ImmutableSetMultimap.Builder<ComponentRequirement, ExecutableElement> setterMethods =
                ImmutableSetMultimap.builder();

        //factoryMethod方法：creator节点中的方法返回类型是当前creator所在component类或其实现类；该方法参数不受限制
        ExecutableElement factoryMethod = null;

        //非private、非static、abstract修饰的方法
        for (ExecutableElement method : elements.getUnimplementedMethods(typeElement)) {

            ExecutableType resolvedMethodType = MoreTypes.asExecutable(types.asMemberOf(type, method));

            //如果creator中的方法返回类型是当前creator所在component类或component类子类，那么表示该方法是factory方法
            if (types.isSubtype(componentType, resolvedMethodType.getReturnType())) {
                factoryMethod = method;
            } else {
                //如果creator类中的方法不是factory方法，那么其他类型方法的参数有且仅有一个
                VariableElement parameter = getOnlyElement(method.getParameters());
                TypeMirror parameterType = getOnlyElement(resolvedMethodType.getParameterTypes());

                setterMethods.put(
                        requirement(method, parameter, parameterType, dependencyRequestFactory, method),
                        method);
            }
        }

        verify(factoryMethod != null); // validation should have ensured this.

        ImmutableSetMultimap.Builder<ComponentRequirement, VariableElement> factoryParameters =
                ImmutableSetMultimap.builder();

        ExecutableType resolvedFactoryMethodType =
                MoreTypes.asExecutable(types.asMemberOf(type, factoryMethod));

        List<? extends VariableElement> parameters = factoryMethod.getParameters();
        List<? extends TypeMirror> parameterTypes = resolvedFactoryMethodType.getParameterTypes();

        for (int i = 0; i < parameters.size(); i++) {
            VariableElement parameter = parameters.get(i);
            TypeMirror parameterType = parameterTypes.get(i);
            factoryParameters.put(
                    requirement(factoryMethod, parameter, parameterType, dependencyRequestFactory, parameter),
                    parameter);
        }

        // Validation should have ensured exactly one creator annotation is present on the type.
        ComponentCreatorAnnotation annotation = getOnlyElement(getCreatorAnnotations(typeElement));

        return new AutoValue_ComponentCreatorDescriptor(
                annotation, //creatorAnnotation注解
                typeElement, //creator节点
                factoryMethod, //factory工厂模式，creator节点中返回类型是creator所在父级component或其实现类的方法

                //除了factoryMethod方法以外的方法，（该方法参数有且仅有一个）,
                //（1）该方法或方法参数使用了BindsInstance注解，生成BOUND_INSTANCE类型ComponentRequirement对象
                //（2）该方法参数是module类，生成MODULE类型ComponentRequirement对象
                //（3）其他情况上生成DEPENDENCY类型ComponentRequirement对象
                setterMethods.build(),//builder构建者模式

                //factoryMethod方法的参数生成(参数不限)
                //(1)方法参数使用了BindsInstance注解，生成BOUND_INSTANCE类型ComponentRequirement对象
                //(2)该方法参数是module类，生成MODULE类型ComponentRequirement对象
                //(3)其他情况上生成DEPENDENCY类型ComponentRequirement对象
                factoryParameters.build()//工厂模式，对方法参数生成的集合
        );
    }


    private static ComponentRequirement requirement(
            ExecutableElement method,
            VariableElement parameter,
            TypeMirror type,
            DependencyRequestFactory dependencyRequestFactory,
            Element elementForVariableName) {

        //1.如果方法或者方法参数使用了BindsInstance注解，那么对该参数（有且仅有一个）生成依赖，在对依赖生成ComponentRequirement对象，类型是BOUND_INSTANCE
        if (isAnnotationPresent(method, BindsInstance.class)
                || isAnnotationPresent(parameter, BindsInstance.class)) {

            DependencyRequest request =
                    dependencyRequestFactory.forRequiredResolvedVariable(parameter, type);
            String variableName = elementForVariableName.getSimpleName().toString();


            return ComponentRequirement.forBoundInstance(
                    request.key(), request.isNullable(), variableName);
        }

        //2.如果type表示的参数类型是使用了Module或ProducerModule注解，那么生成MODULE类型的注解；
        //3.在creator类中，如果该方法或者方法的唯一参数都没有使用BindsInstance修饰，并且参数类型也不是module类型，那么该参数生成DEPENDENCY类型的ComponentRequirement对象
        return moduleAnnotation(asTypeElement(type)).isPresent()
                ? ComponentRequirement.forModule(type)
                : ComponentRequirement.forDependency(type);
    }
}
