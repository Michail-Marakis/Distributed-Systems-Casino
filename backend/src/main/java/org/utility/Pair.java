package org.utility;

/**
 * The type Pair.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 */
public class Pair<K, V> {
    private final K first;
    private final V second;

    /**
     * Instantiates a new Pair.
     *
     * @param first  the first
     * @param second the second
     */
    public Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Gets first.
     *
     * @return the first
     */
    public K getFirst() {
        return first;
    }

    /**
     * Gets second.
     *
     * @return the second
     */
    public V getSecond() {
        return second;
    }
}