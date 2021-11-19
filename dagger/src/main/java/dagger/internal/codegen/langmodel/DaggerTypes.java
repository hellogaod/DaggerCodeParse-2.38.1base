package dagger.internal.codegen.langmodel;


import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Traverser;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.Types;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getOnlyElement;

/**
 * Extension of {@link Types} that adds Dagger-specific methods.
 * <p>
 * Dagger处理TypeMirror，例如T变成Provider<T>,或者反之处理;还有Type类型或element节点可达性判断
 */
public final class DaggerTypes implements Types {
    private final Types types;
    private final DaggerElements elements;

    public DaggerTypes(Types types, DaggerElements elements) {
        this.types = checkNotNull(types);
        this.elements = checkNotNull(elements);
    }


    // Note: This is similar to auto-common's MoreTypes except using ClassName rather than Class.
    // TODO(bcorso): Contribute a String version to auto-common's MoreTypes?

    /**
     * Returns true if the raw type underlying the given {@link TypeMirror} represents the same raw
     * type as the given {@link Class} and throws an IllegalArgumentException if the {@link
     * TypeMirror} does not represent a type that can be referenced by a {@link Class}
     * <p>
     * 判断type是否是typeName类型
     */
    public static boolean isTypeOf(final TypeName typeName, TypeMirror type) {
        checkNotNull(typeName);
        return type.accept(new IsTypeOf(typeName), null);
    }

    //判断type是否是typeName类型
    private static final class IsTypeOf extends SimpleTypeVisitor8<Boolean, Void> {
        private final TypeName typeName;

        IsTypeOf(TypeName typeName) {
            this.typeName = typeName;
        }

        @Override
        protected Boolean defaultAction(TypeMirror type, Void ignored) {
            throw new IllegalArgumentException(type + " cannot be represented as a Class<?>.");
        }

        @Override
        public Boolean visitNoType(NoType noType, Void p) {
            if (noType.getKind().equals(TypeKind.VOID)) {
                return typeName.equals(TypeName.VOID);
            }
            throw new IllegalArgumentException(noType + " cannot be represented as a Class<?>.");
        }

        @Override
        public Boolean visitError(ErrorType errorType, Void p) {
            return false;
        }

        @Override
        public Boolean visitPrimitive(PrimitiveType type, Void p) {
            switch (type.getKind()) {
                case BOOLEAN:
                    return typeName.equals(TypeName.BOOLEAN);
                case BYTE:
                    return typeName.equals(TypeName.BYTE);
                case CHAR:
                    return typeName.equals(TypeName.CHAR);
                case DOUBLE:
                    return typeName.equals(TypeName.DOUBLE);
                case FLOAT:
                    return typeName.equals(TypeName.FLOAT);
                case INT:
                    return typeName.equals(TypeName.INT);
                case LONG:
                    return typeName.equals(TypeName.LONG);
                case SHORT:
                    return typeName.equals(TypeName.SHORT);
                default:
                    throw new IllegalArgumentException(type + " cannot be represented as a Class<?>.");
            }
        }

        @Override
        public Boolean visitArray(ArrayType array, Void p) {
            return (typeName instanceof ArrayTypeName)
                    && isTypeOf(((ArrayTypeName) typeName).componentType, array.getComponentType());
        }

        @Override
        public Boolean visitDeclared(DeclaredType type, Void ignored) {
            TypeElement typeElement = MoreElements.asType(type.asElement());
            return (typeName instanceof ClassName)
                    && typeElement.getQualifiedName().contentEquals(((ClassName) typeName).canonicalName());
        }
    }

    /**
     * Returns the non-{@link Object} superclass of the type with the proper type parameters. An empty
     * {@link Optional} is returned if there is no non-{@link Object} superclass.
     */
    public Optional<DeclaredType> nonObjectSuperclass(DeclaredType type) {
        ////nonObjectSuperclass():返回type类型的非Object类型的父类
        return Optional.ofNullable(MoreTypes.nonObjectSuperclass(types, elements, type).orNull());
    }

