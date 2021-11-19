package dagger.internal.codegen.validation;


import com.google.auto.common.MoreTypes;
import com.google.auto.common.SuperficialValidation;
import com.google.common.base.Equivalence;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.langmodel.DaggerTypes;

/**
 * Utility methods for validating the type hierarchy of a given type.
 * <p>
 * 用于验证给定类型的类型层次结构的实用方法。
 */
final class TypeHierarchyValidator {
    private TypeHierarchyValidator() {
    }

    /**
     * Validate the type hierarchy of the given type including all super classes, interfaces, and
     * type parameters.
     * <p>
     * 验证给定类型的类型层次结构，包括所有超类、接口和类型参数。
     *
     * @throws TypeNotPresentException if an type in the hierarchy is not valid.
     */
    public static void validateTypeHierarchy(TypeMirror type, DaggerTypes types) {
        Queue<TypeMirror> queue = new ArrayDeque<>();
        Set<Equivalence.Wrapper<TypeMirror>> queued = new HashSet<>();
        queue.add(type);
        queued.add(MoreTypes.equivalence().wrap(type));

        //Queue外面再循环，里面可能在添加
        while (!queue.isEmpty()) {

            TypeMirror currType = queue.remove();

            //如果不是一个正确的类或接口，报异常
            if (!SuperficialValidation.validateType(currType)) {
                throw new TypeNotPresentException(currType.toString(), null);
            }

            for (TypeMirror superType : types.directSupertypes(currType)) {
                if (queued.add(MoreTypes.equivalence().wrap(superType))) {
                    queue.add(superType);
                }
            }
        }
    }
}
