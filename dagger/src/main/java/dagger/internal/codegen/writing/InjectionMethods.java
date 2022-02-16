package dagger.internal.codegen.writing;


import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import dagger.internal.Preconditions;
import dagger.internal.codegen.base.UniqueNameSet;
import dagger.internal.codegen.binding.AssistedInjectionAnnotations;
import dagger.internal.codegen.binding.MembersInjectionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.internal.codegen.compileroption.CompilerOptions;
import dagger.internal.codegen.extension.DaggerCollectors;
import dagger.internal.codegen.javapoet.CodeBlocks;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.DaggerAnnotation;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.RequestKind;

import static com.google.auto.common.MoreElements.asExecutable;
import static com.google.auto.common.MoreElements.asType;
import static com.google.auto.common.MoreElements.asVariable;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.Preconditions.checkArgument;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static dagger.internal.codegen.base.RequestKinds.requestTypeName;
import static dagger.internal.codegen.binding.ConfigurationAnnotations.getNullableType;
import static dagger.internal.codegen.binding.SourceFiles.generatedClassNameForBinding;
import static dagger.internal.codegen.binding.SourceFiles.memberInjectedFieldSignatureForVariable;
import static dagger.internal.codegen.binding.SourceFiles.membersInjectorNameForType;
import static dagger.internal.codegen.binding.SourceFiles.protectAgainstKeywords;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableMap;
import static dagger.internal.codegen.javapoet.CodeBlocks.makeParametersCodeBlock;
import static dagger.internal.codegen.javapoet.CodeBlocks.toConcatenatedCodeBlock;
import static dagger.internal.codegen.javapoet.CodeBlocks.toParametersCodeBlock;
import static dagger.internal.codegen.javapoet.TypeNames.rawTypeName;
import static dagger.internal.codegen.langmodel.Accessibility.isElementAccessibleFrom;
import static dagger.internal.codegen.langmodel.Accessibility.isRawTypeAccessible;
import static dagger.internal.codegen.langmodel.Accessibility.isRawTypePubliclyAccessible;
import static dagger.internal.codegen.langmodel.Accessibility.isTypeAccessibleFrom;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.type.TypeKind.VOID;

/**
 * Convenience methods for creating and invoking {@link InjectionMethod}s.
 */
final class InjectionMethods {


    /**
     * A method that returns an object from a {@code @Provides} method or an {@code @Inject}ed
     * constructor. Its parameters match the dependency requests for constructor and members
     * injection.
     * <p>
     * 主要针对@Provides修饰的方法，或者@Inject修饰的构造函数
     *
     * <p>For {@code @Provides} methods named "foo", the method name is "proxyFoo". For example:
     *
     * <pre><code>
     * abstract class FooModule {
     *   {@literal @Provides} static Foo provideFoo(Bar bar, Baz baz) { … }
     * }
     *
     * public static proxyProvideFoo(Bar bar, Baz baz) { … }
     * </code></pre>
     *
     * <p>For {@code @Inject}ed constructors, the method name is "newFoo". For example:
     *
     * <pre><code>
     * class Foo {
     *   {@literal @Inject} Foo(Bar bar) {}
     * }
     *
     * public static Foo newFoo(Bar bar) { … }
     * </code></pre>
     */
    static final class ProvisionMethod {

        // These names are already defined in factories and shouldn't be used for the proxy method name.
        private static final ImmutableSet<String> BANNED_PROXY_NAMES = ImmutableSet.of("get", "create");

        /**
         * Returns a method that invokes the binding's {@linkplain ProvisionBinding#bindingElement()
         * constructor} and injects the instance's members.
         */
        static MethodSpec create(
                ProvisionBinding binding,
                CompilerOptions compilerOptions,
                KotlinMetadataUtil metadataUtil) {
            ExecutableElement element = asExecutable(binding.bindingElement().get());

            switch (element.getKind()) {
                case CONSTRUCTOR://如果方法是构造函数
                    return constructorProxy(element);
                case METHOD://如果只是普通方法
                    return methodProxy(
                            element,
                            methodName(element),//原先方法名带有get或create字样，则加上前缀“proxy”，例如proxyGet；否则直接使用原先方法名
                            InstanceCastPolicy.IGNORE,
                            CheckNotNullPolicy.get(binding, compilerOptions),
                            metadataUtil);
                default:
                    throw new AssertionError(element);
            }
        }

