package dagger.hilt.android.processor.internal.androidentrypoint;


import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import dagger.hilt.android.processor.internal.AndroidClassNames;
import dagger.hilt.processor.internal.BadInputException;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Components;
import dagger.hilt.processor.internal.KotlinMetadataUtils;
import dagger.hilt.processor.internal.ProcessorErrors;
import dagger.hilt.processor.internal.Processors;
import dagger.internal.codegen.kotlin.KotlinMetadataUtil;

import static dagger.hilt.processor.internal.HiltCompilerOptions.isAndroidSuperclassValidationDisabled;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;

/**
 * Metadata class for @AndroidEntryPoint annotated classes.
 */
@AutoValue
public abstract class AndroidEntryPointMetadata {

    /**
     * The class {@link Element} annotated with @AndroidEntryPoint.
     */
    public abstract TypeElement element();

    /**
     * The base class {@link Element} given to @AndroidEntryPoint.
     */
    public abstract TypeElement baseElement();

    /**
     * The name of the generated base class, beginning with 'Hilt_'.
     */
    public abstract ClassName generatedClassName();

    /**
     * Returns {@code true} if the class requires bytecode injection to replace the base class.
     */
    public abstract boolean requiresBytecodeInjection();

    /**
     * Returns the {@link AndroidType} for the annotated element.
     */
    public abstract AndroidType androidType();

    /**
     * Returns {@link Optional} of {@link AndroidEntryPointMetadata}.
     */
    public abstract Optional<AndroidEntryPointMetadata> baseMetadata();

    /**
     * Returns set of scopes that the component interface should be installed in.
     */
    public abstract ImmutableSet<ClassName> installInComponents();

    /**
     * Returns the component manager this generated Hilt class should use.
     */
    public abstract TypeName componentManager();

    /**
     * Returns the initialization arguments for the component manager.
     */
    public abstract Optional<CodeBlock> componentManagerInitArgs();

    /**
     * Returns the metadata for the root most class in the hierarchy.
     *
     * <p>If this is the only metadata in the class hierarchy, it returns this.
     */
    @Memoized
    public AndroidEntryPointMetadata rootMetadata() {
        return baseMetadata().map(AndroidEntryPointMetadata::rootMetadata).orElse(this);
    }

    boolean isRootMetadata() {
        return this.equals(rootMetadata());
    }

    /**
     * Returns true if this class allows optional injection.
     */
    public boolean allowsOptionalInjection() {
        return Processors.hasAnnotation(element(), AndroidClassNames.OPTIONAL_INJECT);
    }

    /**
     * Returns true if any base class (transitively) allows optional injection.
     */
    public boolean baseAllowsOptionalInjection() {
        return baseMetadata().isPresent() && baseMetadata().get().allowsOptionalInjection();
    }

    /**
     * Returns true if any base class (transitively) uses @AndroidEntryPoint.
     */
    public boolean overridesAndroidEntryPointClass() {
        return baseMetadata().isPresent();
    }

    /**
     * The name of the class annotated with @AndroidEntryPoint
     */
    public ClassName elementClassName() {
        return ClassName.get(element());
    }

    /**
     * The name of the base class given to @AndroidEntryPoint
     */
    public TypeName baseClassName() {
        return TypeName.get(baseElement().asType());
    }

    /**
     * The name of the generated injector for the Hilt class.
     */
    public ClassName injectorClassName() {
        return Processors.append(
                Processors.getEnclosedClassName(elementClassName()), "_GeneratedInjector");
    }

    /**
     * The name of inject method for this class. The format is: inject$CLASS. If the class is nested,
     * will return the full name deliminated with '_'. e.g. Foo.Bar.Baz -> injectFoo_Bar_Baz
     */
    public String injectMethodName() {
        return "inject" + Processors.getEnclosedName(elementClassName());
    }

    /**
     * Returns the @InstallIn annotation for the module providing this class.
     */
    public final AnnotationSpec injectorInstallInAnnotation() {
        return Components.getInstallInAnnotationSpec(installInComponents());
    }

