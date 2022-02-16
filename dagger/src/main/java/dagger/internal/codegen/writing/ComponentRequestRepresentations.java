package dagger.internal.codegen.writing;

import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.binding.Binding;
import dagger.internal.codegen.binding.BindingGraph;
import dagger.internal.codegen.binding.BindingRequest;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.ComponentRequirement;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.FrameworkType;
import dagger.internal.codegen.binding.FrameworkTypeMapper;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.RequestKind;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.BindingRequest.bindingRequest;
import static dagger.internal.codegen.javapoet.CodeBlocks.makeParametersCodeBlock;
import static dagger.internal.codegen.langmodel.Accessibility.isRawTypeAccessible;
import static dagger.internal.codegen.langmodel.Accessibility.isTypeAccessibleFrom;

@PerComponentImplementation
public final class ComponentRequestRepresentations {
    // TODO(dpb,ronshapiro): refactor this and ComponentRequirementExpressions into a
    // HierarchicalComponentMap<K, V>, or perhaps this use a flattened ImmutableMap, built from its
    // parents? If so, maybe make RequestRepresentation.Factory create it.

    private final Optional<ComponentRequestRepresentations> parent;
    private final BindingGraph graph;
    private final ComponentImplementation componentImplementation;
    private final ComponentRequirementExpressions componentRequirementExpressions;
    private final LegacyBindingRepresentation.Factory legacyBindingRepresentationFactory;
    private final DaggerTypes types;
    private final CompilerOptions compilerOptions;
    private final Map<BindingRequest, RequestRepresentation> expressions = new HashMap<>();
    private final Map<Binding, BindingRepresentation> representations = new HashMap<>();
    private final SwitchingProviders switchingProviders;

    @Inject
    ComponentRequestRepresentations(
            @ParentComponent Optional<ComponentRequestRepresentations> parent,
            BindingGraph graph,
            ComponentImplementation componentImplementation,
            ComponentRequirementExpressions componentRequirementExpressions,
            LegacyBindingRepresentation.Factory legacyBindingRepresentationFactory,
            DaggerTypes types,
            CompilerOptions compilerOptions) {
        this.parent = parent;
        this.graph = graph;
        this.componentImplementation = componentImplementation;
        this.legacyBindingRepresentationFactory = legacyBindingRepresentationFactory;
        this.componentRequirementExpressions = checkNotNull(componentRequirementExpressions);
        this.types = types;
        this.compilerOptions = compilerOptions;
        this.switchingProviders = new SwitchingProviders(componentImplementation, types);
    }

    /**
     * Returns an expression that evaluates to the value of a binding request for a binding owned by
     * this component or an ancestor.
     *
     * @param requestingClass the class that will contain the expression
     * @throws IllegalStateException if there is no binding expression that satisfies the request
     */
    public Expression getDependencyExpression(BindingRequest request, ClassName requestingClass) {

        //核心纽带
        //1.InjectBindingRegistry作为type生成的key钥匙匹配上InjectBindingRegistryModule的injectBindingRegistry方法
        //DelegateRequestRepresentation对象，参数InjectBindingRegistryImpl作为type的key和RequestKind.INSTANCE 往下执行

        //1.1 InjectBindingRegistryImpl作为type的key匹配上InjectBindingRegistryImpl使用Inject修饰的构造函数
        //DerivedFromFrameworkInstanceRequestRepresentation对象,参数InjectBindingRegistryImpl作为type的key和RequestKind.PROVIDER，FrameworkType.PROVIDER往下执行
        //FrameworkFieldInitializer.FrameworkInstanceCreationExpression对象作为FrameworkFieldInitializer对象参数
        //InjectionOrProvisionProviderCreationExpression对象
        //FrameworkFieldInitializer对象ProviderInstanceRequestRepresentation对象参数

        //1.1.1 InjectBindingRegistryImpl构造函数的参数作为key去匹配

        //2.SourceFileGenerator<ProvisionBinding> factoryGenerator作为type生成的key钥匙匹配上SourceFileGeneratorsModule的factoryGenerator方法
        //PrivateMethodRequestRepresentation对象

        //3.SourceFileGenerator<MembersInjectionBinding> 作为type生成的key钥匙匹配上 SourceFileGeneratorsModule的SourceFileGenerator<MembersInjectionBinding> membersInjectorGenerator方法；
        //PrivateMethodRequestRepresentation对象

        //4.ImmutableList<XProcessingStep> processingSteps匹配上ProcessingStepsModule的processingSteps方法；
        //PrivateMethodRequestRepresentation对象

        //5.ValidationBindingGraphPlugins validationBindingGraphPlugins匹配上ValidationBindingGraphPlugins使用Inject修饰的构造函数
        //PrivateMethodRequestRepresentation对象

        //6.Set<ClearableCache> clearableCaches作为key钥匙匹配上ProcessingEnvironmentModule的daggerElementAsClearableCache方法
        //PrivateMethodRequestRepresentation对象
        return getRequestRepresentation(request).getDependencyExpression(requestingClass);
    }

