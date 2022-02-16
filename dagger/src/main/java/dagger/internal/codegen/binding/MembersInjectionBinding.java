package dagger.internal.codegen.binding;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import java.util.Optional;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import dagger.spi.model.BindingKind;
import dagger.spi.model.DependencyRequest;

import static com.google.auto.common.MoreElements.isAnnotationPresent;
import static java.util.stream.Collectors.toList;


/**
 * Represents the full members injection of a particular type.
 * <p>
 * 成员注入（1）component类中有且仅有一个参数并且方法返回类型是void或返回类型一致，该参数作为一个成员注入；
 * （2）对使用Inject修饰的变量或方法所在类;
 */
@AutoValue
public abstract class MembersInjectionBinding extends Binding {

    @Override
    public final Optional<Element> bindingElement() {
        return Optional.of(membersInjectedType());
    }

    public abstract TypeElement membersInjectedType();//Inject修饰的节点所在类

    @Override
    public abstract Optional<MembersInjectionBinding> unresolved();

    @Override
    public Optional<TypeElement> contributingModule() {
        return Optional.empty();
    }

    /**
     * The set of individual sites where {@link Inject} is applied.
     */
    public abstract ImmutableSortedSet<InjectionSite> injectionSites();

    @Override
    public BindingType bindingType() {
        return BindingType.MEMBERS_INJECTION;
    }

    @Override
    public BindingKind kind() {
        return BindingKind.MEMBERS_INJECTION;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    /**
     * Returns {@code true} if any of this binding's injection sites are directly on the bound type.
     * <p>
     * 如果此绑定的任何注入站点直接位于绑定类型上，则返回 {@code true}。
     */
    public boolean hasLocalInjectionSites() {
        return injectionSites()
                .stream()
                .anyMatch(
                        injectionSite ->
                                injectionSite.element().getEnclosingElement().equals(membersInjectedType()));
    }

    @Override
    public boolean requiresModuleInstance() {
        return false;
    }

    @Memoized
    @Override
    public abstract int hashCode();

    // TODO(ronshapiro,dpb): simplify the equality semantics
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Metadata about a field or method injection site.
     * <p>
     * 使用Inject注解的方法或变量
     */
    @AutoValue
    public abstract static class InjectionSite {
        /**
         * The type of injection site.
         */
        public enum Kind {
            FIELD,
            METHOD,
        }

        public abstract Kind kind();

        public abstract Element element();

        public abstract ImmutableSet<DependencyRequest> dependencies();


        /**
         * Returns the index of {@link #element()} in its parents {@code @Inject} members that have the
         * same simple name. This method filters out private elements so that the results will be
         * consistent independent of whether the build system uses header jars or not.
         * <p>
         * 当前节点所在类里面所有的节点通过条件筛选：1.使用了Inject注解；并且2.没有使用private修饰；
         * 后当前节点所在位置
         */
        @Memoized
        public int indexAmongAtInjectMembersWithSameSimpleName() {
            return element()
                    .getEnclosingElement()
                    .getEnclosedElements()
                    .stream()
                    .filter(element -> isAnnotationPresent(element, Inject.class))
                    .filter(element -> !element.getModifiers().contains(Modifier.PRIVATE))
                    .filter(element -> element.getSimpleName().equals(this.element().getSimpleName()))
                    .collect(toList())
                    .indexOf(element());
        }

        public static InjectionSite field(VariableElement element, DependencyRequest dependency) {
            return new AutoValue_MembersInjectionBinding_InjectionSite(
                    Kind.FIELD, element, ImmutableSet.of(dependency));
        }

        public static InjectionSite method(
                ExecutableElement element, Iterable<DependencyRequest> dependencies) {
            return new AutoValue_MembersInjectionBinding_InjectionSite(
                    Kind.METHOD, element, ImmutableSet.copyOf(dependencies));
        }
    }
}
