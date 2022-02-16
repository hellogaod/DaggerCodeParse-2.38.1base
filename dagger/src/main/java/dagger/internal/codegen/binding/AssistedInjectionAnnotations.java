package dagger.internal.codegen.binding;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.base.Equivalence;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.internal.codegen.writing.ComponentImplementation;
import dagger.spi.model.BindingKind;

import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.auto.common.MoreTypes.asDeclared;
import static com.google.auto.common.MoreTypes.asExecutable;
import static com.google.auto.common.MoreTypes.asTypeElement;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.base.MoreAnnotationValues.getStringValue;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.internal.codegen.langmodel.DaggerElements.getAnnotationMirror;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.constructorsIn;

/**
 * Assisted injection utility methods.
 */
public final class AssistedInjectionAnnotations {
    /**
     * Returns the list of assisted parameters as {@link ParameterSpec}s.
     *
     * <p>The type of each parameter will be the resolved type given by the binding key, and the name
     * of each parameter will be the name given in the {@link
     * dagger.assisted.AssistedInject}-annotated constructor.
     */
    public static ImmutableList<ParameterSpec> assistedParameterSpecs(
            Binding binding, DaggerTypes types, ComponentImplementation.ShardImplementation shardImplementation) {
        checkArgument(binding.kind() == BindingKind.ASSISTED_INJECTION);
        ExecutableElement constructor = MoreElements.asExecutable(binding.bindingElement().get());
        ExecutableType constructorType =
                asExecutable(types.asMemberOf(asDeclared(binding.key().type().java()), constructor));
        return assistedParameterSpecs(
                constructor.getParameters(), constructorType.getParameterTypes(), shardImplementation);
    }

    private static ImmutableList<ParameterSpec> assistedParameterSpecs(
            List<? extends VariableElement> paramElements,
            List<? extends TypeMirror> paramTypes,
            ComponentImplementation.ShardImplementation shardImplementation) {
        ImmutableList.Builder<ParameterSpec> assistedParameterSpecs = ImmutableList.builder();
        for (int i = 0; i < paramElements.size(); i++) {
            VariableElement paramElement = paramElements.get(i);
            TypeMirror paramType = paramTypes.get(i);
            if (AssistedInjectionAnnotations.isAssistedParameter(paramElement)) {
                assistedParameterSpecs.add(
                        ParameterSpec.builder(
                                TypeName.get(paramType),
                                shardImplementation.getUniqueFieldNameForAssistedParam(paramElement))
                                .build());
            }
        }
        return assistedParameterSpecs.build();
    }
    /**
     * Returns the factory method for the given factory {@link TypeElement}.
     * <p>
     * 返回该类上有且仅有的一个abstract、非static、非private 方法
     */
    public static ExecutableElement assistedFactoryMethod(
            TypeElement factory, DaggerElements elements) {
        return getOnlyElement(assistedFactoryMethods(factory, elements));
    }

    /**
     * Returns the list of assisted parameters as {@link ParameterSpec}s.
     *
     * <p>The type of each parameter will be the resolved type given by the binding key, and the name
     * of each parameter will be the name given in the {@link
     * dagger.assisted.AssistedInject}-annotated constructor.
     */
    public static ImmutableList<ParameterSpec> assistedParameterSpecs(
            Binding binding, DaggerTypes types) {
        checkArgument(binding.kind() == BindingKind.ASSISTED_INJECTION);
        ExecutableElement constructor = MoreElements.asExecutable(binding.bindingElement().get());
        ExecutableType constructorType =
                asExecutable(types.asMemberOf(asDeclared(binding.key().type().java()), constructor));
        return assistedParameterSpecs(constructor.getParameters(), constructorType.getParameterTypes());
    }

