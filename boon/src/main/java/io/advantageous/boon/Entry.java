package io.advantageous.boon;

import java.io.Serializable;
import java.util.Map;

/**
* Created by Richard on 9/9/14.
*/
public interface Entry<K, V> extends Comparable<Entry>, Map.Entry<K, V>,
        Serializable, Cloneable {
    K key();

    V value();

    boolean equals(Entry o);
}