        /**
         * Invokes the injection method for {@code binding}, with the dependencies transformed with the
         * {@code dependencyUsage} function.
         */
        static CodeBlock invoke(
                ProvisionBinding binding,
                Function<DependencyRequest, CodeBlock> dependencyUsage,
                Function<VariableElement, String> uniqueAssistedParameterName,
                ClassName requestingClass,
                Optional<CodeBlock> moduleReference,
                CompilerOptions compilerOptions,
                KotlinMetadataUtil metadataUtil) {
            ImmutableList.Builder<CodeBlock> arguments = ImmutableList.builder();

            //1.如果传递的moduleReference存在，添加
            moduleReference.ifPresent(arguments::add);

            //2.参数
            invokeArguments(binding, dependencyUsage, uniqueAssistedParameterName, requestingClass)
                    .forEach(arguments::add);

            //绑定节点生成类名
            ClassName enclosingClass = generatedClassNameForBinding(binding);

            MethodSpec methodSpec = create(binding, compilerOptions, metadataUtil);

            //返回 methodSpec方法名(arguments参数以逗号分隔) 代码块
            return invokeMethod(methodSpec, arguments.build(), enclosingClass, requestingClass);
        }

        /**
         * Returns {@code true} if injecting an instance of {@code binding} from {@code callingPackage}
         * requires the use of an injection method.
         */
        static boolean requiresInjectionMethod(
                ProvisionBinding binding, CompilerOptions compilerOptions, ClassName requestingClass) {
            ExecutableElement method = MoreElements.asExecutable(binding.bindingElement().get());
            return !binding.injectionSites().isEmpty()
                    || binding.shouldCheckForNull(compilerOptions)
                    || !isElementAccessibleFrom(method, requestingClass.packageName())
                    // This check should be removable once we drop support for -source 7
                    || method.getParameters().stream()
                    .map(VariableElement::asType)
                    .anyMatch(type -> !isRawTypeAccessible(type, requestingClass.packageName()));
        }

        //处理方法参数，1.Assisted修饰,则返回该参数代码块；2.如果其他参数，根据实际情况，做外包裹一层架构类型处理
        static ImmutableList<CodeBlock> invokeArguments(
                ProvisionBinding binding,
                Function<DependencyRequest, CodeBlock> dependencyUsage,
                Function<VariableElement, String> uniqueAssistedParameterName,
                ClassName requestingClass) {

            //当前绑定是@Inject} constructor 或 @Provides} method的参数依赖
            ImmutableMap<VariableElement, DependencyRequest> dependencyRequestMap =
                    binding.provisionDependencies().stream()
                            .collect(
                                    toImmutableMap(
                                            request -> MoreElements.asVariable(request.requestElement().get().java()),
                                            request -> request));

            ImmutableList.Builder<CodeBlock> arguments = ImmutableList.builder();
            ExecutableElement method = asExecutable(binding.bindingElement().get());

            //遍历方法参数
            for (VariableElement parameter : method.getParameters()) {
                //如果方法参数使用了Assisted修饰
                if (AssistedInjectionAnnotations.isAssistedParameter(parameter)) {
                    //1.将该参数表示的代码块添加到arguments
                    arguments.add(CodeBlock.of("$L", uniqueAssistedParameterName.apply(parameter)));

                } else if (dependencyRequestMap.containsKey(parameter)) {

                    //2.除了Assisted修饰的参数，其他参数需要加入架构类型生成新的代码块加入到arguments，例如Provider<T>
                    DependencyRequest request = dependencyRequestMap.get(parameter);
                    arguments.add(
                            injectionMethodArgument(request, dependencyUsage.apply(request), requestingClass));
                } else {
                    throw new AssertionError("Unexpected parameter: " + parameter);
                }
            }

            return arguments.build();
        }