    private static ImmutableList<ParameterSpec> assistedParameterSpecs(
            List<? extends VariableElement> paramElements, List<? extends TypeMirror> paramTypes) {
        ImmutableList.Builder<ParameterSpec> assistedParameterSpecs = ImmutableList.builder();
        for (int i = 0; i < paramElements.size(); i++) {
            VariableElement paramElement = paramElements.get(i);
            TypeMirror paramType = paramTypes.get(i);
            if (isAssistedParameter(paramElement)) {
                assistedParameterSpecs.add(
                        ParameterSpec.builder(TypeName.get(paramType), paramElement.getSimpleName().toString())
                                .build());
            }
        }
        return assistedParameterSpecs.build();
    }

    /**
     * Returns the list of assisted factory parameters as {@link ParameterSpec}s.
     *
     * <p>The type of each parameter will be the resolved type given by the binding key, and the name
     * of each parameter will be the name given in the {@link
     * dagger.assisted.AssistedInject}-annotated constructor.
     */
    public static ImmutableList<ParameterSpec> assistedFactoryParameterSpecs(
            Binding binding, DaggerElements elements, DaggerTypes types) {
        checkArgument(binding.kind() == BindingKind.ASSISTED_FACTORY);

        AssistedFactoryMetadata metadata =
                AssistedFactoryMetadata.create(binding.bindingElement().get().asType(), elements, types);
        ExecutableType factoryMethodType =
                asExecutable(
                        types.asMemberOf(asDeclared(binding.key().type().java()), metadata.factoryMethod()));
        return assistedParameterSpecs(
                // Use the order of the parameters from the @AssistedFactory method but use the parameter
                // names of the @AssistedInject constructor.
                metadata.assistedFactoryAssistedParameters().stream()
                        .map(metadata.assistedInjectAssistedParametersMap()::get)
                        .collect(toImmutableList()),
                factoryMethodType.getParameterTypes());
    }
    //如果当前绑定使用的是AssistedInject注解修饰，那么返回该构造函数使用Assisted修饰的参数集合；否则返回空
    public static ImmutableList<VariableElement> assistedParameters(Binding binding) {
        return binding.kind() == BindingKind.ASSISTED_INJECTION
                ? assistedParameters(MoreElements.asExecutable(binding.bindingElement().get()))
                : ImmutableList.of();
    }

    //收集当前构造函数的如果使用了Assisted修饰的参数
    private static ImmutableList<VariableElement> assistedParameters(ExecutableElement constructor) {
        return constructor.getParameters().stream()
                .filter(AssistedInjectionAnnotations::isAssistedParameter)
                .collect(toImmutableList());
    }
    /**
     * Returns {@code true} if this binding is uses assisted injection.
     * <p>
     * 如果使用了Assisted注解，返回true
     */
    public static boolean isAssistedParameter(VariableElement param) {
        return isAnnotationPresent(MoreElements.asVariable(param), Assisted.class);
    }

    /**
     * Returns the list of abstract factory methods for the given factory {@link TypeElement}.
     * <p>
     * 收集节点abstract、非static、非private的方法
     */
    public static ImmutableSet<ExecutableElement> assistedFactoryMethods(
            TypeElement factory, DaggerElements elements) {
        return elements.getLocalAndInheritedMethods(factory).stream()
                .filter(method -> method.getModifiers().contains(ABSTRACT))
                .filter(method -> !method.isDefault())
                .collect(toImmutableSet());
    }


    /**
     * Returns {@code true} if the element uses assisted injection.
     * <p>
     * 类中有且仅有一个构造函数使用了AssistdInject修饰
     */
    public static boolean isAssistedInjectionType(TypeElement typeElement) {
        ImmutableSet<ExecutableElement> injectConstructors = assistedInjectedConstructors(typeElement);
        return !injectConstructors.isEmpty()
                && isAnnotationPresent(getOnlyElement(injectConstructors), AssistedInject.class);
    }

    /**
     * Returns {@code true} if this binding is an assisted factory.
     * <p>
     * 节点是否使用了AssistedFactory注解修饰
     */
    public static boolean isAssistedFactoryType(Element element) {
        return isAnnotationPresent(element, AssistedFactory.class);
    }

