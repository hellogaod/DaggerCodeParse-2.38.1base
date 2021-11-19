package dagger.internal.codegen.kotlin;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import dagger.internal.codegen.extension.DaggerCollectors;
import dagger.internal.codegen.langmodel.DaggerElements;
import kotlin.Metadata;
import kotlinx.metadata.Flag;
import kotlinx.metadata.KmClassVisitor;
import kotlinx.metadata.KmConstructorExtensionVisitor;
import kotlinx.metadata.KmConstructorVisitor;
import kotlinx.metadata.KmExtensionType;
import kotlinx.metadata.KmFunctionExtensionVisitor;
import kotlinx.metadata.KmFunctionVisitor;
import kotlinx.metadata.KmPropertyExtensionVisitor;
import kotlinx.metadata.KmPropertyVisitor;
import kotlinx.metadata.KmValueParameterVisitor;
import kotlinx.metadata.jvm.JvmConstructorExtensionVisitor;
import kotlinx.metadata.jvm.JvmFieldSignature;
import kotlinx.metadata.jvm.JvmFunctionExtensionVisitor;
import kotlinx.metadata.jvm.JvmMethodSignature;
import kotlinx.metadata.jvm.JvmPropertyExtensionVisitor;
import kotlinx.metadata.jvm.KotlinClassHeader;
import kotlinx.metadata.jvm.KotlinClassMetadata;

import static dagger.internal.codegen.base.MoreAnnotationValues.getIntArrayValue;
import static dagger.internal.codegen.base.MoreAnnotationValues.getIntValue;
import static dagger.internal.codegen.base.MoreAnnotationValues.getOptionalIntValue;
import static dagger.internal.codegen.base.MoreAnnotationValues.getOptionalStringValue;
import static dagger.internal.codegen.base.MoreAnnotationValues.getStringArrayValue;
import static dagger.internal.codegen.base.MoreAnnotationValues.getStringValue;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableMap;

import static dagger.internal.codegen.langmodel.DaggerElements.getAnnotationMirror;
import static dagger.internal.codegen.langmodel.DaggerElements.getMethodDescriptor;
import static kotlinx.metadata.Flag.ValueParameter.DECLARES_DEFAULT_VALUE;

/**
 * Data class of a TypeElement and its Kotlin metadata.
 * <p>
 * TypeElement 的数据类及其 Kotlin 元数据。包括Kotlin节点遍历访问处理
 */
@AutoValue
abstract class KotlinMetadata {
    // Kotlin suffix for fields that are for a delegated property.
    // See:
    // https://github.com/JetBrains/kotlin/blob/master/core/compiler.common.jvm/src/org/jetbrains/kotlin/load/java/JvmAbi.kt#L32
    private static final String DELEGATED_PROPERTY_NAME_SUFFIX = "$delegate";

    // Map that associates field elements with its Kotlin synthetic method for annotations.
    private final Map<VariableElement, Optional<MethodForAnnotations>>
            elementFieldAnnotationMethodMap = new HashMap<>();

    // Map that associates field elements with its Kotlin getter method.
    private final Map<VariableElement, Optional<ExecutableElement>> elementFieldGetterMethodMap =
            new HashMap<>();

    abstract TypeElement typeElement();

    abstract ClassMetadata classMetadata();

    @Memoized
    ImmutableMap<String, ExecutableElement> methodDescriptors() {
        return ElementFilter.methodsIn(typeElement().getEnclosedElements()).stream()
                .collect(toImmutableMap(DaggerElements::getMethodDescriptor, Function.identity()));
    }

    /**
     * Returns true if any constructor of the defined a default parameter.
     */
    @Memoized
    boolean containsConstructorWithDefaultParam() {
        return classMetadata().constructors().stream()
                .flatMap(constructor -> constructor.parameters().stream())
                .anyMatch(parameter -> parameter.flags(DECLARES_DEFAULT_VALUE));
    }

    /**
     * Gets the synthetic method for annotations of a given field element.
     */
    Optional<ExecutableElement> getSyntheticAnnotationMethod(VariableElement fieldElement) {
        return getAnnotationMethod(fieldElement)
                .map(
                        methodForAnnotations -> {
                            if (methodForAnnotations == MethodForAnnotations.MISSING) {
                                throw new IllegalStateException(
                                        "Method for annotations is missing for " + fieldElement);
                            }
                            return methodForAnnotations.method();
                        });
    }

