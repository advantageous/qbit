package io.advantageous.boon.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Richard on 9/26/14.
 */
public interface MultiMap<K, V> extends Iterable<Map.Entry<K, Collection<V>>>, Map<K, V>{
    Iterator<Map.Entry<K, Collection<V>>> iterator();

    void add(K key, V v);

    V getFirst(K key);

    Iterable<V> getAll(K key);

    boolean removeValueFrom(K key, V v);

    boolean removeMulti(K key);

    Iterable<K> keySetMulti();

    Iterable<V> valueMulti();

    void putAll(MultiMap<K, V> params);

    Map<? extends K,? extends Collection<V>> baseMap();


    public V getSingleObject(K name);
}
