package dagger.internal.codegen.binding;


import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.base.ComponentAnnotation;
import dagger.internal.codegen.base.ModuleAnnotation;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.Scope;

import static com.google.auto.common.MoreElements.asType;
import static com.google.auto.common.MoreTypes.asTypeElement;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.base.ComponentAnnotation.subcomponentAnnotation;
import static dagger.internal.codegen.base.Scopes.productionScope;
import static dagger.internal.codegen.base.Scopes.scopesOf;
import static dagger.internal.codegen.binding.ComponentCreatorAnnotation.creatorAnnotationsFor;
import static dagger.internal.codegen.binding.ComponentDescriptor.isComponentContributionMethod;
import static dagger.internal.codegen.binding.ConfigurationAnnotations.enclosedAnnotatedTypes;
import static dagger.internal.codegen.binding.ConfigurationAnnotations.isSubcomponentCreator;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.VOID;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * A factory for {@link ComponentDescriptor}s.
 */
public final class ComponentDescriptorFactory {
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final DependencyRequestFactory dependencyRequestFactory;
    private final ModuleDescriptor.Factory moduleDescriptorFactory;
    private final InjectionAnnotations injectionAnnotations;

    @Inject
    ComponentDescriptorFactory(
            DaggerElements elements,
            DaggerTypes types,
            DependencyRequestFactory dependencyRequestFactory,
            ModuleDescriptor.Factory moduleDescriptorFactory,
            InjectionAnnotations injectionAnnotations
    ) {
        this.elements = elements;
        this.types = types;
        this.dependencyRequestFactory = dependencyRequestFactory;
        this.moduleDescriptorFactory = moduleDescriptorFactory;
        this.injectionAnnotations = injectionAnnotations;
    }

    /**
     * Returns a descriptor for a root component type.
     */
    public ComponentDescriptor rootComponentDescriptor(TypeElement typeElement) {
        return create(
                typeElement,
                checkAnnotation(
                        typeElement,
                        ComponentAnnotation::rootComponentAnnotation,
                        "must have a component annotation"));
    }

    /**
     * Returns a descriptor for a subcomponent type.
     */
    public ComponentDescriptor subcomponentDescriptor(TypeElement typeElement) {
        return create(
                typeElement,
                checkAnnotation(
                        typeElement,
                        ComponentAnnotation::subcomponentAnnotation,
                        "must have a subcomponent annotation"));
    }

    /**
     * Returns a descriptor for a fictional component based on a module type in order to validate its
     * bindings.
     * <p>
     * 返回基于module类型的虚构component的描述符以验证其绑定。
     */
    public ComponentDescriptor moduleComponentDescriptor(TypeElement typeElement) {
        return create(
                typeElement,
                //module类使用的注解生成一个FictionalComponentAnnotation对象
                ComponentAnnotation.fromModuleAnnotation(
                        checkAnnotation(
                                typeElement, ModuleAnnotation::moduleAnnotation, "must have a module annotation")));
    }

    private static <A> A checkAnnotation(
            TypeElement typeElement,
            Function<TypeElement, Optional<A>> annotationFunction,
            String message) {
        return annotationFunction
                .apply(typeElement)
                .orElseThrow(() -> new IllegalArgumentException(typeElement + " " + message));
    }