    public ParameterSpec componentManagerParam() {
        return ParameterSpec.builder(componentManager(), "componentManager").build();
    }

    /**
     * Modifiers that should be applied to the generated class.
     *
     * <p>Note that the generated class must have public visibility if used by a
     * public @AndroidEntryPoint-annotated kotlin class. See:
     * https://discuss.kotlinlang.org/t/why-does-kotlin-prohibit-exposing-restricted-visibility-types/7047
     */
    public Modifier[] generatedClassModifiers() {
        return isKotlinClass(element()) && element().getModifiers().contains(Modifier.PUBLIC)
                ? new Modifier[]{Modifier.ABSTRACT, Modifier.PUBLIC}
                : new Modifier[]{Modifier.ABSTRACT};
    }

    private static ClassName generatedClassName(TypeElement element) {
        String prefix = "Hilt_";
        return Processors.prepend(Processors.getEnclosedClassName(ClassName.get(element)), prefix);
    }

    private static final ImmutableSet<ClassName> HILT_ANNOTATION_NAMES =
            ImmutableSet.of(
                    AndroidClassNames.HILT_ANDROID_APP,
                    AndroidClassNames.ANDROID_ENTRY_POINT);

    private static ImmutableSet<? extends AnnotationMirror> hiltAnnotations(Element element) {
        return element.getAnnotationMirrors().stream()
                .filter(mirror -> HILT_ANNOTATION_NAMES.contains(ClassName.get(mirror.getAnnotationType())))
                .collect(toImmutableSet());
    }

    /**
     * Returns true if the given element has Android Entry Point metadata.
     */
    public static boolean hasAndroidEntryPointMetadata(Element element) {
        return !hiltAnnotations(element).isEmpty();
    }

    /**
     * Returns the {@link AndroidEntryPointMetadata} for a @AndroidEntryPoint annotated element.
     */
    public static AndroidEntryPointMetadata of(ProcessingEnvironment env, Element element) {
        LinkedHashSet<Element> inheritanceTrace = new LinkedHashSet<>();
        inheritanceTrace.add(element);
        return of(env, element, inheritanceTrace);
    }

    public static AndroidEntryPointMetadata manuallyConstruct(
            TypeElement element,
            TypeElement baseElement,
            ClassName generatedClassName,
            boolean requiresBytecodeInjection,
            AndroidType androidType,
            Optional<AndroidEntryPointMetadata> baseMetadata,
            ImmutableSet<ClassName> installInComponents,
            TypeName componentManager,
            Optional<CodeBlock> componentManagerInitArgs) {
        return new AutoValue_AndroidEntryPointMetadata(
                element,
                baseElement,
                generatedClassName,
                requiresBytecodeInjection,
                androidType,
                baseMetadata,
                installInComponents,
                componentManager,
                componentManagerInitArgs);
    }

