package dagger.hilt.android.processor.internal.androidentrypoint;


import com.google.common.base.Preconditions;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import dagger.hilt.android.processor.internal.AndroidClassNames;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;

import static com.google.common.collect.Iterables.getOnlyElement;
import static dagger.internal.codegen.extension.DaggerCollectors.toOptional;
import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * Helper class for writing Hilt generators.
 */
final class Generators {

    static void addGeneratedBaseClassJavadoc(TypeSpec.Builder builder, ClassName annotation) {
        builder.addJavadoc("A generated base class to be extended by the @$T annotated class. If using"
                        + " the Gradle plugin, this is swapped as the base class via bytecode transformation.",
                annotation);
    }

    /**
     * Copies all constructors with arguments to the builder.
     */
    static void copyConstructors(TypeElement baseClass, TypeSpec.Builder builder) {
        copyConstructors(baseClass, CodeBlock.builder().build(), builder);
    }

    /**
     * Copies all constructors with arguments along with an appended body to the builder.
     */
    static void copyConstructors(TypeElement baseClass, CodeBlock body, TypeSpec.Builder builder) {
        List<ExecutableElement> constructors =
                ElementFilter.constructorsIn(baseClass.getEnclosedElements())
                        .stream()
                        .filter(constructor -> !constructor.getModifiers().contains(PRIVATE))
                        .collect(Collectors.toList());

        if (constructors.size() == 1
                && getOnlyElement(constructors).getParameters().isEmpty()
                && body.isEmpty()) {
            // No need to copy the constructor if the default constructor will handle it.
            return;
        }

        constructors.forEach(constructor -> builder.addMethod(copyConstructor(constructor, body)));
    }

