package dagger.internal.codegen.base;


import java.util.Map;
import java.util.function.Function;

/**
 * General utilities for the annotation processor.
 */
public final class Util {

    /**
     * A version of {@link Map#computeIfAbsent(Object, Function)} that allows {@code mappingFunction}
     * to update {@code map}.
     */
    public static <K, V> V reentrantComputeIfAbsent(
            Map<K, V> map,
            K key,
            Function<? super K, ? extends V> mappingFunction
    ) {
        V value = map.get(key);
        if (value == null) {
            value = mappingFunction.apply(key);
            if (value != null) {
                map.put(key, value);
            }
        }
        return value;
    }

    private Util() {
    }
}
