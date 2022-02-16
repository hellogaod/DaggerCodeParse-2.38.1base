package dagger.internal.codegen.writing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

import androidx.room.compiler.processing.XFiler;
import dagger.internal.Factory;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.base.UniqueNameSet;
import dagger.internal.codegen.binding.Binding;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.javapoet.CodeBlocks;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.BindingKind;
import dagger.spi.model.DependencyRequest;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.transformValues;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static dagger.internal.codegen.binding.AssistedInjectionAnnotations.assistedParameters;
import static dagger.internal.codegen.binding.ContributionBinding.FactoryCreationStrategy.DELEGATE;
import static dagger.internal.codegen.binding.ContributionBinding.FactoryCreationStrategy.SINGLETON_INSTANCE;
import static dagger.internal.codegen.binding.SourceFiles.bindingTypeElementTypeVariableNames;
import static dagger.internal.codegen.binding.SourceFiles.frameworkFieldUsages;
import static dagger.internal.codegen.binding.SourceFiles.frameworkTypeUsageStatement;
import static dagger.internal.codegen.binding.SourceFiles.generateBindingFieldsForDependencies;
import static dagger.internal.codegen.binding.SourceFiles.generatedClassNameForBinding;
import static dagger.internal.codegen.binding.SourceFiles.parameterizedGeneratedTypeNameForBinding;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableMap;
import static dagger.internal.codegen.javapoet.AnnotationSpecs.Suppression.RAWTYPES;
import static dagger.internal.codegen.javapoet.AnnotationSpecs.Suppression.UNCHECKED;
import static dagger.internal.codegen.javapoet.AnnotationSpecs.suppressWarnings;
import static dagger.internal.codegen.javapoet.CodeBlocks.makeParametersCodeBlock;
import static dagger.internal.codegen.javapoet.TypeNames.factoryOf;
import static dagger.internal.codegen.writing.GwtCompatibility.gwtIncompatibleAnnotation;
import static dagger.spi.model.BindingKind.ASSISTED_INJECTION;
import static dagger.spi.model.BindingKind.INJECTION;
import static dagger.spi.model.BindingKind.PROVISION;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Generates {@link Factory} implementations from {@link ProvisionBinding} instances for {@link
 * Inject} constructors.
 * <p>
 * 针对Inject修饰的构造函数转换的ProvisionBinding对象生成代码
 */
public final class FactoryGenerator extends SourceFileGenerator<ProvisionBinding> {

    private final DaggerTypes types;
    private final CompilerOptions compilerOptions;
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    FactoryGenerator(
            XFiler filer,
            SourceVersion sourceVersion,
            DaggerTypes types,
            DaggerElements elements,
            CompilerOptions compilerOptions,
            KotlinMetadataUtil metadataUtil) {
        super(filer, elements, sourceVersion);
        this.types = types;
        this.compilerOptions = compilerOptions;
        this.metadataUtil = metadataUtil;
    }

    @Override
    public Element originatingElement(ProvisionBinding binding) {
        // we only create factories for bindings that have a binding element
        return binding.bindingElement().get();
    }

    @Override
    public ImmutableList<TypeSpec.Builder> topLevelTypes(ProvisionBinding binding) {
        // We don't want to write out resolved bindings -- we want to write out the generic version.
        checkArgument(!binding.unresolved().isPresent());//未解析绑定不存在
        checkArgument(binding.bindingElement().isPresent());//绑定节点存在

        if (binding.factoryCreationStrategy().equals(DELEGATE)) {//如果使用了@Binds修饰的注解，那么不在此处处理
            return ImmutableList.of();
        }

        return ImmutableList.of(factoryBuilder(binding));
    }