    /**
     * Internal implementation for "of" method, checking inheritance cycle utilizing inheritanceTrace
     * along the way.
     */
    private static AndroidEntryPointMetadata of(
            ProcessingEnvironment env, Element element, LinkedHashSet<Element> inheritanceTrace) {
        ImmutableSet<? extends AnnotationMirror> hiltAnnotations = hiltAnnotations(element);

        //1. 节点使用@HiltAndroidApp或@AndroidEntryPoint注解修饰有且仅有一个；
        ProcessorErrors.checkState(
                hiltAnnotations.size() == 1,
                element,
                "Expected exactly 1 of %s. Found: %s",
                HILT_ANNOTATION_NAMES,
                hiltAnnotations);

        ClassName annotationClassName =
                ClassName.get(
                        MoreTypes.asTypeElement(Iterables.getOnlyElement(hiltAnnotations).getAnnotationType()));

        //2. @HiltAndroidApp或@AndroidEntryPoint注解只能用于修饰类，而不能用于修饰接口;
        ProcessorErrors.checkState(
                element.getKind() == ElementKind.CLASS,
                element,
                "Only classes can be annotated with @%s",
                annotationClassName.simpleName());
        TypeElement androidEntryPointElement = MoreElements.asType(element);

        //3. @HiltAndroidApp或@AndroidEntryPoint注解修饰的类不能使用泛型；
        ProcessorErrors.checkState(
                androidEntryPointElement.getTypeParameters().isEmpty(),
                element,
                "@%s-annotated classes cannot have type parameters.",
                annotationClassName.simpleName());

        final TypeElement androidEntryPointClassValue =
                Processors.getAnnotationClassValue(
                        env.getElementUtils(),
                        Processors.getAnnotationMirror(androidEntryPointElement, annotationClassName),
                        "value");

        final TypeElement baseElement;
        final ClassName generatedClassName;
        boolean requiresBytecodeInjection =
                isAndroidSuperclassValidationDisabled(androidEntryPointElement, env)
                        && MoreTypes.isTypeOf(Void.class, androidEntryPointClassValue.asType());

        if (requiresBytecodeInjection) {
            //baseElement表示当前@HiltAndroidApp或@AndroidEntryPoint注解修饰的类的父级类节点
            baseElement = MoreElements.asType(env.getTypeUtils().asElement(androidEntryPointElement.getSuperclass()));
            // If this AndroidEntryPoint is a Kotlin class and its base type is also Kotlin and has
            // default values declared in its constructor then error out because for the short-form
            // usage of @AndroidEntryPoint the bytecode transformation will be done incorrectly.
            KotlinMetadataUtil metadataUtil = KotlinMetadataUtils.getMetadataUtil();
            ProcessorErrors.checkState(
                    !metadataUtil.hasMetadata(androidEntryPointElement)
                            || !metadataUtil.containsConstructorWithDefaultParam(baseElement),
                    baseElement,
                    "The base class, '%s', of the @AndroidEntryPoint, '%s', contains a constructor with "
                            + "default parameters. This is currently not supported by the Gradle plugin. Either "
                            + "specify the base class as described at "
                            + "https://dagger.dev/hilt/gradle-setup#why-use-the-plugin or remove the default value "
                            + "declaration.",
                    baseElement.getQualifiedName(),
                    androidEntryPointElement.getQualifiedName());
            generatedClassName = generatedClassName(androidEntryPointElement);
        } else {
            //① @HiltAndroidApp或@AndroidEntryPoint注解的value值不能是Void类型
            baseElement = androidEntryPointClassValue;
            ProcessorErrors.checkState(
                    !MoreTypes.isTypeOf(Void.class, baseElement.asType()),
                    androidEntryPointElement,
                    "Expected @%s to have a value."
                            + " Did you forget to apply the Gradle Plugin? (dagger.hilt.android.plugin)\n"
                            + "See https://dagger.dev/hilt/gradle-setup.html",
                    annotationClassName.simpleName());

            // Check that the root $CLASS extends Hilt_$CLASS
            String extendsName =
                    env.getTypeUtils()
                            .asElement(androidEntryPointElement.getSuperclass())
                            .getSimpleName()
                            .toString();

            generatedClassName = generatedClassName(androidEntryPointElement);

            //② @HiltAndroidApp或@AndroidEntryPoint注解修饰的类$CLASS extends Hilt_$CLASS
            ProcessorErrors.checkState(
                    extendsName.contentEquals(generatedClassName.simpleName()),
                    androidEntryPointElement,
                    "@%s class expected to extend %s. Found: %s",
                    annotationClassName.simpleName(),
                    generatedClassName.simpleName(),
                    extendsName);
        }

        Optional<AndroidEntryPointMetadata> baseMetadata =
                baseMetadata(env, androidEntryPointElement, baseElement, inheritanceTrace);

        if (baseMetadata.isPresent()) {
            //如果baseElement的类型（及其遍历父级节点）也是用了@HiltAndroidApp或@AndroidEntryPoint注解修饰
            return manuallyConstruct(
                    androidEntryPointElement,
                    baseElement,
                    generatedClassName,
                    requiresBytecodeInjection,
                    baseMetadata.get().androidType(),
                    baseMetadata,
                    baseMetadata.get().installInComponents(),
                    baseMetadata.get().componentManager(),
                    baseMetadata.get().componentManagerInitArgs());
        } else {
            Type type = Type.of(androidEntryPointElement, baseElement);
            return manuallyConstruct(
                    androidEntryPointElement,//@HiltAndroidApp或@AndroidEntryPoint注解修饰的节点
                    baseElement,//可能是androidEntryPointElement的父级节点，也可能是@HiltAndroidApp或@AndroidEntryPoint注解的value值节点
                    generatedClassName,//Hilt_$CLASS,$CLASS表示androidEntryPointElement节点类型
                    requiresBytecodeInjection,
                    type.androidType,//修饰的节点，Activity，Application
                    Optional.empty(),
                    ImmutableSet.of(type.component),
                    type.manager,
                    Optional.ofNullable(type.componentManagerInitArgs));
        }
    }