        //Inject修饰的构造函数，生成一个使用public static修饰的newInstance方法，
        // 该方法参数：构造函数所在类的所有变量，
        // 方法里面的代码 ：return new 构造函数说在类类型(该类变量以逗号分隔)，
        // 返回类型：构造函数所在类的类型
        private static MethodSpec constructorProxy(ExecutableElement constructor) {

            TypeElement enclosingType = MoreElements.asType(constructor.getEnclosingElement());
            MethodSpec.Builder builder =
                    //方法名是newInstance
                    methodBuilder(methodName(constructor))
                            .addModifiers(PUBLIC, STATIC)
                            //varargs:设置最后一个参数是否为可变参数
                            .varargs(constructor.isVarArgs())
                            //返回类型：当前构造函数所在类的类型
                            .returns(TypeName.get(enclosingType.asType()));

            //把enclosingType上所有的泛型拷贝作为methodBuilder的参数变量
            copyTypeParameters(builder, enclosingType);

            //把constructor方法上的继承异常拷贝到methodBuilder
            copyThrows(builder, constructor);

            //所有参数添加到builder，并且这些参数以逗号分隔形成代码块返回
            CodeBlock arguments =
                    copyParameters(builder, new UniqueNameSet(), constructor.getParameters());

            //arguments是参数形成以逗号分隔的代码块作为代码
            return builder.addStatement("return new $T($L)", enclosingType, arguments).build();
        }


        /**
         * Returns the name of the {@code static} method that wraps {@code method}. For methods that are
         * associated with {@code @Inject} constructors, the method will also inject all {@link
         * MembersInjectionBinding.InjectionSite}s.
         * <p>
         * 方法命名：
         * 1.如果是构造函数，方法名是newInstance；
         * 2.如果是普通方法，原先方法名带有get或create字样，则加上前缀“proxy”，例如proxyGet；否则直接使用原先方法名
         */
        private static String methodName(ExecutableElement method) {
            switch (method.getKind()) {
                case CONSTRUCTOR:
                    return "newInstance";
                case METHOD:
                    String methodName = method.getSimpleName().toString();
                    return BANNED_PROXY_NAMES.contains(methodName)
                            ? "proxy" + LOWER_CAMEL.to(UPPER_CAMEL, methodName)
                            : methodName;
                default:
                    throw new AssertionError(method);
            }
        }
    }

    /**
     * A static method that injects one member of an instance of a type. Its first parameter is an
     * instance of the type to be injected. The remaining parameters match the dependency requests for
     * the injection site.
     * <p>
     * Inject修饰的变量或普通方法代码生成逻辑
     *
     * <p>Example:
     *
     * <pre><code>
     * class Foo {
     *   {@literal @Inject} Bar bar;
     *   {@literal @Inject} void setThings(Baz baz, Qux qux) {}
     * }
     *
     * public static injectBar(Foo instance, Bar bar) { … }
     * public static injectSetThings(Foo instance, Baz baz, Qux qux) { … }
     * </code></pre>
     */
    static final class InjectionSiteMethod {
        /**
         * When a type has an inaccessible member from a supertype (e.g. an @Inject field in a parent
         * that's in a different package), a method in the supertype's package must be generated to give
         * the subclass's members injector a way to inject it. Each potentially inaccessible member
         * receives its own method, as the subclass may need to inject them in a different order from
         * the parent class.
         */
        static MethodSpec create(MembersInjectionBinding.InjectionSite injectionSite, KotlinMetadataUtil metadataUtil) {

            //方法名
            String methodName = methodName(injectionSite);

            switch (injectionSite.kind()) {
                case METHOD://如果是Inject修饰的普通方法
                    return methodProxy(
                            asExecutable(injectionSite.element()),
                            methodName,
                            InstanceCastPolicy.CAST_IF_NOT_PUBLIC,
                            CheckNotNullPolicy.IGNORE,
                            metadataUtil);
                case FIELD://如果是Inject修饰的变量
                    Optional<AnnotationMirror> qualifier =
                            injectionSite.dependencies().stream()
                                    // methods for fields have a single dependency request
                                    .collect(DaggerCollectors.onlyElement())
                                    .key()
                                    .qualifier()
                                    .map(DaggerAnnotation::java);

                    return fieldProxy(asVariable(injectionSite.element()), methodName, qualifier);
            }
            throw new AssertionError(injectionSite);
        }