    /**
     * Equivalent to {@link #getDependencyExpression(BindingRequest, ClassName)} that is used only
     * when the request is for implementation of a component method.
     *
     * @throws IllegalStateException if there is no binding expression that satisfies the request
     */
    Expression getDependencyExpressionForComponentMethod(
            BindingRequest request,
            ComponentDescriptor.ComponentMethodDescriptor componentMethod,
            ComponentImplementation componentImplementation) {
        return getRequestRepresentation(request)
                .getDependencyExpressionForComponentMethod(componentMethod, componentImplementation);
    }

    /**
     * Returns the {@link CodeBlock} for the method arguments used with the factory {@code create()}
     * method for the given {@link ContributionBinding binding}.
     */
    CodeBlock getCreateMethodArgumentsCodeBlock(
            ContributionBinding binding, ClassName requestingClass) {
        return makeParametersCodeBlock(getCreateMethodArgumentsCodeBlocks(binding, requestingClass));
    }

    private ImmutableList<CodeBlock> getCreateMethodArgumentsCodeBlocks(
            ContributionBinding binding, ClassName requestingClass) {
        ImmutableList.Builder<CodeBlock> arguments = ImmutableList.builder();

        if (binding.requiresModuleInstance()) {
            arguments.add(
                    componentRequirementExpressions.getExpressionDuringInitialization(
                            ComponentRequirement.forModule(binding.contributingModule().get().asType()),
                            requestingClass));
        }

        binding.dependencies().stream()
                .map(dependency -> frameworkRequest(binding, dependency))
                .map(request -> getDependencyExpression(request, requestingClass))
                .map(Expression::codeBlock)
                .forEach(arguments::add);

        return arguments.build();
    }

    private static BindingRequest frameworkRequest(
            ContributionBinding binding, DependencyRequest dependency) {
        // TODO(bcorso): See if we can get rid of FrameworkTypeMatcher
        FrameworkType frameworkType =
                FrameworkTypeMapper.forBindingType(binding.bindingType())
                        .getFrameworkType(dependency.kind());
        return BindingRequest.bindingRequest(dependency.key(), frameworkType);
    }

