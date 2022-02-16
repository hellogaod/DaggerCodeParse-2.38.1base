package dagger.internal.codegen.binding;

import com.google.auto.common.MoreElements;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.List;

import javax.inject.Provider;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

import androidx.room.compiler.processing.XTypeElement;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.internal.SetFactory;
import dagger.internal.codegen.base.MapType;
import dagger.internal.codegen.base.SetType;
import dagger.producers.Produced;
import dagger.producers.Producer;
import dagger.producers.internal.SetOfProducedProducer;
import dagger.producers.internal.SetProducer;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.RequestKind;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.javapoet.TypeNames.DOUBLE_CHECK;
import static dagger.internal.codegen.javapoet.TypeNames.MAP_FACTORY;
import static dagger.internal.codegen.javapoet.TypeNames.MAP_OF_PRODUCED_PRODUCER;
import static dagger.internal.codegen.javapoet.TypeNames.MAP_OF_PRODUCER_PRODUCER;
import static dagger.internal.codegen.javapoet.TypeNames.MAP_PRODUCER;
import static dagger.internal.codegen.javapoet.TypeNames.MAP_PROVIDER_FACTORY;
import static dagger.internal.codegen.javapoet.TypeNames.PROVIDER_OF_LAZY;
import static dagger.internal.codegen.javapoet.TypeNames.SET_FACTORY;
import static dagger.internal.codegen.javapoet.TypeNames.SET_OF_PRODUCED_PRODUCER;
import static dagger.internal.codegen.javapoet.TypeNames.SET_PRODUCER;
import static dagger.spi.model.BindingKind.ASSISTED_INJECTION;
import static dagger.spi.model.BindingKind.INJECTION;
import static dagger.spi.model.BindingKind.MULTIBOUND_MAP;
import static dagger.spi.model.BindingKind.MULTIBOUND_SET;
import static javax.lang.model.SourceVersion.isName;

/**
 * Utilities for generating files.
 */
public class SourceFiles {

    private static final Joiner CLASS_FILE_NAME_JOINER = Joiner.on('_');

    /**
     * Generates names and keys for the factory class fields needed to hold the framework classes for
     * all of the dependencies of {@code binding}. It is responsible for choosing a name that
     *
     * <ul>
     *   <li>represents all of the dependency requests for this key
     *   <li>is <i>probably</i> associated with the type being bound
     *   <li>is unique within the class
     * </ul>
     *
     * @param binding must be an unresolved binding (type parameters must match its type element's)
     */
    public static ImmutableMap<DependencyRequest, FrameworkField> generateBindingFieldsForDependencies(Binding binding) {

        checkArgument(!binding.unresolved().isPresent(), "binding must be unresolved: %s", binding);

        FrameworkTypeMapper frameworkTypeMapper =
                FrameworkTypeMapper.forBindingType(binding.bindingType());

        //Map<K,V>，K：当前绑定上的所有依赖；V：FrameworkFiled对象，①使用架构类型根据当前依赖的kind决定；②架构类型包裹的value是当前依赖的key.type.java（即当前参数类型）；③命名
        return Maps.toMap(
                binding.dependencies(),
                dependency ->
                        FrameworkField.create(
                                ClassName.get(
                                        frameworkTypeMapper.getFrameworkType(dependency.kind()).frameworkClass()),
                                TypeName.get(dependency.key().type().java()),
                                DependencyVariableNamer.name(dependency)));
    }

    //返回当前绑定节点生成的类，并且如果该节点的全部参数作为当前类的泛型数据（如果当前节点是ContributionBinding，并且所在module不需要实例化 && 绑定没有使用Inject或AssistedInject修饰）
    public static TypeName parameterizedGeneratedTypeNameForBinding(Binding binding) {

        ClassName className = generatedClassNameForBinding(binding);

        ImmutableList<TypeVariableName> typeParameters = bindingTypeElementTypeVariableNames(binding);
        return typeParameters.isEmpty()
                ? className
                : ParameterizedTypeName.get(className, Iterables.toArray(typeParameters, TypeName.class));
    }

    public static ClassName membersInjectorNameForType(TypeElement typeElement) {
        return siblingClassName(typeElement, "_MembersInjector");
    }

    //返回：类名.变量名
    public static String memberInjectedFieldSignatureForVariable(VariableElement variableElement) {
        return MoreElements.asType(variableElement.getEnclosingElement()).getQualifiedName()
                + "."
                + variableElement.getSimpleName();
    }