    //从这里慢慢理解，直到graph里面的细节部分
    //e.g.A:typeElement表示module类，ComponentAnnotation表示module类使用的Module注解生成的FictionalComponentAnnotation对象
    private ComponentDescriptor create(
            TypeElement typeElement,
            ComponentAnnotation componentAnnotation
    ) {
        //Component#dendencies里面的元素集合转换成DEPENDENCY类型的ComponentRequirement
        //e.g.A:针对module类，该集合为空
        ImmutableSet<ComponentRequirement> componentDependencies =
                componentAnnotation.dependencyTypes().stream()
                        .map(ComponentRequirement::forDependency)
                        .collect(toImmutableSet());

        //收集component#dependencies里面的类的所有无参返回类型不是void的方法
        // K：该方法，V：该方法所在的dependencies类生成的类型为DEPENDENCY的ComponentRequirement对象
        ImmutableMap.Builder<ExecutableElement, ComponentRequirement> dependenciesByDependencyMethod =
                ImmutableMap.builder();

        //遍历componentDependencies
        for (ComponentRequirement componentDependency : componentDependencies) {
            //Component注解的dendencies里面的元素的所有方法
            for (ExecutableElement dependencyMethod :
                    methodsIn(elements.getAllMembers(componentDependency.typeElement()))) {
                //方法无参，返回类型不是void
                if (isComponentContributionMethod(elements, dependencyMethod)) {
                    dependenciesByDependencyMethod.put(dependencyMethod, componentDependency);
                }
            }
        }

        // Start with the component's modules. For fictional components built from a module, start with
        // that module.
        //如果是真的component注解((sub)component类），那么收集component#modules里面的module类；如果是module类，那么直接使用当前module类节点
        ImmutableSet<TypeElement> modules =
                componentAnnotation.isRealComponent()
                        ? componentAnnotation.modules()
                        : ImmutableSet.of(typeElement);

        //对modules收集的module类生成ModuleDescriptor集合：这里的ModuleDescropgtor对象集合当然也包含，moduleAnnotation#includes里面的module类
        ImmutableSet<ModuleDescriptor> transitiveModules =
                moduleDescriptorFactory.transitiveModules(modules);

        //moduleAnnotation#subcomponent里面的所有subcomponent类生成ComponentDescriptor对象并且收集在subcomponentFormModules集合中
        ImmutableSet.Builder<ComponentDescriptor> subcomponentsFromModules = ImmutableSet.builder();
        for (ModuleDescriptor module : transitiveModules) {
            for (SubcomponentDeclaration subcomponentDeclaration : module.subcomponentDeclarations()) {
                TypeElement subcomponent = subcomponentDeclaration.subcomponentType();
                subcomponentsFromModules.add(subcomponentDescriptor(subcomponent));
            }
        }

        //存放非private非static abstract修饰的方法生成的ComponentMethodDescriptor对象
        ImmutableSet.Builder<ComponentDescriptor.ComponentMethodDescriptor> componentMethodsBuilder =
                ImmutableSet.builder();

        //componentAll节点上返回类型是subcomponent节点的方法，
        // K：返回类型是subcomponent节点的componentMethod方法生成的ComponentMethodDescriptor对象，
        // V：当前方法返回类型subcomponent节点生成的ComponentDescriptor对象；
        ImmutableBiMap.Builder<ComponentDescriptor.ComponentMethodDescriptor, ComponentDescriptor>
                subcomponentsByFactoryMethod = ImmutableBiMap.builder();

        //返回类型是subcomponent.creator节点(Builder)的componentMethod方法，
        // K：返回类型是subcomponent.creator节点的componentMethod方法生成ComponentMethodDescriptor对象，
        // V：componentMethod方法的返回类型subcomponent节点生成的ComponentDescriptor对象
        ImmutableBiMap.Builder<ComponentDescriptor.ComponentMethodDescriptor, ComponentDescriptor>
                subcomponentsByBuilderMethod = ImmutableBiMap.builder();

        //如果是一个RealComponentAnnotation对象（不是module类）
        if (componentAnnotation.isRealComponent()) {

            //非private、非static、abstract修饰的方法
            ImmutableSet<ExecutableElement> unimplementedMethods =
                    elements.getUnimplementedMethods(typeElement);

            for (ExecutableElement componentMethod : unimplementedMethods) {

                //遍历unimplementedMethods对里面的item生成ComponentMethodDescriptor对象
                ComponentDescriptor.ComponentMethodDescriptor componentMethodDescriptor =
                        getDescriptorForComponentMethod(typeElement, componentAnnotation, componentMethod);

                componentMethodsBuilder.add(componentMethodDescriptor);
                componentMethodDescriptor
                        .subcomponent()
                        .ifPresent(
                                subcomponent -> {
                                    // If the dependency request is present, that means the method returns the
                                    // subcomponent factory.
                                    //当前返回类型是subcomponent.creator
                                    if (componentMethodDescriptor.dependencyRequest().isPresent()) {
                                        subcomponentsByBuilderMethod.put(componentMethodDescriptor, subcomponent);
                                    } else {
                                        //表示方法返回类型是一个subcomponent类
                                        subcomponentsByFactoryMethod.put(componentMethodDescriptor, subcomponent);
                                    }
                                });
            }

        }

        // Validation should have ensured that this set will have at most one element.
        //收集(sub)component类中的creator类
        ImmutableSet<DeclaredType> enclosedCreators =
                creatorAnnotationsFor(componentAnnotation).stream()
                        .flatMap(
                                creatorAnnotation ->
                                        enclosedAnnotatedTypes(typeElement, creatorAnnotation).stream())
                        .collect(toImmutableSet());

        //(sub)component类的creator类生成ComponentCreatorDescriptor对象
        Optional<ComponentCreatorDescriptor> creatorDescriptor =
                enclosedCreators.isEmpty()
                        ? Optional.empty()
                        : Optional.of(
                        ComponentCreatorDescriptor.create(
                                getOnlyElement(enclosedCreators), elements, types, dependencyRequestFactory));

        //使用了Scope修饰的注解修饰当前typeElement节点
        ImmutableSet<Scope> scopes = scopesOf(typeElement);
        if (componentAnnotation.isProduction()) {
            scopes = ImmutableSet.<Scope>builder().addAll(scopes).add(productionScope(elements)).build();
        }

        return new AutoValue_ComponentDescriptor(
                componentAnnotation,//component类上的注解
                typeElement,//component类（如果是假的component表示的其实是module类）
                componentDependencies,//component#dependencies里面类生成DEPENDENCY类型的ComponentRequirement对象集合
                transitiveModules,//component#modules里面的module，以及该module使用的module#include里面的module，这些module生成的ModuleDescriptor集合
                dependenciesByDependencyMethod.build(),//收集component#dependencies里面的类的所有无参返回类型不是void的方法
                scopes,//使用的scope注解修饰的注解
                subcomponentsFromModules.build(),//（component关联的module或module本身）moduleAnnotaton#subcomponent里面的所有subcomponent类生成ComponentDescriptor对象
                subcomponentsByFactoryMethod.build(),//方法返回类型是一个subcomponent节点
                subcomponentsByBuilderMethod.build(),//方法返回类型是一个subcomonent.creator节点
                componentMethodsBuilder.build(),//存放非private非static abstract修饰的方法生成的ComponentMethodDescriptor对象
                creatorDescriptor);//(sub)component类的creator类生成ComponentCreatorDescriptor对象
    }

