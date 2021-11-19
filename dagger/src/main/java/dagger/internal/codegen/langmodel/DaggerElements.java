package dagger.internal.codegen.langmodel;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.squareup.javapoet.ClassName;

import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor8;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import dagger.Reusable;
import dagger.internal.codegen.base.ClearableCache;

import static com.google.auto.common.MoreElements.hasModifiers;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.asList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.Modifier.ABSTRACT;

/**
 * Extension of {@link Elements} that adds Dagger-specific methods.
 */
@Reusable
public final class DaggerElements implements Elements, ClearableCache {
    //K：节点，V：节点所有方法
    private final Map<TypeElement, ImmutableSet<ExecutableElement>> getLocalAndInheritedMethodsCache =
            new HashMap<>();

    private final Elements elements;
    private final Types types;

    public DaggerElements(Elements elements, Types types) {
        this.elements = checkNotNull(elements);
        this.types = checkNotNull(types);
    }

    /**
     * Returns {@code true} if {@code encloser} is equal to or recursively encloses {@code enclosed}.
     * <p>
     * enclosed类一级级往上找父类，直到就是encloser返回true；否则返回false
     */
    public static boolean transitivelyEncloses(Element encloser, Element enclosed) {
        Element current = enclosed;
        while (current != null) {
            if (current.equals(encloser)) {
                return true;
            }
            //当前类的父类
            current = current.getEnclosingElement();
        }
        return false;
    }

    public ImmutableSet<ExecutableElement> getLocalAndInheritedMethods(TypeElement type) {
        //getLocalAndInheritedMethods():返回类的非private方法非static，其中包含继承的方法
        return getLocalAndInheritedMethodsCache.computeIfAbsent(
                type, k -> MoreElements.getLocalAndInheritedMethods(type, types, elements));
    }

    //返回类的非private、非static、abstract修饰的方法
    public ImmutableSet<ExecutableElement> getUnimplementedMethods(TypeElement type) {
        return FluentIterable.from(getLocalAndInheritedMethods(type))
                .filter(hasModifiers(ABSTRACT))
                .toSet();
    }

    /**
     * Returns the type element for a class.
     */
    public TypeElement getTypeElement(Class<?> clazz) {
        return getTypeElement(clazz.getCanonicalName());
    }

    @Override
    public TypeElement getTypeElement(CharSequence name) {
        return elements.getTypeElement(name);
    }

    /**
     * Returns the type element for a class name.
     */
    public TypeElement getTypeElement(ClassName className) {
        return getTypeElement(className.canonicalName());
    }

    /**
     * Returns the argument or the closest enclosing element that is a {@link TypeElement}.
     * <p>
     * 找到当前element节点所在的类或接口（如果本身就是类或接口，则返回本身）
     */
    public static TypeElement closestEnclosingTypeElement(Element element) {
        Element current = element;
        while (current != null) {
            if (MoreElements.isType(current)) {
                return MoreElements.asType(current);
            }
            current = current.getEnclosingElement();
        }
        throw new IllegalStateException("There is no enclosing TypeElement for: " + element);
    }

