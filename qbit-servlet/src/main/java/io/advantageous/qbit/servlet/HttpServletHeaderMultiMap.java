package io.advantageous.qbit.servlet;

import io.advantageous.qbit.util.MultiMap;
import org.boon.Lists;
import org.boon.Sets;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by rhightower on 2/12/15.
 */
public class HttpServletHeaderMultiMap implements MultiMap<String, String> {
    private final HttpServletRequest request;

    public HttpServletHeaderMultiMap(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Iterator<Entry<String, Collection<String>>> iterator() {

        final Enumeration<String> headerNames = request.getHeaderNames();

        return new Iterator<Entry<String, Collection<String>>>() {
            @Override
            public boolean hasNext() {
                return headerNames.hasMoreElements();
            }

            @Override
            public Entry<String, Collection<String>> next() {
                String currentName = headerNames.nextElement();
                return new Entry<String, Collection<String>>() {
                    @Override
                    public String getKey() {
                        return currentName;
                    }

                    @Override
                    public Collection<String> getValue() {
                        return Lists.list(request.getHeaders(currentName));
                    }

                    @Override
                    public Collection<String> setValue(Collection<String> value) {
                        throw new UnsupportedOperationException("Unsupported");
                    }
                };
            }
        };
    }


    @Override
    public String getFirst(final String key) {
        return request.getHeader(key);
    }

    @Override
    public Iterable<String> getAll(final String key) {
        return Lists.list(request.getHeader(key));
    }


    @Override
    public Iterable<String> keySetMulti() {
        return Lists.list(request.getHeaderNames());
    }


    @Override
    public String getSingleObject(String name) {
        return getFirst(name);
    }

    @Override
    public int size() {
        return Lists.list(request.getHeaderNames()).size();
    }

    @Override
    public boolean isEmpty() {
        return request.getHeaderNames().hasMoreElements();
    }

    @Override
    public boolean containsKey(Object key) {
        return request.getHeader(key.toString()) != null;
    }

    @Override
    public String get(Object key) {
        return getFirst(key.toString());
    }


    @Override
    public Set<String> keySet() {
        return Sets.set(request.getHeaderNames());
    }

}
