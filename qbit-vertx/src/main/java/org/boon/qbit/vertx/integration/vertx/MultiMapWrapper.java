package org.boon.qbit.vertx.integration.vertx;

import io.advantageous.qbit.MultiMap;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 10/13/14.
 */
public class MultiMapWrapper implements MultiMap<String, String> {


    final org.vertx.java.core.MultiMap vertxMap;

    public MultiMapWrapper(org.vertx.java.core.MultiMap vertxMap) {
        this.vertxMap = vertxMap;
    }

    @Override
    public Iterator<Entry<String, Collection<String>>> iterator() {


        final Iterator<Entry<String, String>> iterator = vertxMap.iterator();

        return new Iterator<Entry<String, Collection<String>>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Entry<String, Collection<String>> next() {
                final Entry<String, String> next = iterator.next();

                return new Entry<String, Collection<String>>() {
                    @Override
                    public String getKey() {
                        return next.getKey();
                    }

                    @Override
                    public Collection<String> getValue() {
                        return (Collection<String>) MultiMapWrapper.this.getAll(next.getKey());
                    }

                    @Override
                    public Collection<String> setValue(Collection<String> value) {
                        return null;
                    }
                };
            }
        };

    }

    @Override
    public void add(String key, String s) {

        vertxMap.add(key, s);
    }

    @Override
    public String getFirst(String key) {
        return vertxMap.get(key);
    }

    @Override
    public Iterable<String> getAll(String key) {
        return vertxMap.getAll(key);
    }

    @Override
    public boolean removeValueFrom(String key, String s) {
        return die(Boolean.class, "NOT SUPPORTED");
    }

    @Override
    public boolean removeMulti(String key) {
         vertxMap.remove(key);
         return true;
    }

    @Override
    public Iterable<String> keySetMulti() {
        return vertxMap.names();
    }

    @Override
    public Iterable<String> valueMulti() {
        /* We could support this. */
        return die(Iterable.class, "NOT SUPPORTED");
    }

    @Override
    public void putAll(MultiMap<String, String> params) {

        Set<String> keys = params.keySet();

        for (String key : keys) {
            Iterable<String> values = params.getAll(key);
            for (String value : values) {
                this.add(key, value);
            }
        }
    }

    @Override
    public Map<? extends String, ? extends Collection<String>> baseMap() {

        return die(Map.class, "NOT SUPPORTED");
    }

    @Override
    public String getSingleObject(String name) {
            return vertxMap.get(name);
    }

    @Override
    public int size() {
        return vertxMap.size();
    }

    @Override
    public boolean isEmpty() {
        return vertxMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return vertxMap.contains((String)key);
    }

    @Override
    public boolean containsValue(Object value) {
        return die(Boolean.class, "NOT SUPPORTED");

    }

    @Override
    public String get(Object key) {
        return vertxMap.get((String)key);
    }

    @Override
    public String put(String key, String value) {
        vertxMap.add(key, value);
        return null;
    }

    @Override
    public String remove(Object key) {
        vertxMap.remove(key.toString());
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {

        Set<? extends Entry<? extends String, ? extends String>> entries = m.entrySet();

        for (Entry<? extends String, ? extends String> entry : entries) {
            vertxMap.add(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {

        vertxMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return vertxMap.names();
    }

    @Override
    public Collection<String> values() {
        return die(Collection.class, "Not supported");
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return die(Set.class, "Not supported");
    }
}
