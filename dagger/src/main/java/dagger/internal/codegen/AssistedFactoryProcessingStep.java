package dagger.internal.codegen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import androidx.room.compiler.processing.XFiler;
import androidx.room.compiler.processing.XMessager;
import androidx.room.compiler.processing.XTypeElement;
import androidx.room.compiler.processing.compat.XConverters;
import dagger.internal.codegen.base.SourceFileGenerationException;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.AssistedInjectionAnnotations;
import dagger.internal.codegen.binding.BindingFactory;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import androidx.room.compiler.processing.XProcessingEnv;

import dagger.internal.codegen.validation.TypeCheckingProcessingStep;
import dagger.internal.codegen.validation.ValidationReport;

import static com.google.auto.common.MoreElements.asType;
import static com.google.auto.common.MoreTypes.asDeclared;
import static com.google.auto.common.MoreTypes.asTypeElement;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.binding.AssistedInjectionAnnotations.assistedInjectedConstructors;
import static dagger.internal.codegen.binding.SourceFiles.generatedClassNameForBinding;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.javapoet.CodeBlocks.toParametersCodeBlock;
import static dagger.internal.codegen.javapoet.TypeNames.INSTANCE_FACTORY;
import static dagger.internal.codegen.javapoet.TypeNames.providerOf;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * An annotation processor for {@link dagger.assisted.AssistedFactory}-annotated types.
 */
final class AssistedFactoryProcessingStep extends TypeCheckingProcessingStep<XTypeElement> {

    private final XProcessingEnv processingEnv;
    private final XMessager messager;
    private final XFiler filer;
    private final SourceVersion sourceVersion;
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final BindingFactory bindingFactory;

    @Inject
    AssistedFactoryProcessingStep(
            XProcessingEnv processingEnv,
            XMessager messager,
            XFiler filer,
            SourceVersion sourceVersion,
            DaggerElements elements,
            DaggerTypes types,
            BindingFactory bindingFactory
    ) {
        this.processingEnv = processingEnv;
        this.messager = messager;
        this.filer = filer;
        this.sourceVersion = sourceVersion;
        this.elements = elements;
        this.types = types;
        this.bindingFactory = bindingFactory;
    }

    @Override
    public ImmutableSet<ClassName> annotationClassNames() {
        return ImmutableSet.of(TypeNames.ASSISTED_FACTORY);
    }

    @Override
    protected void process(XTypeElement factory, ImmutableSet<ClassName> annotations) {

        ValidationReport report = new AssistedFactoryValidator().validate(factory);
        report.printMessagesTo(messager);
        if (report.isClean()) {
            try {
                ProvisionBinding binding =
                        bindingFactory.assistedFactoryBinding(XConverters.toJavac(factory), Optional.empty());
                new AssistedFactoryImplGenerator().generate(binding);
            } catch (SourceFileGenerationException e) {
                e.printMessageTo(XConverters.toJavac(messager));
            }
        }
    }

    private final class AssistedFactoryValidator {

