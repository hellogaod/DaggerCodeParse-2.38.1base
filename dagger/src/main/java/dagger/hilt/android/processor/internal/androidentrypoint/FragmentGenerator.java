package dagger.hilt.android.processor.internal.androidentrypoint;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import dagger.hilt.android.processor.internal.AndroidClassNames;
import dagger.hilt.processor.internal.ClassNames;
import dagger.hilt.processor.internal.Processors;

import static dagger.hilt.processor.internal.HiltCompilerOptions.useFragmentGetContextFix;

/**
 * Generates an Hilt Fragment class for the @AndroidEntryPoint annotated class.
 */
public final class FragmentGenerator {
    //private ContextWrapper componentContext;
    private static final FieldSpec COMPONENT_CONTEXT_FIELD =
            FieldSpec.builder(AndroidClassNames.CONTEXT_WRAPPER, "componentContext")
                    .addModifiers(Modifier.PRIVATE)
                    .build();

    private final ProcessingEnvironment env;
    private final AndroidEntryPointMetadata metadata;
    private final ClassName generatedClassName;

    public FragmentGenerator(
            ProcessingEnvironment env,
            AndroidEntryPointMetadata metadata) {
        this.env = env;
        this.metadata = metadata;
        generatedClassName = metadata.generatedClassName();
    }

    public void generate() throws IOException {
        JavaFile.builder(generatedClassName.packageName(), createTypeSpec())
                .build()
                .writeTo(env.getFiler());
    }

    //A generated base class to be extended by the @AndroidEntryPoint annotated class. If using the Gradle plugin, this is swapped as the base class via bytecode transformation.
    // @Generated("FragmentGenerator")
    // @SuppressWarnings("deprecation")
    // abstract class Hilt_$CLASS extends $BASE implements GeneratedComponentManagerHolder {
    //   ...
    // }
    TypeSpec createTypeSpec() {
        TypeSpec.Builder builder =
                TypeSpec.classBuilder(generatedClassName.simpleName())
                        .addOriginatingElement(metadata.element())
                        .superclass(metadata.baseClassName())
                        .addModifiers(metadata.generatedClassModifiers())
                        //private ContextWrapper componentContext;
                        .addField(COMPONENT_CONTEXT_FIELD)
                        //onAttach方法
                        .addMethod(onAttachContextMethod())
                        //onAttach方法
                        .addMethod(onAttachActivityMethod())
                        //initializeComponentContext方法
                        .addMethod(initializeComponentContextMethod())
                        //getContext方法
                        .addMethod(getContextMethod())
                        //onGetLayoutInflater方法
                        .addMethod(inflatorMethod());

        Generators.addGeneratedBaseClassJavadoc(builder, AndroidClassNames.ANDROID_ENTRY_POINT);
        Processors.addGeneratedAnnotation(builder, env, getClass());
        Generators.copyLintAnnotations(metadata.element(), builder);
        Generators.addSuppressAnnotation(builder, "deprecation");
        Generators.copyConstructors(metadata.baseElement(), builder);

        metadata.baseElement().getTypeParameters().stream()
                .map(TypeVariableName::get)
                .forEachOrdered(builder::addTypeVariable);

        Generators.addComponentOverride(metadata, builder);

        Generators.addInjectionMethods(metadata, builder);

        if (!metadata.overridesAndroidEntryPointClass()) {
            //getDefaultViewModelProviderFactory方法
            builder.addMethod(getDefaultViewModelProviderFactory());
        }

        return builder.build();
    }

    // @CallSuper
    // @Override
    // public void onAttach(Context context) {
    //   super.onAttach(context);
    //   initializeComponentContext();
    //   inject();
    // }
    private static MethodSpec onAttachContextMethod() {
        return MethodSpec.methodBuilder("onAttach")
                .addAnnotation(Override.class)
                .addAnnotation(AndroidClassNames.CALL_SUPER)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(AndroidClassNames.CONTEXT, "context")
                .addStatement("super.onAttach(context)")
                .addStatement("initializeComponentContext()")
                // The inject method will internally check if injected already
                .addStatement("inject()")
                .build();
    }

