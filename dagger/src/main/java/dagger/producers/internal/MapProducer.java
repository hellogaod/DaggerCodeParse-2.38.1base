package dagger.producers.internal;


import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import dagger.producers.Producer;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

/**
 * A {@link Producer} implementation used to implement {@link Map} bindings. This producer returns a
 * {@code Map<K, V>} which is populated by calls to the delegate {@link Producer#get} methods.
 */
public final class MapProducer<K, V> extends AbstractMapProducer<K, V, V> {
    private MapProducer(ImmutableMap<K, Producer<V>> contributingMap) {
        super(contributingMap);
    }

    /** Returns a new {@link Builder}. */
    public static <K, V> Builder<K, V> builder(int size) {
        return new Builder<>(size);
    }

    /** A builder for {@link MapProducer} */
    public static final class Builder<K, V> extends AbstractMapProducer.Builder<K, V, V> {
        private Builder(int size) {
            super(size);
        }

        @Override
        public Builder<K, V> put(K key, Producer<V> producerOfValue) {
            super.put(key, producerOfValue);
            return this;
        }

        @Override
        public Builder<K, V> put(K key, Provider<V> providerOfValue) {
            super.put(key, providerOfValue);
            return this;
        }

        @Override
        public Builder<K, V> putAll(Producer<Map<K, V>> mapProducer) {
            super.putAll(mapProducer);
            return this;
        }

        /** Returns a new {@link MapProducer}. */
        public MapProducer<K, V> build() {
            return new MapProducer<>(mapBuilder.build());
        }
    }

    @Override
    protected ListenableFuture<Map<K, V>> compute() {
        final List<ListenableFuture<Map.Entry<K, V>>> listOfEntries = new ArrayList<>();
        for (final Map.Entry<K, Producer<V>> entry : contributingMap().entrySet()) {
            listOfEntries.add(
                    Futures.transform(
                            entry.getValue().get(),
                            new Function<V, Map.Entry<K, V>>() {
                                @Override
                                public Map.Entry<K, V> apply(V computedValue) {
                                    return Maps.immutableEntry(entry.getKey(), computedValue);
                                }
                            },
                            directExecutor()));
        }

        return Futures.transform(
                Futures.allAsList(listOfEntries),
                new Function<List<Map.Entry<K, V>>, Map<K, V>>() {
                    @Override
                    public Map<K, V> apply(List<Map.Entry<K, V>> entries) {
                        return ImmutableMap.copyOf(entries);
                    }
                },
                directExecutor());
    }
}