        //入口，校验使用AssistedFactory修饰的类或接口
        ValidationReport validate(XTypeElement factory) {

            ValidationReport.Builder report = ValidationReport.about(factory);

            //1. 使用AssistedFactory修饰的节点仅仅支持抽象类或接口;
            if (!factory.isAbstract()) {
                return report
                        .addError(
                                "The @AssistedFactory-annotated type must be either an abstract class or "
                                        + "interface.",
                                factory)
                        .build();
            }

            TypeElement javaFactory = XConverters.toJavac(factory);

            //2. 如果AssistedFactory修饰的节点是内部类，那么必须使用static修饰;
            if (javaFactory.getNestingKind().isNested() && !factory.isStatic()) {
                report.addError("Nested @AssistedFactory-annotated types must be static. ", factory);
            }

            //节点abstract、非static、非private的方法
            ImmutableSet<ExecutableElement> abstractFactoryMethods =
                    AssistedInjectionAnnotations.assistedFactoryMethods(javaFactory, elements);

            //3. AssistedFactory修饰的节点必须包含abstract、非static、非private的方法节点；
            if (abstractFactoryMethods.isEmpty()) {
                report.addError(
                        "The @AssistedFactory-annotated type is missing an abstract, non-default method "
                                + "whose return type matches the assisted injection type.",
                        factory);
            }

            for (ExecutableElement method : abstractFactoryMethods) {
                ExecutableType methodType = types.resolveExecutableType(method, javaFactory.asType());

                //4. AssistedFactory修饰的节点中的方法返回类型的构造函数必须使用@AssistedInject修饰；
                if (!isAssistedInjectionType(methodType.getReturnType())) {
                    report.addError(
                            String.format(
                                    "Invalid return type: %s. An assisted factory's abstract method must return a "
                                            + "type with an @AssistedInject-annotated constructor.",
                                    methodType.getReturnType()),
                            XConverters.toXProcessing(method, processingEnv));
                }

                //5. AssistedFactory修饰的节点中的方法不允许使用泛型；
                if (!method.getTypeParameters().isEmpty()) {
                    report.addError(
                            "@AssistedFactory does not currently support type parameters in the creator "
                                    + "method. See https://github.com/google/dagger/issues/2279",
                            XConverters.toXProcessing(method, processingEnv));
                }
            }

            //6.AssistedFactory修饰的类，该类中的abstract、非static、非private的方法有且仅有一个
            if (abstractFactoryMethods.size() > 1) {
                report.addError(
                        "The @AssistedFactory-annotated type should contain a single abstract, non-default"
                                + " method but found multiple: "
                                + abstractFactoryMethods,
                        factory);
            }

            //如果以上已有报错，则返回报错。否则继续往下判断
            if (!report.build().isClean()) {
                return report.build();
            }

            AssistedInjectionAnnotations.AssistedFactoryMetadata metadata =
                    AssistedInjectionAnnotations.AssistedFactoryMetadata.create(javaFactory.asType(), elements, types);


            // Note: We check uniqueness of the @AssistedInject constructor parameters in
            // AssistedInjectProcessingStep. We need to check uniqueness for here too because we may
            // have resolved some type parameters that were not resolved in the @AssistedInject type.
            Set<AssistedInjectionAnnotations.AssistedParameter> uniqueAssistedParameters = new HashSet<>();
            for (AssistedInjectionAnnotations.AssistedParameter assistedParameter : metadata.assistedFactoryAssistedParameters()) {

                //@AssistedFactory修饰的节点中的普通方法都不允许出现重复的@Assisted修饰的参数类型
                //例如C （@Assited A a,Assited A b）,因为a和b的Assited#value相同，并且类型都是A。所以会报错
                if (!uniqueAssistedParameters.add(assistedParameter)) {
                    report.addError(
                            "@AssistedFactory method has duplicate @Assisted types: " + assistedParameter,
                            XConverters.toXProcessing(assistedParameter.variableElement(), processingEnv));
                }
            }

            //6. AssistedFactory修饰的节点中的唯一方法传递的参数 和 该方法返回类型中的构造函数使用@Assisted注解修饰的参数 保持一致。
            if (!ImmutableSet.copyOf(metadata.assistedInjectAssistedParameters())
                    .equals(ImmutableSet.copyOf(metadata.assistedFactoryAssistedParameters()))) {
                report.addError(
                        String.format(
                                "The parameters in the factory method must match the @Assisted parameters in %s."
                                        + "\n      Actual: %s#%s"
                                        + "\n    Expected: %s#%s(%s)",
                                metadata.assistedInjectType(),
                                metadata.factory().getQualifiedName(),
                                metadata.factoryMethod(),
                                metadata.factory().getQualifiedName(),
                                metadata.factoryMethod().getSimpleName(),
                                metadata.assistedInjectAssistedParameters().stream()
                                        .map(AssistedInjectionAnnotations.AssistedParameter::type)
                                        .map(Object::toString)
                                        .collect(joining(", "))),
                        XConverters.toXProcessing(metadata.factoryMethod(), processingEnv));
            }

            return report.build();
        }

        //类中有且仅有一个构造函数使用了AssistdInject修饰
        private boolean isAssistedInjectionType(TypeMirror type) {
            return type.getKind() == TypeKind.DECLARED
                    && AssistedInjectionAnnotations.isAssistedInjectionType(asTypeElement(type));
        }
    }

    /**
     * Generates an implementation of the {@link dagger.assisted.AssistedFactory}-annotated class.
     */
    private final class AssistedFactoryImplGenerator extends SourceFileGenerator<ProvisionBinding> {
        AssistedFactoryImplGenerator() {
            super(filer, elements, sourceVersion);
        }

        @Override
        public Element originatingElement(ProvisionBinding binding) {
            return binding.bindingElement().get();
        }

