package dagger.internal.codegen.componentgenerator;

import com.google.auto.common.MoreTypes;
import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import androidx.room.compiler.processing.XFiler;
import dagger.BindsInstance;
import dagger.internal.codegen.base.SourceFileGenerator;
import dagger.internal.codegen.binding.ComponentCreatorDescriptor;
import dagger.internal.codegen.binding.ComponentCreatorKind;
import dagger.internal.codegen.binding.ComponentDescriptor;
import dagger.internal.codegen.binding.ComponentRequirement;
import dagger.internal.codegen.binding.MethodSignature;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.producers.internal.CancellationListener;

import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.Preconditions.checkArgument;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static dagger.internal.codegen.binding.ComponentCreatorKind.BUILDER;
import static dagger.internal.codegen.javapoet.TypeSpecs.addSupertype;
import static dagger.internal.codegen.langmodel.Accessibility.isElementAccessibleFrom;
import static dagger.internal.codegen.writing.ComponentNames.getRootComponentClassName;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

final class ComponentHjarGenerator extends SourceFileGenerator<ComponentDescriptor> {
    private final DaggerElements elements;
    private final DaggerTypes types;
    private final KotlinMetadataUtil metadataUtil;

    @Inject
    ComponentHjarGenerator(
            XFiler filer,
            DaggerElements elements,
            DaggerTypes types,
            SourceVersion sourceVersion,
            KotlinMetadataUtil metadataUtil) {
        super(filer, elements, sourceVersion);
        this.elements = elements;
        this.types = types;
        this.metadataUtil = metadataUtil;
    }

    @Override
    public Element originatingElement(ComponentDescriptor input) {
        return input.typeElement();
    }

    @Override
    public ImmutableList<TypeSpec.Builder> topLevelTypes(ComponentDescriptor componentDescriptor) {
        ClassName generatedTypeName = getRootComponentClassName(componentDescriptor);
        TypeSpec.Builder generatedComponent =
                TypeSpec.classBuilder(generatedTypeName)
                        .addModifiers(FINAL)
                        .addMethod(privateConstructor());
        if (componentDescriptor.typeElement().getModifiers().contains(PUBLIC)) {
            generatedComponent.addModifiers(PUBLIC);
        }

        TypeElement componentElement = componentDescriptor.typeElement();
        addSupertype(generatedComponent, componentElement);

        TypeName builderMethodReturnType;
        ComponentCreatorKind creatorKind;
        boolean noArgFactoryMethod;
        if (componentDescriptor.creatorDescriptor().isPresent()) {
            ComponentCreatorDescriptor creatorDescriptor = componentDescriptor.creatorDescriptor().get();
            builderMethodReturnType = ClassName.get(creatorDescriptor.typeElement());
            creatorKind = creatorDescriptor.kind();
            noArgFactoryMethod = creatorDescriptor.factoryParameters().isEmpty();
        } else {
            TypeSpec.Builder builder =
                    TypeSpec.classBuilder("Builder")
                            .addModifiers(STATIC, FINAL)
                            .addMethod(privateConstructor());
            if (componentDescriptor.typeElement().getModifiers().contains(PUBLIC)) {
                builder.addModifiers(PUBLIC);
            }

            ClassName builderClassName = generatedTypeName.nestedClass("Builder");
            builderMethodReturnType = builderClassName;
            creatorKind = BUILDER;
            noArgFactoryMethod = true;
            componentRequirements(componentDescriptor)
                    .map(requirement -> builderSetterMethod(requirement.typeElement(), builderClassName))
                    .forEach(builder::addMethod);
            builder.addMethod(builderBuildMethod(componentDescriptor));
            generatedComponent.addType(builder.build());
        }

        generatedComponent.addMethod(staticCreatorMethod(builderMethodReturnType, creatorKind));

        if (noArgFactoryMethod
                && !hasBindsInstanceMethods(componentDescriptor)
                && componentRequirements(componentDescriptor)
                .noneMatch(
                        requirement -> requirement.requiresAPassedInstance(elements, metadataUtil))) {
            generatedComponent.addMethod(createMethod(componentDescriptor));
        }

        DeclaredType componentType = MoreTypes.asDeclared(componentElement.asType());
        // TODO(ronshapiro): unify with ComponentImplementationBuilder
        Set<MethodSignature> methodSignatures =
                Sets.newHashSetWithExpectedSize(componentDescriptor.componentMethods().size());
        componentDescriptor.componentMethods().stream()
                .filter(
                        method -> {
                            return methodSignatures.add(
                                    MethodSignature.forComponentMethod(method, componentType, types));
                        })
                .forEach(
                        method ->
                                generatedComponent.addMethod(
                                        emptyComponentMethod(componentElement, method.methodElement())));

        if (componentDescriptor.isProduction()) {
            generatedComponent
                    .addSuperinterface(ClassName.get(CancellationListener.class))
                    .addMethod(onProducerFutureCancelledMethod());
        }

        return ImmutableList.of(generatedComponent);
    }