    /**
     * Returns Optional with AnnotationSpec for Nullable if found on element, empty otherwise.
     */
    private static Optional<AnnotationSpec> getNullableAnnotationSpec(Element element) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (annotationMirror
                    .getAnnotationType()
                    .asElement()
                    .getSimpleName()
                    .contentEquals("Nullable")) {
                AnnotationSpec annotationSpec = AnnotationSpec.get(annotationMirror);
                // If using the android internal Nullable, convert it to the externally-visible version.
                return AndroidClassNames.NULLABLE_INTERNAL.equals(annotationSpec.type)
                        ? Optional.of(AnnotationSpec.builder(AndroidClassNames.NULLABLE).build())
                        : Optional.of(annotationSpec);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns a ParameterSpec of the input parameter, @Nullable annotated if existing in original
     * (this does not handle Nullable type annotations).
     */
    private static ParameterSpec getParameterSpecWithNullable(VariableElement parameter) {
        ParameterSpec.Builder builder = ParameterSpec.get(parameter).toBuilder();
        //如果参数使用了@Nullable注解，那么将该注解添加到新的构造函数中，其他注解不用理会
        getNullableAnnotationSpec(parameter).ifPresent(builder::addAnnotation);
        return builder.build();
    }

    /**
     * Returns a {@link MethodSpec} for a constructor matching the given {@link ExecutableElement}
     * constructor signature, and just calls super. If the constructor is
     * {@link android.annotation.TargetApi} guarded, adds the TargetApi as well.
     */
    // Example:
    //   Foo(Param1 param1, Param2 param2) {
    //     super(param1, param2);
    //   }
    static MethodSpec copyConstructor(ExecutableElement constructor) {
        return copyConstructor(constructor, CodeBlock.builder().build());
    }

    private static MethodSpec copyConstructor(ExecutableElement constructor, CodeBlock body) {
        List<ParameterSpec> params =
                constructor.getParameters().stream()
                        .map(parameter -> getParameterSpecWithNullable(parameter))
                        .collect(Collectors.toList());

        final MethodSpec.Builder builder =
                MethodSpec.constructorBuilder()
                        .addParameters(params)
                        .addStatement(
                                "super($L)",
                                params.stream().map(param -> param.name).collect(Collectors.joining(", ")))
                        .addCode(body);

        constructor.getAnnotationMirrors().stream()
                .filter(a -> Processors.hasAnnotation(a, AndroidClassNames.TARGET_API))
                .collect(toOptional())
                .map(AnnotationSpec::get)
                .ifPresent(builder::addAnnotation);

        return builder.build();
    }

    /**
     * Copies the Android lint annotations from the annotated element to the generated element.
     *
     * <p>Note: For now we only copy over {@link android.annotation.TargetApi}.
     */
    static void copyLintAnnotations(Element element, TypeSpec.Builder builder) {
        if (Processors.hasAnnotation(element, AndroidClassNames.TARGET_API)) {
            builder.addAnnotation(
                    AnnotationSpec.get(
                            Processors.getAnnotationMirror(element, AndroidClassNames.TARGET_API)));
        }
    }

    // @Override
    // public final Object generatedComponent() {
    //   return this.componentManager().generatedComponent();
    // }
    static void addComponentOverride(AndroidEntryPointMetadata metadata, TypeSpec.Builder builder) {
        if (metadata.overridesAndroidEntryPointClass()) {
            // We don't need to override this method if we are extending a Hilt type.
            return;
        }
        //继承GeneratedComponentManagerHolder接口，并且实现generatedComponent方法
        builder
                .addSuperinterface(ClassNames.GENERATED_COMPONENT_MANAGER_HOLDER)
                .addMethod(
                        MethodSpec.methodBuilder("generatedComponent")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                .returns(TypeName.OBJECT)
                                .addStatement("return $L.generatedComponent()", componentManagerCallBlock(metadata))
                                .build());
    }

    /**
     * Adds the inject() and optionally the componentManager() methods to allow for injection.
     */
    static void addInjectionMethods(AndroidEntryPointMetadata metadata, TypeSpec.Builder builder) {
        switch (metadata.androidType()) {
            case ACTIVITY:
            case FRAGMENT:
            case VIEW:
            case SERVICE:
                addComponentManagerMethods(metadata, builder);
                // fall through
            case BROADCAST_RECEIVER:
                addInjectMethod(metadata, builder);
                break;
            default:
                throw new AssertionError();
        }
    }

    // @Override
    // public FragmentComponentManager componentManager() {
    //   if (componentManager == null) {
    //     synchronize (componentManagerLock) {
    //       if (componentManager == null) {
    //         componentManager = createComponentManager();
    //       }
    //     }
    //   }
    //   return componentManager;
    // }
    private static void addComponentManagerMethods(
            AndroidEntryPointMetadata metadata, TypeSpec.Builder typeSpecBuilder) {
        if (metadata.overridesAndroidEntryPointClass()) {
            // We don't need to override this method if we are extending a Hilt type.
            return;
        }
        //e.g. ActivityComponentManager componentManager
        ParameterSpec managerParam = metadata.componentManagerParam();
        //private volatile ComponentManager componentManager;
        typeSpecBuilder.addField(componentManagerField(metadata));

        typeSpecBuilder.addMethod(createComponentManagerMethod(metadata));

        //实现componentManager方法
        MethodSpec.Builder methodSpecBuilder =
                MethodSpec.methodBuilder("componentManager")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(managerParam.type)
                        .beginControlFlow("if ($N == null)", managerParam);

        // Views do not do double-checked locking because this is called from the constructor
        if (metadata.androidType() != AndroidEntryPointMetadata.AndroidType.VIEW) {
            typeSpecBuilder.addField(componentManagerLockField());

            methodSpecBuilder
                    .beginControlFlow("synchronized (componentManagerLock)")
                    .beginControlFlow("if ($N == null)", managerParam);
        }

        methodSpecBuilder
                .addStatement("$N = createComponentManager()", managerParam)
                .endControlFlow();

        if (metadata.androidType() != AndroidEntryPointMetadata.AndroidType.VIEW) {
            methodSpecBuilder
                    .endControlFlow()
                    .endControlFlow();
        }

        methodSpecBuilder.addStatement("return $N", managerParam);

        typeSpecBuilder.addMethod(methodSpecBuilder.build());
    }

    // protected ActivityComponentManager createComponentManager() {
    //   return new ActivityComponentManager(this);
    // }
    private static MethodSpec createComponentManagerMethod(AndroidEntryPointMetadata metadata) {
        Preconditions.checkState(
                metadata.componentManagerInitArgs().isPresent(),
                "This method should not have been called for metadata where the init args are not"
                        + " present.");
        return MethodSpec.methodBuilder("createComponentManager")
                .addModifiers(Modifier.PROTECTED)
                .returns(metadata.componentManager())
                .addStatement(
                        "return new $T($L)",
                        metadata.componentManager(),
                        metadata.componentManagerInitArgs().get())
                .build();
    }

    // private volatile ComponentManager componentManager;
    private static FieldSpec componentManagerField(AndroidEntryPointMetadata metadata) {
        ParameterSpec managerParam = metadata.componentManagerParam();
        FieldSpec.Builder builder = FieldSpec.builder(managerParam.type, managerParam.name)
                .addModifiers(Modifier.PRIVATE);

        // Views do not need volatile since these are set in the constructor if ever set.
        if (metadata.androidType() != AndroidEntryPointMetadata.AndroidType.VIEW) {
            builder.addModifiers(Modifier.VOLATILE);
        }

        return builder.build();
    }

    // private final Object componentManagerLock = new Object();
    private static FieldSpec componentManagerLockField() {
        return FieldSpec.builder(TypeName.get(Object.class), "componentManagerLock")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new Object()")
                .build();
    }

    // protected void inject() {
    //   if (!injected) {
    //     (($CLASS_GeneratedInjector)this.generatedComponent()).inject$CLASS(($CLASS) this);
    //     injected = true;
    //   }
    // }
    private static void addInjectMethod(
            AndroidEntryPointMetadata metadata, TypeSpec.Builder typeSpecBuilder) {
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PROTECTED);

        // Check if the parent is a Hilt type. If it isn't or if it is but it
        // wasn't injected by hilt, then return.
        // Object parent = ...depends on type...
        // if (!(parent instanceof GeneratedComponentManager)
        //     || ((parent instanceof InjectedByHilt) &&
        //         !((InjectedByHilt) parent).wasInjectedByHilt())) {
        //   return;
        //
        if (metadata.allowsOptionalInjection()) {
            methodSpecBuilder
                    .addStatement("$T parent = $L", ClassNames.OBJECT, getParentCodeBlock(metadata))
                    .beginControlFlow(
                            "if (!(parent instanceof $T) "
                                    + "|| ((parent instanceof $T) && !(($T) parent).wasInjectedByHilt()))",
                            ClassNames.GENERATED_COMPONENT_MANAGER,
                            AndroidClassNames.INJECTED_BY_HILT,
                            AndroidClassNames.INJECTED_BY_HILT)
                    .addStatement("return")
                    .endControlFlow();
        }

        // Only add @Override if an ancestor extends a generated Hilt class.
        // When using bytecode injection, this isn't always guaranteed.
        if (metadata.overridesAndroidEntryPointClass()
                && ancestorExtendsGeneratedHiltClass(metadata)) {
            methodSpecBuilder.addAnnotation(Override.class);
        }
        typeSpecBuilder.addField(injectedField(metadata));

        switch (metadata.androidType()) {
            case ACTIVITY:
            case FRAGMENT:
            case VIEW:
            case SERVICE:
                methodSpecBuilder
                        .beginControlFlow("if (!injected)")
                        .addStatement("injected = true")
                        .addStatement(
                                "(($T) $L).$L($L)",
                                metadata.injectorClassName(),
                                generatedComponentCallBlock(metadata),
                                metadata.injectMethodName(),
                                unsafeCastThisTo(metadata.elementClassName()))
                        .endControlFlow();
                break;
            case BROADCAST_RECEIVER:

//                private final Object injectedLock = new Object();
//                protected void inject(Context context) {
//                    if (!injected) {
//                        synchronized (injectedLock){
//                            if (!injected){
//                                (($CLASS_GeneratedInjector)BroadcastReceiverComponentManager.generatedComponent(context)).inject$CLASS(($CLASS) this);
//                            }
//                        }
//
//                        injected = true;
//                    }
//                }

                typeSpecBuilder.addField(injectedLockField());

                methodSpecBuilder
                        .addParameter(ParameterSpec.builder(AndroidClassNames.CONTEXT, "context").build())
                        .beginControlFlow("if (!injected)")
                        .beginControlFlow("synchronized (injectedLock)")
                        .beginControlFlow("if (!injected)")
                        .addStatement(
                                "(($T) $T.generatedComponent(context)).$L($L)",
                                metadata.injectorClassName(),
                                metadata.componentManager(),
                                metadata.injectMethodName(),
                                unsafeCastThisTo(metadata.elementClassName()))
                        .addStatement("injected = true")
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow();
                break;
            default:
                throw new AssertionError();
        }

        // Also add a wasInjectedByHilt method if needed.
        // Even if we aren't optionally injected, if we override an optionally injected Hilt class
        // we also need to override the wasInjectedByHilt method.
        if (metadata.allowsOptionalInjection() || metadata.baseAllowsOptionalInjection()) {
            typeSpecBuilder.addMethod(
                    MethodSpec.methodBuilder("wasInjectedByHilt")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(boolean.class)
                            .addStatement("return injected")
                            .build());
            // Only add the interface though if this class allows optional injection (not that it
            // really matters since if the base allows optional injection the class implements the
            // interface anyway). But it is probably better to be consistent about only optionally
            // injected classes extend the interface.
            if (metadata.allowsOptionalInjection()) {
                typeSpecBuilder.addSuperinterface(AndroidClassNames.INJECTED_BY_HILT);
            }
        }

        typeSpecBuilder.addMethod(methodSpecBuilder.build());
    }