    private TypeSpec.Builder factoryBuilder(ProvisionBinding binding) {

        TypeSpec.Builder factoryBuilder =
                //ProvisionBinding绑定bindingType是PROVISION，(1)如果绑定节点使用了AssistedFactory注解，那么命名：绑定节点 + "_Impl"
                // (2)如果绑定节点使用AssistedInject、Inject、Provides、Produces修饰，那么命名：绑定节点 + "Factory"
                classBuilder(generatedClassNameForBinding(binding))
                        .addModifiers(PUBLIC, FINAL)
                        //addTypeVariables():方法上表示参数，类上表示变量
                        .addTypeVariables(bindingTypeElementTypeVariableNames(binding));

        //如果当前绑定没有使用AssistedInject注解，那么继承Factory<T>接口
        //e.g.使用Inject注解的构造函数，这里的T表示当前构造函数所在类
        factoryTypeName(binding).ifPresent(factoryBuilder::addSuperinterface);

        //构造函数及其参数，以及构造函数的参数需要对应于该类生成变量
        addConstructorAndFields(binding, factoryBuilder);

        //添加方法
        factoryBuilder.addMethod(getMethod(binding));

        //添加create方法
        addCreateMethod(binding, factoryBuilder);

        factoryBuilder.addMethod(InjectionMethods.ProvisionMethod.create(binding, compilerOptions, metadataUtil));
        gwtIncompatibleAnnotation(binding).ifPresent(factoryBuilder::addAnnotation);

        return factoryBuilder;
    }

    //创建构造函数，根据构造函数参数在类中创建对应的参数
    private void addConstructorAndFields(ProvisionBinding binding, TypeSpec.Builder factoryBuilder) {

        //如果使用的是单例策略，那么下面的操作不需要
        if (binding.factoryCreationStrategy().equals(SINGLETON_INSTANCE)) {
            return;
        }

        // TODO(bcorso): Make the constructor private?
        MethodSpec.Builder constructor = constructorBuilder().addModifiers(PUBLIC);
        constructorParams(binding).forEach(
                param -> {
                    constructor.addParameter(param).addStatement("this.$1N = $1N", param);
                    factoryBuilder.addField(
                            FieldSpec.builder(param.type, param.name, PRIVATE, FINAL).build());
                });
        factoryBuilder.addMethod(constructor.build());
    }

    //构造函数的参数来源：
    // 1.在当前绑定所在的module需要实例化的情况下,module = 当前绑定节点类型；
    // 2.当前绑定依赖生成被架构类型包裹的新类型，Provider<T>
    private ImmutableList<ParameterSpec> constructorParams(ProvisionBinding binding) {
        ImmutableList.Builder<ParameterSpec> params = ImmutableList.builder();
        moduleParameter(binding).ifPresent(params::add);
        frameworkFields(binding).values().forEach(field -> params.add(toParameter(field)));
        return params.build();
    }

    //如果当前绑定需要module实例化，那么将添加构造函数参数：module = 当前绑定节点类型；不需要实例化module，返回空
    private Optional<ParameterSpec> moduleParameter(ProvisionBinding binding) {
        if (binding.requiresModuleInstance()) {
            // TODO(bcorso, dpb): Should this use contributingModule()?
            TypeName type = TypeName.get(binding.bindingTypeElement().get().asType());
            return Optional.of(ParameterSpec.builder(type, "module").build());
        }
        return Optional.empty();
    }

    //绑定的依赖参数生成变量类型，并且该参数是根据依赖kind类型决定一个框架类型包裹该参数实际类型，例如如果参数是T，当前绑定使用了Provides修饰，那么这里的变量类型是Provider<T>
    //还做了防止和module参数命名冲突处理
    private ImmutableMap<DependencyRequest, FieldSpec> frameworkFields(ProvisionBinding binding) {
        UniqueNameSet uniqueFieldNames = new UniqueNameSet();
        // TODO(bcorso, dpb): Add a test for the case when a Factory parameter is named "module".
        moduleParameter(binding).ifPresent(module -> uniqueFieldNames.claim(module.name));
        return ImmutableMap.copyOf(
                transformValues(
                        generateBindingFieldsForDependencies(binding),
                        field ->
                                FieldSpec.builder(
                                        field.type(), uniqueFieldNames.getUniqueName(field.name()), PRIVATE, FINAL)
                                        .build()));
    }