        // For each @AssistedFactory-annotated type, we generates a class named "*_Impl" that implements
        // that type.
        //
        // Note that this class internally delegates to the @AssistedInject generated class, which
        // contains the actual implementation logic for creating the @AssistedInject type. The reason we
        // need both of these generated classes is because while the @AssistedInject generated class
        // knows how to create the @AssistedInject type, it doesn't know about all of the
        // @AssistedFactory interfaces that it needs to extend when it's generated. Thus, the role of
        // the @AssistedFactory generated class is purely to implement the @AssistedFactory type.
        // Furthermore, while we could have put all of the logic into the @AssistedFactory generated
        // class and not generate the @AssistedInject generated class, having the @AssistedInject
        // generated class ensures we have proper accessibility to the @AssistedInject type, and reduces
        // duplicate logic if there are multiple @AssistedFactory types for the same @AssistedInject
        // type.
        //
        // Example:
        // public class FooFactory_Impl implements FooFactory {
        //   private final Foo_Factory delegateFactory;
        //
        //   FooFactory_Impl(Foo_Factory delegateFactory) {
        //     this.delegateFactory = delegateFactory;
        //   }
        //
        //   @Override
        //   public Foo createFoo(AssistedDep assistedDep) {
        //     return delegateFactory.get(assistedDep);
        //   }
        //
        //   public static Provider<FooFactory> create(Foo_Factory delegateFactory) {
        //     return InstanceFactory.create(new FooFactory_Impl(delegateFactory));
        //   }
        // }
        @Override
        public ImmutableList<TypeSpec.Builder> topLevelTypes(ProvisionBinding binding) {
            TypeElement factory = asType(binding.bindingElement().get());

            //生成的类名： 绑定节点 + "_Impl"
            ClassName name = generatedClassNameForBinding(binding);

            TypeSpec.Builder builder =
                    TypeSpec.classBuilder(name)
                            .addModifiers(PUBLIC, FINAL)
                            .addTypeVariables(
                                    factory.getTypeParameters().stream()
                                            .map(TypeVariableName::get)
                                            .collect(toImmutableList())
                            );

            //如果是接口，或者是类
            if (factory.getKind() == ElementKind.INTERFACE) {
                builder.addSuperinterface(factory.asType());
            } else {
                builder.superclass(factory.asType());
            }

            AssistedInjectionAnnotations.AssistedFactoryMetadata metadata =
                    AssistedInjectionAnnotations.AssistedFactoryMetadata.create(asDeclared(factory.asType()), elements, types);

            ParameterSpec delegateFactoryParam =
                    ParameterSpec.builder(
                            delegateFactoryTypeName(metadata.assistedInjectType()), "delegateFactory")
                            .build();

            builder
                    //变量：当前AssistedFactory修饰的类或接口，里面的方法返回类型是使用@AssistedInject修饰的，该返回类型用于生成一个变量
                    .addField(
                            FieldSpec.builder(delegateFactoryParam.type, delegateFactoryParam.name)
                                    .addModifiers(PRIVATE, FINAL)
                                    .build())
                    //构造函数，参数是当前AssistedFactory修饰的类或接口，里面的方法使用@AssistedInject修饰的返回类型
                    .addMethod(
                            MethodSpec.constructorBuilder()
                                    .addParameter(delegateFactoryParam)
                                    .addStatement("this.$1N = $1N", delegateFactoryParam)
                                    .build())

                    //@AssistedFactory修饰的类或接口里面的唯一方法在这里被重写
                    .addMethod(
                            MethodSpec.overriding(metadata.factoryMethod(), metadata.factoryType(), types)
                                    .addStatement(
                                            "return $N.get($L)",
                                            delegateFactoryParam,
                                            // Use the order of the parameters from the @AssistedInject constructor but
                                            // use the parameter names of the @AssistedFactory method.
                                            metadata.assistedInjectAssistedParameters().stream()
                                                    .map(metadata.assistedFactoryAssistedParametersMap()::get)
                                                    .map(param -> CodeBlock.of("$L", param.getSimpleName()))
                                                    .collect(toParametersCodeBlock()))
                                    .build())
                    //添加create方法，方法参数:factoryMethod方法返回类型节点（是用AssistedInject修饰的）的变量
                    .addMethod(
                            MethodSpec.methodBuilder("create")
                                    .addModifiers(PUBLIC, STATIC)
                                    .addParameter(delegateFactoryParam)
                                    .addTypeVariables(
                                            metadata.assistedInjectElement().getTypeParameters().stream()
                                                    .map(TypeVariableName::get)
                                                    .collect(toImmutableList()))
                                    //Provider包裹factory节点
                                    .returns(providerOf(TypeName.get(factory.asType())))
                                    .addStatement(
                                            "return $T.$Lcreate(new $T($N))",
                                            INSTANCE_FACTORY,
                                            // Java 7 type inference requires the method call provide the exact type here.
                                            sourceVersion.compareTo(SourceVersion.RELEASE_7) <= 0
                                                    ? CodeBlock.of("<$T>", types.accessibleType(metadata.factoryType(), name))
                                                    : CodeBlock.of(""),
                                            name,
                                            delegateFactoryParam)
                                    .build());

            return ImmutableList.of(builder);
        }

        /**
         * Returns the generated factory {@link TypeName type} for an @AssistedInject constructor.
         */
        private TypeName delegateFactoryTypeName(DeclaredType assistedInjectType) {
            // The name of the generated factory for the assisted inject type,
            // e.g. an @AssistedInject Foo(...) {...} constructor will generate a Foo_Factory class.
            ClassName generatedFactoryClassName =
                    generatedClassNameForBinding(
                            bindingFactory.injectionBinding(
                                    getOnlyElement(assistedInjectedConstructors(asTypeElement(assistedInjectType))),
                                    Optional.empty()));

            // Return the factory type resolved with the same type parameters as the assisted inject type.
            return assistedInjectType.getTypeArguments().isEmpty()
                    ? generatedFactoryClassName
                    : ParameterizedTypeName.get(
                    generatedFactoryClassName,
                    assistedInjectType.getTypeArguments().stream()
                            .map(TypeName::get)
                            .collect(toImmutableList())
                            .toArray(new TypeName[0]));
        }
    }

}
