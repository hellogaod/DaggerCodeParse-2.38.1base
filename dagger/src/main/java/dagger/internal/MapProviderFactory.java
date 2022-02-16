package dagger.internal;


import java.util.Map;

import javax.inject.Provider;

import dagger.Lazy;

/**
 * A {@link Factory} implementation used to implement {@link Map} bindings. This factory returns a
 * {@code Map<K, Provider<V>>} when calling {@link #get} (as specified by {@link Factory}).
 */
public final class MapProviderFactory<K, V> extends AbstractMapFactory<K, V, Provider<V>>
        implements Lazy<Map<K, Provider<V>>> {

    /** Returns a new {@link Builder} */
    public static <K, V> Builder<K, V> builder(int size) {
        return new Builder<>(size);
    }

    private MapProviderFactory(Map<K, Provider<V>> contributingMap) {
        super(contributingMap);
    }

    /**
     * Returns a {@code Map<K, Provider<V>>} whose iteration order is that of the elements given by
     * each of the providers, which are invoked in the order given at creation.
     */
    @Override
    public Map<K, Provider<V>> get() {
        return contributingMap();
    }

    /** A builder for {@link MapProviderFactory}. */
    public static final class Builder<K, V> extends AbstractMapFactory.Builder<K, V, Provider<V>> {
        private Builder(int size) {
            super(size);
        }

        @Override
        public Builder<K, V> put(K key, Provider<V> providerOfValue) {
            super.put(key, providerOfValue);
            return this;
        }

        @Override
        public Builder<K, V> putAll(Provider<Map<K, Provider<V>>> mapProviderFactory) {
            super.putAll(mapProviderFactory);
            return this;
        }

        /** Returns a new {@link MapProviderFactory}. */
        public MapProviderFactory<K, V> build() {
            return new MapProviderFactory<>(map);
        }
    }
}