    /**
     * Returns the constructors in {@code type} that are annotated with {@link AssistedInject}.
     * <p>
     * 收集类中使用了AssistedInject注解修饰的构造函数集合
     */
    public static ImmutableSet<ExecutableElement> assistedInjectedConstructors(TypeElement type) {
        return constructorsIn(type.getEnclosedElements()).stream()
                .filter(constructor -> isAnnotationPresent(constructor, AssistedInject.class))
                .collect(toImmutableSet());
    }

    /**
     * Metadata about an {@link dagger.assisted.AssistedFactory} annotated type.
     * <p>
     * AssistedFactory修饰的节点生成一个AssitedFactoryMetadata对象
     */
    @AutoValue
    public abstract static class AssistedFactoryMetadata {

        public static AssistedFactoryMetadata create(
                TypeMirror factory,
                DaggerElements elements,
                DaggerTypes types
        ) {
            DeclaredType factoryType = asDeclared(factory);
            TypeElement factoryElement = asTypeElement(factoryType);
            //获取该类上有且仅有的一个abstract、非static、非private 方法 节点
            ExecutableElement factoryMethod = assistedFactoryMethod(factoryElement, elements);
            //获取该类上有且仅有的一个abstract、非static、非private 方法 类型
            ExecutableType factoryMethodType = asExecutable(types.asMemberOf(factoryType, factoryMethod));
            //方法返回类型（是用AssistedInject修饰的）
            DeclaredType assistedInjectType = asDeclared(factoryMethodType.getReturnType());

            return new AutoValue_AssistedInjectionAnnotations_AssistedFactoryMetadata(
                    factoryElement,
                    factoryType,
                    factoryMethod,
                    factoryMethodType,
                    asTypeElement(assistedInjectType),
                    assistedInjectType,
                    AssistedInjectionAnnotations.assistedInjectAssistedParameters(assistedInjectType, types),
                    AssistedInjectionAnnotations.assistedFactoryAssistedParameters(
                            factoryMethod, factoryMethodType));
        }

        public abstract TypeElement factory();//@AssistedFactory修饰的类节点

        public abstract DeclaredType factoryType();//@AssistedFactory修饰的类节点

        public abstract ExecutableElement factoryMethod();//@AssistedFactory修饰的类 里面的有且仅有的一个abstract、非static、非private 方法 类型

        public abstract ExecutableType factoryMethodType();//@AssistedFactory修饰的类 里面的有且仅有的一个abstract、非static、非private 方法 类型

        public abstract TypeElement assistedInjectElement();//factoryMethod方法返回类型节点（是用AssistedInject修饰的）

        public abstract DeclaredType assistedInjectType();//factoryMethod方法返回类型（是用AssistedInject修饰的）

        //针对factoryMethod方法返回类型的构造函数的使用Assited修饰的参数
        public abstract ImmutableList<AssistedParameter> assistedInjectAssistedParameters();

        //AssistedFactory修饰的类，该类中的abstract、非static、非private的方法的参数
        public abstract ImmutableList<AssistedParameter> assistedFactoryAssistedParameters();

        @Memoized
        public ImmutableMap<AssistedParameter, VariableElement> assistedInjectAssistedParametersMap() {
            ImmutableMap.Builder<AssistedParameter, VariableElement> builder = ImmutableMap.builder();
            for (AssistedParameter assistedParameter : assistedInjectAssistedParameters()) {
                builder.put(assistedParameter, assistedParameter.variableElement);
            }
            return builder.build();
        }

        @Memoized
        public ImmutableMap<AssistedParameter, VariableElement> assistedFactoryAssistedParametersMap() {
            ImmutableMap.Builder<AssistedParameter, VariableElement> builder = ImmutableMap.builder();
            for (AssistedParameter assistedParameter : assistedFactoryAssistedParameters()) {
                builder.put(assistedParameter, assistedParameter.variableElement);
            }
            return builder.build();
        }
    }

