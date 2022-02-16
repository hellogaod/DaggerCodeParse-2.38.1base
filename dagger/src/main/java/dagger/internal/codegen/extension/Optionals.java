package dagger.internal.codegen.extension;


import java.util.Comparator;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.asList;

/** Utilities for {@link Optional}s. */
public final class Optionals {


    public static <T> Comparator<Optional<T>> emptiesLast(Comparator<? super T> valueComparator) {
        checkNotNull(valueComparator);
        return Comparator.comparing(o -> o.orElse(null), Comparator.nullsLast(valueComparator));
    }

    /** Returns the first argument that is present, or empty if none are. */
    @SafeVarargs
    public static <T> Optional<T> firstPresent(
            Optional<T> first, Optional<T> second, Optional<T>... rest) {
        return asList(first, second, rest).stream()
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

}
