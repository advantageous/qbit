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
 * created by Richard on 9/26/14.
 *
 * @author rhightower
 */
public interface MultiMap<K, V> extends Iterable<Map.Entry<K, Collection<V>>>, Map<K, V> {

    MultiMap EMPTY = new MultiMap() {

        private final Map empty = Collections.emptyMap();

        @Override
        public Iterator<Entry> iterator() {
            //noinspection unchecked
            return empty.entrySet().iterator();
        }

        @Override
        public MultiMap add(Object key, Object o) {
            return null;
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
        public void putAllCopyLists(MultiMap multiMap) {

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
            //noinspection unchecked
            return empty.put(key, value);
        }

        @Override
        public Object remove(Object key) {
            return empty.remove(key);
        }

        @Override
        public void putAll(Map m) {
            //noinspection unchecked
            empty.putAll(m);
        }

        @Override
        public void clear() {
            empty.clear();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Set keySet() {
            return empty.keySet();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Collection values() {
            return empty.values();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Set<Entry> entrySet() {
            //noinspection unchecked
            return empty.entrySet();
        }
    };

    static <K, V> MultiMap<K, V> multiMap() {
        return new MultiMapImpl<>();
    }

    @SuppressWarnings({"unchecked", "SameReturnValue"})
    static <K, V> MultiMap<K, V> empty() {
        return EMPTY;
    }

    Iterator<Entry<K, Collection<V>>> iterator();

    @SuppressWarnings("UnusedReturnValue")
    default MultiMap<K, V> add(K key, V v) {
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

    V getSingleObject(K name);

    @Override
    default boolean containsValue(Object value) {

        throw new UnsupportedOperationException("Unsupported");

    }

    default V put(K key, V value) {
        throw new UnsupportedOperationException("Unsupported");

    }

    default V remove(Object key) {

        throw new UnsupportedOperationException("Unsupported");
    }

    default void putAll(@SuppressWarnings("NullableProblems") Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    default void clear() {

        throw new UnsupportedOperationException("Unsupported");
    }

    @SuppressWarnings("NullableProblems")
    @Override
    default Collection<V> values() {
        throw new UnsupportedOperationException("Unsupported");
    }

    @SuppressWarnings("NullableProblems")
    @Override
    default Set<Entry<K, V>> entrySet() {

        return Collections.emptySet();
    }

    default Iterable<V> valueMulti() {
        throw new UnsupportedOperationException("Unsupported");
    }

    void putAllCopyLists(MultiMap<K, V> multiMap);

}
