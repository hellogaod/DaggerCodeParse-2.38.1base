package dagger.internal.codegen.writing;


import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Optional;

import dagger.internal.DelegateFactory;
import dagger.internal.codegen.binding.BindingType;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.FrameworkField;
import dagger.internal.codegen.javapoet.AnnotationSpecs;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.producers.internal.DelegateProducer;
import dagger.spi.model.BindingKind;

import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.SourceFiles.generatedClassNameForBinding;
import static dagger.internal.codegen.javapoet.AnnotationSpecs.Suppression.RAWTYPES;
import static dagger.internal.codegen.writing.ComponentImplementation.FieldSpecKind.FRAMEWORK_FIELD;
import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * An object that can initialize a framework-type component field for a binding. An instance should
 * be created for each field.
 */
class FrameworkFieldInitializer implements FrameworkInstanceSupplier {

    /**
     * An object that can determine the expression to use to assign to the component field for a
     * binding.
     */
    interface FrameworkInstanceCreationExpression {
        /**
         * Returns the expression to use to assign to the component field for the binding.
         */
        CodeBlock creationExpression();

        /**
         * Returns the framework class to use for the field, if different from the one implied by the
         * binding. This implementation returns {@link Optional#empty()}.
         */
        default Optional<ClassName> alternativeFrameworkClass() {
            return Optional.empty();
        }

        /**
         * Returns {@code true} if instead of using {@link #creationExpression()} to create a framework
         * instance, a case in {@link SwitchingProviders} should be created for this binding.
         */
        // TODO(ronshapiro): perhaps this isn't the right approach. Instead of saying "Use
        // SetFactory.EMPTY because you will only get 1 class for all types of bindings that use
        // SetFactory", maybe we should still use an inner switching provider but the same switching
        // provider index for all cases.
        default boolean useSwitchingProvider() {
            return true;
        }
    }

    private final ComponentImplementation.ShardImplementation shardImplementation;
    private final ContributionBinding binding;
    private final FrameworkInstanceCreationExpression frameworkInstanceCreationExpression;
    private FieldSpec fieldSpec;
    private InitializationState fieldInitializationState = InitializationState.UNINITIALIZED;

    FrameworkFieldInitializer(
            ComponentImplementation componentImplementation,
            ContributionBinding binding,
            FrameworkInstanceCreationExpression frameworkInstanceCreationExpression) {
        this.binding = checkNotNull(binding);
        this.shardImplementation = checkNotNull(componentImplementation).shardImplementation(binding);
        this.frameworkInstanceCreationExpression = checkNotNull(frameworkInstanceCreationExpression);
    }

    /**
     * Returns the {@link MemberSelect} for the framework field, and adds the field and its
     * initialization code to the component if it's needed and not already added.
     */
    @Override
    public final MemberSelect memberSelect() {

        //初始化变量:
        //e.g.this.assistedFactoryRequestRepresentationProvider = AssistedFactoryRequestRepresentation_Factory.create(componentRequestRepresentationsProvider, processorComponent.daggerTypesProvider, processorComponent.daggerElementsProvider)
        initializeField();
        //返回LocalField对象
        return MemberSelect.localField(shardImplementation, checkNotNull(fieldSpec).name);
    }

