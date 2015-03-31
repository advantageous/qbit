package io.advantageous.qbit.http.jetty.impl.server;

import io.advantageous.qbit.util.MultiMap;
import org.eclipse.jetty.http.HttpFields;

import java.util.*;

/**
 * Created by rhightower on 3/9/15.
 */
public class JettyHeaderAdapter implements MultiMap<String, String> {

    private final HttpFields httpFields;

    public JettyHeaderAdapter(final HttpFields httpFields) {
        this.httpFields = httpFields;
    }
    @Override
    public Iterator<Entry<String, Collection<String>>> iterator() {

        final Iterator<String> iterator = httpFields.getFieldNamesCollection().iterator();

        return new Iterator<Entry<String, Collection<String>>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Entry<String, Collection<String>> next() {

                final String key = iterator.next();
                return new Entry<String, Collection<String>>() {

                    @Override
                    public String getKey() {
                        return key;
                    }

                    @Override
                    public Collection<String> getValue() {

                        return httpFields.getValuesList(key);
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
    public String getFirst(String key) {
        return httpFields.get(key);
    }

    @Override
    public Iterable<String> getAll(String key) {
        return httpFields.getValuesList(key);
    }

    @Override
    public Iterable<String> keySetMulti() {
        return httpFields.getFieldNamesCollection();
    }

    @Override
    public String getSingleObject(String name) {
        return httpFields.get(name);
    }

    @Override
    public int size() {
        return httpFields.size();
    }

    @Override
    public boolean isEmpty() {
        return httpFields.size()>0;
    }

    @Override
    public boolean containsKey(Object key) {
        return httpFields.containsKey(key.toString());
    }

    @Override
    public String get(Object key) {
        return httpFields.get(key.toString());
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<>((Collection<String>)this.keySetMulti());
    }
}
