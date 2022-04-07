package dagger.hilt.processor.internal;

import com.google.auto.common.GeneratedAnnotations;
import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor7;
import javax.lang.model.util.SimpleTypeVisitor7;

import static com.google.auto.common.MoreElements.asPackage;
import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.extension.DaggerCollectors.toOptional;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Static helper methods for writing a processor.
 */
public final class Processors {

    public static final String CONSTRUCTOR_NAME = "<init>";

    public static final String STATIC_INITIALIZER_NAME = "<clinit>";

    private static final String JAVA_CLASS = "java.lang.Class";

    public static void generateAggregatingClass(
            String aggregatingPackage,
            AnnotationSpec aggregatingAnnotation,
            TypeElement element,
            Class<?> generatedAnnotationClass,
            ProcessingEnvironment env) throws IOException {
        ClassName name = ClassName.get(aggregatingPackage, "_" + getFullEnclosedName(element));
        TypeSpec.Builder builder =
                TypeSpec.classBuilder(name)
                        .addModifiers(PUBLIC)
                        .addOriginatingElement(element)
                        .addAnnotation(aggregatingAnnotation)
                        .addJavadoc("This class should only be referenced by generated code!")
                        .addJavadoc("This class aggregates information across multiple compilations.\n");
        ;

        addGeneratedAnnotation(builder, env, generatedAnnotationClass);

        JavaFile.builder(name.packageName(), builder.build()).build().writeTo(env.getFiler());
    }

    /**
     * If the received mirror represents a primitive type or an array of primitive types, this returns
     * the represented primitive type. Otherwise throws an IllegalStateException.
     */
    public static PrimitiveType getPrimitiveType(TypeMirror type) {
        return type.accept(
                new SimpleTypeVisitor7<PrimitiveType, Void>() {
                    @Override
                    public PrimitiveType visitArray(ArrayType type, Void unused) {
                        return getPrimitiveType(type.getComponentType());
                    }

                    @Override
                    public PrimitiveType visitPrimitive(PrimitiveType type, Void unused) {
                        return type;
                    }

                    @Override
                    public PrimitiveType defaultAction(TypeMirror type, Void unused) {
                        throw new IllegalStateException("Unhandled type: " + type);
                    }
                }, null /* the Void accumulator */);
    }

