package dagger.internal;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Collection utility methods in service of Dagger internal classes. <em>Do not use</em> in client
 * code.
 */
public final class DaggerCollections {

    /**
     * The maximum value for a signed 32-bit integer that is equal to a power of 2.
     */
    private static final int MAX_POWER_OF_TWO = 1 << (Integer.SIZE - 2);

    private DaggerCollections() {
    }

    /**
     * Returns a new list that is pre-sized to {@code size}, or {@link Collections#emptyList()} if
     * empty. The list returned is never intended to grow beyond {@code size}, so adding to a list
     * when the size is 0 is an error.
     */
    public static <T> List<T> presizedList(int size) {
        if (size == 0) {
            return Collections.emptyList();
        }
        return new ArrayList<T>(size);
    }

    /**
     * Returns true if at least one pair of items in {@code list} are equals.
     * <p>
     * 校验是否存在相同的值
     */
    public static boolean hasDuplicates(List<?> list) {
        if (list.size() < 2) {
            return false;
        }
        Set<Object> asSet = new HashSet<Object>(list);
        return list.size() != asSet.size();
    }

    /**
     * Creates a {@link HashSet} instance, with a high enough "intial capcity" that it <em>should</em>
     * hold {@code expectedSize} elements without growth.
     */
    static <T> HashSet<T> newHashSetWithExpectedSize(int expectedSize) {
        return new HashSet<T>(calculateInitialCapacity(expectedSize));
    }

    /**
     * Creates a {@link LinkedHashMap} instance, with a high enough "initial capacity" that it
     * <em>should</em> hold {@code expectedSize} elements without growth.
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMapWithExpectedSize(int expectedSize) {
        return new LinkedHashMap<K, V>(calculateInitialCapacity(expectedSize));
    }

    //计算初始容量
    private static int calculateInitialCapacity(int expectedSize) {
        if (expectedSize < 3) {
            return expectedSize + 1;
        }
        if (expectedSize < MAX_POWER_OF_TWO) {
            // This is the calculation used in JDK8 to resize when a putAll
            // happens; it seems to be the most conservative calculation we
            // can make.  0.75 is the default load factor.
            return (int) (expectedSize / 0.75F + 1.0F);
        }
        return Integer.MAX_VALUE; // any large value
    }
}