    private MethodSpec emptyComponentMethod(TypeElement typeElement, ExecutableElement baseMethod) {
        return MethodSpec.overriding(baseMethod, MoreTypes.asDeclared(typeElement.asType()), types)
                .build();
    }

    private static MethodSpec privateConstructor() {
        return constructorBuilder().addModifiers(PRIVATE).build();
    }

    /**
     * Returns the {@link ComponentRequirement}s for a component that does not have a {@link
     * ComponentDescriptor#creatorDescriptor()}.
     */
    private static Stream<ComponentRequirement> componentRequirements(ComponentDescriptor component) {
        // TODO(b/152802759): See if you can merge logics that normal component processing and hjar
        // component processing use. So that there would't be a duplicated logic (like the lines below)
        // everytime we modify the generated code for the component.
        checkArgument(!component.isSubcomponent());
        return Stream.concat(
                component.dependencies().stream(),
                component.modules().stream()
                        .filter(
                                module ->
                                        !module.moduleElement().getModifiers().contains(ABSTRACT)
                                                && isElementAccessibleFrom(
                                                module.moduleElement(),
                                                ClassName.get(component.typeElement()).packageName()))
                        .map(module -> ComponentRequirement.forModule(module.moduleElement().asType())));
    }

    private boolean hasBindsInstanceMethods(ComponentDescriptor componentDescriptor) {
        return componentDescriptor.creatorDescriptor().isPresent()
                && elements
                .getUnimplementedMethods(componentDescriptor.creatorDescriptor().get().typeElement())
                .stream()
                .anyMatch(method -> isBindsInstance(method));
    }

    private static boolean isBindsInstance(ExecutableElement method) {
        if (isAnnotationPresent(method, BindsInstance.class)) {
            return true;
        }

        if (method.getParameters().size() == 1) {
            return isAnnotationPresent(method.getParameters().get(0), BindsInstance.class);
        }

        return false;
    }

    private static MethodSpec builderSetterMethod(
            TypeElement componentRequirement, ClassName builderClass) {
        String simpleName =
                UPPER_CAMEL.to(LOWER_CAMEL, componentRequirement.getSimpleName().toString());
        return MethodSpec.methodBuilder(simpleName)
                .addModifiers(PUBLIC)
                .addParameter(ClassName.get(componentRequirement), simpleName)
                .returns(builderClass)
                .build();
    }

    private static MethodSpec builderBuildMethod(ComponentDescriptor component) {
        return MethodSpec.methodBuilder("build")
                .addModifiers(PUBLIC)
                .returns(ClassName.get(component.typeElement()))
                .build();
    }

    private static MethodSpec staticCreatorMethod(
            TypeName creatorMethodReturnType, ComponentCreatorKind creatorKind) {
        return MethodSpec.methodBuilder(Ascii.toLowerCase(creatorKind.typeName()))
                .addModifiers(PUBLIC, STATIC)
                .returns(creatorMethodReturnType)
                .build();
    }

    private static MethodSpec createMethod(ComponentDescriptor componentDescriptor) {
        return MethodSpec.methodBuilder("create")
                .addModifiers(PUBLIC, STATIC)
                .returns(ClassName.get(componentDescriptor.typeElement()))
                .build();
    }

    private static MethodSpec onProducerFutureCancelledMethod() {
        return MethodSpec.methodBuilder("onProducerFutureCancelled")
                .addModifiers(PUBLIC)
                .addParameter(TypeName.BOOLEAN, "mayInterruptIfRunning")
                .build();
    }
}
