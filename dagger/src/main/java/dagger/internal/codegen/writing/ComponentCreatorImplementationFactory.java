package dagger.internal.codegen.writing;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;

import dagger.internal.Preconditions;
import dagger.internal.codegen.base.UniqueNameSet;
import dagger.internal.codegen.binding.ComponentCreatorDescriptor;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.ComponentRequirement;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.auto.common.MoreTypes.asDeclared;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static dagger.internal.codegen.binding.SourceFiles.simpleVariableName;
import static dagger.internal.codegen.javapoet.CodeBlocks.toParametersCodeBlock;
import static dagger.internal.codegen.javapoet.TypeSpecs.addSupertype;
import static dagger.internal.codegen.langmodel.Accessibility.isElementAccessibleFrom;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;


/**
 * Factory for creating {@link ComponentCreatorImplementation} instances.
 * <p>
 * 实现Creator类的生成
 */
final class ComponentCreatorImplementationFactory {


    private final ComponentImplementation componentImplementation;
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final KotlinMetadataUtil metadataUtil;
    private final ModuleProxies moduleProxies;

    @Inject
    ComponentCreatorImplementationFactory(
            ComponentImplementation componentImplementation,
            DaggerElements elements,
            DaggerTypes types,
            KotlinMetadataUtil metadataUtil,
            ModuleProxies moduleProxies) {
        this.componentImplementation = componentImplementation;
        this.elements = elements;
        this.types = types;
        this.metadataUtil = metadataUtil;
        this.moduleProxies = moduleProxies;
    }

    /**
     * Returns a new creator implementation for the given component, if necessary.
     * <p>
     * component内部类creator节点生成Creator类
     */
    Optional<ComponentCreatorImplementation> create() {
        if (!componentImplementation.componentDescriptor().hasCreator()) {
            return Optional.empty();
        }

        Optional<ComponentCreatorDescriptor> creatorDescriptor =
                componentImplementation.componentDescriptor().creatorDescriptor();

        Builder builder =
                creatorDescriptor.isPresent()
                        ? new BuilderForCreatorDescriptor(creatorDescriptor.get())
                        : new BuilderForGeneratedRootComponentBuilder();
        return Optional.of(builder.build());
    }

    /**
     * Base class for building a creator implementation.
     */
    private abstract class Builder {

        private final TypeSpec.Builder classBuilder =
                classBuilder(componentImplementation.getCreatorName());

        private final UniqueNameSet fieldNames = new UniqueNameSet();

        private ImmutableMap<ComponentRequirement, FieldSpec> fields;

        /**
         * Builds the {@link ComponentCreatorImplementation}.
         */
        ComponentCreatorImplementation build() {
            //1.修饰
            setModifiers();
            //2.继承
            setSupertype();
            //3.构造函数
            addConstructor();
            //4.变量
            this.fields = addFields();

            //5.SetterMethod方法-Builder模式下的setterMethod方法在Creator类中创建新的方法
            addSetterMethods();

            //6. FactoryMethod方法-Factory模式下的factoryMethod方法 或 Builder模式下的build方法
            addFactoryMethod();

            return ComponentCreatorImplementation.create(
                    classBuilder.build(), componentImplementation.getCreatorName(), fields);
        }

        /**
         * Returns the descriptor for the component.
         */
        final ComponentDescriptor componentDescriptor() {
            return componentImplementation.componentDescriptor();
        }

        /**
         * The set of requirements that must be passed to the component's constructor in the order
         * they must be passed.
         * <p>
         * 需要实例的类型：
         * （1）componentAnnotation#dependencies；
         * （2）当前graph图中需要被实例化的module；
         * （3）使用@BindsInstance修饰的方法或方法参数生成的ComponentRequirement集合；
         * （4）component中的返回类型是ChildComponent类型的方法，那么对当前方法的参数（参数肯定是module节点）生成Module类型的ComponentRequirement对象；
         */
        final ImmutableSet<ComponentRequirement> componentConstructorRequirements() {
            return componentImplementation.graph().componentRequirements();
        }

        /**
         * Returns the requirements that have setter methods on the creator type.
         */
        abstract ImmutableSet<ComponentRequirement> setterMethods();

        /**
         * Returns the component requirements that have factory method parameters, mapped to the name
         * for that parameter.
         */
        abstract ImmutableMap<ComponentRequirement, String> factoryMethodParameters();

