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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Concurrent HashSet.
 *
 * @param <T> type the sets hold
 * @author rhightower
 */
public class ConcurrentHashSet<T> implements Set<T> {

    private static final Object NOTHING = new Object();
    private final Map<T, Object> map;

    public ConcurrentHashSet(int size) {
        map = new ConcurrentHashMap<>(size);
    }

    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<>();
    }


    @Override
    public boolean add(T e) {
        return map.put(e, NOTHING) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) == null;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return map.keySet().containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        boolean added = false;
        for (T e : collection) {
            if (map.put(e, NOTHING) == null) {
                added = true;
            }
        }
        return added;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        map.clear();
    }


    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    }
}