    private static Optional<AndroidEntryPointMetadata> baseMetadata(
            ProcessingEnvironment env,
            TypeElement element,
            TypeElement baseElement,
            LinkedHashSet<Element> inheritanceTrace) {

        //5.检查baseElement的类型是当前注解修饰的类，导致循环引用问题
        ProcessorErrors.checkState(
                inheritanceTrace.add(baseElement),
                element,
                cyclicInheritanceErrorMessage(inheritanceTrace, baseElement));

        //如果baseElement也是用了@HiltAndroidApp或@AndroidEntryPoint注解修饰
        if (hasAndroidEntryPointMetadata(baseElement)) {
            AndroidEntryPointMetadata baseMetadata =
                    AndroidEntryPointMetadata.of(env, baseElement, inheritanceTrace);
            checkConsistentAnnotations(element, baseMetadata);
            return Optional.of(baseMetadata);
        }

        //baseElement的父级类型是接口或对象：对该父级节点递归执行baseMetadata方法，直到存在@HiltAndroidApp或@AndroidEntryPoint注解修饰，又或者它的父节点不是接口或对象返回empty；
        TypeMirror superClass = baseElement.getSuperclass();
        // None type is returned if this is an interface or Object
        if (superClass.getKind() != TypeKind.NONE && superClass.getKind() != TypeKind.ERROR) {
            Preconditions.checkState(superClass.getKind() == TypeKind.DECLARED);
            return baseMetadata(env, element, MoreTypes.asTypeElement(superClass), inheritanceTrace);
        }

        return Optional.empty();
    }

    private static String cyclicInheritanceErrorMessage(
            LinkedHashSet<Element> inheritanceTrace, TypeElement cycleEntryPoint) {
        return String.format(
                "Cyclic inheritance detected. Make sure the base class of @AndroidEntryPoint "
                        + "is not the annotated class itself or subclass of the annotated class.\n"
                        + "The cyclic inheritance structure: %s --> %s\n",
                inheritanceTrace.stream()
                        .map(Element::asType)
                        .map(TypeMirror::toString)
                        .collect(Collectors.joining(" --> ")),
                cycleEntryPoint.asType());
    }

    private static boolean isKotlinClass(TypeElement typeElement) {
        return typeElement.getAnnotationMirrors().stream()
                .map(mirror -> mirror.getAnnotationType())
                .anyMatch(type -> ClassName.get(type).equals(ClassNames.KOTLIN_METADATA));
    }

    /**
     * The Android type of the Android Entry Point element. Component splits (like with fragment
     * bindings) are coalesced.
     */
    public enum AndroidType {
        APPLICATION,
        ACTIVITY,
        BROADCAST_RECEIVER,
        FRAGMENT,
        SERVICE,
        VIEW
    }