        /**
         * The {@link ComponentRequirement}s that this creator allows users to set. Values are a status
         * for each requirement indicating what's needed for that requirement in the implementation
         * class currently being generated.
         */
        abstract ImmutableMap<ComponentRequirement, RequirementStatus> userSettableRequirements();

        /**
         * Component requirements that are both settable by the creator and needed to construct the
         * component.
         */
        private Set<ComponentRequirement> neededUserSettableRequirements() {
            //userSettableRequirements()：factory模式下方法参数生成的ComponentRequirement或builder模式下的setterMethod方法参数生成的ComponentRequirement
            //componentConstructorRequirements:需要实例的类型
            //  ①当前componentAnnotation#dependencies；
            //  ②当前component需要实例化的module节点；
            //  ③当前creator使用@BindsInstance修饰的方法或方法参数
            //  ④component返回类型是subcomponent的节点；
            return Sets.intersection(
                    userSettableRequirements().keySet(), componentConstructorRequirements());
        }

        //creator生成类设置修饰符
        private void setModifiers() {
            visibility().ifPresent(classBuilder::addModifiers);
            classBuilder.addModifiers(STATIC, FINAL);
        }

        /**
         * Returns the visibility modifier the generated class should have, if any.
         */
        protected abstract Optional<Modifier> visibility();

        /**
         * Sets the superclass being extended or interface being implemented for this creator.
         */
        protected abstract void setSupertype();

        /**
         * Adds a constructor for the creator type, if needed.
         */
        protected void addConstructor() {
            //构造函数使用private修饰
            MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(PRIVATE);

            componentImplementation
                    .creatorComponentFields()
                    .forEach(
                            field -> {
                                //变量名
                                fieldNames.claim(field.name);
                                //类添加变量
                                classBuilder.addField(field);
                                //构造函数添加参数
                                constructor.addParameter(field.type, field.name);
                                //构造函数添加代码块
                                constructor.addStatement("this.$1N = $1N", field);
                            });
            classBuilder.addMethod(constructor.build());
        }

        private ImmutableMap<ComponentRequirement, FieldSpec> addFields() {
            // Fields in an abstract creator class need to be visible from subclasses.
            ImmutableMap<ComponentRequirement, FieldSpec> result =
                    Maps.toMap(
                            //neededUserSettableRequirements：
                            //  userSettableRequirements()：factory模式下方法参数生成的ComponentRequirement或builder模式下的setterMethod方法参数生成的ComponentRequirement
                            //  componentConstructorRequirements:需要实例的类型
                            //   ①当前componentAnnotation#dependencies；
                            //   ②当前component需要实例化的module节点；
                            //   ③当前creator使用@BindsInstance修饰的方法或方法参数
                            //   ④component返回类型是subcomponent的节点；

                            //setterMethods:BuilderForCreatorDescriptor实现类中表示creator是Builder情况下setterMethod方法参数生成的ComponentRequirement对象

                            //intersection获取的是两个方法的交集：所以获取的是:creator是Builder情况下setterMethod方法参数生成的ComponentRequirement对象
                            Sets.intersection(neededUserSettableRequirements(), setterMethods()),
                            requirement ->
                                    FieldSpec.builder(
                                            TypeName.get(requirement.type()),
                                            fieldNames.getUniqueName(requirement.variableName()),
                                            PRIVATE)
                                            .build());
            classBuilder.addFields(result.values());
            return result;
        }

        private void addSetterMethods() {
            //Builder模式下的setterMethod方法在Creator类中创建新的方法
            Maps.filterKeys(userSettableRequirements(), setterMethods()::contains)
                    .forEach(
                            (requirement, status) ->
                                    createSetterMethod(requirement, status).ifPresent(classBuilder::addMethod));
        }

        /**
         * Creates a new setter method builder, with no method body, for the given requirement.
         */
        protected abstract MethodSpec.Builder setterMethodBuilder(ComponentRequirement requirement);

