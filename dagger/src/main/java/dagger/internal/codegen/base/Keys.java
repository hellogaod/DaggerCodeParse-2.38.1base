package dagger.internal.codegen.base;


import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor6;

import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.Key;

/**
 * Utility methods related to {@link Key}s.
 */
public final class Keys {

    /**
     * Returns {@code true} if a key with {@code qualifier} and {@code type} is valid as an implicit
     * key (that is, if it's valid for a just-in-time binding by discovering an {@code @Inject}
     * constructor).
     * <p>
     * 如果具有 {@code qualifier} 和 {@code type} 的键作为隐式键有效（即，如果它通过发现 {@code @Inject 对即时绑定有效），
     * 则返回 {@code true} } 构造函数）。
     */
    public static boolean isValidImplicitProvisionKey(
            Optional<? extends AnnotationMirror> qualifier,
            TypeMirror type,
            final DaggerTypes types
    ) {
        // Qualifiers disqualify implicit provisioning.
        if (qualifier.isPresent()) {//1.不存在Qualifier注解，返回false
            return false;
        }

        return type.accept(
                new SimpleTypeVisitor6<Boolean, Void>(false) {
                    @Override
                    public Boolean visitDeclared(DeclaredType type, Void ignored) {//只有是类或接口的情况下才去检查

                        // Non-classes or abstract classes aren't allowed.
                        TypeElement element = MoreElements.asType(type.asElement());

                        //2.如果节点不是类 || 节点修饰符是使用abstract修饰 ，返回false
                        if (!element.getKind().equals(ElementKind.CLASS)
                                || element.getModifiers().contains(Modifier.ABSTRACT)) {
                            return false;
                        }

                        // If the key has type arguments, validate that each type argument is declared.
                        // Otherwise the type argument may be a wildcard (or other type), and we can't
                        // resolve that to actual types.
                        //3.如果节点使用了泛型，并且泛型类型有存在不是接口或类的kind，返回false
                        for (TypeMirror arg : type.getTypeArguments()) {
                            if (arg.getKind() != TypeKind.DECLARED) {
                                return false;
                            }
                        }

                        // Also validate that the key is not the erasure of a generic type.
                        // If it is, that means the user referred to Foo<T> as just 'Foo',
                        // which we don't allow.  (This is a judgement call -- we *could*
                        // allow it and instantiate the type bounds... but we don't.)
                        //erasure()返回类型擦除后的类型

                        //4.节点泛型为空 || type和element类型擦除后的类型一直，则返回true
                        return MoreTypes.asDeclared(element.asType()).getTypeArguments().isEmpty()
                                || !types.isSameType(types.erasure(element.asType()), type);
                    }
                },
                null);
    }
}