    /**
     * Returns true if the synthetic method for annotations is missing. This can occur when inspecting
     * the Kotlin metadata of a property from another compilation unit.
     */
    boolean isMissingSyntheticAnnotationMethod(VariableElement fieldElement) {
        return getAnnotationMethod(fieldElement)
                .map(methodForAnnotations -> methodForAnnotations == MethodForAnnotations.MISSING)
                // This can be missing if there was no property annotation at all (e.g. no annotations or
                // the qualifier is already properly attached to the field). For these cases, it isn't
                // considered missing since there was no method to look for in the first place.
                .orElse(false);
    }

    private Optional<MethodForAnnotations> getAnnotationMethod(VariableElement fieldElement) {
        return elementFieldAnnotationMethodMap.computeIfAbsent(
                fieldElement, this::getAnnotationMethodUncached);
    }


    private Optional<MethodForAnnotations> getAnnotationMethodUncached(VariableElement fieldElement) {
        return findProperty(fieldElement)
                .methodForAnnotationsSignature()
                .map(
                        signature ->
                                Optional.ofNullable(methodDescriptors().get(signature))
                                        .map(MethodForAnnotations::create)
                                        // The method may be missing across different compilations.
                                        // See https://youtrack.jetbrains.com/issue/KT-34684
                                        .orElse(MethodForAnnotations.MISSING));
    }

    /**
     * Gets the getter method of a given field element corresponding to a property.
     */
    Optional<ExecutableElement> getPropertyGetter(VariableElement fieldElement) {
        return elementFieldGetterMethodMap.computeIfAbsent(
                fieldElement, this::getPropertyGetterUncached);
    }

    private Optional<ExecutableElement> getPropertyGetterUncached(VariableElement fieldElement) {
        return findProperty(fieldElement)
                .getterSignature()
                .flatMap(signature -> Optional.ofNullable(methodDescriptors().get(signature)));
    }

    private PropertyMetadata findProperty(VariableElement field) {
        String fieldDescriptor = DaggerElements.getFieldDescriptor(field);
        //存在于classMeda中
        if (classMetadata().propertiesByFieldSignature().containsKey(fieldDescriptor)) {
            return classMetadata().propertiesByFieldSignature().get(fieldDescriptor);
        } else {
            // Fallback to finding property by name, see: https://youtrack.jetbrains.com/issue/KT-35124
            final String propertyName = getPropertyNameFromField(field);
            return classMetadata().propertiesByFieldSignature().values().stream()
                    .filter(property -> propertyName.contentEquals(property.name()))
                    .collect(DaggerCollectors.onlyElement());
        }
    }


    //截取变量名称
    private static String getPropertyNameFromField(VariableElement field) {
        String name = field.getSimpleName().toString();
        if (name.endsWith(DELEGATED_PROPERTY_NAME_SUFFIX)) {
            return name.substring(0, name.length() - DELEGATED_PROPERTY_NAME_SUFFIX.length());
        } else {
            return name;
        }
    }

    FunctionMetadata getFunctionMetadata(ExecutableElement method) {
        return classMetadata().functionsBySignature().get(getMethodDescriptor(method));
    }

    /**
     * Parse Kotlin class metadata from a given type element *
     */
    static KotlinMetadata from(TypeElement typeElement) {
        return new AutoValue_KotlinMetadata(
                typeElement, ClassVisitor.createClassMetadata(metadataOf(typeElement)));
    }


    private static KotlinClassMetadata.Class metadataOf(TypeElement typeElement) {
        Optional<AnnotationMirror> metadataAnnotation =
                getAnnotationMirror(typeElement, ClassName.get(Metadata.class));
        Preconditions.checkState(metadataAnnotation.isPresent());
        KotlinClassHeader header =
                new KotlinClassHeader(
                        getIntValue(metadataAnnotation.get(), "k"),
                        getIntArrayValue(metadataAnnotation.get(), "mv"),
                        getStringArrayValue(metadataAnnotation.get(), "d1"),
                        getStringArrayValue(metadataAnnotation.get(), "d2"),
                        getStringValue(metadataAnnotation.get(), "xs"),
                        getOptionalStringValue(metadataAnnotation.get(), "pn").orElse(null),
                        getOptionalIntValue(metadataAnnotation.get(), "xi").orElse(null));

        KotlinClassMetadata metadata = KotlinClassMetadata.read(header);
        if (metadata == null) {
            // Should only happen on Kotlin < 1.0 (i.e. metadata version < 1.1)
            throw new IllegalStateException(
                    "Unsupported metadata version. Check that your Kotlin version is >= 1.0");
        }
        if (metadata instanceof KotlinClassMetadata.Class) {
            // TODO(danysantiago): If when we need other types of metadata then move to right method.
            return (KotlinClassMetadata.Class) metadata;
        } else {
            throw new IllegalStateException("Unsupported metadata type: " + metadata);
        }
    }

