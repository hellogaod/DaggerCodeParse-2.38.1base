package dagger.internal.codegen.binding;


import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor6;

import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.BindingKind;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.Scope;

import static com.google.common.base.Suppliers.memoize;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * An abstract type for classes representing a Dagger binding. Particularly, contains the {@link
 * Element} that generated the binding and the {@link DependencyRequest} instances that are required
 * to satisfy the binding, but leaves the specifics of the <i>mechanism</i> of the binding to the
 * subtypes.
 */
public abstract class Binding extends BindingDeclaration {


    /**
     * Returns {@code true} if using this binding requires an instance of the {@link
     * #contributingModule()}.
     * <p>
     * 是否需要生成一个module实例，必须满足：
     * 1.bindingElement和contributingModule都存着，
     * 2.bindingElement绑定节不能使用abstrct和static修饰
     */
    public boolean requiresModuleInstance() {
        if (!bindingElement().isPresent() || !contributingModule().isPresent()) {
            return false;
        }
        Set<Modifier> modifiers = bindingElement().get().getModifiers();
        return !modifiers.contains(ABSTRACT) && !modifiers.contains(STATIC);
    }

    /**
     * Returns {@code true} if this binding may provide {@code null} instead of an instance of {@link
     * #key()}. Nullable bindings cannot be requested from {@linkplain DependencyRequest#isNullable()
     * non-nullable dependency requests}.
     */
    public abstract boolean isNullable();

    /**
     * The kind of binding this instance represents.
     * <p>
     * 此实例表示的绑定类型。
     */
    public abstract BindingKind kind();

    /**
     * The {@link BindingType} of this binding. 不同绑定类型决定对应的框架类型
     */
    public abstract BindingType bindingType();

    /**
     * The {@link FrameworkType} of this binding. 框架类型有上面的bindingType绑定类型决定
     */
    public final FrameworkType frameworkType() {
        return FrameworkType.forBindingType(bindingType());
    }

    /**
     * The explicit set of {@link DependencyRequest dependencies} required to satisfy this binding as
     * defined by the user-defined injection sites.
     * <p>
     * 绑定的直接或显式依赖
     */
    public abstract ImmutableSet<DependencyRequest> explicitDependencies();

    /**
     * The set of {@link DependencyRequest dependencies} that are added by the framework rather than a
     * user-defined injection site. This returns an unmodifiable set.
     * <p>
     * 绑定的间接或隐藏的依赖
     */
    public ImmutableSet<DependencyRequest> implicitDependencies() {
        return ImmutableSet.of();
    }

    //绑定的依赖：一般情况下表示explicitDependencies；如果implicitDependencies隐式的存在，那么表示explicitDependencies + implicitDependencies。
    private final Supplier<ImmutableSet<DependencyRequest>> dependencies =
            memoize(
                    () -> {
                        ImmutableSet<DependencyRequest> implicitDependencies = implicitDependencies();
                        return ImmutableSet.copyOf(
                                implicitDependencies.isEmpty()
                                        ? explicitDependencies()
                                        : Sets.union(implicitDependencies, explicitDependencies()));
                    });

    /**
     * The set of {@link DependencyRequest dependencies} required to satisfy this binding. This is the
     * union of {@link #explicitDependencies()} and {@link #implicitDependencies()}. This returns an
     * unmodifiable set.
     */
    public final ImmutableSet<DependencyRequest> dependencies() {
        return dependencies.get();
    }

    /**
     * If this binding's key's type parameters are different from those of the {@link
     * #bindingTypeElement()}, this is the binding for the {@link #bindingTypeElement()}'s unresolved
     * type.
     * <p>
     * 如果绑定的key的参数不同于bindingTypeElement中的参数类型，那么表示未解析绑定
     */
    public abstract Optional<? extends Binding> unresolved();

    public Optional<Scope> scope() {
        return Optional.empty();
    }


    // TODO(sameb): Remove the TypeElement parameter and pull it from the TypeMirror.
    //如果element表示List，但是Type是List<T>,那么返回true：因为存在类型不一致情况
    static boolean hasNonDefaultTypeParameters(
            TypeElement element,
            TypeMirror type,
            DaggerTypes types
    ) {
        // If the element has no type parameters, nothing can be wrong.
        if (element.getTypeParameters().isEmpty()) {
            return false;
        }

        List<TypeMirror> defaultTypes = Lists.newArrayList();
        for (TypeParameterElement parameter : element.getTypeParameters()) {
            defaultTypes.add(parameter.asType());
        }

        List<TypeMirror> actualTypes =
                type.accept(
                        new SimpleTypeVisitor6<List<TypeMirror>, Void>() {
                            @Override
                            protected List<TypeMirror> defaultAction(TypeMirror e, Void p) {
                                return ImmutableList.of();
                            }

                            @Override
                            public List<TypeMirror> visitDeclared(DeclaredType t, Void p) {
                                return ImmutableList.<TypeMirror>copyOf(t.getTypeArguments());
                            }
                        },
                        null);

        // The actual type parameter size can be different if the user is using a raw type.
        if (defaultTypes.size() != actualTypes.size()) {
            return true;
        }

        for (int i = 0; i < defaultTypes.size(); i++) {
            if (!types.isSameType(defaultTypes.get(i), actualTypes.get(i))) {
                return true;
            }
        }
        return false;
    }
}
