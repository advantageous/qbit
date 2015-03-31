/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
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
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.util;

import java.util.*;

/**
 * Created by Richard on 9/26/14.
 *
 * @author rhightower
 */
public interface MultiMap<K, V> extends Iterable<Map.Entry<K, Collection<V>>>, Map<K, V> {
    static final MultiMap EMPTY = new MultiMap() {

        private Map empty = Collections.emptyMap();

        @Override
        public Iterator<Entry> iterator() {
            return empty.entrySet().iterator();
        }

        @Override
        public void add(Object key, Object o) {
        }

        @Override
        public Object getFirst(Object key) {
            return null;
        }

        @Override
        public Iterable getAll(Object key) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public boolean removeValueFrom(Object key, Object o) {
            return false;
        }

        @Override
        public boolean removeMulti(Object key) {
            return false;
        }

        @Override
        public Iterable keySetMulti() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Iterable valueMulti() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public void putAll(MultiMap params) {
        }

        @Override
        public Map baseMap() {
            return empty;
        }

        @Override
        public Object getSingleObject(Object name) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public Object get(Object key) {
            return empty.get(key);
        }

        @Override
        public Object put(Object key, Object value) {
            return empty.put(key, value);
        }

        @Override
        public Object remove(Object key) {
            return empty.remove(key);
        }

        @Override
        public void putAll(Map m) {
            empty.putAll(m);
        }

        @Override
        public void clear() {
            empty.clear();
        }

        @Override
        public Set keySet() {
            return empty.keySet();
        }

        @Override
        public Collection values() {
            return empty.values();
        }

        @Override
        public Set<Entry> entrySet() {
            return empty.entrySet();
        }
    };

    @SuppressWarnings("unchecked")
    static <K, V> MultiMap<K, V> empty() {
        return EMPTY;
    }

    Iterator<Entry<K, Collection<V>>> iterator();

    default void add(K key, V v) {
        {
            throw new UnsupportedOperationException("add");
        }
    }

    V getFirst(K key);

    Iterable<V> getAll(K key);

    default boolean removeValueFrom(K key, V v) {
        throw new UnsupportedOperationException("removeValueFrom");
    }

    default boolean removeMulti(K key) {
        throw new UnsupportedOperationException("removeMulti");
    }

    Iterable<K> keySetMulti();

    default void putAll(MultiMap<K, V> params) {
        throw new UnsupportedOperationException("putAll");
    }

    default Map<? extends K, ? extends Collection<V>> baseMap() {
        throw new UnsupportedOperationException("baseMap");
    }

    public V getSingleObject(K name);

    @Override
    default public boolean containsValue(Object value) {

        throw new UnsupportedOperationException("Unsupported");

    }

    default public V put(K key, V value) {
        throw new UnsupportedOperationException("Unsupported");

    }

    default public V remove(Object key) {

        throw new UnsupportedOperationException("Unsupported");
    }

    default void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    default void clear() {

        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    default Collection<V> values() {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    default Set<Entry<K, V>> entrySet() {

        return Collections.emptySet();
    }

    default Iterable<V> valueMulti() {
        throw new UnsupportedOperationException("Unsupported");
    }




}