    /**
     * Returns an expression that evaluates to the value of a dependency request, for passing to a
     * binding method, an {@code @Inject}-annotated constructor or member, or a proxy for one.
     *
     * <p>If the method is a generated static {@link InjectionMethods injection method}, each
     * parameter will be {@link Object} if the dependency's raw type is inaccessible. If that is the
     * case for this dependency, the returned expression will use a cast to evaluate to the raw type.
     *
     * @param requestingClass the class that will contain the expression
     */
    Expression getDependencyArgumentExpression(
            DependencyRequest dependencyRequest, //Inject修饰的变量生成的InjectionSite对象的依赖
            ClassName requestingClass) {

        TypeMirror dependencyType = dependencyRequest.key().type().java();
        BindingRequest bindingRequest = bindingRequest(dependencyRequest);

        Expression dependencyExpression = getDependencyExpression(bindingRequest, requestingClass);

        if (dependencyRequest.kind().equals(RequestKind.INSTANCE)
                && !isTypeAccessibleFrom(dependencyType, requestingClass.packageName())
                && isRawTypeAccessible(dependencyType, requestingClass.packageName())) {
            return dependencyExpression.castTo(types.erasure(dependencyType));
        }

        return dependencyExpression;
    }

    /**
     * Returns the implementation of a component method.
     */
    public MethodSpec getComponentMethod(ComponentDescriptor.ComponentMethodDescriptor componentMethod) {
        checkArgument(componentMethod.dependencyRequest().isPresent());

        BindingRequest request = bindingRequest(componentMethod.dependencyRequest().get());

        //e.g.在生成的Component类中生成继承component节点的void inject(ComponentProcessor processor)方法
        return MethodSpec.overriding(
                componentMethod.methodElement(),
                MoreTypes.asDeclared(graph.componentTypeElement().asType()),
                types)
                .addCode(
                        //调用MembersInjectionRequestRepresentation.getComponentMethodImplementation:（以及ComponentProcessor_MembersInjector类）

                        //重写的void inject(ComponentProcessor processor)方法如下代码片段：
                        //ComponentProcessor_MembersInjector.injectInjectBindingRegistry(instance, (InjectBindingRegistry) injectBindingRegistryImplProvider.get());
                        //ComponentProcessor_MembersInjector.injectFactoryGenerator(instance, sourceFileGeneratorOfProvisionBinding());
                        //ComponentProcessor_MembersInjector.injectMembersInjectorGenerator(instance, sourceFileGeneratorOfMembersInjectionBinding());
                        //ComponentProcessor_MembersInjector.injectProcessingSteps(instance, immutableListOfXProcessingStep());
                        //ComponentProcessor_MembersInjector.injectValidationBindingGraphPlugins(instance, validationBindingGraphPlugins());
                        //ComponentProcessor_MembersInjector.injectExternalBindingGraphPlugins(instance, externalBindingGraphPlugins());
                        //ComponentProcessor_MembersInjector.injectClearableCaches(instance, setOfClearableCache());

                        //request:componentMethod依赖生成
                        getRequestRepresentation(request)

                                .getComponentMethodImplementation(componentMethod, componentImplementation))
                .build();
    }

    /**
     * Returns the {@link RequestRepresentation} for the given {@link BindingRequest}.
     */
    RequestRepresentation getRequestRepresentation(BindingRequest request) {
        if (expressions.containsKey(request)) {
            return expressions.get(request);
        }

        Optional<Binding> localBinding =
                request.isRequestKind(RequestKind.MEMBERS_INJECTION)
                        ? graph.localMembersInjectionBinding(request.key())
                        : graph.localContributionBinding(request.key());

        if (localBinding.isPresent()) {

            RequestRepresentation expression =
                    getBindingRepresentation(localBinding.get()).getRequestRepresentation(request);
            expressions.put(request, expression);
            return expression;
        }

        checkArgument(parent.isPresent(), "no expression found for %s", request);
        return parent.get().getRequestRepresentation(request);
    }

    BindingRepresentation getBindingRepresentation(Binding binding) {
        if (representations.containsKey(binding)) {
            return representations.get(binding);
        }
        BindingRepresentation representation =
                legacyBindingRepresentationFactory.create(isFastInit(), binding, switchingProviders);
        representations.put(binding, representation);
        return representation;
    }

    private boolean isFastInit() {
        return compilerOptions.fastInit(
                parent.map(p -> p.graph).orElse(graph).componentDescriptor().typeElement());
    }
}