        /**
         * Invokes each of the injection methods for {@code injectionSites}, with the dependencies
         * transformed using the {@code dependencyUsage} function.
         *
         * @param instanceType the type of the {@code instance} parameter
         */
        static CodeBlock invokeAll(
                ImmutableSet<MembersInjectionBinding.InjectionSite> injectionSites,
                ClassName generatedTypeName,
                CodeBlock instanceCodeBlock,
                TypeMirror instanceType,
                Function<DependencyRequest, CodeBlock> dependencyUsage,
                DaggerTypes types,
                KotlinMetadataUtil metadataUtil
        ) {
            //e.g. 遍历执行ComponentProcessor类中使用Inject修饰的变量在新生成的Component的injectComponentProcessor方法代码块，如下：
            //        ComponentProcessor_MembersInjector.injectInjectBindingRegistry(instance, (InjectBindingRegistry) injectBindingRegistryImplProvider.get());
            //        ComponentProcessor_MembersInjector.injectFactoryGenerator(instance, sourceFileGeneratorOfProvisionBinding());
            //        ComponentProcessor_MembersInjector.injectMembersInjectorGenerator(instance, sourceFileGeneratorOfMembersInjectionBinding());
            //        ComponentProcessor_MembersInjector.injectProcessingSteps(instance, immutableListOfXProcessingStep());
            //        ComponentProcessor_MembersInjector.injectValidationBindingGraphPlugins(instance, validationBindingGraphPlugins());
            //        ComponentProcessor_MembersInjector.injectExternalBindingGraphPlugins(instance, externalBindingGraphPlugins());
            //        ComponentProcessor_MembersInjector.injectClearableCaches(instance, setOfClearableCache());
            return injectionSites.stream()
                    .map(
                            injectionSite -> {
                                TypeMirror injectSiteType =
                                        types.erasure(injectionSite.element().getEnclosingElement().asType());

                                // If instance has been declared as Object because it is not accessible from the
                                // component, but the injectionSite is in a supertype of instanceType that is
                                // publicly accessible, the InjectionSiteMethod will request the actual type and not
                                // Object as the first parameter. If so, cast to the supertype which is accessible
                                // from within generatedTypeName
                                CodeBlock maybeCastedInstance =
                                        !types.isSubtype(instanceType, injectSiteType)
                                                && isTypeAccessibleFrom(injectSiteType, generatedTypeName.packageName())
                                                ? CodeBlock.of("($T) $L", injectSiteType, instanceCodeBlock)
                                                : instanceCodeBlock;

                                return CodeBlock.of(
                                        "$L;",
                                        invoke(
                                                injectionSite,
                                                generatedTypeName,
                                                maybeCastedInstance,
                                                dependencyUsage,
                                                metadataUtil));
                            })
                    .collect(toConcatenatedCodeBlock());
        }

        /**
         * Invokes the injection method for {@code injectionSite}, with the dependencies transformed
         * using the {@code dependencyUsage} function.
         */
        private static CodeBlock invoke(
                MembersInjectionBinding.InjectionSite injectionSite,
                ClassName generatedTypeName,
                CodeBlock instanceCodeBlock,
                Function<DependencyRequest, CodeBlock> dependencyUsage,
                KotlinMetadataUtil metadataUtil) {

            ImmutableList.Builder<CodeBlock> arguments = ImmutableList.builder();
            arguments.add(instanceCodeBlock);

            if (!injectionSite.dependencies().isEmpty()) {
                arguments.addAll(
                        injectionSite.dependencies().stream().map(dependencyUsage).collect(toList()));
            }

            //生成的类名：当前Inject节点所在父类 + "_MembersInjector"
            //e.g.ComponentProcessor_MembersInjector
            ClassName enclosingClass =
                    membersInjectorNameForType(asType(injectionSite.element().getEnclosingElement()));

            //e.g. ComponentProcessor根据使用Inject修饰的变量生成对应的方法
            MethodSpec methodSpec = create(injectionSite, metadataUtil);

            return invokeMethod(methodSpec, arguments.build(), enclosingClass, generatedTypeName);
        }

        /*
         * TODO(ronshapiro): this isn't perfect, as collisions could still exist. Some examples:
         *
         *  - @Inject void members() {} will generate a method that conflicts with the instance
         *    method `injectMembers(T)`
         *  - Adding the index could conflict with another member:
         *      @Inject void a(Object o) {}
         *      @Inject void a(String s) {}
         *      @Inject void a1(String s) {}
         *
         *    Here, Method a(String) will add the suffix "1", which will conflict with the method
         *    generated for a1(String)
         *  - Members named "members" or "methods" could also conflict with the {@code static} injection
         *    method.
         */
        private static String methodName(MembersInjectionBinding.InjectionSite injectionSite) {

            //当前injectionSite在父类中的所有使用Inject修饰的非private节点的位置
            int index = injectionSite.indexAmongAtInjectMembersWithSameSimpleName();
            String indexString = index == 0 ? "" : String.valueOf(index + 1);

            //方法名："inject" + TestData格式的injectionSite节点 + 如果第一位? "":当前第几位
            return "inject"
                    + LOWER_CAMEL.to(UPPER_CAMEL, injectionSite.element().getSimpleName().toString())
                    + indexString;
        }

    }