    /**
     * Returns the {@linkplain #directSupertypes(TypeMirror) supertype}s of a type in breadth-first
     * order.
     */
    public Iterable<TypeMirror> supertypes(TypeMirror type) {
        //广度优先，以此获取当前类及其父类
        return Traverser.<TypeMirror>forGraph(this::directSupertypes).breadthFirst(type);
    }

    /**
     * Returns {@code type}'s single type argument.
     *
     * <p>For example, if {@code type} is {@code List<Number>} this will return {@code Number}.
     *
     * @throws IllegalArgumentException if {@code type} is not a declared type or has zero or more
     *                                  than one type arguments.
     */
    public static TypeMirror unwrapType(TypeMirror type) {
        TypeMirror unwrapped = unwrapTypeOrDefault(type, null);
        checkArgument(unwrapped != null, "%s is a raw type", type);
        return unwrapped;
    }

    /**
     * Returns {@code type}'s single type argument, if one exists, or {@link Object} if not.
     *
     * <p>For example, if {@code type} is {@code List<Number>} this will return {@code Number}.
     *
     * @throws IllegalArgumentException if {@code type} is not a declared type or has more than one
     *                                  type argument.
     */
    public TypeMirror unwrapTypeOrObject(TypeMirror type) {
        //剥离type的泛型，并且该泛型仅仅只有一个，如果出现错误，使用Object替代
        return unwrapTypeOrDefault(type, elements.getTypeElement(Object.class).asType());
    }

    //相当于A<T>获取T，错误情况下则使用defaultType
    private static TypeMirror unwrapTypeOrDefault(TypeMirror type, TypeMirror defaultType) {
        DeclaredType declaredType = MoreTypes.asDeclared(type);
        TypeElement typeElement = MoreElements.asType(declaredType.asElement());
        checkArgument(
                !typeElement.getTypeParameters().isEmpty(),
                "%s does not have a type parameter",
                typeElement.getQualifiedName());
        return getOnlyElement(declaredType.getTypeArguments(), defaultType);
    }

    /**
     * Returns {@code type} wrapped in {@code wrappingClass}.
     *
     * <p>For example, if {@code type} is {@code List<Number>} and {@code wrappingClass} is {@code
     * Set.class}, this will return {@code Set<List<Number>>}.
     */
    public DeclaredType wrapType(TypeMirror type, Class<?> wrappingClass) {

        //原type被wrappingClass包裹
        return types.getDeclaredType(elements.getTypeElement(wrappingClass), type);
    }

    /**
     * Returns {@code type}'s single type argument wrapped in {@code wrappingClass}.
     *
     * <p>For example, if {@code type} is {@code List<Number>} and {@code wrappingClass} is {@code
     * Set.class}, this will return {@code Set<Number>}.
     *
     * <p>If {@code type} has no type parameters, returns a {@link TypeMirror} for {@code
     * wrappingClass} as a raw type.
     *
     * @throws IllegalArgumentException if {@code} has more than one type argument.
     */
    public DeclaredType rewrapType(TypeMirror type, Class<?> wrappingClass) {
        List<? extends TypeMirror> typeArguments = MoreTypes.asDeclared(type).getTypeArguments();
        TypeElement wrappingType = elements.getTypeElement(wrappingClass);
        switch (typeArguments.size()) {
            case 0:
                return getDeclaredType(wrappingType);
            case 1:
                return getDeclaredType(wrappingType, getOnlyElement(typeArguments));
            default:
                throw new IllegalArgumentException(type + " has more than 1 type argument");
        }
    }