    private static CodeBlock getParentCodeBlock(AndroidEntryPointMetadata metadata) {
        switch (metadata.androidType()) {
            case ACTIVITY:
            case SERVICE:
                return CodeBlock.of("$T.getApplication(getApplicationContext())", ClassNames.CONTEXTS);
            case FRAGMENT:
                return CodeBlock.of("getHost()");
            case VIEW:
                return CodeBlock.of(
                        "$L.maybeGetParentComponentManager()", componentManagerCallBlock(metadata));
            case BROADCAST_RECEIVER:
                // Broadcast receivers receive a "context" parameter
                return CodeBlock.of(
                        "$T.getApplication(context.getApplicationContext())",
                        ClassNames.CONTEXTS);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns the call to {@code generatedComponent()} with casts if needed.
     *
     * <p>A cast is required when the root generated Hilt class uses bytecode injection because
     * subclasses won't have access to the {@code generatedComponent()} method in that case.
     */
    private static CodeBlock generatedComponentCallBlock(AndroidEntryPointMetadata metadata) {
        return CodeBlock.of(
                "$L.generatedComponent()",
                !metadata.isRootMetadata() && metadata.rootMetadata().requiresBytecodeInjection()
                        ? unsafeCastThisTo(ClassNames.GENERATED_COMPONENT_MANAGER_HOLDER)
                        : "this");
    }

    /**
     * Returns the call to {@code componentManager()} with casts if needed.
     *
     * <p>A cast is required when the root generated Hilt class uses bytecode injection because
     * subclasses won't have access to the {@code componentManager()} method in that case.
     */
    private static CodeBlock componentManagerCallBlock(AndroidEntryPointMetadata metadata) {
        return CodeBlock.of(
                "$L.componentManager()",
                !metadata.isRootMetadata() && metadata.rootMetadata().requiresBytecodeInjection()
                        ? unsafeCastThisTo(ClassNames.GENERATED_COMPONENT_MANAGER_HOLDER)
                        : "this");
    }

    static CodeBlock unsafeCastThisTo(ClassName castType) {
        return CodeBlock.of("$T.<$T>unsafeCast(this)", ClassNames.UNSAFE_CASTS, castType);
    }

    /**
     * Returns {@code true} if the an ancestor annotated class extends the generated class
     */
    private static boolean ancestorExtendsGeneratedHiltClass(AndroidEntryPointMetadata metadata) {
        while (metadata.baseMetadata().isPresent()) {
            metadata = metadata.baseMetadata().get();
            if (!metadata.requiresBytecodeInjection()) {
                return true;
            }
        }
        return false;
    }

    // private boolean injected = false;
    private static FieldSpec injectedField(AndroidEntryPointMetadata metadata) {
        FieldSpec.Builder builder = FieldSpec.builder(TypeName.BOOLEAN, "injected")
                .addModifiers(Modifier.PRIVATE);

        // Broadcast receivers do double-checked locking so this needs to be volatile
        if (metadata.androidType() == AndroidEntryPointMetadata.AndroidType.BROADCAST_RECEIVER) {
            builder.addModifiers(Modifier.VOLATILE);
        }

        // Views should not add an initializer here as this runs after the super constructor
        // and may reset state set during the super constructor call.
        if (metadata.androidType() != AndroidEntryPointMetadata.AndroidType.VIEW) {
            builder.initializer("false");
        }
        return builder.build();
    }

    // private final Object injectedLock = new Object();
    private static FieldSpec injectedLockField() {
        return FieldSpec.builder(TypeName.OBJECT, "injectedLock")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", TypeName.OBJECT)
                .build();
    }

    /**
     * Adds the SupressWarnings to supress a warning in the generated code.
     *
     * @param keys the string keys of the warnings to suppress, e.g. 'deprecation', 'unchecked', etc.
     */
    public static void addSuppressAnnotation(TypeSpec.Builder builder, String... keys) {
        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(SuppressWarnings.class);
        for (String key : keys) {
            annotationBuilder.addMember("value", "$S", key);
        }
        builder.addAnnotation(annotationBuilder.build());
    }

    private Generators() {
    }
}