    public static ClassName generatedMonitoringModuleName(XTypeElement componentElement) {
        return generatedMonitoringModuleName(XConverters.toJavac(componentElement));
    }

    //拼接 "_MonitoringModule"
    public static ClassName generatedMonitoringModuleName(TypeElement componentElement) {
        return siblingClassName(componentElement, "_MonitoringModule");
    }

    // TODO(ronshapiro): when JavaPoet migration is complete, replace the duplicated code
    // which could use this.
    private static ClassName siblingClassName(TypeElement typeElement, String suffix) {
        ClassName className = ClassName.get(typeElement);
        return className.topLevelClassName().peerClass(classFileName(className) + suffix);
    }

    /**
     * The {@link java.util.Map} factory class name appropriate for map bindings.
     */
    public static ClassName mapFactoryClassName(ContributionBinding binding) {
        checkState(binding.kind().equals(MULTIBOUND_MAP), binding.kind());
        MapType mapType = MapType.from(binding.key());
        switch (binding.bindingType()) {
            case PROVISION:
                return mapType.valuesAreTypeOf(Provider.class) ? MAP_PROVIDER_FACTORY : MAP_FACTORY;
            case PRODUCTION:
                return mapType.valuesAreFrameworkType()
                        ? mapType.valuesAreTypeOf(Producer.class)
                        ? MAP_OF_PRODUCER_PRODUCER
                        : MAP_OF_PRODUCED_PRODUCER
                        : MAP_PRODUCER;
            default:
                throw new IllegalArgumentException(binding.bindingType().toString());
        }
    }

    /**
     * The {@link java.util.Set} factory class name appropriate for set bindings.
     *
     * <ul>
     *   <li>{@link SetFactory} for provision bindings.
     *   <li>{@link SetProducer} for production bindings for {@code Set<T>}.
     *   <li>{@link SetOfProducedProducer} for production bindings for {@code Set<Produced<T>>}.
     * </ul>
     */
    public static ClassName setFactoryClassName(ContributionBinding binding) {
        checkArgument(binding.kind().equals(MULTIBOUND_SET));
        if (binding.bindingType().equals(BindingType.PROVISION)) {
            return SET_FACTORY;
        } else {
            SetType setType = SetType.from(binding.key());
            return setType.elementsAreTypeOf(Produced.class) ? SET_OF_PRODUCED_PRODUCER : SET_PRODUCER;
        }
    }

    //如果className是内部类，如类A里面包含B，那么针对B生成的String：A_B
    public static String classFileName(ClassName className) {
        return CLASS_FILE_NAME_JOINER.join(className.simpleNames());
    }

    public static CodeBlock frameworkTypeUsageStatement(
            CodeBlock frameworkTypeMemberSelect,
            RequestKind dependencyKind
    ) {
        switch (dependencyKind) {
            case LAZY:
                return CodeBlock.of("$T.lazy($L)", DOUBLE_CHECK, frameworkTypeMemberSelect);
            case INSTANCE:
            case FUTURE:
                return CodeBlock.of("$L.get()", frameworkTypeMemberSelect);
            case PROVIDER:
            case PRODUCER:
                return frameworkTypeMemberSelect;
            case PROVIDER_OF_LAZY:
                return CodeBlock.of("$T.create($L)", PROVIDER_OF_LAZY, frameworkTypeMemberSelect);
            default: // including PRODUCED
                throw new AssertionError(dependencyKind);
        }
    }

    //绑定节点上所有的节点参数
    public static ImmutableList<TypeVariableName> bindingTypeElementTypeVariableNames(
            Binding binding) {

        //如果是ContributionBinding绑定，并且该绑定kind 既没有使用Inject修饰也没用使用AssistedInject && 该绑定不需要所在的module实例化：返回空
        if (binding instanceof ContributionBinding) {
            ContributionBinding contributionBinding = (ContributionBinding) binding;
            if (!(contributionBinding.kind() == INJECTION
                    || contributionBinding.kind() == ASSISTED_INJECTION)
                    && !contributionBinding.requiresModuleInstance()) {
                return ImmutableList.of();
            }
        }
        //当前绑定节点上所有的节点
        List<? extends TypeParameterElement> typeParameters =
                binding.bindingTypeElement().get().getTypeParameters();
        return typeParameters.stream().map(TypeVariableName::get).collect(toImmutableList());
    }

