package dagger.internal.codegen.langmodel;

import com.google.auto.common.MoreElements;

import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.SimpleTypeVisitor8;

import static com.google.auto.common.MoreElements.getPackage;
import static com.google.common.base.Preconditions.checkArgument;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Utility methods for determining whether a {@linkplain TypeMirror type} or an {@linkplain Element
 * element} is accessible given the rules outlined in <a
 * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-6.html#jls-6.6">section 6.6 of the
 * Java Language Specification</a>.
 *
 * <p>This class only provides an approximation for accessibility. It does not always yield the same
 * result as the compiler, but will always err on the side of declaring something inaccessible. This
 * ensures that using this class will never result in generating code that will not compile.
 *
 * <p>Whenever compiler independence is not a requirement, the compiler-specific implementation of
 * this functionality should be preferred. For example, {@link
 * com.sun.source.util.Trees#isAccessible(com.sun.source.tree.Scope, TypeElement)} would be
 * preferable for {@code javac}.
 * <p>
 * 主要用于判断TypeMirror 类型或 Element节点可访问判断
 */
public final class Accessibility {

    /**
     * Returns true if the given type can be referenced from any package.
     *
     * 如果type可以被任意包名所引用
     */
    public static boolean isTypePubliclyAccessible(TypeMirror type) {
        return type.accept(new TypeAccessibilityVisitor(), null);
    }

    /**
     * Returns true if the given type can be referenced from code in the given package.
     * <p>
     * 当前的type里面的代码是否可被给定的包引用
     */
    public static boolean isTypeAccessibleFrom(TypeMirror type, String packageName) {
        return type.accept(new TypeAccessibilityVisitor(packageName), null);
    }

    private static boolean isTypeAccessibleFrom(TypeMirror type, Optional<String> packageName) {
        return type.accept(new TypeAccessibilityVisitor(packageName), null);
    }

    //类型判断
    private static final class TypeAccessibilityVisitor extends SimpleTypeVisitor6<Boolean, Void> {
        final Optional<String> packageName;

        TypeAccessibilityVisitor() {
            this(Optional.empty());
        }

        TypeAccessibilityVisitor(String packageName) {
            this(Optional.of(packageName));
        }

        TypeAccessibilityVisitor(Optional<String> packageName) {
            this.packageName = packageName;
        }

        boolean isAccessible(TypeMirror type) {
            return type.accept(this, null);
        }

        @Override
        public Boolean visitNoType(NoType type, Void p) {
            return true;
        }

        @Override
        public Boolean visitDeclared(DeclaredType type, Void p) {
            //if 是类或接口

            //1.对当前类父类型进行判断
            if (!isAccessible(type.getEnclosingType())) {
                // TODO(gak): investigate this check.  see comment in Binding
                return false;
            }

            //2.当前类型的节点进行判断
            if (!isElementAccessibleFrom(type.asElement(), packageName)) {
                return false;
            }

            //3.如果当前类或接口，携带了泛型，对泛型类型进行判断
            for (TypeMirror typeArgument : type.getTypeArguments()) {
                if (!isAccessible(typeArgument)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Boolean visitArray(ArrayType type, Void p) {
            //if 数组；例如A[],对A进行可访问性判断
            return type.getComponentType().accept(this, null);
        }

        @Override
        public Boolean visitPrimitive(PrimitiveType type, Void p) {
            //if 原始类型，原始类型都可访问
            return true;
        }

        @Override
        public Boolean visitNull(NullType type, Void p) {
            // if Nulltype类型
            return true;
        }

        @Override
        public Boolean visitTypeVariable(TypeVariable type, Void p) {
            // a _reference_ to a type variable is always accessible
            return true;
        }

        @Override
        public Boolean visitWildcard(WildcardType type, Void p) {
            // if 通配符类型

            //上界，e.g.<T extend List>。检查List可达性
            if (type.getExtendsBound() != null && !isAccessible(type.getExtendsBound())) {
                return false;
            }

            //下界，e.g.<T super List>。检查List可达性
            if (type.getSuperBound() != null && !isAccessible(type.getSuperBound())) {
                return false;
            }
            return true;
        }

        @Override
        protected Boolean defaultAction(TypeMirror type, Void p) {//其他类型
            throw new IllegalArgumentException(
                    String.format(
                            "%s of kind %s should not be checked for accessibility", type, type.getKind()));
        }
    }

    /**
     * Returns true if the given element can be referenced from any package.
     */
    public static boolean isElementPubliclyAccessible(Element element) {
        return element.accept(new ElementAccessibilityVisitor(), null);
    }

    /**
     * Returns true if the given element can be referenced from code in the given package.
     */
    // TODO(gak): account for protected
    public static boolean isElementAccessibleFrom(Element element, String packageName) {
        return element.accept(new ElementAccessibilityVisitor(packageName), null);
    }

    private static boolean isElementAccessibleFrom(Element element, Optional<String> packageName) {
        return element.accept(new ElementAccessibilityVisitor(packageName), null);
    }

    /**
     * Returns true if the given element can be referenced from other code in its own package.
     */
    public static boolean isElementAccessibleFromOwnPackage(Element element) {
        return isElementAccessibleFrom(
                element, MoreElements.getPackage(element).getQualifiedName().toString());
    }

    private static final class ElementAccessibilityVisitor
            extends SimpleElementVisitor6<Boolean, Void> {
        final Optional<String> packageName;

        ElementAccessibilityVisitor() {
            this(Optional.empty());
        }

        ElementAccessibilityVisitor(String packageName) {
            this(Optional.of(packageName));
        }

        ElementAccessibilityVisitor(Optional<String> packageName) {
            this.packageName = packageName;
        }

        @Override
        public Boolean visitPackage(PackageElement element, Void p) {
            //if 节点是包名节点，直接返回true
            return true;
        }

        @Override
        public Boolean visitType(TypeElement element, Void p) {
            // if 是一个类或接口节点
            switch (element.getNestingKind()) {//element.getNestingKind()类型大全
                case MEMBER://如果该类是一个内部类
                    return accessibleMember(element);
                case TOP_LEVEL://如果该类不是内部类
                    return accessibleModifiers(element);
                case ANONYMOUS://匿名内部类
                case LOCAL://方法中声明的类
                    return false;
            }
            throw new AssertionError();
        }

        //先对其父节点进行判断，如果不为false，则继续对当前节点的修饰符判断
        boolean accessibleMember(Element element) {

            if (!element.getEnclosingElement().accept(this, null)) {
                return false;
            }
            return accessibleModifiers(element);
        }

        //节点修饰符判断：
        // 1.public 为true；
        // 2.private为false；
        // 3.element所在的包 = 传递的参数packageName(前提条件是packageName存在)
        //4.其他的一律false
        boolean accessibleModifiers(Element element) {
            if (element.getModifiers().contains(PUBLIC)) {
                return true;
            } else if (element.getModifiers().contains(PRIVATE)) {
                return false;
            } else if (packageName.isPresent()
                    && getPackage(element).getQualifiedName().contentEquals(packageName.get())) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Boolean visitTypeParameter(TypeParameterElement element, Void p) {
            //if 是一个参数节点，这里检查可访问性没有任何意义
            throw new IllegalArgumentException(
                    "It does not make sense to check the accessibility of a type parameter");
        }

        @Override
        public Boolean visitExecutable(ExecutableElement element, Void p) {
            //if 是一个方法节点，对父节点继续本轮可访问检查，在可达情况下，对当前方法修饰符进行判断。
            return accessibleMember(element);
        }

        @Override
        public Boolean visitVariable(VariableElement element, Void p) {
            // if 是一个变量节点，必须是一个变量，并且对父节点进行本轮可访问检查，在可达情况下，对当前方法修饰符进行判断。
            ElementKind kind = element.getKind();
            checkArgument(kind.isField(), "checking a variable that isn't a field: %s", kind);
            return accessibleMember(element);
        }
    }

    //type类型判断，
    private static final TypeVisitor<Boolean, Optional<String>> RAW_TYPE_ACCESSIBILITY_VISITOR =
            new SimpleTypeVisitor8<Boolean, Optional<String>>() {
                @Override
                protected Boolean defaultAction(TypeMirror e, Optional<String> requestingPackage) {
                    //if 不是一个类或接口，访问type类型
                    return isTypeAccessibleFrom(e, requestingPackage);
                }

                @Override
                public Boolean visitDeclared(DeclaredType t, Optional<String> requestingPackage) {
                    //if 是一个类或接口，访问element节点
                    return isElementAccessibleFrom(t.asElement(), requestingPackage);
                }
            };

    /**
     * Returns true if the raw type of {@code type} is accessible from the given package.
     * <p>
     * 判断type类型是否可被requestingPackage包访问
     */
    public static boolean isRawTypeAccessible(TypeMirror type, String requestingPackage) {
        return type.accept(RAW_TYPE_ACCESSIBILITY_VISITOR, Optional.of(requestingPackage));
    }

    /**
     * Returns true if the raw type of {@code type} is accessible from any package.
     * <p>
     * 用于判断type类型是否可被任意包访问
     */
    public static boolean isRawTypePubliclyAccessible(TypeMirror type) {
        return type.accept(RAW_TYPE_ACCESSIBILITY_VISITOR, Optional.empty());
    }

    private Accessibility() {
    }
}
