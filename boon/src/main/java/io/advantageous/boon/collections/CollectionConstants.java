package io.advantageous.boon.collections;

import java.util.*;

/**
 * Created by Richard on 9/26/14.
 */
public class CollectionConstants {



    public static MultiMap EMPTY_MULTI_MAP = new MultiMap() {

        Map empty = Collections.emptyMap();
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


    public static final <K,V> MultiMap<K,V> emptyMultiMap() {
        return (MultiMap<K,V>) EMPTY_MULTI_MAP;
    }
}
