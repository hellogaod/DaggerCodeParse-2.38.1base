package dagger.internal;

import java.util.Collections;
import java.util.Map;

import static dagger.internal.DaggerCollections.newLinkedHashMapWithExpectedSize;

/**
 * A fluent builder class that returns a {@link Map}. Used in component implementations where a map
 * must be created in one fluent statement for inlined request fulfillments.
 */
public final class MapBuilder<K, V> {
    private final Map<K, V> contributions;

    private MapBuilder(int size) {
        contributions = newLinkedHashMapWithExpectedSize(size);
    }

    /**
     * Creates a new {@link MapBuilder} with {@code size} elements.
     */
    public static <K, V> MapBuilder<K, V> newMapBuilder(int size) {
        return new MapBuilder<>(size);
    }

    public MapBuilder<K, V> put(K key, V value) {
        contributions.put(key, value);
        return this;
    }

    public MapBuilder<K, V> putAll(Map<K, V> map) {
        contributions.putAll(map);
        return this;
    }

    public Map<K, V> build() {
        if (contributions.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(contributions);
    }
}