    private static final class ClassVisitor extends KmClassVisitor {

        static ClassMetadata createClassMetadata(KotlinClassMetadata.Class data) {
            ClassVisitor visitor = new ClassVisitor();
            data.accept(visitor);
            return visitor.classMetadata.build();
        }

        private final ClassMetadata.Builder classMetadata = ClassMetadata.builder();

        @Override
        public void visit(int flags, String name) {
            classMetadata.flags(flags).name(name);
        }

        @Override
        public KmConstructorVisitor visitConstructor(int flags) {
            return new KmConstructorVisitor() {
                private final FunctionMetadata.Builder constructor =
                        FunctionMetadata.builder(flags, "<init>");

                @Override
                public KmValueParameterVisitor visitValueParameter(int flags, String name) {
                    constructor.addParameter(ValueParameterMetadata.create(flags, name));
                    return super.visitValueParameter(flags, name);
                }

                @Override
                public KmConstructorExtensionVisitor visitExtensions(KmExtensionType kmExtensionType) {
                    return kmExtensionType.equals(JvmConstructorExtensionVisitor.TYPE)
                            ? new JvmConstructorExtensionVisitor() {
                        @Override
                        public void visit(JvmMethodSignature jvmMethodSignature) {
                            constructor.signature(jvmMethodSignature.asString());
                        }
                    }
                            : null;
                }

                @Override
                public void visitEnd() {
                    classMetadata.addConstructor(constructor.build());
                }
            };
        }

        @Override
        public KmFunctionVisitor visitFunction(int flags, String name) {
            return new KmFunctionVisitor() {
                private final FunctionMetadata.Builder function = FunctionMetadata.builder(flags, name);

                @Override
                public KmValueParameterVisitor visitValueParameter(int flags, String name) {
                    function.addParameter(ValueParameterMetadata.create(flags, name));
                    return super.visitValueParameter(flags, name);
                }

                @Override
                public KmFunctionExtensionVisitor visitExtensions(KmExtensionType kmExtensionType) {
                    return kmExtensionType.equals(JvmFunctionExtensionVisitor.TYPE)
                            ? new JvmFunctionExtensionVisitor() {
                        @Override
                        public void visit(JvmMethodSignature jvmMethodSignature) {
                            function.signature(jvmMethodSignature.asString());
                        }
                    }
                            : null;
                }

                @Override
                public void visitEnd() {
                    classMetadata.addFunction(function.build());
                }
            };
        }

        @Override
        public void visitCompanionObject(String companionObjectName) {
            classMetadata.companionObjectName(companionObjectName);
        }

        @Override
        public KmPropertyVisitor visitProperty(
                int flags, String name, int getterFlags, int setterFlags) {
            return new KmPropertyVisitor() {
                private final PropertyMetadata.Builder property = PropertyMetadata.builder(flags, name);

                @Override
                public KmPropertyExtensionVisitor visitExtensions(KmExtensionType kmExtensionType) {
                    if (!kmExtensionType.equals(JvmPropertyExtensionVisitor.TYPE)) {
                        return null;
                    }

                    return new JvmPropertyExtensionVisitor() {
                        @Override
                        public void visit(
                                int jvmFlags,
                                @Nullable JvmFieldSignature jvmFieldSignature,
                                @Nullable JvmMethodSignature jvmGetterSignature,
                                @Nullable JvmMethodSignature jvmSetterSignature) {
                            property.fieldSignature(
                                    Optional.ofNullable(jvmFieldSignature).map(JvmFieldSignature::asString));
                            property.getterSignature(
                                    Optional.ofNullable(jvmGetterSignature).map(JvmMethodSignature::asString));
                        }

                        @Override
                        public void visitSyntheticMethodForAnnotations(
                                @Nullable JvmMethodSignature methodSignature) {
                            property.methodForAnnotationsSignature(
                                    Optional.ofNullable(methodSignature).map(JvmMethodSignature::asString));
                        }
                    };
                }

                @Override
                public void visitEnd() {
                    classMetadata.addProperty(property.build());
                }
            };
        }
    }