    /**
     * Returns {@code true} iff the given element has an {@link AnnotationMirror} whose {@linkplain
     * AnnotationMirror#getAnnotationType() annotation type} has the same canonical name as any of
     * that of {@code annotationClasses}.
     * <p>
     * element节点是否使用了annotationClasses集合中的注解，只要使用了，就返回true
     */
    public static boolean isAnyAnnotationPresent(
            Element element, Iterable<ClassName> annotationClasses) {
        for (ClassName annotation : annotationClasses) {
            if (isAnnotationPresent(element, annotation)) {//element节点的所有注解 匹配annotation,匹配上返回true
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} iff the given element has an {@link AnnotationMirror} whose {@link
     * AnnotationMirror#getAnnotationType() annotation type} has the same canonical name as that of
     * {@code annotationClass}. This method is a safer alternative to calling {@link
     * Element#getAnnotation} and checking for {@code null} as it avoids any interaction with
     * annotation proxies.
     * <p>
     * element节点的所有注解 匹配annotationName，匹配上返回true
     */
    public static boolean isAnnotationPresent(Element element, ClassName annotationName) {
        return getAnnotationMirror(element, annotationName).isPresent();
    }

    /**
     * Returns the annotation present on {@code element} whose type is {@code first} or within {@code
     * rest}, checking each annotation type in order.
     * <p>
     * element使用first，reset里面的注解，返回第一个
     */
    @SafeVarargs
    public static Optional<AnnotationMirror> getAnyAnnotation(
            Element element, ClassName first, ClassName... rest) {
        return getAnyAnnotation(element, asList(first, rest));
    }

    /**
     * Returns the annotation present on {@code element} whose type is in {@code annotations},
     * checking each annotation type in order.
     * <p>
     * element使用annotations里面的注解，返回第一个
     */
    public static Optional<AnnotationMirror> getAnyAnnotation(
            Element element, Collection<ClassName> annotations) {
        return element.getAnnotationMirrors().stream()
                .filter(hasAnnotationTypeIn(annotations))
                .map((AnnotationMirror a) -> a) // Avoid returning Optional<? extends AnnotationMirror>.
                .findFirst();
    }

    /**
     * Returns the annotations present on {@code element} of all types.
     * <p>
     * 返回element上的注解集合，该注解集合存在于first,rest形成的集合中。
     */
    @SafeVarargs
    public static ImmutableSet<AnnotationMirror> getAllAnnotations(
            Element element, ClassName first, ClassName... rest) {
        return ImmutableSet.copyOf(
                Iterables.filter(
                        element.getAnnotationMirrors(), hasAnnotationTypeIn(asList(first, rest))::test));
    }

    // Note: This is similar to auto-common's MoreElements except using ClassName rather than Class.
    // TODO(bcorso): Contribute a String version to auto-common's MoreElements?

    /**
     * Returns an {@link AnnotationMirror} for the annotation of type {@code annotationClass} on
     * {@code element}, or {@link Optional#empty()} if no such annotation exists. This method is a
     * safer alternative to calling {@link Element#getAnnotation} as it avoids any interaction with
     * annotation proxies.
     * <p>
     * 修饰element节点的所有注解 匹配annotationName，匹配上返回，否则返回空
     */
    public static Optional<AnnotationMirror> getAnnotationMirror(
            Element element, ClassName annotationName) {
        String annotationClassName = annotationName.canonicalName();
        //修饰节点的所有注解集
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            TypeElement annotationTypeElement =
                    MoreElements.asType(annotationMirror.getAnnotationType().asElement());
            if (annotationTypeElement.getQualifiedName().contentEquals(annotationClassName)) {
                return Optional.of(annotationMirror);
            }
        }
        return Optional.empty();
    }

    //传递的注解是否包含在annotations集合中
    private static Predicate<AnnotationMirror> hasAnnotationTypeIn(
            Collection<ClassName> annotations) {
        Set<String> annotationClassNames =
                annotations.stream().map(ClassName::canonicalName).collect(toSet());
        return annotation ->
                annotationClassNames.contains(
                        MoreTypes.asTypeElement(annotation.getAnnotationType()).getQualifiedName().toString());
    }

    /**
     * Returns the field descriptor of the given {@code element}.
     *
     * <p>This is useful for matching Kotlin Metadata JVM Signatures with elements from the AST.
     *
     * <p>For reference, see the <a
     * href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.3.2">JVM
     * specification, section 4.3.2</a>.
     */
    public static String getFieldDescriptor(VariableElement element) {
        //变量名称 + ":" + 变量类型形成的不同逻辑形式
        return element.getSimpleName() + ":" + getDescriptor(element.asType());
    }

    /**
     * Returns the method descriptor of the given {@code element}.
     *
     * <p>This is useful for matching Kotlin Metadata JVM Signatures with elements from the AST.
     *
     * <p>For reference, see the <a
     * href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.3.3">JVM
     * specification, section 4.3.3</a>.
     */
    public static String getMethodDescriptor(ExecutableElement element) {
        return element.getSimpleName() + getDescriptor(element.asType());
    }

    private static String getDescriptor(TypeMirror t) {
        return t.accept(JVM_DESCRIPTOR_TYPE_VISITOR, null);
    }


    private static final AbstractTypeVisitor8<String, Void> JVM_DESCRIPTOR_TYPE_VISITOR =
            new AbstractTypeVisitor8<String, Void>() {

                @Override
                public String visitArray(ArrayType arrayType, Void v) {//如果是数组
                    return "[" + getDescriptor(arrayType.getComponentType());
                }

                @Override
                public String visitDeclared(DeclaredType declaredType, Void v) {//如果是类或接口
                    return "L" + getInternalName(declaredType.asElement()) + ";";
                }

                @Override
                public String visitError(ErrorType errorType, Void v) {//错误类型
                    // For descriptor generating purposes we don't need a fully modeled type since we are
                    // only interested in obtaining the class name in its "internal form".
                    return visitDeclared(errorType, v);
                }

                @Override
                public String visitExecutable(ExecutableType executableType, Void v) {//如果是方法类型
                    String parameterDescriptors =
                            executableType.getParameterTypes().stream()
                                    .map(DaggerElements::getDescriptor)
                                    .collect(Collectors.joining());
                    String returnDescriptor = getDescriptor(executableType.getReturnType());
                    return "(" + parameterDescriptors + ")" + returnDescriptor;
                }

                @Override
                public String visitIntersection(IntersectionType intersectionType, Void v) {
                    // For a type variable with multiple bounds: "the erasure of a type variable is determined
                    // by the first type in its bound" - JVM Spec Sec 4.4
                    return getDescriptor(intersectionType.getBounds().get(0));
                }

                @Override
                public String visitNoType(NoType noType, Void v) {
                    return "V";
                }

                @Override
                public String visitNull(NullType nullType, Void v) {
                    return visitUnknown(nullType, null);
                }

                @Override
                public String visitPrimitive(PrimitiveType primitiveType, Void v) {
                    switch (primitiveType.getKind()) {
                        case BOOLEAN:
                            return "Z";
                        case BYTE:
                            return "B";
                        case SHORT:
                            return "S";
                        case INT:
                            return "I";
                        case LONG:
                            return "J";
                        case CHAR:
                            return "C";
                        case FLOAT:
                            return "F";
                        case DOUBLE:
                            return "D";
                        default:
                            throw new IllegalArgumentException("Unknown primitive type.");
                    }
                }

                @Override
                public String visitTypeVariable(TypeVariable typeVariable, Void v) {//变量类型
                    // The erasure of a type variable is the erasure of its leftmost bound. - JVM Spec Sec 4.6
                    return getDescriptor(typeVariable.getUpperBound());
                }

                @Override
                public String visitUnion(UnionType unionType, Void v) {
                    return visitUnknown(unionType, null);
                }

                @Override
                public String visitUnknown(TypeMirror typeMirror, Void v) {
                    throw new IllegalArgumentException("Unsupported type: " + typeMirror);
                }

                @Override
                public String visitWildcard(WildcardType wildcardType, Void v) {
                    return "";
                }

                /**
                 * Returns the name of this element in its "internal form".
                 *
                 * <p>For reference, see the <a
                 * href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.2">JVM
                 * specification, section 4.2</a>.
                 */
                private String getInternalName(Element element) {
                    try {
                        TypeElement typeElement = MoreElements.asType(element);
                        switch (typeElement.getNestingKind()) {
                            case TOP_LEVEL:
                                return typeElement.getQualifiedName().toString().replace('.', '/');
                            case MEMBER:
                                return getInternalName(typeElement.getEnclosingElement())
                                        + "$"
                                        + typeElement.getSimpleName();
                            default:
                                throw new IllegalArgumentException("Unsupported nesting kind.");
                        }
                    } catch (IllegalArgumentException e) {
                        // Not a TypeElement, try something else...
                    }

                    if (element instanceof QualifiedNameable) {
                        QualifiedNameable qualifiedNameElement = (QualifiedNameable) element;
                        return qualifiedNameElement.getQualifiedName().toString().replace('.', '/');
                    }

                    return element.getSimpleName().toString();
                }
            };

    @Override
    public PackageElement getPackageElement(CharSequence name) {
        return elements.getPackageElement(name);
    }

    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(
            AnnotationMirror a) {
        return elements.getElementValuesWithDefaults(a);
    }

    @Override
    public String getDocComment(Element e) {
        return elements.getDocComment(e);
    }

    @Override
    public boolean isDeprecated(Element e) {
        return elements.isDeprecated(e);
    }

    @Override
    public Name getBinaryName(TypeElement type) {
        return elements.getBinaryName(type);
    }

    @Override
    public PackageElement getPackageOf(Element type) {
        return elements.getPackageOf(type);
    }

    @Override
    public List<? extends Element> getAllMembers(TypeElement type) {
        return elements.getAllMembers(type);
    }

    @Override
    public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
        return elements.getAllAnnotationMirrors(e);
    }

    @Override
    public boolean hides(Element hider, Element hidden) {
        return elements.hides(hider, hidden);
    }

    @Override
    public boolean overrides(
            ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
        return elements.overrides(overrider, overridden, type);
    }

    @Override
    public String getConstantExpression(Object value) {
        return elements.getConstantExpression(value);
    }

    @Override
    public void printElements(Writer w, Element... elements) {
        this.elements.printElements(w, elements);
    }

    @Override
    public Name getName(CharSequence cs) {
        return elements.getName(cs);
    }

    @Override
    public boolean isFunctionalInterface(TypeElement type) {
        return elements.isFunctionalInterface(type);
    }

    @Override
    public void clearCache() {
        getLocalAndInheritedMethodsCache.clear();
    }
}