    private static CodeBlock injectionMethodArgument(
            DependencyRequest dependency,
            CodeBlock argument,
            ClassName generatedTypeName
    ) {
        TypeMirror keyType = dependency.key().type().java();

        CodeBlock.Builder codeBlock = CodeBlock.builder();

        //如果当前依赖类型不能被generatedTypeName所在包访问 && 当前依赖类型可以被引用
        if (!isRawTypeAccessible(keyType, generatedTypeName.packageName())
                && isTypeAccessibleFrom(keyType, generatedTypeName.packageName())) {
            //如果依赖kind不是Instance
            if (!dependency.kind().equals(RequestKind.INSTANCE)) {

                //当前依赖参数可被访问，那么根据依赖kind给参数外包裹一层架构类型，如Provider<T>;不可见则直接使用Object对象
                TypeName usageTypeName = accessibleType(dependency);
                //例如(Provider<T>)Provider
                codeBlock.add("($T) ($T)", usageTypeName, rawTypeName(usageTypeName));
            } else if (dependency.requestElement().get().java().asType().getKind().equals(
                    TypeKind.TYPEVAR)) {

                codeBlock.add("($T)", keyType);
            }
        }

        return codeBlock.add(argument).build();
    }

    /**
     * Returns the parameter type for {@code dependency}. If the raw type is not accessible, returns
     * {@link Object}.
     * <p>
     * 返回 {@code dependency} 的参数类型。 如果原始类型不可访问，则返回 {@link Object}。
     */
    private static TypeName accessibleType(DependencyRequest dependency) {

        //根据依赖kind，给依赖参数最外层包裹架构类型
        TypeName typeName =
                requestTypeName(dependency.kind(), accessibleType(dependency.key().type().java()));
        return dependency
                .requestElement()
                .map(element -> element.java().asType().getKind().isPrimitive())
                .orElse(false)
                ? typeName.unbox()//如果依赖所在节点是原始类型，那么使用去盒类型
                : typeName;
    }

    /**
     * Returns the accessible type for {@code type}. If the raw type is not accessible, returns {@link
     * Object}.
     * <p>
     * 返回 {@code type} 的可访问类型。 如果原始类型不可访问，则返回 {@link Object}。
     */
    private static TypeName accessibleType(TypeMirror type) {
        return isRawTypePubliclyAccessible(type) ? TypeName.get(type) : TypeName.OBJECT;
    }

    private enum InstanceCastPolicy {
        CAST_IF_NOT_PUBLIC, IGNORE;

        //如果非public && 不能被任意包访问，则使用Object对象
        boolean useObjectType(TypeMirror instanceType) {
            return this == CAST_IF_NOT_PUBLIC && !isRawTypePubliclyAccessible(instanceType);
        }
    }

    private enum CheckNotNullPolicy {
        IGNORE, CHECK_FOR_NULL;

        CodeBlock checkForNull(CodeBlock maybeNull) {
            return this.equals(IGNORE)
                    ? maybeNull
                    : CodeBlock.of("$T.checkNotNullFromProvides($L)", Preconditions.class, maybeNull);
        }

        //如果需要检查null：使用Preconditions.checkNotNullFromProvides包裹
        static CheckNotNullPolicy get(ProvisionBinding binding, CompilerOptions compilerOptions) {
            return binding.shouldCheckForNull(compilerOptions) ? CHECK_FOR_NULL : IGNORE;
        }

    }