    @AutoValue
    abstract static class ClassMetadata extends BaseMetadata {
        abstract Optional<String> companionObjectName();

        abstract ImmutableSet<FunctionMetadata> constructors();

        abstract ImmutableMap<String, FunctionMetadata> functionsBySignature();

        abstract ImmutableMap<String, PropertyMetadata> propertiesByFieldSignature();

        static Builder builder() {
            return new AutoValue_KotlinMetadata_ClassMetadata.Builder();
        }

        @AutoValue.Builder
        abstract static class Builder implements BaseMetadata.Builder<Builder> {
            abstract Builder companionObjectName(String companionObjectName);

            abstract ImmutableSet.Builder<FunctionMetadata> constructorsBuilder();

            abstract ImmutableMap.Builder<String, FunctionMetadata> functionsBySignatureBuilder();

            abstract ImmutableMap.Builder<String, PropertyMetadata> propertiesByFieldSignatureBuilder();

            Builder addConstructor(FunctionMetadata constructor) {
                constructorsBuilder().add(constructor);
                functionsBySignatureBuilder().put(constructor.signature(), constructor);
                return this;
            }

            Builder addFunction(FunctionMetadata function) {
                functionsBySignatureBuilder().put(function.signature(), function);
                return this;
            }

            Builder addProperty(PropertyMetadata property) {
                if (property.fieldSignature().isPresent()) {
                    propertiesByFieldSignatureBuilder().put(property.fieldSignature().get(), property);
                }
                return this;
            }

            abstract ClassMetadata build();
        }
    }

    @AutoValue
    abstract static class FunctionMetadata extends BaseMetadata {
        abstract String signature();

        abstract ImmutableList<ValueParameterMetadata> parameters();

        static Builder builder(int flags, String name) {
            return new AutoValue_KotlinMetadata_FunctionMetadata.Builder().flags(flags).name(name);
        }

        @AutoValue.Builder
        abstract static class Builder implements BaseMetadata.Builder<Builder> {
            abstract Builder signature(String signature);

            abstract ImmutableList.Builder<ValueParameterMetadata> parametersBuilder();

            Builder addParameter(ValueParameterMetadata parameter) {
                parametersBuilder().add(parameter);
                return this;
            }

            abstract FunctionMetadata build();
        }
    }

    @AutoValue
    abstract static class PropertyMetadata extends BaseMetadata {
        /**
         * Returns the JVM field descriptor of the backing field of this property.
         */
        abstract Optional<String> fieldSignature();

        abstract Optional<String> getterSignature();

        /**
         * Returns JVM method descriptor of the synthetic method for property annotations.
         */
        abstract Optional<String> methodForAnnotationsSignature();

        static Builder builder(int flags, String name) {
            return new AutoValue_KotlinMetadata_PropertyMetadata.Builder().flags(flags).name(name);
        }

        @AutoValue.Builder
        interface Builder extends BaseMetadata.Builder<Builder> {
            Builder fieldSignature(Optional<String> signature);

            Builder getterSignature(Optional<String> signature);

            Builder methodForAnnotationsSignature(Optional<String> signature);

            PropertyMetadata build();
        }
    }

    @AutoValue
    abstract static class ValueParameterMetadata extends BaseMetadata {
        private static ValueParameterMetadata create(int flags, String name) {
            return new AutoValue_KotlinMetadata_ValueParameterMetadata(flags, name);
        }
    }

    abstract static class BaseMetadata {
        /**
         * Returns the Kotlin metadata flags for this property.
         */
        abstract int flags();

        /**
         * returns {@code true} if the given flag (e.g. {@link Flag.IS_PRIVATE}) applies.
         */
        boolean flags(Flag flag) {
            return flag.invoke(flags());
        }

        /**
         * Returns the simple name of this property.
         */
        abstract String name();

        interface Builder<BuilderT> {
            BuilderT flags(int flags);

            BuilderT name(String name);
        }
    }

    @AutoValue
    abstract static class MethodForAnnotations {
        static MethodForAnnotations create(ExecutableElement method) {
            return new AutoValue_KotlinMetadata_MethodForAnnotations(method);
        }

        static final MethodForAnnotations MISSING = MethodForAnnotations.create(null);

        @Nullable
        abstract ExecutableElement method();
    }
}
