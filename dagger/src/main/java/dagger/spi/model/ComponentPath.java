package dagger.spi.model;


import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getLast;
import static java.util.stream.Collectors.joining;

/**
 * A path containing a component and all of its ancestor components.
 * <p>
 * 用于存储一个component类以及其所有的父级component
 */
@AutoValue
public abstract class ComponentPath {

    /**
     * Returns a new {@link ComponentPath} from {@code components}.
     */
    public static ComponentPath create(Iterable<DaggerTypeElement> components) {
        return new AutoValue_ComponentPath(ImmutableList.copyOf(components));
    }

    /**
     * Returns the component types, starting from the {@linkplain #rootComponent() root
     * component} and ending with the {@linkplain #currentComponent() current component}.
     */
    public abstract ImmutableList<DaggerTypeElement> components();

    /**
     * Returns the root {@link dagger.Component}- or {@link
     * dagger.producers.ProductionComponent}-annotated type
     */
    public final DaggerTypeElement rootComponent() {
        return components().get(0);
    }

    /**
     * Returns the component at the end of the path.
     */
    @Memoized
    public DaggerTypeElement currentComponent() {
        return getLast(components());
    }

    /**
     * Returns the parent of the {@linkplain #currentComponent()} current component}.
     * <p>
     * 获取currentComponent的父级component节点
     *
     * @throws IllegalStateException if the current graph is the {@linkplain #atRoot() root component}
     */
    public final DaggerTypeElement parentComponent() {
        checkState(!atRoot());
        //reverse():list集合数据逆转
        return components().reverse().get(1);
    }

    /**
     * Returns this path's parent path.
     * <p>
     * 去掉了components的最后一个item，然后生成一个新的ComponentPath
     *
     * @throws IllegalStateException if the current graph is the {@linkplain #atRoot() root component}
     */
    // TODO(ronshapiro): consider memoizing this
    public final ComponentPath parent() {
        checkState(!atRoot());
        return create(components().subList(0, components().size() - 1));
    }

    /**
     * Returns the path from the root component to the {@code child} of the current component.
     * <p>
     * 当前componentPath追加child子component类
     */
    public final ComponentPath childPath(DaggerTypeElement child) {
        return create(
                ImmutableList.<DaggerTypeElement>builder().addAll(components()).add(child).build());
    }

    /**
     * Returns {@code true} if the {@linkplain #currentComponent()} current component} is the
     * {@linkplain #rootComponent()} root component}.
     */
    public final boolean atRoot() {
        return components().size() == 1;
    }

    @Override
    public final String toString() {
        return components().stream()
                .map(DaggerTypeElement::className)
                .map(ClassName::canonicalName)
                .collect(joining(" → "));
    }

    @Memoized
    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