    //@Provides修饰的方法
    //1.原先方法名带有get或create字样，则加上前缀“proxy”，例如proxyGet；否则直接使用原先方法名
    //2.方法返回类型沿用方法的返回类型，方法参数和方法抛出异常沿用之前方法，public static修饰
    //3.方法里面的代码：return instance.方法名(以逗号分隔的方法参数)
    private static MethodSpec methodProxy(
            ExecutableElement method,
            String methodName,
            InstanceCastPolicy instanceCastPolicy,
            CheckNotNullPolicy checkNotNullPolicy,
            KotlinMetadataUtil metadataUtil) {

        //public static修饰当前方法
        MethodSpec.Builder builder =
                methodBuilder(methodName).addModifiers(PUBLIC, STATIC).varargs(method.isVarArgs());

        TypeElement enclosingType = asType(method.getEnclosingElement());
        boolean isMethodInKotlinObject = metadataUtil.isObjectClass(enclosingType);
        boolean isMethodInKotlinCompanionObject = metadataUtil.isCompanionObjectClass(enclosingType);
        UniqueNameSet parameterNameSet = new UniqueNameSet();

        CodeBlock instance;
        if (isMethodInKotlinCompanionObject || method.getModifiers().contains(STATIC)) {

            //1.如果方法所在类是Kotlin Companion Object对象 || 当前方法使用了static修饰：当前方法所在父类的类型（如果类型带有嵌套,例如List<T>返回List）作为一个代码块
            instance = CodeBlock.of("$T", rawTypeName(TypeName.get(enclosingType.asType())));

        } else if (isMethodInKotlinObject) {

            //2.如果是一个Kotlin Object对象：代码块，$T.INSTANCE = 当前方法所在父类的类型（如果类型带有嵌套,例如List<T>返回List）作为一个代码块
            // Call through the singleton instance.
            // See: https://kotlinlang.org/docs/reference/java-to-kotlin-interop.html#static-methods
            instance = CodeBlock.of("$T.INSTANCE", rawTypeName(TypeName.get(enclosingType.asType())));
        } else {
            //将方法所在类的所有泛型作为builder的参数
            copyTypeParameters(builder, enclosingType);
            //是否必须使用Object对象强转
            boolean useObject = instanceCastPolicy.useObjectType(enclosingType.asType());
            instance = copyInstance(builder, parameterNameSet, enclosingType.asType(), useObject);
        }
        //method方法参数拷贝到builder，并且这些方法以逗号分隔作为代码块返回
        CodeBlock arguments = copyParameters(builder, parameterNameSet, method.getParameters());

        //instance.方法名(以逗号分隔的方法参数)
        CodeBlock invocation =
                checkNotNullPolicy.checkForNull(
                        CodeBlock.of("$L.$L($L)", instance, method.getSimpleName(), arguments));

        //method方法泛型和继承的异常拷贝到builder
        copyTypeParameters(builder, method);
        copyThrows(builder, method);

        //方法返回类型是void
        if (method.getReturnType().getKind().equals(VOID)) {
            return builder.addStatement("$L", invocation).build();
        } else {
            //添加Nullable注解，原先方法返回类型作为当前方法返回类型
            //方法里面的代码：return instance.方法名(以逗号分隔的方法参数)
            getNullableType(method)
                    .ifPresent(annotation -> CodeBlocks.addAnnotation(builder, annotation));
            return builder
                    .returns(TypeName.get(method.getReturnType()))
                    .addStatement("return $L", invocation).build();
        }
    }

    private static MethodSpec fieldProxy(
            VariableElement field,
            String methodName,
            Optional<AnnotationMirror> qualifierAnnotation
    ) {
        //添加InjectedFieldSignature修饰变量
        MethodSpec.Builder builder =
                methodBuilder(methodName)
                        .addModifiers(PUBLIC, STATIC)
                        .addAnnotation(
                                AnnotationSpec.builder(TypeNames.INJECTED_FIELD_SIGNATURE)
                                        .addMember("value", "$S", memberInjectedFieldSignatureForVariable(field))
                                        .build());

        //如果使用了Qualifier修饰的注解修饰，那么添加该注解
        qualifierAnnotation.map(AnnotationSpec::get).ifPresent(builder::addAnnotation);

        TypeElement enclosingType = asType(field.getEnclosingElement());

        //拷贝变量所在类的所有泛型作为当前方法的参数
        copyTypeParameters(builder, enclosingType);

        boolean useObject = !isRawTypePubliclyAccessible(enclosingType.asType());
        UniqueNameSet parameterNameSet = new UniqueNameSet();
        CodeBlock instance = copyInstance(builder, parameterNameSet, enclosingType.asType(), useObject);
        CodeBlock argument = copyParameters(builder, parameterNameSet, ImmutableList.of(field));

        return builder.addStatement("$L.$L = $L", instance, field.getSimpleName(), argument).build();
    }