    //绑定节点没有使用Inject注解也没用使用AssistedInject注解 && 绑定的module类不需要实例化返回空；否则，如果返回绑定节点上所有参数
    public static ImmutableList<TypeVariableName> bindingTypeElementTypeVariableNames(
            Binding binding) {
        if (binding instanceof ContributionBinding) {
            ContributionBinding contributionBinding = (ContributionBinding) binding;

            //绑定节点没有使用Inject注解也没用使用AssistedInject注解 && 绑定的module类不需要实例化
            if (!(contributionBinding.kind() == INJECTION
                    || contributionBinding.kind() == ASSISTED_INJECTION)
                    && !contributionBinding.requiresModuleInstance()) {
                return ImmutableList.of();
            }
        }

        //getTypeParameters():返回element上面的声明的所有参数(泛型)
        List<? extends TypeParameterElement> typeParameters =
                binding.bindingTypeElement().get().getTypeParameters();
        return typeParameters.stream().map(TypeVariableName::get).collect(toImmutableList());
    }

    private void addCreateMethod(ProvisionBinding binding, TypeSpec.Builder factoryBuilder) {
        // If constructing a factory for @Inject or @Provides bindings, we use a static create method
        // so that generated components can avoid having to refer to the generic types
        // of the factory.  (Otherwise they may have visibility problems referring to the types.)
        MethodSpec.Builder createMethodBuilder =
                methodBuilder("create")
                        .addModifiers(PUBLIC, STATIC)
                        .returns(parameterizedGeneratedTypeNameForBinding(binding))
                        .addTypeVariables(bindingTypeElementTypeVariableNames(binding));

        switch (binding.factoryCreationStrategy()) {
            case SINGLETON_INSTANCE://单例
                FieldSpec.Builder instanceFieldBuilder =
                        FieldSpec.builder(
                                generatedClassNameForBinding(binding), "INSTANCE", PRIVATE, STATIC, FINAL)
                                .initializer("new $T()", generatedClassNameForBinding(binding));

                if (!bindingTypeElementTypeVariableNames(binding).isEmpty()) {
                    // If the factory has type parameters, ignore them in the field declaration & initializer
                    instanceFieldBuilder.addAnnotation(suppressWarnings(RAWTYPES));
                    createMethodBuilder.addAnnotation(suppressWarnings(UNCHECKED));
                }

                ClassName instanceHolderName =
                        generatedClassNameForBinding(binding).nestedClass("InstanceHolder");
                createMethodBuilder.addStatement("return $T.INSTANCE", instanceHolderName);
                factoryBuilder.addType(
                        TypeSpec.classBuilder(instanceHolderName)
                                .addModifiers(PRIVATE, STATIC, FINAL)
                                .addField(instanceFieldBuilder.build())
                                .build());
                break;
            case CLASS_CONSTRUCTOR:
                List<ParameterSpec> params = constructorParams(binding);
                createMethodBuilder.addParameters(params);
                createMethodBuilder.addStatement(
                        "return new $T($L)",
                        parameterizedGeneratedTypeNameForBinding(binding),
                        makeParametersCodeBlock(Lists.transform(params, input -> CodeBlock.of("$N", input))));
                break;
            default:
                throw new AssertionError();
        }
        factoryBuilder.addMethod(createMethodBuilder.build());
    }