    /**
     * The type of Android Entry Point element. This includes splits for different components.
     */
    private static final class Type {
        private static final Type APPLICATION =
                new Type(
                        AndroidClassNames.SINGLETON_COMPONENT,//SingletonComponent接口
                        AndroidType.APPLICATION,
                        AndroidClassNames.APPLICATION_COMPONENT_MANAGER,//ApplicationComponentManager类
                        null);
        private static final Type SERVICE =
                new Type(
                        AndroidClassNames.SERVICE_COMPONENT,//ServiceComponent接口
                        AndroidType.SERVICE,
                        AndroidClassNames.SERVICE_COMPONENT_MANAGER,//ServiceComponentManager类
                        CodeBlock.of("this"));
        private static final Type BROADCAST_RECEIVER =
                new Type(
                        AndroidClassNames.SINGLETON_COMPONENT,//SingletonComponent接口
                        AndroidType.BROADCAST_RECEIVER,
                        AndroidClassNames.BROADCAST_RECEIVER_COMPONENT_MANAGER,//BroadcastReceiverComponentManager类
                        null);
        private static final Type ACTIVITY =
                new Type(
                        AndroidClassNames.ACTIVITY_COMPONENT,//ActivityComponent接口
                        AndroidType.ACTIVITY,
                        AndroidClassNames.ACTIVITY_COMPONENT_MANAGER,//ActivityComponentManager类
                        CodeBlock.of("this"));
        private static final Type FRAGMENT =
                new Type(
                        AndroidClassNames.FRAGMENT_COMPONENT,//FragmentComponent接口
                        AndroidType.FRAGMENT,
                        AndroidClassNames.FRAGMENT_COMPONENT_MANAGER,//FragmentComponentManager类
                        CodeBlock.of("this"));
        private static final Type VIEW =
                new Type(
                        AndroidClassNames.VIEW_WITH_FRAGMENT_COMPONENT,//ViewWithFragmentComponent接口
                        AndroidType.VIEW,
                        AndroidClassNames.VIEW_COMPONENT_MANAGER,//ViewComponentManager类
                        CodeBlock.of("this, true /* hasFragmentBindings */"));
        private static final Type VIEW_NO_FRAGMENT =
                new Type(
                        AndroidClassNames.VIEW_COMPONENT,//ViewComponent接口
                        AndroidType.VIEW,
                        AndroidClassNames.VIEW_COMPONENT_MANAGER,//ViewComponentManager类
                        CodeBlock.of("this, false /* hasFragmentBindings */"));

        final ClassName component;
        final AndroidType androidType;
        final ClassName manager;
        final CodeBlock componentManagerInitArgs;

        Type(
                ClassName component,
                AndroidType androidType,
                ClassName manager,
                CodeBlock componentManagerInitArgs) {
            this.component = component;
            this.androidType = androidType;
            this.manager = manager;
            this.componentManagerInitArgs = componentManagerInitArgs;
        }

        AndroidType androidType() {
            return androidType;
        }

        private static Type of(TypeElement element, TypeElement baseElement) {
            if (Processors.hasAnnotation(element, AndroidClassNames.HILT_ANDROID_APP)) {
                return forHiltAndroidApp(element, baseElement);
            }
            return forAndroidEntryPoint(element, baseElement);
        }

        private static Type forHiltAndroidApp(TypeElement element, TypeElement baseElement) {
            //7. @HiltAndroidApp只能用于修饰Application的继承类；
            ProcessorErrors.checkState(
                    Processors.isAssignableFrom(baseElement, AndroidClassNames.APPLICATION),
                    element,
                    "@HiltAndroidApp base class must extend Application. Found: %s",
                    baseElement);
            return Type.APPLICATION;
        }

