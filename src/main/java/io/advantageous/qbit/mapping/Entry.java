package io.advantageous.qbit.mapping;

/**
 * Represents a key value entry.
 * @author Richard Hightower
 */
public interface Entry <K, V> {

    K key();
    V value();

}