    private MethodSpec getMethod(ProvisionBinding binding) {

        UniqueNameSet uniqueFieldNames = new UniqueNameSet();
        ImmutableMap<DependencyRequest, FieldSpec> frameworkFields = frameworkFields(binding);
        frameworkFields.values().forEach(field -> uniqueFieldNames.claim(field.name));

        //AssistedInject注解修饰的构造函数，收集使用Assited修饰的参数
        Map<VariableElement, ParameterSpec> assistedParameters =
                assistedParameters(binding).stream()
                        .collect(
                                toImmutableMap(
                                        element -> element,
                                        element ->
                                                ParameterSpec.builder(
                                                        TypeName.get(element.asType()),
                                                        uniqueFieldNames.getUniqueName(element.getSimpleName()))
                                                        .build()));

        //get方法
        //e.g. 使用Inject修饰的构造函数，这里会生成一个get(),无参，返回类型是构造函数所在类的类型
        TypeName providedTypeName = providedTypeName(binding);
        MethodSpec.Builder getMethod =
                methodBuilder("get")
                        .addModifiers(PUBLIC)
                        .returns(providedTypeName)
                        .addParameters(assistedParameters.values());

        //如果没有使用AssistedInject注解修饰,那么get()方法还需要有Override注解修饰，表示重写
        if (factoryTypeName(binding).isPresent()) {
            getMethod.addAnnotation(Override.class);
        }

        CodeBlock invokeNewInstance =
                InjectionMethods.ProvisionMethod.invoke(
                        binding,
                        //依赖kind决定使用CodeBlock代码块
                        request ->
                                frameworkTypeUsageStatement(
                                        CodeBlock.of("$N", frameworkFields.get(request)), request.kind()),
                        //Assisted修饰的参数
                        param -> assistedParameters.get(param).name,
                        //当前绑定bindingType决定绑定节点形成的新类名
                        generatedClassNameForBinding(binding),
                        //实例化（如果需要的话，不需要则为空）当前绑定所在的module类
                        moduleParameter(binding).map(module -> CodeBlock.of("$N", module)),
                        compilerOptions,
                        metadataUtil);

        if (binding.kind().equals(PROVISION)) {
            binding
                    .nullableType()
                    .ifPresent(nullableType -> CodeBlocks.addAnnotation(getMethod, nullableType));
            getMethod.addStatement("return $L", invokeNewInstance);

        } else if (!binding.injectionSites().isEmpty()) {
            CodeBlock instance = CodeBlock.of("instance");
            getMethod
                    .addStatement("$T $L = $L", providedTypeName, instance, invokeNewInstance)
                    .addCode(
                            InjectionMethods.InjectionSiteMethod.invokeAll(
                                    //当前绑定所在父类的所有使用Inject修饰的普通方法或变量
                                    binding.injectionSites(),
                                    //当前绑定节点生成的类名
                                    generatedClassNameForBinding(binding),
                                    //instance
                                    instance,
                                    //当前绑定（以Inject修饰构造函数为例）所在类的类型
                                    binding.key().type().java(),
                                    //当前绑定的参数形成的Map<K,V>,K:当前参数依赖；V：当前参数依赖形成的变量根据当前kind依赖类型决定使用哪种架构类型包裹形成的代码块
                                    frameworkFieldUsages(binding.dependencies(), frameworkFields)::get,
                                    types,
                                    metadataUtil))
                    .addStatement("return $L", instance);
        } else {
            getMethod.addStatement("return $L", invokeNewInstance);
        }
        return getMethod.build();
    }

    //e.g.Inject修饰的构造函数生成了ProvisionBinding,这里返回的TypeName表示的是当前构造函数所在的类节点
    private static TypeName providedTypeName(ProvisionBinding binding) {
        return TypeName.get(binding.contributedType());
    }

    private static Optional<TypeName> factoryTypeName(ProvisionBinding binding) {
        return binding.kind() == BindingKind.ASSISTED_INJECTION
                ? Optional.empty()//如果使用AssistedInject注解修饰，那么返回空
                : Optional.of(factoryOf(providedTypeName(binding)));//Factory<T>,这里的T表示binding.contrubutedType根据实际类型返回的key里面的java类信息
    }

    //变量转换参数类型
    private static ParameterSpec toParameter(FieldSpec field) {
        return ParameterSpec.builder(field.type, field.name).build();
    }
}