    /**
     * Adds the field and its initialization code to the component.
     */
    private void initializeField() {
        switch (fieldInitializationState) {
            case UNINITIALIZED:
                // Change our state in case we are recursively invoked via initializeRequestRepresentation
                fieldInitializationState = InitializationState.INITIALIZING;
                CodeBlock.Builder codeBuilder = CodeBlock.builder();
                //e.g.返回的是AssistedFactoryRequestRepresentation_Factory.create(componentRequestRepresentationsProvider, processorComponent.daggerTypesProvider, processorComponent.daggerElementsProvider)
                CodeBlock fieldInitialization = frameworkInstanceCreationExpression.creationExpression();

                //e.g.this.assistedFactoryRequestRepresentationProvider = fieldInitialization代码块
                CodeBlock initCode = CodeBlock.of("this.$N = $L;", getOrCreateField(), fieldInitialization);

                if (fieldInitializationState == InitializationState.DELEGATED) {
                    codeBuilder.add(
                            "$T.setDelegate($N, $L);", delegateType(), fieldSpec, fieldInitialization);
                } else {
                    codeBuilder.add(initCode);
                }
                shardImplementation.addInitialization(codeBuilder.build());

                fieldInitializationState = InitializationState.INITIALIZED;
                break;

            case INITIALIZING:
                // We were recursively invoked, so create a delegate factory instead
                fieldInitializationState = InitializationState.DELEGATED;
                shardImplementation.addInitialization(
                        CodeBlock.of("this.$N = new $T<>();", getOrCreateField(), delegateType()));
                break;

            case DELEGATED:
            case INITIALIZED:
                break;
        }
    }

    /**
     * Adds a field representing the resolved bindings, optionally forcing it to use a particular
     * binding type (instead of the type the resolved bindings would typically use).
     * <p>
     * 创建变量并且获取,e.g.
     *
     * @SuppressWarnings("rawtypes") private AssistedFactoryRequestRepresentation_Factory assistedFactoryRequestRepresentationProvider;
     */
    private FieldSpec getOrCreateField() {
        if (fieldSpec != null) {
            return fieldSpec;
        }
        boolean useRawType = !shardImplementation.isTypeAccessible(binding.key().type().java());
        FrameworkField contributionBindingField =
                FrameworkField.forBinding(
                        binding, frameworkInstanceCreationExpression.alternativeFrameworkClass());

        TypeName fieldType =
                useRawType ? contributionBindingField.type().rawType : contributionBindingField.type();

        if (binding.kind() == BindingKind.ASSISTED_INJECTION) {
            // An assisted injection factory doesn't extend Provider, so we reference the generated
            // factory type directly (i.e. Foo_Factory<T> instead of Provider<Foo<T>>).
            TypeName[] typeParameters =
                    MoreTypes.asDeclared(binding.key().type().java()).getTypeArguments().stream()
                            .map(TypeName::get)
                            .toArray(TypeName[]::new);
            fieldType =
                    typeParameters.length == 0
                            ? generatedClassNameForBinding(binding)
                            : ParameterizedTypeName.get(generatedClassNameForBinding(binding), typeParameters);
        }

        FieldSpec.Builder contributionField =
                FieldSpec.builder(
                        fieldType, shardImplementation.getUniqueFieldName(contributionBindingField.name()));
        contributionField.addModifiers(PRIVATE);
        if (useRawType) {
            contributionField.addAnnotation(AnnotationSpecs.suppressWarnings(RAWTYPES));
        }

        fieldSpec = contributionField.build();
        shardImplementation.addField(FRAMEWORK_FIELD, fieldSpec);

        return fieldSpec;
    }

    private Class<?> delegateType() {
        return isProvider() ? DelegateFactory.class : DelegateProducer.class;
    }

    private boolean isProvider() {
        return binding.bindingType().equals(BindingType.PROVISION)
                && frameworkInstanceCreationExpression
                .alternativeFrameworkClass()
                .map(TypeNames.PROVIDER::equals)
                .orElse(true);
    }

    /**
     * Initialization state for a factory field.
     */
    private enum InitializationState {
        /**
         * The field is {@code null}.
         */
        UNINITIALIZED,

        /**
         * The field's dependencies are being set up. If the field is needed in this state, use a {@link
         * DelegateFactory}.
         */
        INITIALIZING,

        /**
         * The field's dependencies are being set up, but the field can be used because it has already
         * been set to a {@link DelegateFactory}.
         */
        DELEGATED,

        /**
         * The field is set to an undelegated factory.
         */
        INITIALIZED;
    }
}
