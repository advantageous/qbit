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
import java.util.concurrent.ConcurrentHashMap;

/**
 * @param <K> key
 * @param <V> value
 * @author rhightower
 */

public class MultiMapImpl<K, V> implements MultiMap<K, V> {

    private int initialSize = 10;
    private Map<K, Collection<V>> map = new ConcurrentHashMap<>();
    private Class<? extends Collection> collectionClass = ArrayList.class;

    public MultiMapImpl(Class<? extends Collection> collectionClass, int initialSize) {
        this.collectionClass = collectionClass;
        this.initialSize = initialSize;
    }

    public MultiMapImpl(Class<? extends Collection> collectionClass) {
        this.collectionClass = collectionClass;
    }


    public MultiMapImpl() {
    }

    static Collection<Object> createCollectionFromClass(Class<?> type, int size) {

        if (type == List.class) {
            return new ArrayList<>(size);
        } else if (type == SortedSet.class) {
            return new TreeSet<>();
        } else if (type == Set.class) {
            return new LinkedHashSet<>(size);
        } else if (type.isAssignableFrom(List.class)) {
            return new ArrayList<>();
        } else if (type.isAssignableFrom(SortedSet.class)) {
            return new TreeSet<>();
        } else if (type.isAssignableFrom(Set.class)) {
            return new LinkedHashSet<>(size);
        } else {
            return new ArrayList(size);
        }

    }

    @Override
    public Iterator<Entry<K, Collection<V>>> iterator() {
        return map.entrySet().iterator();
    }

    @Override
    public void add(K key, V v) {
        Collection<V> collection = map.get(key);
        if (collection == null) {
            collection = createCollection(key);
        }
        collection.add(v);
    }

    @Override
    public V put(K key, V value) {
        Collection<V> collection = map.get(key);
        if (collection == null) {
            collection = createCollection(key);
        }
        collection.add(value);
        return null;
    }

    @Override
    public V remove(Object key) {
        map.remove(key);
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        final Set<? extends Entry<? extends K, ? extends V>> entries = m.entrySet();
        for (Entry<? extends K, ? extends V> entry : entries) {
            this.add(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public V get(Object key) {
        return getFirst((K) key);
    }

    @Override
    public V getFirst(K key) {
        Collection<V> collection = map.get(key);
        if (collection == null || collection.size() == 0) {
            return null;
        }
        return collection.iterator().next();
    }

    @Override
    public Iterable<V> getAll(K key) {
        Collection<V> collection = map.get(key);
        if (collection == null) {
            return Collections.emptyList();
        }
        return collection;
    }

    @Override
    public boolean removeValueFrom(K key, V v) {
        Collection<V> collection = map.get(key);
        if (collection == null) {
            return false;
        }
        return collection.remove(v);
    }

    @Override
    public boolean removeMulti(K key) {
        return map.remove(key) != null;
    }

    private Collection<V> createCollection(K key) {
        Collection<V> collection = (Collection<V>) createCollectionFromClass(collectionClass, initialSize);
        map.put(key, collection);
        return collection;
    }

    @Override
    public Iterable<K> keySetMulti() {
        return map.keySet();
    }

    @Override
    public Iterable<V> valueMulti() {

        List list = new ArrayList();
        Collection<Collection<V>> values = map.values();

        for (Collection c : values) {
            for (Object o : c) {
                list.add(o);
            }
        }
        return list;
    }

    @Override
    public Collection<V> values() {

        List list = new ArrayList();
        Collection<Collection<V>> values = map.values();

        for (Collection c : values) {
            for (Object o : c) {
                list.add(o);
            }
        }
        return list;

    }

    @Override
    public Set<Entry<K, V>> entrySet() {


        final Set<Entry<K, Collection<V>>> entries = map.entrySet();

        return new Set<Entry<K, V>>() {

            @Override
            public int size() {
                return entries.size();
            }

            @Override
            public boolean isEmpty() {
                return entries.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return entries.contains(o);
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                final Iterator<Entry<K, Collection<V>>> iterator = entries.iterator();
                return new Iterator<Entry<K, V>>() {

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        final Entry<K, Collection<V>> next = iterator.next();
                        final Collection<V> value = next.getValue();
                        V theValue = null;
                        if (value instanceof List) {
                            theValue = ((List<V>) value).get(0);
                        }

                        final V item = theValue;

                        return new Entry<K, V>() {
                            @Override
                            public K getKey() {
                                return next.getKey();
                            }

                            @Override
                            public V getValue() {
                                return item;
                            }

                            @Override
                            public V setValue(V value) {
                                return null;
                            }
                        };
                    }

                    @Override
                    public void remove() {

                    }
                };

            }


            @Override
            public Object[] toArray() {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean add(Entry<K, V> kvEntry) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addAll(Collection<? extends Entry<K, V>> c) {
                throw new UnsupportedOperationException();
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
                throw new UnsupportedOperationException();
            }
        };
    }

    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (!map.containsKey(key)) {
            return false;
        } else {
            Collection<V> collection = map.get(key);
            if (collection == null || collection.size() == 0) {
                return false;
            }
            return true;
        }
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(MultiMap<K, V> params) {
        map.putAll(params.baseMap());

    }

    @Override
    public Map<? extends K, ? extends Collection<V>> baseMap() {
        return map;
    }

    @Override
    public V getSingleObject(K name) {
        final Collection<V> vs = map.get(name);
        if (vs == null || vs.size() == 0) {
            return null;
        }
        if (vs.size() == 1) {
            vs.iterator().hasNext();
            return vs.iterator().next();
        } else {
            return null;
        }

    }

    @Override
    public String toString() {
        return "MultiMapImpl{" +
                "initialSize=" + initialSize +
                ", map=" + map +
                ", collectionClass=" + collectionClass +
                '}';
    }
}