        private static Type forAndroidEntryPoint(TypeElement element, TypeElement baseElement) {

            //8. @AndroidEntryPoint可用于修饰Activity、Service、Broadcast_service,Fragment,View：
            //
            // - （1）@AndroidEntryPoint只能用于修饰Activity时，只能修饰androidx.activity.ComponentActivity的继承节点；
            //
            // - （2）@AndroidEntryPoint不能用于修饰Application的继承类。
            if (Processors.isAssignableFrom(baseElement, AndroidClassNames.ACTIVITY)) {
                ProcessorErrors.checkState(
                        Processors.isAssignableFrom(baseElement, AndroidClassNames.COMPONENT_ACTIVITY),
                        element,
                        "Activities annotated with @AndroidEntryPoint must be a subclass of "
                                + "androidx.activity.ComponentActivity. (e.g. FragmentActivity, "
                                + "AppCompatActivity, etc.)"
                );
                return Type.ACTIVITY;
            } else if (Processors.isAssignableFrom(baseElement, AndroidClassNames.SERVICE)) {
                return Type.SERVICE;
            } else if (Processors.isAssignableFrom(baseElement, AndroidClassNames.BROADCAST_RECEIVER)) {
                return Type.BROADCAST_RECEIVER;
            } else if (Processors.isAssignableFrom(baseElement, AndroidClassNames.FRAGMENT)) {
                return Type.FRAGMENT;
            } else if (Processors.isAssignableFrom(baseElement, AndroidClassNames.VIEW)) {
                //是否使用了@WithFragmentBindings注解修饰
                boolean withFragmentBindings =
                        Processors.hasAnnotation(element, AndroidClassNames.WITH_FRAGMENT_BINDINGS);
                return withFragmentBindings ? Type.VIEW : Type.VIEW_NO_FRAGMENT;
            } else if (Processors.isAssignableFrom(baseElement, AndroidClassNames.APPLICATION)) {
                throw new BadInputException(
                        "@AndroidEntryPoint cannot be used on an Application. Use @HiltAndroidApp instead.",
                        element);
            }
            throw new BadInputException(
                    "@AndroidEntryPoint base class must extend ComponentActivity, (support) Fragment, "
                            + "View, Service, or BroadcastReceiver.",
                    element);
        }
    }

    //6. baseElement表示@HiltAndroidApp或@AndroidEntryPoint注解的value值节点（或@HiltAndroidApp或@AndroidEntryPoint注解修饰的类的父类），如果baseElement使用了@HiltAndroidApp或@AndroidEntryPoint注解修饰：
    private static void checkConsistentAnnotations(
            TypeElement element, AndroidEntryPointMetadata baseMetadata) {
        TypeElement baseElement = baseMetadata.element();

        // (1) @HiltAndroidApp或@AndroidEntryPoint注解修饰的类 和 其baseElement节点要么都使用@WithFragmentBindings注解修饰，要么都不要使用该注解；
        checkAnnotationsMatch(element, baseElement, AndroidClassNames.WITH_FRAGMENT_BINDINGS);

        // (2) @HiltAndroidApp或@AndroidEntryPoint注解修饰的类不要使用@OptionalInject修饰的注解修饰 || baseElement类型使用@OptionalInject修饰的注解修饰；
        ProcessorErrors.checkState(
                baseMetadata.allowsOptionalInjection()
                        || !Processors.hasAnnotation(element, AndroidClassNames.OPTIONAL_INJECT),
                element,
                "@OptionalInject Hilt class cannot extend from a non-optional @AndroidEntryPoint "
                        + "base: %s",
                element);
    }

    private static void checkAnnotationsMatch(
            TypeElement element, TypeElement baseElement, ClassName annotationName) {
        boolean isAnnotated = Processors.hasAnnotation(element, annotationName);
        boolean isBaseAnnotated = Processors.hasAnnotation(baseElement, annotationName);
        ProcessorErrors.checkState(
                isAnnotated == isBaseAnnotated,
                element,
                isBaseAnnotated
                        ? "Classes that extend an @%1$s base class must also be annotated @%1$s"
                        : "Classes that extend a @AndroidEntryPoint base class must not use @%1$s when the "
                        + "base class "
                        + "does not use @%1$s",
                annotationName.simpleName());
    }
}
