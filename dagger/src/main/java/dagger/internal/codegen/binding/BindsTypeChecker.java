package dagger.internal.codegen.binding;

import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableList;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import dagger.internal.codegen.base.ContributionType;
import dagger.internal.codegen.langmodel.DaggerElements;
import dagger.internal.codegen.langmodel.DaggerTypes;

import static com.google.common.collect.Iterables.getOnlyElement;

/**
 * Checks the assignability of one type to another, given a {@link ContributionType} context. This
 * is used by {@link dagger.internal.codegen.validation.BindsMethodValidator} to validate that the
 * right-hand- side of a {@link dagger.Binds} method is valid, as well as in {@link
 * dagger.internal.codegen.writing.DelegateRequestRepresentation} when the right-hand-side in
 * generated code might be an erased type due to accessibility.
 */
public final class BindsTypeChecker {
    private final DaggerTypes types;
    private final DaggerElements elements;

    // TODO(bcorso): Make this pkg-private. Used by DelegateRequestRepresentation.
    @Inject
    public BindsTypeChecker(
            DaggerTypes types,
            DaggerElements elements
    ) {
        this.types = types;
        this.elements = elements;
    }

    /**
     * Checks the assignability of {@code rightHandSide} to {@code leftHandSide} given a {@link
     * ContributionType} context.
     * <p>
     * t1可分配给t2，例如t2表示Set<String>的add方法，t1表示"1"的类型，那么返回true
     */
    public boolean isAssignable(
            TypeMirror rightHandSide, TypeMirror leftHandSide, ContributionType contributionType) {
        //rightHandSide可以分配给desiredAssignableType(leftHandSide, contributionType)
        return types.isAssignable(rightHandSide, desiredAssignableType(leftHandSide, contributionType));
    }

    //根据多重绑定类型，获取对应的TypeMirror类型
    private TypeMirror desiredAssignableType(
            TypeMirror leftHandSide, ContributionType contributionType) {
        switch (contributionType) {
            case UNIQUE://没有使用多重绑定，直接返回
                return leftHandSide;
            case SET://使用@IntoSet修饰
                //返回类型使用Set<leftHandSide>包裹
                DeclaredType parameterizedSetType = types.getDeclaredType(setElement(), leftHandSide);

                //返回Set<leftHandSide>的add方法表示的TypeMirror类型
                return methodParameterType(parameterizedSetType, "add");

            case SET_VALUES:
                //如果使用@ElementsIntoSet修饰了，那么当前leftHandSide肯定是Set类型，返回leftHandSide的addAll方法表示的TypeMirror类型
                return methodParameterType(MoreTypes.asDeclared(leftHandSide), "addAll");

            case MAP://使用@IntoMap修饰
                //Map<T,leftHandSide>
                DeclaredType parameterizedMapType =
                        types.getDeclaredType(mapElement(), unboundedWildcard(), leftHandSide);
                //返回Map对象的put方法表示的TypeMirror类型
                return methodParameterTypes(parameterizedMapType, "put").get(1);
        }
        throw new AssertionError("Unknown contribution type: " + contributionType);
    }

    //获取type类或接口中与methodName名称相同的方法类型（有且仅存在一个，否则报错）
    private ImmutableList<TypeMirror> methodParameterTypes(DeclaredType type, String methodName) {
        ImmutableList.Builder<ExecutableElement> methodsForName = ImmutableList.builder();
        for (ExecutableElement method :
            // type.asElement().getEnclosedElements() is not used because some non-standard JDKs (e.g.
            // J2CL) don't redefine Set.add() (whose only purpose of being redefined in the standard JDK
            // is documentation, and J2CL's implementation doesn't declare docs for JDK types).
            // getLocalAndInheritedMethods ensures that the method will always be present.
                elements.getLocalAndInheritedMethods(MoreTypes.asTypeElement(type))) {

            if (method.getSimpleName().contentEquals(methodName)) {
                methodsForName.add(method);
            }
        }
        ExecutableElement method = getOnlyElement(methodsForName.build());
        return ImmutableList.copyOf(
                MoreTypes.asExecutable(types.asMemberOf(type, method)).getParameterTypes());
    }

    //获取type类或接口methodName方法（该方法名只会存在一个，否则报错）
    private TypeMirror methodParameterType(DeclaredType type, String methodName) {
        return getOnlyElement(methodParameterTypes(type, methodName));
    }

    private TypeElement setElement() {//返回Set类型
        return elements.getTypeElement(Set.class);
    }

    private TypeElement mapElement() {//返回Map类型
        return elements.getTypeElement(Map.class);
    }

    private TypeMirror unboundedWildcard() {//无界通配符
        return types.getWildcardType(null, null);
    }

}
