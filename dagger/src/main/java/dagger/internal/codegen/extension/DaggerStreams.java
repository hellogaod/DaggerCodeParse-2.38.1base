package dagger.internal.codegen.extension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * Utilities for streams.
 */
public final class DaggerStreams {

    /**
     * Returns a {@link Collector} that accumulates the input elements into a new {@link
     * ImmutableList}, in encounter order.
     */
    // TODO(b/68008628): Use ImmutableList.toImmutableList().
    public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {
        return collectingAndThen(toList(), ImmutableList::copyOf);
    }

    /**
     * A function that you can use to extract the present values from a stream of {@link Optional}s.
     *
     * <pre>{@code
     * Set<Foo> foos =
     *     optionalFoos()
     *         .flatMap(DaggerStreams.presentValues())
     *         .collect(toSet());
     * }</pre>
     */
    public static <T> Function<Optional<T>, Stream<T>> presentValues() {
        return optional -> optional.map(Stream::of).orElse(Stream.empty());
    }

    /**
     * Returns a {@link Collector} that accumulates the input elements into a new {@link
     * ImmutableSet}, in encounter order.
     * <p>
     * 转换为新的ImmutableSet对象
     */
    // TODO(b/68008628): Use ImmutableSet.toImmutableSet().
    public static <T> Collector<T, ?, ImmutableSet<T>> toImmutableSet() {
        return collectingAndThen(toList(), ImmutableSet::copyOf);
    }

    /**
     * Returns a {@link Collector} that accumulates elements into an {@code ImmutableMap} whose keys
     * and values are the result of applying the provided mapping functions to the input elements.
     * Entries appear in the result {@code ImmutableMap} in encounter order.
     * <p>
     * 转换成Map类型集合
     */
    // TODO(b/68008628): Use ImmutableMap.toImmutableMap().
    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
            Function<? super T, K> keyMapper, Function<? super T, V> valueMapper) {
        return Collectors.mapping(
                value -> Maps.immutableEntry(keyMapper.apply(value), valueMapper.apply(value)),
                Collector.of(
                        ImmutableMap::builder,
                        (ImmutableMap.Builder<K, V> builder, Map.Entry<K, V> entry) -> builder.put(entry),
                        (left, right) -> left.putAll(right.build()),
                        ImmutableMap.Builder::build));
    }

    /**
     * Returns a {@link Collector} that accumulates elements into an {@code ImmutableSetMultimap}
     * whose keys and values are the result of applying the provided mapping functions to the input
     * elements. Entries appear in the result {@code ImmutableSetMultimap} in encounter order.
     */
    // TODO(b/68008628): Use ImmutableSetMultimap.toImmutableSetMultimap().
    public static <T, K, V> Collector<T, ?, ImmutableSetMultimap<K, V>> toImmutableSetMultimap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper
    ) {
        return Collectors.mapping(
                value -> Maps.immutableEntry(keyMapper.apply(value), valueMapper.apply(value)),
                Collector.of(
                        ImmutableSetMultimap::builder,
                        (ImmutableSetMultimap.Builder<K, V> builder, Map.Entry<K, V> entry) ->
                                builder.put(entry),
                        (left, right) -> left.putAll(right.build()),
                        ImmutableSetMultimap.Builder::build));
    }

    /**
     * Returns a function from {@link Object} to {@code Stream<T>}, which returns a stream containing
     * its input if its input is an instance of {@code T}.
     *
     * <p>Use as an argument to {@link Stream#flatMap(Function)}:
     *
     * <pre>{@code Stream<Bar>} barStream = fooStream.flatMap(instancesOf(Bar.class));</pre>
     */
    public static <T> Function<Object, Stream<T>> instancesOf(Class<T> to) {
        return f -> to.isInstance(f) ? Stream.of(to.cast(f)) : Stream.empty();
    }

    /**
     * Returns a stream of all values of the given {@code enumType}.
     * <p>
     * Class类型转换成枚举
     */
    public static <E extends Enum<E>> Stream<E> valuesOf(Class<E> enumType) {
        return EnumSet.allOf(enumType).stream();
    }

    /**
     * Returns a sequential {@link Stream} of the contents of {@code iterable}, delegating to {@link
     * Collection#stream} if possible.
     */
    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return (iterable instanceof Collection)
                ? ((Collection<T>) iterable).stream()
                : StreamSupport.stream(iterable.spliterator(), false);
    }

    private DaggerStreams() {
    }
}