    private static CodeBlock invokeMethod(
            MethodSpec methodSpec,
            ImmutableList<CodeBlock> parameters,
            ClassName enclosingClass,
            ClassName requestingClass) {

        checkArgument(methodSpec.parameters.size() == parameters.size());

        CodeBlock parameterBlock = makeParametersCodeBlock(parameters);
        return enclosingClass.equals(requestingClass)
                ? CodeBlock.of("$L($L)", methodSpec.name, parameterBlock)
                : CodeBlock.of("$T.$L($L)", enclosingClass, methodSpec.name, parameterBlock);
    }

    //把element节点上所有的泛型拷贝作为methodBuilder的参数变量
    private static void copyTypeParameters(
            MethodSpec.Builder methodBuilder,
            Parameterizable element
    ) {
        element.getTypeParameters().stream()
                .map(TypeVariableName::get)
                .forEach(methodBuilder::addTypeVariable);
    }

    //把method方法上的继承异常拷贝到methodBuilder
    private static void copyThrows(MethodSpec.Builder methodBuilder, ExecutableElement method) {
        method.getThrownTypes().stream().map(TypeName::get).forEach(methodBuilder::addException);
    }

    private static CodeBlock copyParameters(
            MethodSpec.Builder methodBuilder,
            UniqueNameSet parameterNameSet,
            List<? extends VariableElement> parameters) {

        return parameters.stream()
                .map(
                        parameter -> {
                            //校验参数名，并且返回合理的参数名
                            String name =
                                    parameterNameSet.getUniqueName(validJavaName(parameter.getSimpleName()));

                            //如果参数不是可以被任意包访问，那么表示使用Object对象转换，并且将当前参数作为methodBuilder方法，并且返回该参数生成的代码块
                            TypeMirror type = parameter.asType();
                            boolean useObject = !isRawTypePubliclyAccessible(type);
                            return copyParameter(methodBuilder, type, name, useObject);
                        })
                //所有参数生成的代码块以逗号分隔返回
                .collect(toParametersCodeBlock());
    }


    //方法添加参数，并且返回该参数形成的代码块
    private static CodeBlock copyParameter(
            MethodSpec.Builder methodBuilder,
            TypeMirror type,
            String name,
            boolean useObject
    ) {
        //如果方法参数类型使用了Object类型，那么typeName类型是Object，否则使用type类型
        TypeName typeName = useObject ? TypeName.OBJECT : TypeName.get(type);
        methodBuilder.addParameter(ParameterSpec.builder(typeName, name).build());

        //如果使用了Object，那么加上类型转换
        return useObject ? CodeBlock.of("($T) $L", type, name) : CodeBlock.of("$L", name);
    }

    private static CodeBlock copyInstance(
            MethodSpec.Builder methodBuilder,
            UniqueNameSet parameterNameSet,
            TypeMirror type,
            boolean useObject) {

        //方法添加参数，并且返回该参数形成的代码块
        CodeBlock instance =
                copyParameter(methodBuilder, type, parameterNameSet.getUniqueName("instance"), useObject);

        // If we had to cast the instance add an extra parenthesis incase we're calling a method on it.
        return useObject ? CodeBlock.of("($L)", instance) : instance;
    }

    //校验名称
    private static String validJavaName(CharSequence name) {
        //SourceVersion.isIdentifier()：是否是java关键字
        //如果名称是标识符，那么将name转换成标识符缩写，以保证标识符没有被使用
        if (SourceVersion.isIdentifier(name)) {
            return protectAgainstKeywords(name.toString());
        }

        //Character.isJavaIdentifierStart():确定是否允许将指定字符作为 Java 标识符中的首字符
        //如果名称首字母不允许作为java标识符的首字符，那么当前当前name凭借上"_"
        StringBuilder newName = new StringBuilder(name.length());
        char firstChar = name.charAt(0);
        if (!Character.isJavaIdentifierStart(firstChar)) {
            newName.append('_');
        }

        //Character.isJavaIdentifierPart():确定指定字符是否可以是 Java 标识符中首字符以外的部分
        name.chars().forEach(c -> newName.append(Character.isJavaIdentifierPart(c) ? c : '_'));
        return newName.toString();
    }
}