        //处理Builder模式下的setterMethod方法
        private Optional<MethodSpec> createSetterMethod(
                ComponentRequirement requirement, RequirementStatus status) {
            switch (status) {
                case NEEDED:
                    //正常继承setterMethod方法即可
                    return Optional.of(normalSetterMethod(requirement));
                case UNNEEDED:
                    // TODO(bcorso): Don't generate noop setters for any unneeded requirements.
                    // However, since this is a breaking change we can at least avoid trying
                    // to generate noop setters for impossible cases like when the requirement type
                    // is in another package. This avoids unnecessary breakages in Dagger's generated
                    // due to the noop setters.
                    if (isElementAccessibleFrom(
                            requirement.typeElement(), componentImplementation.name().packageName())) {
                        return Optional.of(noopSetterMethod(requirement));
                    } else {
                        return Optional.empty();
                    }
                case UNSETTABLE_REPEATED_MODULE:
                    return Optional.of(repeatedModuleSetterMethod(requirement));
            }
            throw new AssertionError();
        }

        //正常的setterMethod方法生成新的继承方法
        private MethodSpec normalSetterMethod(ComponentRequirement requirement) {
            MethodSpec.Builder method = setterMethodBuilder(requirement);
            ParameterSpec parameter = parameter(method.build());
            method.addStatement(
                    "this.$N = $L",
                    fields.get(requirement),
                    requirement.nullPolicy(elements, metadataUtil).equals(ComponentRequirement.NullPolicy.ALLOW)
                            ? CodeBlock.of("$N", parameter)
                            : CodeBlock.of("$T.checkNotNull($N)", Preconditions.class, parameter));
            return maybeReturnThis(method);
        }

        private MethodSpec noopSetterMethod(ComponentRequirement requirement) {
            MethodSpec.Builder method = setterMethodBuilder(requirement);
            ParameterSpec parameter = parameter(method.build());
            method
                    .addAnnotation(Deprecated.class)
                    .addJavadoc(
                            "@deprecated This module is declared, but an instance is not used in the component. "
                                    + "This method is a no-op. For more, see https://dagger.dev/unused-modules.\n")
                    .addStatement("$T.checkNotNull($N)", Preconditions.class, parameter);
            return maybeReturnThis(method);
        }

        //没有找到当前方法
        private MethodSpec repeatedModuleSetterMethod(ComponentRequirement requirement) {
            return setterMethodBuilder(requirement)
                    .addStatement(
                            "throw new $T($T.format($S, $T.class.getCanonicalName()))",
                            UnsupportedOperationException.class,
                            String.class,
                            "%s cannot be set because it is inherited from the enclosing component",
                            TypeNames.rawTypeName(TypeName.get(requirement.type())))
                    .build();
        }

        private ParameterSpec parameter(MethodSpec method) {//参数
            return getOnlyElement(method.parameters);
        }

        private MethodSpec maybeReturnThis(MethodSpec.Builder method) {
            MethodSpec built = method.build();
            return built.returnType.equals(TypeName.VOID)
                    ? built
                    : method.addStatement("return this").build();
        }

        private void addFactoryMethod() {
            classBuilder.addMethod(factoryMethod());
        }

        MethodSpec factoryMethod() { //factoryMethod方法创建或buildMethod方法的创建

            //方法继承
            MethodSpec.Builder factoryMethod = factoryMethodBuilder();

            //方法修饰符和返回类型
            factoryMethod
                    .returns(ClassName.get(componentDescriptor().typeElement()))
                    .addModifiers(PUBLIC);

            //方法参数
            ImmutableMap<ComponentRequirement, String> factoryMethodParameters =
                    factoryMethodParameters();

            //factory模式下方法参数生成的ComponentRequirement或builder模式下的setterMethod方法参数生成的ComponentRequirement
            userSettableRequirements()
                    .keySet()
                    .forEach(
                            requirement -> {
                                //Builder模式下的build方法
                                if (fields.containsKey(requirement)) {
                                    FieldSpec field = fields.get(requirement);
                                    addNullHandlingForField(requirement, field, factoryMethod);
                                }
                                //Factory模式下factoryMethod方法
                                else if (factoryMethodParameters.containsKey(requirement)) {
                                    String parameterName = factoryMethodParameters.get(requirement);
                                    addNullHandlingForParameter(requirement, parameterName, factoryMethod);
                                }
                            });

            factoryMethod.addStatement(
                    "return new $T($L)",
                    componentImplementation.name(),
                    componentConstructorArgs(factoryMethodParameters));
            return factoryMethod.build();
        }