    /**
     * Returns a publicly accessible type based on {@code type}:
     *
     * <ul>
     *   <li>If {@code type} is publicly accessible, returns it.
     *   <li>If not, but {@code type}'s raw type is publicly accessible, returns the raw type.
     *   <li>Otherwise returns {@link Object}.
     * </ul>
     */
    public TypeMirror publiclyAccessibleType(TypeMirror type) {
        //1.先判断是否可以被任意包引用，如果是直接返回type
        //2.在判断是否可悲任意包访问，返回type的DelareType类型
        //3.以上都不行，返回Object
        return accessibleType(
                type, Accessibility::isTypePubliclyAccessible, Accessibility::isRawTypePubliclyAccessible);
    }

    /**
     * Returns an accessible type in {@code requestingClass}'s package based on {@code type}:
     *
     * <ul>
     *   <li>If {@code type} is accessible from the package, returns it.
     *   <li>If not, but {@code type}'s raw type is accessible from the package, returns the raw type.
     *   <li>Otherwise returns {@link Object}.
     * </ul>
     */
    public TypeMirror accessibleType(TypeMirror type, ClassName requestingClass) {

        //这里的意义在于，首先判断type代码可以被requestingClass包使用，直接返回type即可
        // 如果这个条件不满足在判断type是否可悲requestClass包访问，返回type转换成DeclaredType类型
        //如果以上两个都不满足，那么返回Object的TypeMirror类型
        return accessibleType(
                type,
                t -> Accessibility.isTypeAccessibleFrom(t, requestingClass.packageName()),//t的代码是否可被requestingClass所在的包引用
                t -> Accessibility.isRawTypeAccessible(t, requestingClass.packageName()));//requestingClass所在包可以访问t
    }

    private TypeMirror accessibleType(
            TypeMirror type,
            Predicate<TypeMirror> accessibilityPredicate,
            Predicate<TypeMirror> rawTypeAccessibilityPredicate) {
        //执行accessibilityPredicate.test
        if (accessibilityPredicate.test(type)) {//方法执行返回true，则返回type
            return type;
        }
        //type是类或接口，并且执行rawTypeAccessibilityPredicate.test方法返回true
        else if (type.getKind().equals(TypeKind.DECLARED)
                && rawTypeAccessibilityPredicate.test(type)) {
            //type转换成DeclaredType类型
            return getDeclaredType(MoreTypes.asTypeElement(type));
        } else {
            //返回Object对象
            return elements.getTypeElement(Object.class).asType();
        }
    }

    /**
     * Throws {@link TypeNotPresentException} if {@code type} is an {@link
     * javax.lang.model.type.ErrorType}.
     * <p>
     * 当前类型判断，如果是ErrType，报错。
     */
    public static void checkTypePresent(TypeMirror type) {
        type.accept(
                // TODO(ronshapiro): Extract a base class that visits all components of a complex type
                // and put it in auto.common
                new SimpleTypeVisitor8<Void, Void>() {
                    @Override
                    public Void visitArray(ArrayType arrayType, Void p) {
                        //如果是数组，例如A[],对A在进行if else判断
                        return arrayType.getComponentType().accept(this, p);
                    }

                    @Override
                    public Void visitDeclared(DeclaredType declaredType, Void p) {
                        //如果是泛型，对泛型参数类型进行if else判断
                        declaredType.getTypeArguments().forEach(t -> t.accept(this, p));
                        return null;
                    }

                    @Override
                    public Void visitError(ErrorType errorType, Void p) {
                        throw new TypeNotPresentException(type.toString(), null);
                    }
                },
                null);
    }

    private static final ImmutableSet<Class<?>> FUTURE_TYPES =
            ImmutableSet.of(ListenableFuture.class, FluentFuture.class);

    public static boolean isFutureType(TypeMirror type) {
        return FUTURE_TYPES.stream().anyMatch(t -> MoreTypes.isTypeOf(t, type));
    }