    /**
     * Shortcut for converting from upper camel to lower camel case
     *
     * <p>Example: "SomeString" => "someString"
     */
    public static String upperToLowerCamel(String upperCamel) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, upperCamel);
    }

    /**
     * Returns the {@link TypeElement} for a class attribute on an annotation.
     */
    public static TypeElement getAnnotationClassValue(
            Elements elements, AnnotationMirror annotation, String key) {
        return Iterables.getOnlyElement(getAnnotationClassValues(elements, annotation, key));
    }

    /**
     * Returns a list of {@link TypeElement}s for a class attribute on an annotation.
     */
    public static ImmutableList<TypeElement> getAnnotationClassValues(
            Elements elements, AnnotationMirror annotation, String key) {
        ImmutableList<TypeElement> values = getOptionalAnnotationClassValues(elements, annotation, key);

        ProcessorErrors.checkState(
                values.size() >= 1,
                // TODO(b/152801981): Point to the annotation value rather than the annotated element.
                annotation.getAnnotationType().asElement(),
                "@%s, '%s' class is invalid or missing: %s",
                annotation.getAnnotationType().asElement().getSimpleName(),
                key,
                annotation);

        return values;
    }

    /**
     * Returns a multimap from attribute name to elements for class valued attributes.
     */
    private static Multimap<String, DeclaredType> getAnnotationClassValues(
            Elements elements, AnnotationMirror annotation) {
        Element javaClass = elements.getTypeElement(JAVA_CLASS);
        SetMultimap<String, DeclaredType> annotationMembers = LinkedHashMultimap.create();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e :
                elements.getElementValuesWithDefaults(annotation).entrySet()) {
            Optional<DeclaredType> returnType = getOptionalDeclaredType(e.getKey().getReturnType());
            if (returnType.isPresent() && returnType.get().asElement().equals(javaClass)) {
                String attribute = e.getKey().getSimpleName().toString();
                Set<DeclaredType> declaredTypes = new LinkedHashSet<DeclaredType>();
                e.getValue().accept(new DeclaredTypeAnnotationValueVisitor(), declaredTypes);
                annotationMembers.putAll(attribute, declaredTypes);
            }
        }
        return annotationMembers;
    }

    /**
     * Returns a list of {@link TypeElement}s for a class attribute on an annotation.
     */
    public static ImmutableList<TypeElement> getOptionalAnnotationClassValues(
            Elements elements, AnnotationMirror annotation, String key) {
        return ImmutableList.copyOf(
                getAnnotationClassValues(elements, annotation).get(key).stream()
                        .map(MoreTypes::asTypeElement)
                        .collect(Collectors.toList()));
    }

    /**
     * Returns an {@link Optional#of} the declared type if the received mirror represents a declared
     * type or an array of declared types, otherwise returns {@link Optional#empty}.
     */
    public static Optional<DeclaredType> getOptionalDeclaredType(TypeMirror type) {
        return Optional.ofNullable(
                type.accept(
                        new SimpleTypeVisitor7<DeclaredType, Void>(null /* defaultValue */) {
                            @Override
                            public DeclaredType visitArray(ArrayType type, Void unused) {
                                return MoreTypes.asDeclared(type.getComponentType());
                            }

                            @Override
                            public DeclaredType visitDeclared(DeclaredType type, Void unused) {
                                return type;
                            }

                            @Override
                            public DeclaredType visitError(ErrorType type, Void unused) {
                                return type;
                            }
                        },
                        null /* the Void accumulator */));
    }

    private static final class DeclaredTypeAnnotationValueVisitor
            extends SimpleAnnotationValueVisitor7<Void, Set<DeclaredType>> {

        @Override
        public Void visitArray(
                List<? extends AnnotationValue> vals, Set<DeclaredType> types) {
            for (AnnotationValue val : vals) {
                val.accept(this, types);
            }
            return null;
        }

        @Override
        public Void visitType(TypeMirror t, Set<DeclaredType> types) {
            DeclaredType declared = MoreTypes.asDeclared(t);
            checkNotNull(declared);
            types.add(declared);
            return null;
        }
    }

    /**
     * Returns the fully qualified class name, with _ instead of . For elements that are not type
     * elements, this continues to append the simple name of elements. For example,
     * foo_bar_Outer_Inner_fooMethod.
     */
    public static String getFullEnclosedName(Element element) {
        Preconditions.checkNotNull(element);
        String qualifiedName = "";
        while (element != null) {
            if (element.getKind().equals(ElementKind.PACKAGE)) {
                qualifiedName = asPackage(element).getQualifiedName() + qualifiedName;
            } else {
                // This check is needed to keep the name stable when compiled with jdk8 vs jdk11. jdk11
                // contains newly added "module" enclosing elements of packages, which adds an addtional "_"
                // prefix to the name due to an empty module element compared with jdk8.
                if (!element.getSimpleName().toString().isEmpty()) {
                    qualifiedName = "." + element.getSimpleName() + qualifiedName;
                }
            }
            element = element.getEnclosingElement();
        }
        return qualifiedName.replace('.', '_');
    }

    /**
     * Returns true if the given element has an annotation with the given class name.
     */
    public static boolean hasAnnotation(Element element, ClassName className) {
        return getAnnotationMirrorOptional(element, className).isPresent();
    }

    /**
     * Returns true if the given element has an annotation with the given class name.
     */
    public static boolean hasAnnotation(AnnotationMirror mirror, ClassName className) {
        return hasAnnotation(mirror.getAnnotationType().asElement(), className);
    }
    /**
     * Returns the name of a class, including prefixing with enclosing class names. i.e. for inner
     * class Foo enclosed by Bar, returns Bar_Foo instead of just Foo
     */
    public static String getEnclosedName(ClassName name) {
        return Joiner.on('_').join(name.simpleNames());
    }

    /**
     * Returns an equivalent class name with the {@code .} (dots) used for inner classes replaced with
     * {@code _}.
     */
    public static ClassName getEnclosedClassName(ClassName className) {
        return ClassName.get(className.packageName(), getEnclosedName(className));
    }

    /**
     * Appends the given string to the end of the class name.
     */
    public static ClassName append(ClassName name, String suffix) {
        return name.peerClass(name.simpleName() + suffix);
    }

    /**
     * Prepends the given string to the beginning of the class name.
     */
    public static ClassName prepend(ClassName name, String prefix) {
        return name.peerClass(prefix + name.simpleName());
    }

    /**
     * Returns the annotation mirror from the given element that corresponds to the given class.
     *
     * @return {@link Optional#empty()} if no annotation is found on the element.
     * @throws {@link IllegalArgumentException} if 2 or more annotations are found.
     */
    static Optional<AnnotationMirror> getAnnotationMirrorOptional(
            Element element, ClassName className) {
        return element.getAnnotationMirrors().stream()
                .filter(mirror -> ClassName.get(mirror.getAnnotationType()).equals(className))
                .collect(toOptional());
    }

    /**
     * @return true if element inherits directly or indirectly from the className
     */
    public static boolean isAssignableFrom(TypeElement element, ClassName className) {
        return isAssignableFromAnyOf(element, ImmutableSet.of(className));
    }

    /**
     * @return true if element inherits directly or indirectly from any of the classNames
     */
    public static boolean isAssignableFromAnyOf(TypeElement element,
                                                ImmutableSet<ClassName> classNames) {
        for (ClassName className : classNames) {
            if (ClassName.get(element).equals(className)) {
                return true;
            }
        }

        TypeMirror superClass = element.getSuperclass();
        // None type is returned if this is an interface or Object
        // Error type is returned for classes that are generated by this processor
        if ((superClass.getKind() != TypeKind.NONE) && (superClass.getKind() != TypeKind.ERROR)) {
            Preconditions.checkState(superClass.getKind() == TypeKind.DECLARED);
            if (isAssignableFromAnyOf(MoreTypes.asTypeElement(superClass), classNames)) {
                return true;
            }
        }

        for (TypeMirror iface : element.getInterfaces()) {
            // Skip errors and keep looking. This is especially needed for classes generated by this
            // processor.
            if (iface.getKind() == TypeKind.ERROR) {
                continue;
            }
            Preconditions.checkState(iface.getKind() == TypeKind.DECLARED,
                    "Interface type is %s", iface.getKind());
            if (isAssignableFromAnyOf(MoreTypes.asTypeElement(iface), classNames)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the annotation mirror from the given element that corresponds to the given class.
     *
     * @throws IllegalStateException if the given element isn't annotated with that annotation.
     */
    public static AnnotationMirror getAnnotationMirror(Element element, ClassName className) {
        Optional<AnnotationMirror> annotationMirror = getAnnotationMirrorOptional(element, className);
        if (annotationMirror.isPresent()) {
            return annotationMirror.get();
        } else {
            throw new IllegalStateException(
                    String.format(
                            "Couldn't find annotation %s on element %s. Found annotations: %s",
                            className, element.getSimpleName(), element.getAnnotationMirrors()));
        }
    }

    public static void addGeneratedAnnotation(
            TypeSpec.Builder typeSpecBuilder, ProcessingEnvironment env, Class<?> generatorClass) {
        addGeneratedAnnotation(typeSpecBuilder, env, generatorClass.getName());
    }

    public static void addGeneratedAnnotation(
            TypeSpec.Builder typeSpecBuilder, ProcessingEnvironment env, String generatorClass) {
        GeneratedAnnotations.generatedAnnotation(env.getElementUtils(), env.getSourceVersion())
                .ifPresent(
                        annotation ->
                                typeSpecBuilder.addAnnotation(
                                        AnnotationSpec.builder(ClassName.get(annotation))
                                                .addMember("value", "$S", generatorClass)
                                                .build()));
    }

    public static AnnotationSpec getOriginatingElementAnnotation(TypeElement element) {
        TypeName rawType = rawTypeName(ClassName.get(getTopLevelType(element)));
        return AnnotationSpec.builder(ClassNames.ORIGINATING_ELEMENT)
                .addMember("topLevelClass", "$T.class", rawType)
                .build();
    }

    /**
     * Returns the {@link TypeName} for the raw type of the given type name. If the argument isn't a
     * parameterized type, it returns the argument unchanged.
     */
    public static TypeName rawTypeName(TypeName typeName) {
        //如果是泛型
        return (typeName instanceof ParameterizedTypeName)
                ? ((ParameterizedTypeName) typeName).rawType
                : typeName;
    }

    public static TypeElement getTopLevelType(Element originalElement) {
        checkNotNull(originalElement);
        for (Element e = originalElement; e != null; e = e.getEnclosingElement()) {
            if (isTopLevel(e)) {
                return MoreElements.asType(e);
            }
        }
        throw new IllegalStateException("Cannot find a top-level type for " + originalElement);
    }

    /**
     * Returns true if the given element is a top-level element.
     */
    public static boolean isTopLevel(Element element) {
        return element.getEnclosingElement().getKind() == ElementKind.PACKAGE;
    }

}