        private void addNullHandlingForField(
                ComponentRequirement requirement, FieldSpec field, MethodSpec.Builder factoryMethod) {
            switch (requirement.nullPolicy(elements, metadataUtil)) {
                case NEW:
                    checkState(requirement.kind().isModule());
                    factoryMethod
                            .beginControlFlow("if ($N == null)", field)
                            .addStatement("this.$N = $L", field, newModuleInstance(requirement))
                            .endControlFlow();
                    break;
                case THROW:
                    // TODO(cgdecker,ronshapiro): ideally this should use the key instead of a class for
                    // @BindsInstance requirements, but that's not easily proguardable.
                    factoryMethod.addStatement(
                            "$T.checkBuilderRequirement($N, $T.class)",
                            Preconditions.class,
                            field,
                            TypeNames.rawTypeName(field.type));
                    break;
                case ALLOW:
                    break;
            }
        }

        private void addNullHandlingForParameter(
                ComponentRequirement requirement, String parameter, MethodSpec.Builder factoryMethod) {
            if (!requirement.nullPolicy(elements, metadataUtil).equals(ComponentRequirement.NullPolicy.ALLOW)) {
                // Factory method parameters are always required unless they are a nullable
                // binds-instance (i.e. ALLOW)
                factoryMethod.addStatement("$T.checkNotNull($L)", Preconditions.class, parameter);
            }
        }

        /**
         * Returns a builder for the creator's factory method.
         */
        protected abstract MethodSpec.Builder factoryMethodBuilder();

        private CodeBlock componentConstructorArgs(
                ImmutableMap<ComponentRequirement, String> factoryMethodParameters) {
            //合并
            return Stream.concat(
                    //1.currentComponent及其父级currentComponent生成的变量，但是要排除当前currentComponent生成的变量
                    componentImplementation.creatorComponentFields().stream()
                            .map(field -> CodeBlock.of("$N", field)),
                    //2. factoryMethod方法中的参数或setterMethod方法中的参数
                    componentConstructorRequirements().stream()
                            .map(
                                    requirement -> {
                                        if (fields.containsKey(requirement)) {
                                            return CodeBlock.of("$N", fields.get(requirement));
                                        } else if (factoryMethodParameters.containsKey(requirement)) {
                                            return CodeBlock.of("$L", factoryMethodParameters.get(requirement));
                                        } else {
                                            return newModuleInstance(requirement);
                                        }
                                    }))
                    .collect(toParametersCodeBlock());
        }

        private CodeBlock newModuleInstance(ComponentRequirement requirement) {
            checkArgument(requirement.kind().isModule()); // this should be guaranteed to be true here
            return moduleProxies.newModuleInstance(
                    requirement.typeElement(), componentImplementation.getCreatorName());
        }
    }

    /**
     * Builder for a creator type defined by a {@code ComponentCreatorDescriptor}.
     */
    private final class BuilderForCreatorDescriptor extends Builder {
        final ComponentCreatorDescriptor creatorDescriptor;

        BuilderForCreatorDescriptor(ComponentCreatorDescriptor creatorDescriptor) {
            this.creatorDescriptor = creatorDescriptor;
        }

        @Override
        protected ImmutableMap<ComponentRequirement, RequirementStatus> userSettableRequirements() {
            //creator ：factory模式下方法参数生成的ComponentRequirement或builder模式下的setterMethod方法参数生成的ComponentRequirement
            return Maps.toMap(creatorDescriptor.userSettableRequirements(), this::requirementStatus);
        }

        @Override
        protected Optional<Modifier> visibility() {
            return Optional.of(PRIVATE);
        }

        @Override
        protected void setSupertype() {
            //继承creator节点
            addSupertype(super.classBuilder, creatorDescriptor.typeElement());
        }

        @Override
        protected void addConstructor() {
            //如果componentImplementation用于生成creator的变量不为空
            if (!componentImplementation.creatorComponentFields().isEmpty()) {
                super.addConstructor();
            }
        }

        @Override
        protected ImmutableSet<ComponentRequirement> setterMethods() {
            //creator中除了factoryMethod外所有方法参数生成的ComponentRequirement集合
            return ImmutableSet.copyOf(creatorDescriptor.setterMethods().keySet());
        }

        @Override
        protected ImmutableMap<ComponentRequirement, String> factoryMethodParameters() {//factoryMethod方法参数
            return ImmutableMap.copyOf(
                    Maps.transformValues(
                            creatorDescriptor.factoryParameters(),
                            element -> element.getSimpleName().toString()));
        }

        private DeclaredType creatorType() {
            return asDeclared(creatorDescriptor.typeElement().asType());
        }