    public static boolean hasTypeVariable(TypeMirror type) {
        //对type类型进行判断，相当于if else。如下
        return type.accept(
                new SimpleTypeVisitor8<Boolean, Void>() {
                    @Override
                    public Boolean visitArray(ArrayType arrayType, Void p) {
                        //if类型是一个数组，对数组类型再进行if else判断。例如A[],对A进行if else判断
                        return arrayType.getComponentType().accept(this, p);
                    }

                    @Override
                    public Boolean visitDeclared(DeclaredType declaredType, Void p) {//if 类或接口

                        //DeclaredType.getTypeArguments()返回此类型的实际类型参数。即对于嵌套在参数化类型中的类型，
                        // 如A<? extends Fragment，T>中的 <? extends Fragment>和T
                        //对泛型参数进行if else判断
                        return declaredType.getTypeArguments().stream().anyMatch(type -> type.accept(this, p));
                    }

                    @Override
                    public Boolean visitTypeVariable(TypeVariable t, Void aVoid) {// if 类型变量，返回true
                        return true;
                    }

                    @Override
                    protected Boolean defaultAction(TypeMirror e, Void aVoid) { //if 其他行为，返回false
                        return false;
                    }
                },
                null);
    }

    /**
     * Resolves the type of the given executable element as a member of the given type. This may
     * resolve type variables to concrete types, etc.
     * <p>
     * element是containerType成员并且返回element的Type类型
     */
    public ExecutableType resolveExecutableType(ExecutableElement element, TypeMirror containerType) {
        return MoreTypes.asExecutable(
                asMemberOf(
                        MoreTypes.asDeclared(containerType), element
                )
        );
    }

    // Implementation of Types methods, delegating to types.

    @Override
    public Element asElement(TypeMirror t) {
        return types.asElement(t);
    }

    @Override
    public boolean isSameType(TypeMirror t1, TypeMirror t2) {
        return types.isSameType(t1, t2);
    }

    @Override
    public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
        return types.isSubtype(t1, t2);
    }

    @Override
    public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
        //isAssignable(t1, t2):t1可分配给t2，例如t2表示Set<String>的add方法，t1表示"1"的类型，那么返回true
        return types.isAssignable(t1, t2);
    }

    @Override
    public boolean contains(TypeMirror t1, TypeMirror t2) {
        return types.contains(t1, t2);
    }

    @Override
    public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
        return types.isSubsignature(m1, m2);
    }

    @Override
    public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
        //返回t类型的直接超类（父类）
        return types.directSupertypes(t);
    }

    @Override
    public TypeMirror erasure(TypeMirror t) {
        return types.erasure(t);
    }

    @Override
    public TypeElement boxedClass(PrimitiveType p) {
        return types.boxedClass(p);
    }

    @Override
    public PrimitiveType unboxedType(TypeMirror t) {
        return types.unboxedType(t);
    }

    @Override
    public TypeMirror capture(TypeMirror t) {
        return types.capture(t);
    }

    @Override
    public PrimitiveType getPrimitiveType(TypeKind kind) {
        return types.getPrimitiveType(kind);
    }

    @Override
    public NullType getNullType() {
        return types.getNullType();
    }

    @Override
    public NoType getNoType(TypeKind kind) {
        return types.getNoType(kind);
    }

    @Override
    public ArrayType getArrayType(TypeMirror componentType) {
        return types.getArrayType(componentType);
    }

    @Override
    public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) {
        return types.getWildcardType(extendsBound, superBound);
    }

    @Override
    public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
        //getDeclaredType(typeElem, typeArgs)：使用typeElem包裹typeArgs,例如A<T,B>。A可以看做typeEle，T,B可以用typeArgs表示
        return types.getDeclaredType(typeElem, typeArgs);
    }

    @Override
    public DeclaredType getDeclaredType(
            DeclaredType containing, TypeElement typeElem, TypeMirror... typeArgs) {
        return types.getDeclaredType(containing, typeElem, typeArgs);
    }

    @Override
    public TypeMirror asMemberOf(DeclaredType containing, Element element) {
        //asMemberOf():element是否containing的成员，并且返回从containing的类型看的element的类型
        return types.asMemberOf(containing, element);
    }
}
