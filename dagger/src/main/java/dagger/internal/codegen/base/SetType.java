package dagger.internal.codegen.base;

import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.common.base.Equivalence;

import java.util.Set;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import dagger.spi.model.Key;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Information about a {@link Set} {@link TypeMirror}.
 * <p>
 * Set类型使用该对象表示
 */
@AutoValue
public abstract class SetType {

    /**
     * The set type itself, wrapped using {@link MoreTypes#equivalence()}. Use
     * {@link #declaredSetType()} instead.
     * <p>
     * 包裹的Set类型的类
     */
    protected abstract Equivalence.Wrapper<DeclaredType> wrappedDeclaredSetType();

    /**
     * The set type itself.
     */
    private DeclaredType declaredSetType() {
        return wrappedDeclaredSetType().get();
    }

    /**
     * {@code true} if the set type is the raw {@link Set} type.
     */
    public boolean isRawType() {
        return declaredSetType().getTypeArguments().isEmpty();
    }

    /**
     * The element type.
     * <p>
     * Set<T>里面的T
     */
    public TypeMirror elementType() {
        return declaredSetType().getTypeArguments().get(0);
    }


    /**
     * {@code true} if {@link #elementType()} is a {@code clazz}.
     * <p>
     * 判断Set<T>里面的T是否是clazz类
     */
    public boolean elementsAreTypeOf(Class<?> clazz) {
        return MoreTypes.isType(elementType()) && MoreTypes.isTypeOf(clazz, elementType());
    }

    /**
     * {@code T} if {@link #elementType()} is a {@code WrappingClass<T>}.
     *
     * @throws IllegalStateException    if {@link #elementType()} is not a {@code WrappingClass<T>}
     * @throws IllegalArgumentException if {@code wrappingClass} does not have exactly one type
     *                                  parameter
     */
    public TypeMirror unwrappedElementType(Class<?> wrappingClass) {

        //wrappingClass里面有且仅有一个泛型数据
        checkArgument(
                wrappingClass.getTypeParameters().length == 1,
                "%s must have exactly one type parameter",
                wrappingClass);

        //wrappingClass必须是Set<T>中T的类型
        checkArgument(
                elementsAreTypeOf(wrappingClass),
                "expected elements to be %s, but this type is %s",
                wrappingClass,
                declaredSetType());

        //Set<wrappingClass<T>>,返回的是T
        return MoreTypes.asDeclared(elementType()).getTypeArguments().get(0);
    }

    /**
     * {@code true} if {@code type} is a {@link Set} type.
     * <p>
     * 判断是一个Set类型
     */
    public static boolean isSet(TypeMirror type) {
        return MoreTypes.isType(type) && MoreTypes.isTypeOf(Set.class, type);
    }

    /**
     * {@code true} if {@code key.type()} is a {@link Set} type.
     */
    public static boolean isSet(Key key) {
        return isSet(key.type().java());
    }

    /**
     * Returns a {@link SetType} for {@code type}.
     * <p>
     * 如果type是一个Set类型，生成一个SetType对象
     *
     * @throws IllegalArgumentException if {@code type} is not a {@link Set} type
     */
    public static SetType from(TypeMirror type) {
        checkArgument(isSet(type), "%s must be a Set", type);
        return new AutoValue_SetType(MoreTypes.equivalence().wrap(MoreTypes.asDeclared(type)));
    }

    /**
     * Returns a {@link SetType} for {@code key}'s {@link Key#type() type}.
     *
     * @throws IllegalArgumentException if {@code key.type()} is not a {@link Set} type
     */
    public static SetType from(Key key) {
        return from(key.type().java());
    }
}