    /**
     * Metadata about an {@link Assisted} annotated parameter.
     * <p>
     * 使用Assited注解修饰的参数（1.构造函数中的Assisted修饰参数，
     * 或2.AssistedFactory修饰的类，该类中的abstract、非static、非private的方法的参数）
     *
     * <p>This parameter can represent an {@link Assisted} annotated parameter from an {@link
     * AssistedInject} constructor or an {@link AssistedFactory} method.
     */
    @AutoValue
    public abstract static class AssistedParameter {

        //入口，创建AssitedParameter对象，参数和参数对应的类型
        public static AssistedParameter create(VariableElement parameter, TypeMirror parameterType) {
            AssistedParameter assistedParameter =
                    new AutoValue_AssistedInjectionAnnotations_AssistedParameter(
                            getAnnotationMirror(parameter, TypeNames.ASSISTED)
                                    .map(assisted -> getStringValue(assisted, "value"))
                                    .orElse(""),
                            MoreTypes.equivalence().wrap(parameterType));
            assistedParameter.variableElement = parameter;
            return assistedParameter;
        }

        private VariableElement variableElement;//传递过来的参数，而不是auto生成的

        /**
         * Returns the string qualifier from the {@link Assisted#value()}.
         */
        public abstract String qualifier();

        /**
         * Returns the wrapper for the type annotated with {@link Assisted}.
         */
        public abstract Equivalence.Wrapper<TypeMirror> wrappedType();//使用Assited注解修饰的元素类型

        /**
         * Returns the type annotated with {@link Assisted}.
         */
        public final TypeMirror type() {
            return wrappedType().get();
        }

        public final VariableElement variableElement() {
            return variableElement;
        }

        @Override
        public final String toString() {
            return qualifier().isEmpty()
                    ? String.format("@Assisted %s", type())
                    : String.format("@Assisted(\"%s\") %s", qualifier(), type());
        }
    }

    public static ImmutableList<AssistedParameter> assistedInjectAssistedParameters(
            DeclaredType assistedInjectType,
            DaggerTypes types
    ) {
        // We keep track of the constructor both as an ExecutableElement to access @Assisted
        // parameters and as an ExecutableType to access the resolved parameter types.

        //有且仅存在一个AssistedInject修饰的构造函数节点
        ExecutableElement assistedInjectConstructor =
                getOnlyElement(assistedInjectedConstructors(asTypeElement(assistedInjectType)));

        //有且仅存在一个AssistedInject修饰的构造函数类型
        ExecutableType assistedInjectConstructorType =
                asExecutable(types.asMemberOf(assistedInjectType, assistedInjectConstructor));

        //下面代码是对使用AssistedInject修饰的构造函数的参数，如果参数使用了Assisted修饰了，那么生成AssistedParameter对象
        ImmutableList.Builder<AssistedParameter> builder = ImmutableList.builder();
        for (int i = 0; i < assistedInjectConstructor.getParameters().size(); i++) {
            VariableElement parameter = assistedInjectConstructor.getParameters().get(i);
            TypeMirror parameterType = assistedInjectConstructorType.getParameterTypes().get(i);
            if (isAnnotationPresent(parameter, Assisted.class)) {
                builder.add(AssistedParameter.create(parameter, parameterType));
            }
        }
        return builder.build();
    }

    //AssistedFactory修饰的类，该类中的abstract、非static、非private有且仅有的方法的，该方法使用Assisted修饰的参数生成AssistedParameter对象
    public static ImmutableList<AssistedParameter> assistedFactoryAssistedParameters(
            ExecutableElement factoryMethod, ExecutableType factoryMethodType) {
        ImmutableList.Builder<AssistedParameter> builder = ImmutableList.builder();
        for (int i = 0; i < factoryMethod.getParameters().size(); i++) {
            VariableElement parameter = factoryMethod.getParameters().get(i);
            TypeMirror parameterType = factoryMethodType.getParameterTypes().get(i);
            builder.add(AssistedParameter.create(parameter, parameterType));
        }
        return builder.build();
    }

    private AssistedInjectionAnnotations() {
    }

}
