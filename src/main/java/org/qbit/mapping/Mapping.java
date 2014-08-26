package org.qbit.mapping;

/**
 * Created by Richard on 7/18/14.
 * Represents an asynchronous mapping.
 * @author Richard Hightower
 */
public interface Mapping<K, V> {

    InputMap<K, V> get();

    OutputMap<K, V> set();

    InputOutputMap<K, V> put();

}
