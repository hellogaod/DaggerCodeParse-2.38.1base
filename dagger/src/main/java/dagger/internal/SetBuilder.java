package dagger.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dagger.internal.Preconditions.checkNotNull;

/**
 * A fluent builder class that returns a {@link Set}. Used in component implementations where a set
 * must be created in one fluent statement for inlined request fulfillments.
 */
public final class SetBuilder<T> {
    private static final String SET_CONTRIBUTIONS_CANNOT_BE_NULL =
            "Set contributions cannot be null";
    private final List<T> contributions;

    private SetBuilder(int estimatedSize) {
        contributions = new ArrayList<>(estimatedSize);
    }

    /**
     * {@code estimatedSize} is the number of bindings which contribute to the set. They may each
     * provide {@code [0..n)} instances to the set. Because the final size is unknown, {@code
     * contributions} are collected in a list and only hashed in {@link #build()}.
     */
    public static <T> SetBuilder<T> newSetBuilder(int estimatedSize) {
        return new SetBuilder<T>(estimatedSize);
    }

    public SetBuilder<T> add(T t) {
        contributions.add(checkNotNull(t, SET_CONTRIBUTIONS_CANNOT_BE_NULL));
        return this;
    }

    public SetBuilder<T> addAll(Collection<? extends T> collection) {
        for (T item : collection) {
            checkNotNull(item, SET_CONTRIBUTIONS_CANNOT_BE_NULL);
        }
        contributions.addAll(collection);
        return this;
    }

    public Set<T> build() {
        if (contributions.isEmpty()) {
            return Collections.emptySet();
        } else if (contributions.size() == 1) {
            return Collections.singleton(contributions.get(0));
        } else {
            return Collections.unmodifiableSet(new HashSet<>(contributions));
        }
    }
}