    //对component类中的非private，非static并且使用abstract修饰的方法生成ComponentMethodDescriptor对象
    private ComponentDescriptor.ComponentMethodDescriptor getDescriptorForComponentMethod(
            TypeElement componentElement,
            ComponentAnnotation componentAnnotation,
            ExecutableElement componentMethod) {

        //方法节点
        ComponentDescriptor.ComponentMethodDescriptor.Builder descriptor =
                ComponentDescriptor.ComponentMethodDescriptor.builder(componentMethod);

        ExecutableType resolvedComponentMethod =
                MoreTypes.asExecutable(
                        types.asMemberOf(MoreTypes.asDeclared(componentElement.asType()), componentMethod));

        TypeMirror returnType = resolvedComponentMethod.getReturnType();

        //返回类型是一个类或接口，并且该方法没有使用Qualifier修饰的注解修饰
        if (returnType.getKind().equals(DECLARED)
                && !injectionAnnotations.getQualifier(componentMethod).isPresent()) {

            TypeElement returnTypeElement = asTypeElement(returnType);

            //如果返回类型是一个subcomponent类，当前ComponentMethodDescriptor对象添加subcomponent类型的ComponentDescriptor对象
            if (subcomponentAnnotation(returnTypeElement).isPresent()) {
                // It's a subcomponent factory method. There is no dependency request, and there could be
                // any number of parameters. Just return the descriptor.
                return descriptor.subcomponent(subcomponentDescriptor(returnTypeElement)).build();
            }
            //如果返回类型是一个subcomponent类里面的creator类型，那么subcompnent里面的ComponentDescriptor对象表示该subcomponent生成的ComponentMethodDescriptor对象
            if (isSubcomponentCreator(returnTypeElement)) {
                descriptor.subcomponent(
                        subcomponentDescriptor(asType(returnTypeElement.getEnclosingElement())));
            }
        }

        //ComponentMethodDescriptor对象添加依赖
        switch (componentMethod.getParameters().size()) {
            case 0://如果方法参数为0，返回类型不能为void。对方法节点和方法类型生成依赖
                checkArgument(
                        !returnType.getKind().equals(VOID),
                        "component method cannot be void: %s",
                        componentMethod);

                descriptor.dependencyRequest(
                        componentAnnotation.isProduction()
                                ? dependencyRequestFactory.forComponentProductionMethod(
                                componentMethod, resolvedComponentMethod)
                                : dependencyRequestFactory.forComponentProvisionMethod(
                                componentMethod, resolvedComponentMethod));
                break;

            case 1://如果参数有一个，该方法作为成员注入当前component组件，返回类型是void或者参数类型和方法返回类型必须一致。方法节点和方法类型生成一个成员注入依赖
                checkArgument(
                        returnType.getKind().equals(VOID)
                                || MoreTypes.equivalence()
                                .equivalent(returnType, resolvedComponentMethod.getParameterTypes().get(0)),
                        "members injection method must return void or parameter type: %s",
                        componentMethod);
                descriptor.dependencyRequest(
                        dependencyRequestFactory.forComponentMembersInjectionMethod(
                                componentMethod, resolvedComponentMethod));
                break;

            default:
                throw new IllegalArgumentException(
                        "component method has too many parameters: " + componentMethod);
        }

        return descriptor.build();
    }
}