    /**
     * Returns a mapping of {@link DependencyRequest}s to {@link CodeBlock}s that {@linkplain
     * #frameworkTypeUsageStatement(CodeBlock, RequestKind) use them}.
     */
    public static ImmutableMap<DependencyRequest, CodeBlock> frameworkFieldUsages(
            ImmutableSet<DependencyRequest> dependencies,
            ImmutableMap<DependencyRequest, FieldSpec> fields) {

        //依赖以及依赖生成的变量生成一个Map<K,V>:
        //K:依赖
        //V：依赖生成的变量根据当前依赖所在父级的kind绑定类型，决定该变量使用哪种架构类型包裹
        return Maps.toMap(
                dependencies,
                dep -> frameworkTypeUsageStatement(CodeBlock.of("$N", fields.get(dep)), dep.kind()));
    }

    /**
     * Returns the generated factory or members injector name for a binding.
     * <p>
     * 1.如果绑定BindingType是PROVISION或PRODUCTION
     * (1)如果BindingKind是ASSISTED_INJECTION、INJECTION、PROVISION、PRODUCTION，那么返回bindingElement + "Factory"
     * (2)如果BindingKind是ASSISTED_FACTORY,那么返回bindingElement + "_Impl"
     * 2.如果绑定BindingType是MEMBERS_INJECTION,返回membersInjectedType + "_MembersInjector"
     */
    public static ClassName generatedClassNameForBinding(Binding binding) {
        switch (binding.bindingType()) {
            case PROVISION:
            case PRODUCTION:
                ContributionBinding contribution = (ContributionBinding) binding;
                switch (contribution.kind()) {
                    case ASSISTED_INJECTION:
                    case INJECTION:
                    case PROVISION:
                    case PRODUCTION:
                        return elementBasedClassName(
                                MoreElements.asExecutable(binding.bindingElement().get()), "Factory");

                    case ASSISTED_FACTORY:
                        return siblingClassName(MoreElements.asType(binding.bindingElement().get()), "_Impl");

                    default:
                        throw new AssertionError();
                }

            case MEMBERS_INJECTION:
                return membersInjectorNameForType(
                        ((MembersInjectionBinding) binding).membersInjectedType());
        }
        throw new AssertionError();
    }

    /**
     * Calculates an appropriate {@link ClassName} for a generated class that is based on {@code
     * element}, appending {@code suffix} at the end.
     *
     * <p>This will always return a {@linkplain ClassName#topLevelClassName() top level class name},
     * even if {@code element}'s enclosing class is a nested type.
     */
    public static ClassName elementBasedClassName(ExecutableElement element, String suffix) {
        ClassName enclosingClassName =
                ClassName.get(MoreElements.asType(element.getEnclosingElement()));
        String methodName =
                element.getKind().equals(ElementKind.CONSTRUCTOR)
                        ? ""
                        : LOWER_CAMEL.to(UPPER_CAMEL, element.getSimpleName().toString());
        return ClassName.get(
                enclosingClassName.packageName(),
                classFileName(enclosingClassName) + "_" + methodName + suffix);
    }

    /**
     * Returns a name to be used for variables of the given {@linkplain TypeElement type}. Prefer
     * semantically meaningful variable names, but if none can be derived, this will produce something
     * readable.
     * <p>
     * 对类名校验更正：1.使用TestData格式；2.如果包含java关键字那么使用其缩略字符串；
     */
    // TODO(gak): maybe this should be a function of TypeMirrors instead of Elements?
    public static String simpleVariableName(TypeElement typeElement) {
        return simpleVariableName(ClassName.get(typeElement));
    }

    /**
     * Returns a name to be used for variables of the given {@linkplain ClassName}. Prefer
     * semantically meaningful variable names, but if none can be derived, this will produce something
     * readable.
     * <p>
     * 对类名校验更正：1.使用testData格式；2.如果包含java关键字那么使用其缩略字符串；
     */
    public static String simpleVariableName(ClassName className) {
        String candidateName = UPPER_CAMEL.to(LOWER_CAMEL, className.simpleName());
        String variableName = protectAgainstKeywords(candidateName);
        verify(isName(variableName), "'%s' was expected to be a valid variable name");
        return variableName;
    }

    public static String protectAgainstKeywords(String candidateName) {
        switch (candidateName) {
            case "package":
                return "pkg";
            case "boolean":
                return "b";
            case "double":
                return "d";
            case "byte":
                return "b";
            case "int":
                return "i";
            case "short":
                return "s";
            case "char":
                return "c";
            case "void":
                return "v";
            case "class":
                return "clazz";
            case "float":
                return "f";
            case "long":
                return "l";
            default:
                return SourceVersion.isKeyword(candidateName) ? candidateName + '_' : candidateName;
        }
    }

    private SourceFiles() {
    }
}
