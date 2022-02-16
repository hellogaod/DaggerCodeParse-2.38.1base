package dagger.internal;


import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Provider;

import static dagger.internal.DaggerCollections.newLinkedHashMapWithExpectedSize;
import static dagger.internal.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableMap;

/**
 * An {@code abstract} {@link Factory} implementation used to implement {@link Map} bindings.
 *
 * @param <K> the key type of the map that this provides
 * @param <V> the type that each contributing factory
 * @param <V2> the value type of the map that this provides
 */
abstract class AbstractMapFactory<K, V, V2> implements Factory<Map<K, V2>> {
    private final Map<K, Provider<V>> contributingMap;

    AbstractMapFactory(Map<K, Provider<V>> map) {
        this.contributingMap = unmodifiableMap(map);
    }

    /** The map of {@link Provider}s that contribute to this map binding. */
    final Map<K, Provider<V>> contributingMap() {
        return contributingMap;
    }

    /** A builder for {@link AbstractMapFactory}. */
    public abstract static class Builder<K, V, V2> {
        final LinkedHashMap<K, Provider<V>> map;

        Builder(int size) {
            this.map = newLinkedHashMapWithExpectedSize(size);
        }

        // Unfortunately, we cannot return a self-type here because a raw Provider type passed to one of
        // these methods affects the returned type of the method. The first put*() call erases the self
        // type to the "raw" self type, and the second erases the type to the upper bound
        // (AbstractMapFactory.Builder), which doesn't have a build() method.
        //
        // The methods are therefore not declared public so that each subtype will redeclare them and
        // expand their accessibility

        /** Associates {@code key} with {@code providerOfValue}. */
        Builder<K, V, V2> put(K key, Provider<V> providerOfValue) {
            map.put(checkNotNull(key, "key"), checkNotNull(providerOfValue, "provider"));
            return this;
        }

        Builder<K, V, V2> putAll(Provider<Map<K, V2>> mapOfProviders) {
            if (mapOfProviders instanceof DelegateFactory) {
                @SuppressWarnings("unchecked")
                DelegateFactory<Map<K, V2>> asDelegateFactory = (DelegateFactory) mapOfProviders;
                return putAll(asDelegateFactory.getDelegate());
            }
            @SuppressWarnings("unchecked")
            AbstractMapFactory<K, V, ?> asAbstractMapFactory =
                    ((AbstractMapFactory<K, V, ?>) (Provider) mapOfProviders);
            map.putAll(asAbstractMapFactory.contributingMap);
            return this;
        }
    }
}