        @Override
        protected MethodSpec.Builder factoryMethodBuilder() {//factoryMethod方法继承
            return MethodSpec.overriding(creatorDescriptor.factoryMethod(), creatorType(), types);
        }

        private RequirementStatus requirementStatus(ComponentRequirement requirement) {
            //当前creator被@BindsInstance修饰的参数表示的节点，既不存在于当前component中，也不存在与整个BindingGraph有向图中。
            if (isRepeatedModule(requirement)) {
                return RequirementStatus.UNSETTABLE_REPEATED_MODULE;
            }

            //如果存在于component中，表示needed；否则表示unnneeded
            return componentConstructorRequirements().contains(requirement)
                    ? RequirementStatus.NEEDED
                    : RequirementStatus.UNNEEDED;
        }

        /**
         * Returns whether the given requirement is for a repeat of a module inherited from an ancestor
         * component. This creator is not allowed to set such a module.
         */
        final boolean isRepeatedModule(ComponentRequirement requirement) {
            return !componentConstructorRequirements().contains(requirement)
                    && !isOwnedModule(requirement);
        }

        /**
         * Returns whether the given {@code requirement} is for a module type owned by the component.
         */
        private boolean isOwnedModule(ComponentRequirement requirement) {
            return componentImplementation.graph().ownedModuleTypes().contains(requirement.typeElement());
        }

        @Override
        protected MethodSpec.Builder setterMethodBuilder(ComponentRequirement requirement) {
            //creator节点中Builder模式下的setterMethod方法生成新的方法
            ExecutableElement supertypeMethod = creatorDescriptor.setterMethods().get(requirement);
            MethodSpec.Builder method = MethodSpec.overriding(supertypeMethod, creatorType(), types);
            if (!supertypeMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
                // Take advantage of covariant returns so that we don't have to worry about type variables
                method.returns(componentImplementation.getCreatorName());
            }
            return method;
        }
    }

    /**
     * Builder for a component builder class that is automatically generated for a root component that
     * does not have its own user-defined creator type (i.e. a {@code ComponentCreatorDescriptor}).
     */
    private final class BuilderForGeneratedRootComponentBuilder extends Builder {

        @Override
        protected ImmutableMap<ComponentRequirement, RequirementStatus> userSettableRequirements() {
            return Maps.toMap(
                    setterMethods(),
                    requirement ->
                            componentConstructorRequirements().contains(requirement)
                                    ? RequirementStatus.NEEDED
                                    : RequirementStatus.UNNEEDED);
        }

        @Override
        protected Optional<Modifier> visibility() {
            //如果creator所在的component节点使用了public，那么当前creator使用public；否则什么都不需要
            return componentImplementation
                    .componentDescriptor()
                    .typeElement()
                    .getModifiers()
                    .contains(PUBLIC) ? Optional.of(PUBLIC) : Optional.empty();
        }

        @Override
        protected void setSupertype() {
            // There's never a supertype for a root component auto-generated builder type.
        }

        @Override
        protected ImmutableSet<ComponentRequirement> setterMethods() {
            //当前Component 关联的所有非abstact修饰的module类和dependencies生成ComponentRequirement对象集合
            return componentDescriptor().dependenciesAndConcreteModules();
        }

        @Override
        protected ImmutableMap<ComponentRequirement, String> factoryMethodParameters() {
            return ImmutableMap.of();
        }

        @Override
        protected MethodSpec.Builder factoryMethodBuilder() {
            return methodBuilder("build");
        }

        @Override
        protected MethodSpec.Builder setterMethodBuilder(ComponentRequirement requirement) {
            String name = simpleVariableName(requirement.typeElement());
            return methodBuilder(name)
                    .addModifiers(PUBLIC)
                    .addParameter(TypeName.get(requirement.type()), name)
                    .returns(componentImplementation.getCreatorName());
        }
    }

    /**
     * Enumeration of statuses a component requirement may have in a creator.
     */
    enum RequirementStatus {
        /**
         * An instance is needed to create the component.
         */
        NEEDED,

        /**
         * An instance is not needed to create the component, but the requirement is for a module owned
         * by the component. Setting the requirement is a no-op and any setter method should be marked
         * deprecated on the generated type as a warning to the user.
         */
        UNNEEDED,

        /**
         * The requirement may not be set in this creator because the module it is for is already
         * inherited from an ancestor component. Any setter method for it should throw an exception.
         */
        UNSETTABLE_REPEATED_MODULE,
        ;
    }
}