    // @CallSuper
    // @MainThread
    // @Override
    // public void onAttach(Activity activity) {
    //   super.onAttach(activity);
    //   Preconditions.checkState(
    //       componentContext == null || FragmentComponentManager.findActivity(
    //           componentContext) == activity, "...");
    //   initializeComponentContext();
    //   inject();
    // }
    private static MethodSpec onAttachActivityMethod() {
        return MethodSpec.methodBuilder("onAttach")
                .addAnnotation(Override.class)
                .addAnnotation(AndroidClassNames.CALL_SUPER)
                .addAnnotation(AndroidClassNames.MAIN_THREAD)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(AndroidClassNames.ACTIVITY, "activity")
                .addStatement("super.onAttach(activity)")
                .addStatement(
                        "$T.checkState($N == null || $T.findActivity($N) == activity, $S)",
                        ClassNames.PRECONDITIONS,
                        COMPONENT_CONTEXT_FIELD,
                        AndroidClassNames.FRAGMENT_COMPONENT_MANAGER,
                        COMPONENT_CONTEXT_FIELD,
                        "onAttach called multiple times with different Context! "
                                + "Hilt Fragments should not be retained.")
                .addStatement("initializeComponentContext()")
                // The inject method will internally check if injected already
                .addStatement("inject()")
                .build();
    }

    // private void initializeComponentContext() {
    //   if (componentContext == null) {
    //     // Note: The LayoutInflater provided by this componentContext may be different from super
    //     // Fragment's because we are getting it from base context instead of cloning from super
    //     // Fragment's LayoutInflater.
    //     componentContext = FragmentComponentManager.createContextWrapper(super.getContext(), this);
    //   }
    // }
    private MethodSpec initializeComponentContextMethod() {
        return MethodSpec.methodBuilder("initializeComponentContext")
                .addModifiers(Modifier.PRIVATE)
                .beginControlFlow("if ($N == null)", COMPONENT_CONTEXT_FIELD)
                .addComment(
                        "Note: The LayoutInflater provided by this componentContext may be different from"
                                + " super Fragment's because we getting it from base context instead of cloning"
                                + " from the super Fragment's LayoutInflater.")
                .addStatement(
                        "$N = $T.createContextWrapper(super.getContext(), this)",
                        COMPONENT_CONTEXT_FIELD,
                        metadata.componentManager())
                .endControlFlow()
                .build();
    }

    // @Override
    // public Context getContext() {
    //   if (super.getContext() == null)
    //     return null;
    //   initializeComponentContext();
    //   return componentContext;
    // }
    private MethodSpec getContextMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getContext")
                .returns(AndroidClassNames.CONTEXT)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);

        if (useFragmentGetContextFix(env)) {
            builder.beginControlFlow("if (super.getContext() == null)");
        } else {
            builder.beginControlFlow(
                    "if (super.getContext() == null && $N == null)",
                    COMPONENT_CONTEXT_FIELD);
        }

        return builder
                .addStatement("return null")
                .endControlFlow()
                .addStatement("initializeComponentContext()")
                .addStatement("return $N", COMPONENT_CONTEXT_FIELD)
                .build();
    }

    // @Override
    // public LayoutInflater onGetLayoutInflater(Bundle savedInstanceState) {
    //   LayoutInflater inflater = super.onGetLayoutInflater(savedInstanceState);
    //   return LayoutInflater.from(FragmentComponentManager.createContextWrapper(inflater, this));
    // }
    private MethodSpec inflatorMethod() {
        return MethodSpec.methodBuilder("onGetLayoutInflater")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(AndroidClassNames.BUNDLE, "savedInstanceState")
                .returns(AndroidClassNames.LAYOUT_INFLATER)
                .addStatement(
                        "$T inflater = super.onGetLayoutInflater(savedInstanceState)",
                        AndroidClassNames.LAYOUT_INFLATER)
                .addStatement(
                        "return $T.from($T.createContextWrapper(inflater, this))",
                        AndroidClassNames.LAYOUT_INFLATER,
                        metadata.componentManager())
                .build();
    }

    // @Override
    // public ViewModelProvider.Factory getDefaultViewModelProviderFactory() {
    //   return DefaultViewModelFactories.getFragmentFactory(
    //       this, super.getDefaultViewModelProviderFactory());
    // }
    private MethodSpec getDefaultViewModelProviderFactory() {
        return MethodSpec.methodBuilder("getDefaultViewModelProviderFactory")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(AndroidClassNames.VIEW_MODEL_PROVIDER_FACTORY)
                .addStatement(
                        "return $T.getFragmentFactory(this, super.getDefaultViewModelProviderFactory())",
                        AndroidClassNames.DEFAULT_VIEW_MODEL_FACTORIES)
                .build();
    }
}
