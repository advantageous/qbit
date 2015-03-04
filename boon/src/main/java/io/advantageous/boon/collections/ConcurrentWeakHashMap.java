/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

package io.advantageous.boon.collections;


import io.advantageous.boon.Exceptions;
import io.advantageous.boon.Pair;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.advantageous.boon.Exceptions.die;

/**
 * I got a lot of ideas from concurrent java books. and looking at open source implementations of this
 * This is written from scratch. It was heavily influenced by IntelliJ's ConcurrentWeakHashMap open source
 * project. Theirs is better. Mine is good enough for boon's needs.
 *
 * @param <K> key
 * @param <V> value
 */
public class ConcurrentWeakHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {

    static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final ThreadLocal<HardRefKeyValue> HARD_REF = new ThreadLocal<HardRefKeyValue>() {
        @Override
        protected HardRefKeyValue initialValue() {
            return new HardRefKeyValue();
        }
    };
    private final ConcurrentHashMap<KeyValue<K, V>, V> map;
    private final ReferenceQueue<K> referenceQueue = new ReferenceQueue<>();
    private EntrySet entrySet;

    public ConcurrentWeakHashMap(int initialCapacity,
                                 float loadFactor,
                                 int concurrencyLevel ) {

        map = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }

    public ConcurrentWeakHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL);
    }


    public ConcurrentWeakHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }


    public ConcurrentWeakHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }

    private HardRefKeyValue<K, V> createHardRef(K k) {
        HardRefKeyValue hardKey = HARD_REF.get();
        hardKey.set(k, null, k.hashCode());
        return hardKey;
    }


    @Override
    public V get(Object key) {

        if (key == null) {
            Exceptions.die("Null keys not allowed");
        }
        final HardRefKeyValue<K, V> hardRef = createHardRef((K) key);

        V result = map.get(hardRef);
        hardRef.clear();
        return result;
    }

    @Override
    public V put(K key, V value) {
        evictEntires();

        if (key == null) {
            Exceptions.die("No null keys");
        }
        KeyValue<K, V> weakKey = createWeakKey(key, value);
        return map.put(weakKey, value);

    }

    @Override
    public V remove(Object key) {
        evictEntires();


        if (key == null) {
            Exceptions.die("Null keys not allowed");
        }
        final HardRefKeyValue<K, V> hardRef = createHardRef((K) key);

        V removedValue = map.remove(hardRef);

        hardRef.clear();

        return removedValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {

        final Set<? extends Entry<? extends K, ? extends V>> entries = map.entrySet();

        for (Entry<? extends K, ? extends V> entry : entries) {
            this.put(entry.getKey(), entry.getValue());
        }

    }


    @Override
    public int size() {
        return entrySet().size();
    }

    @Override
    public void clear() {
        evictEntires();
        map.clear();
    }


    @Override
    public Collection<V> values() {
        evictEntires();
        return map.values();
    }

    @Override
    public boolean isEmpty() {
        return entrySet().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {


        if (key == null) {
            Exceptions.die("Null keys not allowed");
        }
        final HardRefKeyValue<K, V> hardRef = createHardRef((K) key);

        boolean containsKey = map.containsKey(hardRef);

        hardRef.clear();
        return containsKey;
    }

    @Override
    public boolean containsValue(Object value) {


        return map.containsKey(value);

    }

    private boolean evictEntires() {
        WeakRefKeyValue<K, V> weakKeyValue = (WeakRefKeyValue<K, V>) referenceQueue.poll();

        boolean processed = false;

        while (weakKeyValue != null) {
            V value = weakKeyValue.getValue();
            map.remove(weakKeyValue, value);
            processed = true;
        }
        return processed;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) entrySet = new EntrySet();
        return entrySet;
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        evictEntires();
        return map.putIfAbsent(createWeakKey(key, value), value);
    }

    private KeyValue<K, V> createWeakKey(K key, V value) {
        return new WeakRefKeyValue<>(key, value, referenceQueue);
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        evictEntires();
        return map.remove(createWeakKey((K) key, (V) value), value);
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        evictEntires();
        return map.replace(createWeakKey(key, oldValue), oldValue, newValue);
    }

    @Override
    public V replace(final K key, final V value) {
        evictEntires();
        return map.replace(createWeakKey(key, value), value);
    }

    private static interface KeyValue<K, V> {


        public K getKey();

        public V getValue();


        public int hashCode();

    }

    private static class HardRefKeyValue<K, V> implements KeyValue<K, V> {

        K key;
        V value;
        int hashCode;


        void set(K key, V value, int hashCode) {
            this.key = key;
            this.value = value;
            this.hashCode = hashCode;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        public void clear() {
            value = null;
            hashCode = 0;
            key = null;
        }


        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof KeyValue)) {
                return false;
            }
            Object ours = getKey();

            Object theirs = ((KeyValue) o).getKey();
            if (theirs == null || ours == null) {
                return false;
            }

            if (ours == theirs) {
                return true;
            }
            return ours.equals(theirs);
        }

        public int hashCode() {
            return hashCode;
        }
    }

    private static class WeakRefKeyValue<K, V> extends WeakReference<K> implements KeyValue {
        protected final ReferenceQueue<K> referenceQueue;
        private final int hashCode;
        private final V value;

        private WeakRefKeyValue(K referent, V v, ReferenceQueue<K> q) {
            super(referent, q);
            value = v;
            this.hashCode = referent.hashCode();
            this.referenceQueue = q;
        }

        public K getKey() {
            return this.get();
        }

        public V getValue() {
            return value;
        }


        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof KeyValue)) {
                return false;
            }
            Object ours = get();

            Object theirs = ((KeyValue) o).getKey();
            if (theirs == null || ours == null) {
                return false;
            }

            if (ours == theirs) {
                return true;
            }
            return ours.equals(theirs);
        }

        public int hashCode() {
            return hashCode;
        }
    }

    /* Internal class for entry sets */
    private class EntrySet extends AbstractSet<Entry<K, V>> {

        final Iterator<Map.Entry<KeyValue<K, V>, V>> iterator = map.entrySet().iterator();

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new Iterator<Map.Entry<K, V>>() {
                Pair<K, V> next = null;

                @Override
                public boolean hasNext() {
                    next = null;

                    while (iterator.hasNext()) {
                        Map.Entry<KeyValue<K, V>, V> entry = iterator.next();
                        KeyValue<K, V> kv = entry.getKey();
                        K key = kv != null ? kv.getKey() : null;
                        if (key == null) {
                            continue;
                        }
                        next = new Pair<>(key, kv.getValue());

                    }
                    return next != null;
                }

                @Override
                public Map.Entry<K, V> next() {
                    return next;
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        }

        @Override
        public boolean isEmpty() {
            return !iterator().hasNext();
        }

        @Override
        public int size() {
            int count = 0;
            for (Iterator i = iterator(); i.hasNext(); i.next()) {
                count++;
            }
            return count;
        }

        @Override
        public boolean remove(Object o) {
            evictEntires();


            HardRefKeyValue<K, V> key = createHardRef((K) o);


            boolean removed = map.remove(key) != null;

            key.clear();
            return removed;
        }

    }

}
